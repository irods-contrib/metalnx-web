/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.*;
import com.emc.metalnx.services.interfaces.*;
import org.apache.commons.io.FilenameUtils;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Service
@Transactional
public class FileOperationServiceImpl implements FileOperationService {

    private static final String CONTENT_TYPE = "application/octet-stream";
    private static final String HEADER_FORMAT = "attachment;filename=\"%s\"";
    private static final Logger logger = LoggerFactory.getLogger(FileOperationServiceImpl.class);
    @Autowired
    IRODSServices irodsServices;
    @Autowired
    CollectionService collectionService;
    @Autowired
    UserBookmarkService userBookmarkService;
    @Autowired
    GroupBookmarkService groupBookmarkService;
    @Autowired
    FavoritesService favoritesService;
    @Autowired
    ResourceService resourceService;
    @Autowired
    RuleService rs;

    @Override
    public boolean copy(String sourcePath, String targetPath) throws DataGridConnectionRefusedException {

        IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();

        DataTransferOperations dataTransferOperations = irodsServices.getDataTransferOperations();

        try {

            IRODSFile source = irodsFileFactory.instanceIRODSFile(sourcePath);
            IRODSFile target = irodsFileFactory.instanceIRODSFile(targetPath);

            dataTransferOperations.copy(source, target, null, null);

            return true;
        }
        catch (JargonException e) {
            logger.error("Could not copy item from " + sourcePath + " to " + targetPath + ": ", e.getMessage());
        }

        return false;
    }

    @Override
    public boolean copy(List<String> sourcePaths, String targetPath) throws DataGridConnectionRefusedException {

        boolean isCopied = true;

        for (String sourcePath : sourcePaths) {
            if (!this.copy(sourcePath, targetPath)) isCopied = false;
        }

        return isCopied;
    }

    @Override
    public boolean deleteItem(String path, boolean force) throws DataGridConnectionRefusedException {

        IRODSFileSystemAO irodsFileSystemAO = irodsServices.getIRODSFileSystemAO();
        IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();
        boolean itemDeleted = false;

        try {
            IRODSFile itemToBeRemoved = irodsFileFactory.instanceIRODSFile(path);

            if (irodsFileSystemAO.isDirectory(itemToBeRemoved)) {
                // if force set to true, we do an irm -rf
                if (force) {
                    irodsFileSystemAO.directoryDeleteForce(itemToBeRemoved);
                }
                // irm
                else {
                    irodsFileSystemAO.directoryDeleteNoForce(itemToBeRemoved);
                }
            }
            else {
                // if force set to false, we do an irm
                if (force) {
                    irodsFileSystemAO.fileDeleteForce(itemToBeRemoved);
                }
                // irm
                else {
                    irodsFileSystemAO.fileDeleteNoForce(itemToBeRemoved);
                }
            }

            itemDeleted = true;

            // item deleted, we need to delete any bookmarks related to it
            userBookmarkService.removeBookmarkBasedOnPath(path);
            userBookmarkService.removeBookmarkBasedOnRelativePath(path);

            groupBookmarkService.removeBookmarkBasedOnPath(path);
            groupBookmarkService.removeBookmarkBasedOnRelativePath(path);

            favoritesService.removeFavoriteBasedOnPath(path);
            favoritesService.removeFavoriteBasedOnRelativePath(path);

        }
        catch (JargonException e) {
            logger.error("Could not delete item " + path + ": ", e);
        }

        return itemDeleted;
    }

    @Override
    public boolean deleteCollection(String collectionPath, boolean forceFlag) throws DataGridException {
        IRODSFileSystemAO irodsFileSystemAO = irodsServices.getIRODSFileSystemAO();
        IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();

        try {
            IRODSFile collectionToBeRemoved = irodsFileFactory.instanceIRODSFile(collectionPath);

            if (forceFlag) {
                irodsFileSystemAO.directoryDeleteForce(collectionToBeRemoved);
            }
            else {
                irodsFileSystemAO.directoryDeleteNoForce(collectionToBeRemoved);
            }
            return true;
        }
        catch (JargonException e) {
            logger.error("Could not delete collection: ", e.getMessage());
        }

        return false;
    }

