package com.emc.metalnx.service.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import com.emc.metalnx.services.exceptions.DataGridCorruptedFileException;
import com.emc.metalnx.services.exceptions.DataGridCorruptedPartException;

/**
 * Class that defines a Data Grid File object used for uploading purposes. It
 * contains file properties such as file size, chunk size and total chunks
 * useful for uploading a big files in small chunks.
 */

public final class DataGridFileForUpload {
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

    // counts how many bytes were transferred
    private int currentTransferredChunks;

    // total number of chunks this part has
    private int totalChunks;

    // map for all parts that will be uploaded until the file transferring gets
    // completed
    private Map<String, DataGridPartForUpload> filePartsMap;

    // user who is doing the upload
    private String user;

    // time stamp when the upload started
    private long timeStamp;

    // path where the parts will be placed under the user upload dir
    private String pathToParts;

    // DataGrid upload variables

    // Whether or not it's needed to compute checksum on the data grid
    private boolean dataGridComputeChecksum;

    // Whether or not it's needed to overwrite existing files with the same name
    // in the data grid
    private boolean dataGridOverwriteDuplicatedFiles;

    // Whether or not this file should be replicated in another resource
    private boolean replicateFile;

    // destination resource on the data grid the file will be sent
    private String dataGridDestinationResource;

    // data grid resource where the file will be replicated
    private String dataGridReplicationResource;

    // path this file will uploaded into the data grid
    private String targetPath;

    private static final Logger logger = LoggerFactory.getLogger(DataGridFileForUpload.class);

    public static final String FILE_PART_NUM_FORMAT = "00000";

    /**
     * Creates a file instance for chucking upload.
     *
     * @param fileName
     *            name of the file that will be uploaded
     * @param fileSize
     *            size of the file that will be uploaded
     * @param partSize
     *            part size of the file
     * @param totalParts
     *            number of parts that will be uploaded
     * @param chunkSize
     *            size of each chunk from a part
     * @param totalChunksPerPart
     *            max number of chunks that will be sent for each part
     * @param totalChunks
     *            max number of chunks sent in total
     * @param destResource
     *            where this file has to live in the data grid
     * @throws FileNotFoundException
     */
    public DataGridFileForUpload(String fileName, long fileSize, long partSize, int totalParts, int chunkSize, int totalChunksPerPart,
            int totalChunks, String destResource, String targetPath, String user) throws FileNotFoundException {

        this.fileName = fileName;
        this.fileSize = fileSize;
        this.partSize = partSize;
        this.totalParts = totalParts;
        this.chunkSize = chunkSize;
        this.totalChunksPerPart = totalChunksPerPart;
        this.totalChunks = totalChunks;
        this.isFileCorrupted = false;
        this.currentTransferredChunks = 0;
        this.dataGridDestinationResource = destResource;
        this.targetPath = targetPath;
        this.user = user;
        this.timeStamp = System.currentTimeMillis();
        this.pathToParts = fileName + "_" + this.timeStamp;

        // creating the user temp directory
        File userDirForUpload = new File(user);
        if ( !userDirForUpload.exists()) {
            userDirForUpload.mkdir();
        }

        // creating directory where the parts will stay (under the user temp
        // directory)
        this.fileDir = new File(user, this.pathToParts);
        this.fileDir.mkdir();

        // creating hash map for file partss
        this.filePartsMap = new HashMap<String, DataGridPartForUpload>();

    }

