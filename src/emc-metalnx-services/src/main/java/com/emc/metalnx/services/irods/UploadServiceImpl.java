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

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridFileAlreadyExists;
import com.emc.metalnx.service.utils.DataGridChunkForUpload;
import com.emc.metalnx.service.utils.DataGridFileForUpload;
import com.emc.metalnx.services.interfaces.*;
import com.emc.metalnx.services.machine.util.DataGridUtils;
import org.irods.jargon.core.exception.JargonException;
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
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.util.HashMap;

@Service
@Transactional
public class UploadServiceImpl implements UploadService {

    private static final int MEGABYTE = 1024 * 1024;
    private static final Logger logger = LoggerFactory.getLogger(UploadServiceImpl.class);
    @Autowired
    CollectionService cs;
    @Autowired
    RuleService rs;
    @Autowired
    FileOperationService fos;
    @Autowired
    IRODSServices is;

    @Override
    public DataGridFileForUpload buildFileForUpload(HttpServletRequest request) throws DataGridException {

        String fileName = request.getParameter("fileName");
        String path = request.getParameter("path");
        String destResc = request.getParameter("destResc");
        String replResc = request.getParameter("replResc");

        long fileSize = Long.valueOf(request.getParameter("fileSize"));
        long partSize = Long.valueOf(request.getParameter("partSize"));
        int totalParts = Integer.valueOf(request.getParameter("totalParts"));
        int chunkSize = Integer.valueOf(request.getParameter("chunkSize"));
        int totalChunksPerPart = Integer.valueOf(request.getParameter("totalChunksPerPart"));
        int totalChunks = Integer.valueOf(request.getParameter("totalChunks"));

        boolean checksum = Boolean.valueOf(request.getParameter("checksum"));
        boolean replicate = Boolean.valueOf(request.getParameter("replicate"));
        boolean overwrite = Boolean.valueOf(request.getParameter("overwrite"));

        return buildFileForUpload(fileName, fileSize, partSize, totalParts, chunkSize, totalChunksPerPart, totalChunks,
                destResc, replResc, path, checksum, overwrite, replicate);
    }

    @Override
    public DataGridChunkForUpload getChunk(HttpServletRequest request) {
        MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
        MultipartFile multipartFileChunk = multipartRequest.getFile("fileChunk");

        String[] chunkNumberParam = (String[]) request.getParameterMap().get("chunkNumber");
        String[] filePartParam = (String[]) request.getParameterMap().get("filePart");
        String[] partCRC32Param = (String[]) request.getParameterMap().get("partCRC32");

        int chunkNumber = Integer.valueOf(chunkNumberParam[0]);
        int partNumber = Integer.valueOf(filePartParam[0]);
        long partCRC32 = Long.valueOf(partCRC32Param[0]);

        return new DataGridChunkForUpload(multipartFileChunk, partNumber, chunkNumber, partCRC32);
    }

    @Override
    public DataGridFileForUpload buildFileForUpload(String filename, long fileSize, long partSize, int totalParts,
                                                    int chunkSize, int totalChunksPerPart, int totalChunks,
                                                    String destResc, String replResc, String path,
                                                    boolean calcChkSum, boolean overwrite, boolean replicate)
            throws DataGridException {

        DataGridFileForUpload fileForUpload = new DataGridFileForUpload(filename, fileSize, partSize, totalParts,
                chunkSize, totalChunksPerPart, totalChunks, destResc, path, is.getCurrentUser());

        fileForUpload.setDataGridComputeChecksum(calcChkSum);
        fileForUpload.setDataGridOverwriteDuplicatedFiles(overwrite);
        fileForUpload.setReplicateFile(replicate);
        fileForUpload.setReplResc(replResc);

        return fileForUpload;
    }

