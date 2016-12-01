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

package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.service.utils.DataGridChunkForUpload;
import com.emc.metalnx.service.utils.DataGridFileForUpload;

import javax.servlet.http.HttpServletRequest;

public interface UploadService {

    /**
     * Creates a file for upload based on an HTTP request.
     *
     * @param request prepare files for upload request
     * @return DataGridFileForUpload object
     */
    DataGridFileForUpload buildFileForUpload(HttpServletRequest request) throws DataGridException;

    /**
     * Constructs a chunk object based on the chunk sequence number, file part and CRC 32 coming from an HTTP request.
     *
     * @param request HTTP request for file transfer
     * @return Chunk object
     */
    DataGridChunkForUpload getChunk(HttpServletRequest request);

    /**
     * Creates a data grid file for upload object
     *
     * @param filename           name of the file being uploaded
     * @param fileSize           size of the file
     * @param partSize           size of each part
     * @param totalParts         number of parts to transfer the file
     * @param chunkSize          size of each chunk
     * @param totalChunksPerPart number of chunks per part
     * @param totalChunks        total number of chunks
     * @param destResc           destination resource
     * @param replResc           replication resource
     * @param path               path in the data grid the file is uploaded to
     * @param calcChkSum         whether or not calculate the file checksum
     * @param overwrite          whether or not overwrite possible duplicates
     * @param replicate          indicates if the file needs to be replicated or not
     * @return DataGridFileForUpload object
     */
    DataGridFileForUpload buildFileForUpload(String filename, long fileSize, long partSize, int totalParts,
                                             int chunkSize, int totalChunksPerPart, int totalChunks,
                                             String destResc, String replResc, String path, boolean calcChkSum,
                                             boolean overwrite, boolean replicate) throws DataGridException;

    /**
     * Transfer a file to the data grid.
     *
     * @param fileForUpload file that will be transferred
     * @return True, if file or collection was uploaded. False, otherwise.
     * @throws DataGridException if en error occurred during the transfer
     */
    boolean transferFileToDataGrid(DataGridFileForUpload fileForUpload) throws DataGridException;

    /**
     * Procedures that are executed after an transfer of a file to the data grid (iput)
     *
     * @param file file uploaded
     * @throws DataGridException if rules cannot be executed
     */
    void postProcForTransfer(DataGridFileForUpload file) throws DataGridException;
}