    @Override
    public boolean deleteDataObject(String dataObjectPath, boolean forceFlag) throws DataGridException {

        boolean dataObjDeleted = false;

        try {
            IRODSFileSystemAO irodsFileSystemAO = irodsServices.getIRODSFileSystemAO();
            IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();

            IRODSFile fileToBeRemoved = irodsFileFactory.instanceIRODSFile(dataObjectPath);
            if (forceFlag) {
                irodsFileSystemAO.fileDeleteForce(fileToBeRemoved);
            }
            else {
                irodsFileSystemAO.fileDeleteNoForce(fileToBeRemoved);
            }

            dataObjDeleted = true;
        }
        catch (JargonException e) {
            logger.error("Could not delete data object: {}", e.getMessage());
        }

        return dataObjDeleted;
    }

    @Override
    public boolean deleteReplica(String path, String fileName, int replicaNumber, boolean inAdminMode) throws DataGridConnectionRefusedException {

        boolean deleteSuccess = false;
        try {
            String parentPath = path.substring(0, path.lastIndexOf("/"));
            irodsServices.getDataObjectAO().trimDataObjectReplicas(parentPath, fileName, "", -1, replicaNumber, inAdminMode);
            deleteSuccess = true;
        }
        catch (DataNotFoundException e) {
            logger.error("Data object could not be found: " + e.toString());
        }
        catch (JargonException e) {
            logger.error("Delete replica operation failed: " + e.toString());
        }
        return deleteSuccess;
    }

    @Override
    public boolean download(String path, HttpServletResponse response, boolean removeTempCollection) throws DataGridException {

        logger.debug("Downloading file ", path);

        if (path == null || path.isEmpty() || response == null) {
            return false;
        }

        logger.debug("Copying file into the HTTP response");

        boolean isDownloadSuccessful = copyFileIntoHttpResponse(path, response);

        String fileName = path.substring(path.lastIndexOf("/"), path.length());

        // getting the temporary collection name from the compressed file name,
        // and removing the ".tar" extension from it
        String tempColl = collectionService.getHomeDirectyForCurrentUser() + fileName.substring(0, fileName.length() - 4);

        /*
         * String tempTrashColl = getTrashDirectoryForCurrentUser() +
         * fileName.substring(0, fileName.length() - 4);
         */

        logger.debug("Removing compressed file");

        // removing any temporary collections and tar files created for downloading
        if (removeTempCollection) {
            deleteDataObject(path, removeTempCollection);

            logger.debug("Removing temporary collection");

            // removing temporary collection
            deleteCollection(tempColl, removeTempCollection);
        }

        return isDownloadSuccessful;
    }

    @Override
    public boolean emptyTrash(DataGridUser user, String currentPath) throws DataGridConnectionRefusedException {
        if (user == null) {
            return false;
        }

        boolean itemsDeleted = false;
        RuleProcessingAO ruleProcessingAO = irodsServices.getRuleProcessingAO();


        try{
            StringBuilder ruleString = new StringBuilder();
            ruleString.append("mlxEmptyTrash {\n");
            ruleString.append(String.format(" msiRmColl(\"%s\",\"%s\",\"null\");", currentPath, user.isAdmin() ? "irodsAdminRmTrash=" : "irodsRmTrash="));
            ruleString.append("}\n");
            ruleString.append("OUTPUT ruleExecOut");
            ruleProcessingAO.executeRule(ruleString.toString());
            itemsDeleted = true;
        }catch(Exception e){
            logger.error("Could not execute rule on path {}: ", currentPath, e.getMessage());
        }


        return itemsDeleted;
    }

