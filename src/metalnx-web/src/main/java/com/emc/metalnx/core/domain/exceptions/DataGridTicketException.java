 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when an error occurs during any ticket operation
 */
public class DataGridTicketException extends DataGridException {
    public DataGridTicketException(String msg) { super(msg); }
}
