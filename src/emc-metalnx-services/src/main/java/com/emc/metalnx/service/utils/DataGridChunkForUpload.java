package com.emc.metalnx.service.utils;

import org.springframework.web.multipart.MultipartFile;

/**
 * Class that represents a chunk of file coming from the browser.
 */
public class DataGridChunkForUpload {

    // actual set of bytes
    private MultipartFile chunk;

    // part sequence number
    private int partNumber;

    // chunk sequence number
    private int chunkNumber;

    // chunk CRC 32 code
    private long crc;

    public DataGridChunkForUpload(MultipartFile chunk, int partNumber, int chunkNumber, long crc) {
        this.chunk = chunk;
        this.partNumber = partNumber;
        this.chunkNumber = chunkNumber;
        this.crc = crc;
    }

    public MultipartFile getChunk() {
        return chunk;
    }

    public int getPartNumber() {
        return partNumber;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public long getCrc() {
        return crc;
    }
}
