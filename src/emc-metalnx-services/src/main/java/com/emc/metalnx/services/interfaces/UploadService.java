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

package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    /**
     * Transfer a file to the data grid without chuncking file.
     *
     * @param fileName
     *          file name that's going to be transferred
     * @param multipartFile
     *          file to be uploaded
     * @param targetPath
     *          path to which the file is going to be tranferred
     * @param computeCheckSum
     *          True if user checked checksum option in UI
     * @param replicateFile
     *          True if user checked replica option in UI
     * @param replicationResource
     *          resources to which the file is going to be replicated into
     * @param destinationResource
     *          resource in which the file is going to be uploaded
     * @param overwriteDuplicateFiles
     *          option to overwrite in case the file already exists in iRODS
     * @return
     * @throws DataGridException
     */
    boolean tranferFileDirectlyToJargon(String fileName, MultipartFile multipartFile, String targetPath, boolean computeCheckSum,
                                        boolean replicateFile, String replicationResource, String destinationResource,
                                        boolean overwriteDuplicateFiles) throws DataGridException;
}
