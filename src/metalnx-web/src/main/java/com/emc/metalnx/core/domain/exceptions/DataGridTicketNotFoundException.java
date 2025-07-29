 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when ticket cannot be found.
 */
public class DataGridTicketNotFoundException extends DataGridException {
    public DataGridTicketNotFoundException(String message) {
        super(message);
    }
}
