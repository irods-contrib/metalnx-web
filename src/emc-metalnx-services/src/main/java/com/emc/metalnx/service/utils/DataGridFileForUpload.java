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

package com.emc.metalnx.service.utils;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridFileNotFoundException;
import com.emc.metalnx.services.exceptions.DataGridCorruptedFileException;
import com.emc.metalnx.services.exceptions.DataGridCorruptedPartException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * Class that defines a Data Grid File object used for uploading purposes. It
 * contains file properties such as file size, chunk size and total chunks
 * useful for uploading a big files in small chunks.
 */

public final class DataGridFileForUpload {
    private static final Logger logger = LoggerFactory.getLogger(DataGridFileForUpload.class);
    private static final String FILE_PART_NUM_FORMAT = "00000";
    // file instance being uploaded
    private File finalFile;
    // file dir being uploaded
    private File fileDir;
    // name of the file being uploaded
    private String fileName;
    // size of the file
    private long fileSize;
    // size of the part
    private long partSize;
    // total number of parts ("big chunks")
    private int totalParts;
    // total number of chunks per part after splitting up a file part
    private int totalChunksPerPart;
    // size of each chunk
    private int chunkSize;
    // property used for checking if the file was uploaded to
    private boolean isFileCorrupted;
    // used to check whether or not the file can be transferred to the data grid
    private boolean isFileReady;
    // counts how many bytes were transferred
    private int currentTransferredChunks;
    // total number of chunks this part has
    private int totalChunks;
    // map for all parts that will be uploaded until the file transferring gets
    // completed
    private Map<String, DataGridPartForUpload> filePartsMap;

    // DataGrid upload variables
    // user who is doing the upload
    private String user;
    // path where the parts will be placed under the user upload dir
    private String pathToParts;
    // Whether or not it's needed to compute checksum on the data grid
    private boolean dataGridComputeChecksum;
    // Whether or not it's needed to overwrite existing files with the same name
    // in the data grid
    private boolean dataGridOverwriteDuplicatedFiles;
    // Whether or not this file should be replicated in another resource
    private boolean replicateFile;
    // destination resource on the data grid the file will be sent
    private String destinationResource;
    // data grid resource where the file will be replicated
    private String replResc;
    // path this file will uploaded into the data grid
    private String targetPath;

    /**
     * Creates a file instance for chucking upload.
     *
     * @param fileName name of the file that will be uploaded
     * @param fileSize size of the file that will be uploaded
     * @param partSize part size of the file
     * @param totalParts number of parts that will be uploaded
     * @param chunkSize size of each chunk from a part
     * @param totalChunksPerPart max number of chunks that will be sent for each part
     * @param totalChunks max number of chunks sent in total
     * @param destResource where this file has to live in the data grid
     */
    public DataGridFileForUpload(String fileName, long fileSize, long partSize, int totalParts, int chunkSize,
                                 int totalChunksPerPart, int totalChunks, String destResource, String targetPath,
                                 String user) {

        this.fileName = fileName;
        this.fileSize = fileSize;
        this.partSize = partSize;
        this.totalParts = totalParts;
        this.chunkSize = chunkSize;
        this.totalChunksPerPart = totalChunksPerPart;
        this.totalChunks = totalChunks;
        this.isFileCorrupted = false;
        this.isFileReady = false;
        this.currentTransferredChunks = 0;
        this.destinationResource = destResource;
        this.targetPath = targetPath;
        this.user = user;
        this.pathToParts = fileName + "_" + System.currentTimeMillis();

        // creating the user temp directory
        File userDirForUpload = new File(user);
        if ( !userDirForUpload.exists()) {
            userDirForUpload.mkdir();
        }

        // creating directory where the parts will stay (under the user temp
        // directory)
        fileDir = new File(user, this.pathToParts);
        fileDir.mkdir();

        // creating hash map for file partss
        this.filePartsMap = new HashMap<>();
    }

    /**
     * Checks whether or not the file is ready to be sent to the data grid
     *
     * @return True, if the file is ready for transferring. False, otherwise.
     */
    synchronized public boolean isFileReadyForDataGrid() {
        return this.isFileReady && !this.isFileCorrupted();
    }

    /**
     * Writes a file chunk into its corresponding part.
     *
     * @param chunk piece of a file
     */
    synchronized public void writeChunk(DataGridChunkForUpload chunk) throws DataGridException {
        this.writeChunk(chunk.getChunk(), chunk.getPartNumber(), chunk.getChunkNumber(), chunk.getCrc());
    }