    @Override
    public boolean transferFileToDataGrid(DataGridFileForUpload file) throws DataGridException {
        logger.info("File transferring complete. Sending it to the data grid.");

        boolean isFileValid = file != null && file.getFile() != null && !file.isFileCorrupted();
        boolean isTargetPathValid = isFileValid && !file.getTargetPath().isEmpty();
        boolean isDestRescValid = isFileValid && !file.getDestResc().isEmpty();

        if (!isFileValid || !isTargetPathValid || !isDestRescValid) {
            logger.error("File is invalid and cannot be transferred to the data grid.");
            return false;
        }

        boolean fileIsAlreadyInCollection =
                cs.isFileInCollection(file.getFile().getName(), file.getTargetPath());

        if (fileIsAlreadyInCollection && !file.isDataGridOverwriteDuplicatedFiles()) {
            String msg = "File already exists. Not overwriting it.";
            logger.info(msg);
            throw new DataGridFileAlreadyExists(msg);
        }

        String defaultStorageResource = is.getDefaultStorageResource();
        String destinationResource = file.getDestResc();

        logger.info("Setting default resource to {}", destinationResource);

        // Setting temporarily the defaultStorageResource for the logged user
        is.setDefaultStorageResource(destinationResource);

        stream2stream(file);

        file.removeTempFile();

        postProcForTransfer(file);

        // Setting the default resource back to the original one.
        is.setDefaultStorageResource(defaultStorageResource);

        return true;
    }

    /**
     * Stream a file to the data grid
     *
     * @param file to be streamed
     * @throws DataGridException is thrown if the file cannot be sent to the data grid
     */
    private void stream2stream(DataGridFileForUpload file) throws DataGridException {
        try {
            logger.info("Transferring {} to iRODS.", file.getFileName());

            IRODSFileFactory irodsFileFactory = is.getIRODSFileFactory();
            IRODSFile targetFile = irodsFileFactory.instanceIRODSFile(file.getTargetPath(), file.getFile().getName());
            targetFile.setResource(file.getDestResc());

            InputStream inputStream = new BufferedInputStream(new FileInputStream(file.getFile()));
            Stream2StreamAO stream2StreamA0 = is.getStream2StreamAO();
            stream2StreamA0.transferStreamToFileUsingIOStreams(inputStream, (File) targetFile, 0, MEGABYTE);
            inputStream.close();

            logger.info("Completed upload to resource {} in {}.", file.getDestResc(), file.getTargetPath());
        } catch (JargonException | IOException e) {
            logger.error("Upload stream failed from Metalnx to the data grid. {}", e.getMessage());
            throw new DataGridException("Upload failed. Resource(s) might be full.");
        }
    }

    @Override
    public void postProcForTransfer(DataGridFileForUpload file) throws DataGridException {

        try {
            if (file.isDataGridComputeChecksum()) fos.computeChecksum(file.getTargetPath(), file.getFile().getName());

            if (file.isReplicateFile()) fos.replicateDataObject(file.getPath(), file.getReplResc(), false);

            ResourceAO rao = is.getResourceAO();
            HashMap<String, String> resourceMap = DataGridUtils.buildMapForResourcesNamesAndMountPoints(rao.findAll());

            IRODSFileFactory irodsFileFactory = is.getIRODSFileFactory();
            IRODSFile targetFile = irodsFileFactory.instanceIRODSFile(file.getTargetPath(), file.getFile().getName());

            String objPath = targetFile.getCanonicalPath();
            String destResc = file.getDestResc();
            String filePath = resourceMap.get(destResc) + objPath.substring(objPath.indexOf("/", 1), objPath.length());

            rs.execBamCramMetadataRule(destResc, objPath, filePath);

            rs.execVCFMetadataRule(destResc, objPath, filePath);

            rs.execPopulateMetadataRule(destResc, objPath);

            rs.execImageRule(destResc, objPath, filePath);

            rs.execIlluminaMetadataRule(destResc, file.getTargetPath(), objPath);

            rs.execManifestFileRule(destResc, file.getTargetPath(), objPath, filePath);
        } catch (JargonException e) {
            logger.error("Could not build Resource map for upload: ", e.getMessage());
            throw new DataGridException("Procedures not run after upload. Resource Map creation failed.");
        } catch (IOException e) {
            logger.error("Could not get canonical path of file: ", e.getMessage());
            throw new DataGridException("Procedures not run after upload. Canonical path of file failed.");
        }
    }


}
