 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when there is an error during download with tickets.
 */
public class DataGridTicketDownloadException extends DataGridException {
    private String path, ticketString;

    public DataGridTicketDownloadException(String msg, String path, String ticketString) {
        super(msg);
        this.path = path;
        this.ticketString = ticketString;
    }

    public String getPath() {
        return path;
    }

    public String getTicketString() {
        return ticketString;
    }
}
