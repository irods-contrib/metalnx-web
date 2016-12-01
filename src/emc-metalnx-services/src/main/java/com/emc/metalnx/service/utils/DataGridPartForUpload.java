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

import com.emc.metalnx.services.exceptions.DataGridCorruptedPartException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that defines a Data Grid File part object used for uploading purposes.
 * It contains file part properties such as part size, part number and list of
 * chunks received useful for uploading big files in smaller parts.
 */

public final class DataGridPartForUpload {
    public static final String FILE_PART_NUM_FORMAT = "00000";
    private static final Logger logger = LoggerFactory.getLogger(DataGridPartForUpload.class);
    // name of the file being uploaded
    private String fileName;
    // size of the file this part belongs to
    private long fileSize;
    // name of the file part being uploaded
    private String filePartName;
    // file part instance being uploaded
    private File filePart;
    // number representing which part of the file this instance is
    private int partNumber;
    // size of the part
    private long partSize;
    // total number of chunks per part after splitting up a file part
    private int totalChunksPerPart;
    // property used for checking if all chunks of this file were uploaded
    private boolean isPartCorrupted;
    // contains all chunks' sequence number in the same order the chunks get to
    // the server
    private List<Integer> listOfChunksReceived;
    // size of each chunk
    private int chunkSize;
    private long partCRC32;

    /**
     * Constructor for a file part that will be uploaded to the server.
     * 
     * @param fileName
     *            file name this part belongs to
     * @param fileSize
     *            size of the file this part belongs to
     * @param partNumber
     *            sequence number of the part
     * @param partSize
     *            size of the part
     * @param totalChunksPerPart
     *            max number of chunks that will be sent to server for the part
     * @param chunkSize
     *            size of each chunk
     * @param path
     *            where in the server the part is located
     * @throws FileNotFoundException
     */
    public DataGridPartForUpload(String fileName, long fileSize, int partNumber, long partSize, int totalChunksPerPart,
                                 int chunkSize, String path, long partCRC32) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.partNumber = partNumber;
        this.partSize = partSize;
        this.totalChunksPerPart = totalChunksPerPart;
        this.chunkSize = chunkSize;
        this.isPartCorrupted = false;

        String partNumFormatted = new DecimalFormat(FILE_PART_NUM_FORMAT).format(partNumber);
        this.filePartName = String.format("%s.%s", fileName, partNumFormatted);
        this.filePart = new File(path, filePartName);

        this.partCRC32 = partCRC32;

        listOfChunksReceived = new ArrayList<>();
    }

    /**
     * Writes a multipart file chunk into a file part in the server file system.
     * 
     * @param multipartFileChunk
     *            file chunk that will be stored in the server
     * @param chunkNumber
     *            sequence number of the chunk
     * @throws DataGridCorruptedPartException
     *             exception thrown when the file chunk is corrupted
     * @throws IOException
     */
    synchronized public void writeChunkToFilePart(MultipartFile multipartFileChunk, int chunkNumber)
            throws DataGridCorruptedPartException, IOException {

        if (chunkNumber < 0 || chunkNumber >= totalChunksPerPart) {
            isPartCorrupted = true;
            StringBuffer errMsg = new StringBuffer();
            errMsg.append("File " + fileName);
            errMsg.append(". Invalid chunk " + chunkNumber);
            errMsg.append(" for part " + partNumber);
            logger.error(errMsg.toString());
            throw new DataGridCorruptedPartException(errMsg.toString());
        }

        byte[] bytesToBeTransferred = multipartFileChunk.getBytes();

        logger.info("Writing chunk {} into part: {}", chunkNumber, filePart);
        RandomAccessFile raf = null;

        try {
            raf = new RandomAccessFile(filePart, "rw");
            raf.seek(chunkNumber * chunkSize);
            raf.write(bytesToBeTransferred);
            raf.close();

            // updating the number of chunks received for this part
            addChunkToListOfChunksReceived(chunkNumber);
        } catch (IOException e) {
            logger.error("Could not write chunk to its part: {}", e.getMessage());
        } finally {
            if (raf != null) raf.close();
        }
    }

    /**
     * Removes a temporary file part from the server
     */
    public void removeTempFilePart() {
        logger.info("Deleting temp file part {}", partNumber);

        if (filePart.exists()) {
            try {
                logger.info("Force delete {}", filePart.getName());
                FileUtils.forceDelete(filePart);
            }
            catch (IOException e) {
                logger.error("Could not delete directory {}", filePart.getPath());
            }
        }
    }

    /**
     * Lists all chunks that already exist in the server
     * 
     * @return list of integers that represent the sequence number of all chunks
     *         sent
     */
    public List<Integer> getListOfChunksUploaded() {
        return listOfChunksReceived;
    }

    /**
     * Get the last chunk uploaded to the server
     * 
     * @return sequence number of the last chunk sent
     */
    public int getLastChunkUploaded() {
        return listOfChunksReceived.get(listOfChunksReceived.size() - 1);
    }

    /**
     * Adds the sequence number of a new chunk that arrived in the server. The
     * latest chunk received by the server will be the last item in the list.
     * 
     * @param chunkNumber
     */
    public void addChunkToListOfChunksReceived(int chunkNumber) {
        listOfChunksReceived.add(chunkNumber);
    }

    /**
     * Removes a chunk based on its sequence number from the list of chunks
     * already sent to the server
     * 
     * @param chunkNumber
     */
    public void removeChunkFromListOfChunksReceived(int chunkNumber) {
        listOfChunksReceived.add(chunkNumber);
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName
     *            the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * @return the filePart
     */
    public File getFilePart() {
        return filePart;
    }

    /**
     * @param filePart
     *            the filePart to set
     */
    public void setFilePart(File filePart) {
        this.filePart = filePart;
    }

    /**
     * @return the partNumber
     */
    public int getPartNumber() {
        return partNumber;
    }

    /**
     * @param partNumber
     *            the partNumber to set
     */
    public void setPartNumber(int partNumber) {
        this.partNumber = partNumber;
    }

    /**
     * @return the isPartCorrupted
     */
    public boolean isPartCorrupted() {
        long lastFilePartSize = fileSize % partSize;

        // if this is the last part, it must match the last file part size
        if (filePart.length() == lastFilePartSize) {
            isPartCorrupted = false;
        }
        // if this part isn't the last, it must match the size of a part
        else isPartCorrupted = filePart.length() != partSize;

        return isPartCorrupted;
    }

    /**
     * @return the listOfChunksReceived
     */
    public List<Integer> getListOfChunksReceived() {
        return listOfChunksReceived;
    }

    /**
     * @return the partCRC32
     */
    public long getPartCRC32() {
        return partCRC32;
    }

}
