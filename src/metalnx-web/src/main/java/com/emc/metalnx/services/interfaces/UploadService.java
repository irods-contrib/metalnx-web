 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    /**
     * Transfer a file to the data grid without chuncking file.
     *
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
    boolean upload(MultipartFile multipartFile, String targetPath, boolean computeCheckSum,
                   boolean replicateFile, String replicationResource, String destinationResource,
                   boolean overwriteDuplicateFiles) throws DataGridException;
}
