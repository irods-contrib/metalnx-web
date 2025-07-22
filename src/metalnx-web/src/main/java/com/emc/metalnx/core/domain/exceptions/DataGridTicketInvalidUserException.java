 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when a not-authenticated user (anonymous) tries to access a file/collection via ticket.
 */
public class DataGridTicketInvalidUserException extends DataGridException {
    public DataGridTicketInvalidUserException(String message) {
        super(message);
    }
}
