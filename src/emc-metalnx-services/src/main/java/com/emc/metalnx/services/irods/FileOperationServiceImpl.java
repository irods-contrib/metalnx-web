package com.emc.metalnx.services.irods;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.ResourceAO;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.service.utils.DataGridFileForUpload;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FavoritesService;
import com.emc.metalnx.services.interfaces.FileOperationService;
import com.emc.metalnx.services.interfaces.GroupBookmarkService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.UserBookmarkService;
import com.emc.metalnx.services.machine.util.DataGridUtils;

@Service
@Transactional
public class FileOperationServiceImpl implements FileOperationService {

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

    @Value("${populate.msi.enabled}")
    private boolean populateMsiEnabled;

    @Value("${illumina.msi.enabled}")
    private boolean illuminaMsiEnabled;

    @Value("${irods.host}")
    private String iCATHost;

    private static final int MEGABYTE = 1 * 1024 * 1024;
    private static final String CONTENT_TYPE = "application/octet-stream";
    private static final String HEADER_FORMAT = "attachment;filename=\"%s\"";

    private static final Logger logger = LoggerFactory.getLogger(FileOperationServiceImpl.class);

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
            if (this.copy(sourcePath, targetPath) == false) {
                isCopied = false;
            }
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
    public boolean deleteReplica(String path, String fileName, int replicaNumber) throws DataGridConnectionRefusedException {

        boolean deleteSuccess = false;
        try {
            String parentPath = path.substring(0, path.lastIndexOf("/"));
            irodsServices.getDataObjectAO().trimDataObjectReplicas(parentPath, fileName, "", -1, replicaNumber, true);
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

        boolean isDownloadSuccesful = true;

        logger.debug("Copying file into the HTTP response");

        isDownloadSuccesful = copyFileIntoHttpResponse(path, response);

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

        return isDownloadSuccesful;
    }

    @Override
    public boolean emptyTrash(DataGridUser user) throws DataGridConnectionRefusedException {
        if (user == null) {
            return false;
        }

        boolean itemsDeleted = true;
        String trashDirectory = "/" + user.getAdditionalInfo() + "/trash/home/" + user.getUsername();
        List<DataGridCollectionAndDataObject> trashItems = null;

        try {
            trashItems = collectionService.getSubCollectionsAndDataObjetsUnderPath(trashDirectory);

            // trash is already empty
            if (trashItems == null || trashItems.isEmpty()) {
                return false;
            }

            for (DataGridCollectionAndDataObject item : trashItems) {
                if (item.isCollection()) {
                    deleteCollection(item.getPath(), true);
                }
                else {
                    deleteDataObject(item.getPath(), true);
                }
            }
        }
        catch (DataGridConnectionRefusedException e) {
            throw e;
        }
        catch (DataGridException e) {
            logger.error("Could not delete items from the trash.");
            itemsDeleted = false;
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
    public boolean replicateDataObject(String path, String targetResource) throws DataGridConnectionRefusedException {

        logger.debug("Replicating " + path + " into the resource " + targetResource);
        DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();

        try {
            dataObjectAO.replicateIrodsDataObject(path, targetResource);
            return true;
        }
        catch (JargonException e) {
            logger.error("Could not replicate " + path, e.getMessage());
        }

        return false;
    }

    @Override
    public boolean transferFileToDataGrid(DataGridFileForUpload fileForUpload) throws DataGridException {

        if (fileForUpload == null || fileForUpload.getFile() == null || fileForUpload.isFileCorrupted() || fileForUpload.getTargetPath().isEmpty()
                || fileForUpload.getDataGridDestinationResource().isEmpty()) {

            logger.error("File could not be sent to the data grid.");
            return false;
        }

        String defaultStorageResource = irodsServices.getDefaultStorageResource();
        String targetPath = fileForUpload.getTargetPath();
        String destinationResource = fileForUpload.getDataGridDestinationResource();
        String replicationResource = fileForUpload.getDataGridReplicationResource();
        boolean computeCheckSum = fileForUpload.isDataGridComputeChecksum();
        boolean replicateFile = fileForUpload.isReplicateFile();

        logger.info("Setting default resource to {}", destinationResource);

        // Setting temporarily the defaultStorageResource for the logged user
        irodsServices.setDefaultStorageResource(destinationResource);

        // Getting DataObjectAO in order to create the new file
        IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();
        DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
        String originalFilename = "";

        // Creating set of filenames on the current collection
        List<DataGridCollectionAndDataObject> filesInColl = collectionService.getSubCollectionsAndDataObjetsUnderPath(targetPath);

        Set<String> setOfFilesInColl = new HashSet<String>();
        for (DataGridCollectionAndDataObject dataObj : filesInColl) {
            if (!dataObj.isCollection()) {
                setOfFilesInColl.add(dataObj.getName());
            }
        }

        IRODSFile targetFile = null;
        RuleProcessingAO ruleProcessingAO = irodsServices.getRuleProcessingAO();
        Stream2StreamAO stream2StreamA0 = irodsServices.getStream2StreamAO();

        // Getting list of resources for upload - TODO
        HashMap<String, String> resourceMap = null;
        try {
            ResourceAO resourceAO = irodsServices.getResourceAO();
            resourceMap = DataGridUtils.buildMapForResourcesNamesAndMountPoints(resourceAO.findAll());
        }
        catch (JargonException e) {
            logger.error("Could not build Resource map for upload", e);
        }

        File file = fileForUpload.getFile();
        boolean isFileUploaded = false;
        boolean fileIsAlreadyInCollection = setOfFilesInColl.contains(file.getName());

        // If file already exists and we do not want to overwrite it, the
        // transferring is aborted.
        if (fileIsAlreadyInCollection && !fileForUpload.isDataGridOverwriteDuplicatedFiles()) {
            logger.info("File already exists. Not overwriting it.");
            return true;
        }

        try {
            originalFilename = file.getName();
            targetFile = irodsFileFactory.instanceIRODSFile(targetPath, originalFilename);
            targetFile.setResource(destinationResource);

            String tmpFileName = null;

            if (fileIsAlreadyInCollection) {
                tmpFileName = targetFile.getAbsolutePath() + ".tmp_for_upload";
                move(targetFile.getAbsolutePath(), tmpFileName);
            }

            logger.info("Creating input stream for {} to be transferred", fileForUpload.getFileName());

            InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

            logger.info("Transferring file to iRODS.");

            // Transfering file to iRODS filesystem
            stream2StreamA0.transferStreamToFileUsingIOStreams(inputStream, (File) targetFile, 0, MEGABYTE);

            logger.info("Transferring done. Removing files from Tomcat");

            // removing temporary files from the server after they get
            // transferred to iRODS
            fileForUpload.removeTempFile();

            logger.info("Files removed from Tomcat");

            // Computing a check sum for this file just uploaded to iRODS
            if (computeCheckSum) {
                logger.info("Computing checksum for {}", targetFile.getAbsoluteFile());
                dataObjectAO.computeMD5ChecksumOnDataObject(targetFile);
            }

            // Replicating file into desired resource
            if (replicateFile) {
                logger.info("Replicating data object into {}", replicationResource);
                dataObjectAO.replicateIrodsDataObject(targetFile.getPath(), replicationResource);
            }

            // Closing streams opened
            inputStream.close();

            if (fileIsAlreadyInCollection) {
                logger.info("Deleting file {} ", tmpFileName);
                deleteDataObject(tmpFileName, false);
            }

            String remoteHeader = "";
            String remoteFooter = "";

            DataGridResource destResc = resourceService.find(destinationResource);

            if (!iCATHost.startsWith(destResc.getHost())) {
                String remoteHost = destResc.getHost();
                remoteHeader = String.format("remote( \"%s\", \"\" ) {\n", remoteHost);
                remoteFooter = "}\n";
            }

            try {
                StringBuilder ruleString = new StringBuilder();
                String objPath = targetFile.getCanonicalPath();
                String filePath = resourceMap.get(destinationResource) + objPath.substring(objPath.indexOf("/", 1), objPath.length());

                if (objPath.endsWith(".cram") || objPath.endsWith(".bam")) {
                    ruleString.append("automaticBamMetadataExtraction {\n");
                    ruleString.append(remoteHeader);
                    ruleString.append(" msiobjput_mdbam(\"" + objPath + "\", \"" + filePath + "\");\n");
                    ruleString.append(remoteFooter);
                    ruleString.append("}\n");
                    ruleString.append("OUTPUT ruleExecOut");
                    ruleProcessingAO.executeRule(ruleString.toString());
                }

                if (isVCFFile(objPath)) {
                    ruleString.append("automaticVcfMetadataExtraction {\n");
                    ruleString.append(remoteHeader);
                    ruleString.append(" msiobjput_mdvcf(\"" + objPath + "\", \"" + filePath + "\");\n");
                    ruleString.append(remoteFooter);
                    ruleString.append("}\n");
                    ruleString.append("OUTPUT ruleExecOut");
                    ruleProcessingAO.executeRule(ruleString.toString());
                }

                if (isImageFile(objPath)) {
                    ruleString.append("automaticJpgMetadataExtraction {\n");
                    ruleString.append(remoteHeader);
                    ruleString.append(" msiobjjpeg_extract(\"" + objPath + "\", \"" + filePath + "\");\n");
                    ruleString.append(remoteFooter);
                    ruleString.append("}\n");
                    ruleString.append("OUTPUT ruleExecOut");
                    ruleProcessingAO.executeRule(ruleString.toString());
                }

                if (isPrideXMLManifestFile(objPath)) {
                    StringBuilder xmlManifestFileRule = new StringBuilder();
                    List<DataGridCollectionAndDataObject> objectsToApplyMetadata = collectionService
                            .getSubCollectionsAndDataObjetsUnderPath(targetPath);
                    for (DataGridCollectionAndDataObject obj : objectsToApplyMetadata) {
                        String objPathToApplyMetadata = obj.getPath();

                        logger.info("Extracting metadata from [{}] and applying on [{}]", filePath, objPath);
                        xmlManifestFileRule.append("automaticExtractMetadataFromXMLManifest {\n");
                        ruleString.append(remoteHeader);
                        xmlManifestFileRule.append("  msiobjput_mdmanifest(\'" + objPathToApplyMetadata + "\', \'" + filePath + "\', \'" + filePath
                                + "\');\n");
                        ruleString.append(remoteFooter);
                        xmlManifestFileRule.append("}\n");
                        xmlManifestFileRule.append("OUTPUT ruleExecOut");
                        ruleProcessingAO.executeRule(xmlManifestFileRule.toString());
                        xmlManifestFileRule.setLength(0);
                    }
                }

                // Making sure the populate MSI is only executed on debug mode
                if (populateMsiEnabled) {
                    ruleString = new StringBuilder();
                    ruleString.append("populateMetadataForFile {\n");
                    ruleString.append(remoteHeader);
                    ruleString.append(" msiobjput_populate(\"" + objPath + "\");\n");
                    ruleString.append(remoteFooter);
                    ruleString.append("}\n");
                    ruleString.append("OUTPUT ruleExecOut");
                    ruleProcessingAO.executeRule(ruleString.toString());
                }

                if (illuminaMsiEnabled && objPath.endsWith("_SSU.tar")) {
                    ruleString = new StringBuilder();
                    ruleString.append("illuminaMetadataForFile {\n");
                    ruleString.append(remoteHeader);
                    ruleString.append(String.format("msiTarFileExtract(\"%s\", \"%s\", \"%s\", *Status);\n", objPath, targetPath, destinationResource));
                    ruleString.append(String.format("msiget_illumina_meta(\"%s\", \"%s\");", objPath, destinationResource));
                    ruleString.append(remoteFooter);
                    ruleString.append("}\n");
                    ruleString.append("OUTPUT ruleExecOut");
                    ruleProcessingAO.executeRule(ruleString.toString());
                }
            }
            catch (Exception e) {
                logger.error("Could not execute rule: ", e.getMessage());
            }

            isFileUploaded = true;
        }
        catch (IOException e) {
            logger.error("Could not create input stream for {}", originalFilename, e);
        }
        catch (JargonException e) {
            logger.error("Could not upload IRODSFile for {}", targetPath, e);
            throw new DataGridException(e.getMessage());
        }

        // Setting the default resource back to the original one.
        irodsServices.setDefaultStorageResource(defaultStorageResource);

        return isFileUploaded;
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
     * @throws DataGridConnectionRefusedException
     * @throws JargonException
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
                irodsFileInputStream.close();
                irodsFile.close();
            }
            catch (Exception e) {
                logger.error("Could not close stream(s): ", e.getMessage());
            }
        }

        return isCopySuccessFul;
    }

    /**
     * Auxiliary method to determine wether a file is an image file
     *
     * @param filePath
     * @return bool
     */
    private boolean isImageFile(String filePath) {
        Set<String> extensions = new HashSet<String>();
        extensions.add("png");
        extensions.add("PNG");
        extensions.add("jpg");
        extensions.add("JPG");
        extensions.add("jpeg");
        extensions.add("JPEG");
        extensions.add("bmp");
        extensions.add("BMP");

        String fileExtension = "";

        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            fileExtension = filePath.substring(i + 1);
        }

        return extensions.contains(fileExtension);
    }

    /**
     * Auxiliary method to determine wether a file is a VCF file
     *
     * @param filePath
     * @return bool
     */
    private boolean isVCFFile(String filePath) {
        Set<String> extensions = new HashSet<String>();
        extensions.add("vcf");
        extensions.add("VCF");

        String fileExtension = "";

        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            fileExtension = filePath.substring(i + 1);
        }

        return extensions.contains(fileExtension);
    }

    /**
     * Auxiliary method to determine wether a file is a VCF file
     *
     * @param filePath
     * @return bool
     */
    private boolean isPrideXMLManifestFile(String filePath) {
        Set<String> extensions = new HashSet<String>();
        extensions.add("xml");

        String fileExtension = "";

        int i = filePath.lastIndexOf('.');
        if (i > 0) {
            fileExtension = filePath.substring(i + 1);
        }

        return extensions.contains(fileExtension);
    }

}