    /**
     * Method that will get a chunk and write this chunk to its corresponding
     * file part.
     *
     * @param multipartFileChunk
     *            chunk of a part
     * @param partNumber
     *            number of the part that the chunk belongs to
     * @param chunkNumber
     *            chunk sequence number
     * @throws DataGridCorruptedPartException
     *             Exception thrown when a chunk it's smaller or greater than
     *             it's supposed to
     * @return True, if the file was completely transferred to the server (all
     *         its parts were joined). False, otherwise.
     */
    synchronized public boolean writeChunkToFile(MultipartFile multipartFileChunk, int partNumber, int chunkNumber, long partCRC32) {

        // validating both part and chunk to avoid part or chunk with a sequence
        // number that
        // exceeds the total number of parts and total chunks per part agreed in
        // the handshake
        if (partNumber >= this.totalParts || partNumber < 0 || chunkNumber < 0 || chunkNumber >= this.totalChunksPerPart) {
            logger.error("Invalid {} part {} and chunk {}. Dropping chunk.", this.fileName, partNumber, chunkNumber);
            return false;
        }

        DataGridPartForUpload currentDataGridPartForUpload = null;
        boolean isFileTransferredSuccessfully = false;

        try {
            String tempFilePartName = this.fileName + "." + new DecimalFormat(FILE_PART_NUM_FORMAT).format(partNumber);

            // another chunk of that part was already sent, the part already
            // exists
            if (this.filePartsMap.containsKey(tempFilePartName)) {
                currentDataGridPartForUpload = this.filePartsMap.get(tempFilePartName);
            } else {
                // if file part doesn't exist, create it and put it into the map
                currentDataGridPartForUpload = new DataGridPartForUpload(this.fileName, this.fileSize, partNumber, this.partSize,
                        this.totalChunksPerPart, this.chunkSize, this.fileDir.getPath(), partCRC32);

                this.filePartsMap.put(tempFilePartName, currentDataGridPartForUpload);
            }

            // delegate the function of really writing the chunk to the file to
            // its corresponding part
            currentDataGridPartForUpload.writeChunkToFilePart(multipartFileChunk, chunkNumber);
            this.currentTransferredChunks++;

            logger.info("Chunk {} written to {} part {}", chunkNumber, this.fileName, partNumber);

            // end of file transferring - all parts were uploaded to the server.
            // Time to join them.
            if (this.currentTransferredChunks == this.totalChunks) {
                logger.info("Current Total Chunks: {} - Total chunks : {}", this.currentTransferredChunks, this.totalChunks);

                logger.info("Last chunk of the last part. Time to join the parts.");
                isFileTransferredSuccessfully = this.joinFileParts();
                this.currentTransferredChunks = 0;
            }

        }
        catch (DataGridCorruptedPartException e) {
            logger.error("Corrupted part in file {}", this.fileName, e.getMessage());
            isFileTransferredSuccessfully = false;
            this.currentTransferredChunks = this.currentTransferredChunks - currentDataGridPartForUpload.getListOfChunksReceived().size();
            throw e;
        }
        catch (IOException e) {
            logger.error("Could not write chunk into file {}", this.fileName, e);
            isFileTransferredSuccessfully = false;
        }
        catch (NullPointerException e) {
            logger.error("Could not join parts.", this.fileName, e.getMessage());
            isFileTransferredSuccessfully = false;
        }

        return isFileTransferredSuccessfully;
    }