    @Override
    public boolean move(String sourcePath, String targetPath) throws DataGridConnectionRefusedException {

        IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();

        DataTransferOperations dataTransferOperations = irodsServices.getDataTransferOperations();

        try {
            IRODSFile source = irodsFileFactory.instanceIRODSFile(sourcePath);

            if (source.isDirectory()) {
                targetPath += "/" + FilenameUtils.getBaseName(sourcePath);
            }

            IRODSFile target = irodsFileFactory.instanceIRODSFile(targetPath);

            dataTransferOperations.move(source, target);

            return true;
        }
        catch (JargonException e) {
            logger.error("Could not move item from " + sourcePath + " to " + targetPath + ": ", e.getMessage());
        }

        return false;
    }

    @Override
    public void replicateDataObject(String path, String targetResource, boolean inAdminMode) throws DataGridReplicateException, DataGridConnectionRefusedException {
        logger.info("Replicating {} into the resource {} [admin mode: {}]", path, targetResource, inAdminMode);

        try {
            rs.execReplDataObjRule(targetResource, path, inAdminMode);
        } catch (DataGridRuleException e) {
            logger.info("File replication failed ({}) into the resource {} [admin mode: {}]", path, targetResource, inAdminMode);
            throw new DataGridReplicateException("File replication failed.");
        }
    }

    @Override
    public void computeChecksum(String path, String filename) throws DataGridChecksumException, DataGridConnectionRefusedException {
        if (path == null || path.isEmpty() || filename == null || filename.isEmpty())
            throw new DataGridChecksumException("Could not calculate checksum. File path is invalid.");

        logger.info("Computing checksum for {} ({})", filename, path);

        IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();
        DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
        IRODSFile file;

        try {
            file = irodsFileFactory.instanceIRODSFile(path, filename);
            dataObjectAO.computeMD5ChecksumOnDataObject(file);
        } catch (JargonException e) {
            logger.error("Could not calculate checksum: {}", e.getMessage());
            throw new DataGridChecksumException("Could not calculate checksum.");
        }
    }

    /*
     * *************************************************************************
     * *************************** PRIVATE METHODS *****************************
     * *************************************************************************
     */

    /**
     * Copies a buffered input stream from a file to a HTTP response for
     * downloading.
     *
     * @param path
     *            path to the file in iRODS to be added to the HTTP response
     * @param response
     *            HTTP response to let the user download the file
     * @return True, if the file was successfully added to the HTTP response.
     *         False, otherwise.
     * @throws DataGridConnectionRefusedException is Metalnx cannot connect to the data grid
     */
    private boolean copyFileIntoHttpResponse(String path, HttpServletResponse response) throws DataGridConnectionRefusedException {

        boolean isCopySuccessFul = true;
        IRODSFileInputStream irodsFileInputStream = null;
        IRODSFile irodsFile = null;

        logger.debug("Trying to copy path stream {} to user", path);

        try {
            String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
            logger.debug("The filename is [{}]", fileName);

            logger.debug("Initiating iRodsFileFactory");
            IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();

            logger.debug("Getting iRodsFileFactory instance for {}", path);
            irodsFile = irodsFileFactory.instanceIRODSFile(path);

            logger.debug("Creating stream from {}", irodsFile);
            irodsFileInputStream = irodsFileFactory.instanceIRODSFileInputStream(irodsFile);

            // set file mime type
            response.setContentType(CONTENT_TYPE);
            response.setHeader("Content-Disposition", String.format(HEADER_FORMAT, fileName));
            response.setContentLength((int) irodsFile.length());

            FileCopyUtils.copy(irodsFileInputStream, response.getOutputStream());

        }
        catch (IOException e) {
            logger.error("Could not put the file in the Http response ", e);
            isCopySuccessFul = false;
        }
        catch (JargonException e) {
            logger.error("Could not copy file in the Http response: ", e.getMessage());
            isCopySuccessFul = false;
        }

        finally {
            try {
                if (irodsFileInputStream != null) irodsFileInputStream.close();
                if (irodsFile != null) irodsFile.close();
            }
            catch (Exception e) {
                logger.error("Could not close stream(s): ", e.getMessage());
            }
        }

        return isCopySuccessFul;
    }
}
