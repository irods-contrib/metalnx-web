/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridFileAlreadyExists;
import com.emc.metalnx.services.interfaces.*;
import com.emc.metalnx.services.machine.util.DataGridUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.ResourceAO;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

@Service
@Transactional
public class UploadServiceImpl implements UploadService {

    private static final int BUFFER_SIZE = 4 * 1024 * 1024;
    private static final Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);

    @Autowired
    private CollectionService cs;

    @Autowired
    private RuleService rs;

    @Autowired
    private FileOperationService fos;

    @Autowired
    private IRODSServices is;

    @Autowired
    private MSIService msiService;

    @Autowired
    private IRODSAccessObjectFactory irodsAccessObjectFactory;

    @Override
    public boolean upload(MultipartFile file, String targetPath, boolean computeCheckSum, boolean replicateFile,
                          String replicationResc, String destResc, boolean overwrite)
            throws DataGridException {

        if (file == null || file.isEmpty() || "".equals(targetPath) || targetPath == null
                || "".equals(destResc) || destResc == null) {
            logger.error("File could not be sent to the data grid.");
            return false;
        }

        InputStream inputStream;
        try {
            inputStream = file.getInputStream();
        } catch (IOException e) {
            logger.error("Could not get input stream from file: ", e.getMessage());
            throw new DataGridException("Could not get input stream from file.");
        }

        String defaultStorageResource = is.getDefaultStorageResource();

        logger.info("Setting default resource to {}", destResc);

        // Setting temporarily the defaultStorageResource for the logged user
        is.setDefaultStorageResource(destResc);

        boolean isFileUploaded;

        // Getting DataObjectAO in order to create the new file
        IRODSFileFactory irodsFileFactory = is.getIRODSFileFactory();
        Stream2StreamAO stream2StreamA0 = is.getStream2StreamAO();
        IRODSFile targetFile = null;
        try {
            String fileName = file.getOriginalFilename();

            if (fileName.isEmpty()) fileName = file.getName();

            targetFile = irodsFileFactory.instanceIRODSFile(targetPath, fileName);

            // file already exists and we do not want to overwrite it, the transferring is aborted.
            if (targetFile.exists() && !overwrite) {
                String msg = "File already exists. Not overwriting it.";
                logger.info(msg);
                throw new DataGridFileAlreadyExists(msg);
            }

            // Transfering file to iRODS filesystem
            stream2StreamA0.transferStreamToFileUsingIOStreams(inputStream, (File) targetFile, 0, BUFFER_SIZE);

            // Computing a check sum for this file just uploaded to iRODS
            if (computeCheckSum) fos.computeChecksum(targetPath, fileName);

            // Replicating file into desired resource
            if (replicateFile) fos.replicateDataObject(targetFile.getPath(), replicationResc, false);

            // Getting list of resources for upload
            HashMap<String, String> resourceMap = null;
            try {
                ResourceAO resourceAO = is.getResourceAO();
                resourceMap = DataGridUtils.buildMapForResourcesNamesAndMountPoints(resourceAO.findAll());
            }
            catch (JargonException e) {
                logger.error("Could not build Resource map for upload", e);
                throw new DataGridException("Procedures not run after upload. Resource Map creation failed.");
            }
            String objPath = targetFile.getCanonicalPath();
            String filePath = resourceMap.get(destResc) +
                    objPath.substring(objPath.indexOf("/", 1), objPath.length());

            rs.execBamCramMetadataRule(destResc, objPath, filePath);
            rs.execVCFMetadataRule(destResc, objPath, filePath);
            rs.execPopulateMetadataRule(destResc, objPath);
            rs.execImageRule(destResc, objPath, filePath);
            rs.execIlluminaMetadataRule(destResc, targetPath, objPath);
            rs.execManifestFileRule(destResc, targetPath, objPath, filePath);

            isFileUploaded = true;
        } catch (JargonException e) {
            fos.deleteDataObject(targetFile.getPath(), true);
            logger.error("Upload stream failed from Metalnx to the data grid. {}", e.getMessage());
            throw new DataGridException("Upload failed. Resource(s) might be full.");
        } catch (IOException e) {
            logger.error("Could not get canonical path", e.getMessage());
            throw new DataGridException("Could not get canonical path");
        } finally {
            try {
                inputStream.close(); // Closing streams opened
            } catch (IOException e) {
                logger.error("Could close stream: ", e.getMessage());
            }
        }

        // Setting the default resource back to the original one.
        is.setDefaultStorageResource(defaultStorageResource);

        return isFileUploaded;
    }
}