    /**
     * Method that will get a chunk and write this chunk to its corresponding file part.
     *
     * @param multipartFileChunk chunk of a part
     * @param partNumber         number of the part that the chunk belongs to
     * @param chunkNumber        chunk sequence number
     * @throws DataGridCorruptedPartException is thrown when a chunk it's smaller or greater than it is supposed to
     */
    synchronized private void writeChunk(MultipartFile multipartFileChunk, int partNumber, int chunkNumber, long partCRC32) throws DataGridException {

        // validating both part and chunk to avoid part or chunk with a sequence number that exceeds the total number of
        // parts and total chunks per part agreed in the handshake
        if (partNumber >= this.totalParts || partNumber < 0 || chunkNumber < 0 || chunkNumber >= this.totalChunksPerPart) {
            logger.error("Invalid {} part {} and chunk {}. Dropping chunk.", this.fileName, partNumber, chunkNumber);
            return;
        }

        DataGridPartForUpload part = null;

        try {
            String decimalFormat = new DecimalFormat(FILE_PART_NUM_FORMAT).format(partNumber);
            String tempFilePartName = String.format("%s.%s", this.fileName, decimalFormat);

            // another chunk of that part was already sent, the part already exists
            if (filePartsMap.containsKey(tempFilePartName)) {
                part = filePartsMap.get(tempFilePartName);
            } else {
                // if file part doesn't exist, create it and put it into the map
                part = new DataGridPartForUpload(fileName, fileSize, partNumber, partSize, totalChunksPerPart, chunkSize, fileDir.getPath(), partCRC32);
                filePartsMap.put(tempFilePartName, part);
            }

            // delegate the function of really writing the chunk to the file to its corresponding part
            part.writeChunkToFilePart(multipartFileChunk, chunkNumber);
            this.currentTransferredChunks++;

            logger.info("Chunk {} written to {} part {}", chunkNumber, this.fileName, partNumber);

            if (currentTransferredChunks == this.totalChunks) {
                logger.info("Current Total Chunks: {} - Total chunks : {}", currentTransferredChunks, this.totalChunks);
                logger.info("Last chunk of the last part. Time to join the parts.");

                isFileReady = this.joinFileParts();
                currentTransferredChunks = 0;
            }

        } catch (DataGridException e) {
            logger.error("Corrupted part in file {}", this.fileName, e.getMessage());
            this.currentTransferredChunks = this.currentTransferredChunks - part.getListOfChunksReceived().size();
            throw e;
        } catch (IOException e) {
            logger.error("Could write junk to part: ", e.getMessage());
            throw new DataGridFileNotFoundException("Could not file part to write chunk");
        } catch (NullPointerException e) {
            logger.error("Could not join parts.", this.fileName, e.getMessage());
        }
    }

    /**
     * Method responsible for re-assembling the file parts into a single file.
     * It removes all parts from the server file system.
     *
     * @return True, if all parts were joined successfully. False, otherwise.
     * @throws DataGridCorruptedFileException
     *             Exception thrown when one or more parts of the file are corrupted.
     */
    synchronized private boolean joinFileParts() throws DataGridException {
        logger.info("Asseblying file parts to a single file");

        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        DataGridPartForUpload dataGridPartForUpload = null;
        String currentFilePartName;
        CRC32 crc32 = new CRC32();

        try {
            this.finalFile = new File(this.fileDir.getPath(), this.fileName);
            bos = new BufferedOutputStream(new FileOutputStream(this.finalFile, true));

            for (int i = 0; i < this.filePartsMap.size(); i++) {
                currentFilePartName = this.fileName + "." + new DecimalFormat(FILE_PART_NUM_FORMAT).format(i);
                dataGridPartForUpload = this.filePartsMap.get(currentFilePartName);
                crc32.reset();

                if (dataGridPartForUpload.isPartCorrupted()) {
                    StringBuilder errMsg = new StringBuilder();
                    errMsg.append("Part ");
                    errMsg.append(dataGridPartForUpload.getPartNumber());
                    errMsg.append(" of ");
                    errMsg.append(this.fileName);
                    errMsg.append(" is corrupted. File cannot be joined.");

                    // marking this file as corrupted
                    this.isFileCorrupted = true;

                    // deleting this file since one or more of its parts are
                    // corrupted
                    this.finalFile.delete();

                    // closing buffered input stream to this file
                    bos.close();

                    logger.error(errMsg.toString());
                    throw new DataGridCorruptedPartException(errMsg.toString());
                }

                File currentFilePart = dataGridPartForUpload.getFilePart();

                logger.debug("Reading part {}", currentFilePartName);

                bis = new BufferedInputStream(new FileInputStream(currentFilePart));

                // reading a part and appending it to the file
                int currentFilePartLength = (int) currentFilePart.length();
                byte[] bytesOfFilePart = new byte[currentFilePartLength];
                bis.read(bytesOfFilePart, 0, currentFilePartLength);
                bos.write(bytesOfFilePart);
                crc32.update(bytesOfFilePart);
                bos.flush();

                // closing input stream of the current part
                bis.close();

                logger.info("Finished reading part {}", currentFilePartName);

                logger.info("Part " + i + " | CRC32 from front-end: " + dataGridPartForUpload.getPartCRC32() + " | CRC32 calculated in back-end: "
                        + crc32.getValue());

                if (dataGridPartForUpload.getPartCRC32() != crc32.getValue()) {
                    bos.close();
                    throw new DataGridCorruptedPartException("CRC32 for part " + i + " doesn't match");
                }
            }

        }
        catch (IOException e) {
            logger.error("Could not join file parts for {}", e);
            this.isFileCorrupted = true;
        }
        finally {
            try {
                if (bis != null) bis.close();
                if (bos != null) bos.close();
                if (dataGridPartForUpload != null) dataGridPartForUpload.removeTempFilePart();
            }
            catch (Exception e) {
                logger.error("Could not close buffers: {}", e);
            }
        }

        // if file is not corrupted, it means all parts were joined
        // successfully.
        return !this.isFileCorrupted;
    }

