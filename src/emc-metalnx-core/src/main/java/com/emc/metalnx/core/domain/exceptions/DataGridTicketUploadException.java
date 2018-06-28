 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Class that represents the error during an upload with a ticket
 */
public class DataGridTicketUploadException extends DataGridException {
    public DataGridTicketUploadException(String message) {
        super(message);
    }
}
