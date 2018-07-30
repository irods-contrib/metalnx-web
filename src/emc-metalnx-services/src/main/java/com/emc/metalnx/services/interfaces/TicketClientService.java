 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.exceptions.DataGridTicketDownloadException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketInvalidUserException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketUploadException;

import java.io.File;

/**
 * Client access to the grid via Tickets.
 */
public interface TicketClientService {

    /**
     * Transfers a file to the grid using a ticket.
     * @param ticketString ticket string
     * @param localFile local file to be transferred to the grid
     * @param destPath path where the file will be uploaded to
     * @throws DataGridTicketUploadException if ticket string, local file or destination path are not provided
     * @throws DataGridTicketInvalidUserException if anonymous account is not valid (account does not exist)
     */
    void transferFileToIRODSUsingTicket(String ticketString, File localFile, String destPath)
            throws DataGridTicketUploadException, DataGridTicketInvalidUserException;

    /**
     * Gets a file from the grid.
     * @param ticketString ticket string to access a collection or an object
     * @param path path to get files from
     * @return {@code File} file
     * @throws DataGridTicketInvalidUserException if anonymous account is not valid (account does not exist)
     * @throws DataGridTicketDownloadException if any other error happens during download
     */
    File getFileFromIRODSUsingTicket(String ticketString, String path)
            throws DataGridTicketInvalidUserException, DataGridTicketDownloadException;

    /**
     * Deletes the temporary directory created after downloading files from the grid using a ticket.
     */
    void deleteTempTicketDir();
}