    /**
     * Method responsible for re-assembling the file parts into a single file.
     * It removes all parts from the server file system.
     *
     * @return True, if all parts were joined successfully. False, otherwise.
     * @throws DataGridCorruptedFileException
     *             Exception thrown when one or more parts of the file are
     *             corrupted.
     */
    synchronized public boolean joinFileParts() throws DataGridCorruptedFileException {
        logger.info("Asseblying file parts to a single file");
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        String currentFilePartName = "";
        CRC32 crc32 = new CRC32();
        DataGridPartForUpload dataGridPartForUpload = null;

        try {
            this.finalFile = new File(this.fileDir.getPath(), this.fileName);
            bos = new BufferedOutputStream(new FileOutputStream(this.finalFile, true));

            for (int i = 0; i < this.filePartsMap.size(); i++) {
                currentFilePartName = this.fileName + "." + new DecimalFormat(FILE_PART_NUM_FORMAT).format(i);
                dataGridPartForUpload = this.filePartsMap.get(currentFilePartName);
                crc32.reset();

                if (dataGridPartForUpload.isPartCorrupted()) {
                    StringBuffer errMsg = new StringBuffer();
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
        catch (FileNotFoundException e) {
            logger.error("Could not join file parts for {}", e);
            this.isFileCorrupted = true;
        }
        catch (IOException e) {
            logger.error("Could not join file parts for {}", e);
            this.isFileCorrupted = true;
        }
        finally {
            try {
                if (bis != null) {
                    bis.close();
                }

                bos.close();
                if (dataGridPartForUpload != null) {
                    dataGridPartForUpload.removeTempFilePart();
                }
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
        logger.info("Deleting temp directory {}", this.fileDir.getPath());

        if (this.fileDir.exists()) {
            try {
                logger.info("Force delete {}", this.fileDir);
                FileUtils.forceDelete(this.fileDir);
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
     * @param isFileCorrupted
     *            the isFileCorrupted to set
     */
    public void setFileCorrupted(boolean isFileCorrupted) {
        this.isFileCorrupted = isFileCorrupted;
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
     * @return the fileSize
     */
    public long getFileSize() {
        return this.fileSize;
    }

    /**
     * @param fileSize
     *            the fileSize to set
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * @return the chunkSize
     */
    public long getChunkSize() {
        return this.chunkSize;
    }

    /**
     * @param chunkSize
     *            the chunkSize to set
     */
    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    /**
     * @return the totalChunksPerPart
     */
    public int getTotalChunksPerPart() {
        return this.totalChunksPerPart;
    }

    /**
     * @param totalChunks
     *            the totalChunksPerPart to set
     */
    public void setTotalChunksPerPart(int totalChunksPerPart) {
        this.totalChunksPerPart = totalChunksPerPart;
    }

    /**
     * @return the totalParts
     */
    public int getTotalParts() {
        return this.totalParts;
    }

    /**
     * @param totalParts
     *            the totalParts to set
     */
    public void setTotalParts(int totalParts) {
        this.totalParts = totalParts;
    }

    /**
     * @return the totalChunks
     */
    public int getTotalChunks() {
        return this.totalChunks;
    }

    /**
     * @param totalChunks
     *            the totalChunks to set
     */
    public void setTotalChunks(int totalChunks) {
        this.totalChunks = totalChunks;
    }

    /**
     * @return the fileParts
     */
    public Map<String, DataGridPartForUpload> getFilePartsMap() {
        return this.filePartsMap;
    }

    /**
     * @param fileParts
     *            the fileParts to set
     */
    public void setFilePartsMap(Map<String, DataGridPartForUpload> filePartsMap) {
        this.filePartsMap = filePartsMap;
    }

    /**
     * @return the currentTransferredChunks
     */
    public int getCurrentTransferredChunks() {
        return this.currentTransferredChunks;
    }

    /**
     * @param currentTransferredChunks
     *            the currentTransferredChunks to set
     */
    public void setCurrentTransferredChunks(int currentTransferredChunks) {
        this.currentTransferredChunks = currentTransferredChunks;
    }

    /**
     * @return the partSize
     */
    public long getPartSize() {
        return this.partSize;
    }

    /**
     * @param partSize
     *            the partSize to set
     */
    public void setPartSize(long partSize) {
        this.partSize = partSize;
    }

    /**
     * @return the file
     */
    public File getFile() {
        return this.finalFile;
    }

    /**
     * @param file
     *            the file to set
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
     * @param dataGridComputeChecksum
     *            the dataGridComputeChecksum to set
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
     * @param dataGridOverwriteExistingFiles
     *            the dataGridOverwriteExistingFiles to set
     */
    public void setDataGridOverwriteDuplicatedFiles(boolean dataGridOverwriteDuplicatedFiles) {
        this.dataGridOverwriteDuplicatedFiles = dataGridOverwriteDuplicatedFiles;
    }

    /**
     * @return the dataGridDestinationResource
     */
    public String getDataGridDestinationResource() {
        return this.dataGridDestinationResource;
    }

    /**
     * @param dataGridDestinationResource
     *            the dataGridDestinationResource to set
     */
    public void setDataGridDestinationResource(String dataGridDestinationResource) {
        this.dataGridDestinationResource = dataGridDestinationResource;
    }

    /**
     * @return the dataGridReplicationResource
     */
    public String getDataGridReplicationResource() {
        return this.dataGridReplicationResource;
    }

    /**
     * @param dataGridReplicationResource
     *            the dataGridReplicationResource to set
     */
    public void setDataGridReplicationResource(String dataGridReplicationResource) {
        this.dataGridReplicationResource = dataGridReplicationResource;
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
     * @param targetPath
     *            the targetPath to set
     */
    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    /**
     * @return the userSessionID
     */
    public String getUser() {
        return this.user;
    }

    /**
     * @param userSessionID
     *            the userSessionID to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the timeStamp
     */
    public long getTimeStamp() {
        return this.timeStamp;
    }

    /**
     * @param timeStamp
     *            the timeStamp to set
     */
    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * @return the pathToParts
     */
    public String getPathToParts() {
        return this.pathToParts;
    }

    /**
     * @param pathToParts
     *            the pathToParts to set
     */
    public void setPathToParts(String pathToParts) {
        this.pathToParts = pathToParts;
    }

}