    /**
     * Removes the file from the server. This method should be called after the
     * file is completely transferred to the data grid.
     */
    public void removeTempFile() {
        logger.info("Removing temporary files from Tomcat. Deleting temp directory {}", this.fileDir.getPath());

        if (this.fileDir.exists()) {
            try {
                logger.info("Force delete {}", this.fileDir);
                FileUtils.forceDelete(this.fileDir);
                logger.info("Temporary file removed from Tomcat.");
            }
            catch (IOException e) {
                logger.error("Could not delete directory {}", this.fileDir.getPath());
            }
        }
    }

    /**
     * Finds which is the latest part uploaded
     *
     * @return number of the latest part uploaded
     */
    public int getLastPartUploaded() {
        return this.filePartsMap.size() - 1;
    }

    /**
     * Lists all chunks already uploaded to the server.
     *
     * @return list of the chunks the server already received
     */
    public List<Integer> getChunksUploadedFromLastPartUploaded() {
        int lastPartUploaded = this.getLastPartUploaded();

        String lastPartUploadedName = this.fileName + "." + new DecimalFormat(FILE_PART_NUM_FORMAT).format(lastPartUploaded);

        return this.filePartsMap.get(lastPartUploadedName).getListOfChunksUploaded();
    }

    /**
     * @return the isFileCorrupted
     */
    public boolean isFileCorrupted() {
        return this.isFileCorrupted;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return this.fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return this.finalFile;
    }

    /**
     * @param finalFile the file to set
     */
    public void setFile(File finalFile) {
        this.finalFile = finalFile;
    }

    /**
     * @return the dataGridComputeChecksum
     */
    public boolean isDataGridComputeChecksum() {
        return this.dataGridComputeChecksum;
    }

    /**
     * @param dataGridComputeChecksum the dataGridComputeChecksum to set
     */
    public void setDataGridComputeChecksum(boolean dataGridComputeChecksum) {
        this.dataGridComputeChecksum = dataGridComputeChecksum;
    }

    /**
     * @return the dataGridOverwriteExistingFiles
     */
    public boolean isDataGridOverwriteDuplicatedFiles() {
        return this.dataGridOverwriteDuplicatedFiles;
    }

    /**
     * @param dataGridOverwriteDuplicatedFiles the dataGridOverwriteDuplicatedFiles to set
     */
    public void setDataGridOverwriteDuplicatedFiles(boolean dataGridOverwriteDuplicatedFiles) {
        this.dataGridOverwriteDuplicatedFiles = dataGridOverwriteDuplicatedFiles;
    }

    /**
     * @return the destinationResource
     */
    public String getDestResc() {
        return this.destinationResource;
    }

    /**
     * Get the replication resource name
     * @return the name of the resource for replication
     */
    public String getReplResc() {
        return this.replResc;
    }

    /**
     * Sets the resource to replicate the file
     * @param replResc resource name for replication
     */
    public void setReplResc(String replResc) {
        this.replResc = replResc;
    }

    /**
     * @return the replicateFile
     */
    public boolean isReplicateFile() {
        return this.replicateFile;
    }

    /**
     * @param replicateFile
     *            the replicateFile to set
     */
    public void setReplicateFile(boolean replicateFile) {
        this.replicateFile = replicateFile;
    }

    /**
     * @return the targetPath
     */
    public String getTargetPath() {
        return this.targetPath;
    }

    /**
     * @return the full file path (/full/path/to/file/nameofthefile)
     */
    public String getPath() {
        return String.format("%s/%s", getTargetPath(), fileName);
    }

    /**
     * @return the userSessionID
     */
    public String getUser() {
        return this.user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the pathToParts
     */
    public String getPathToParts() {
        return this.pathToParts;
    }
}
