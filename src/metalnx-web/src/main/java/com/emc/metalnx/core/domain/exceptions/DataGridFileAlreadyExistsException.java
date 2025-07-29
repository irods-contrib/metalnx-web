 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception classes used when a file is moved, copied, uploaded to a collection, but it already exists there.
 */
public class DataGridFileAlreadyExistsException extends DataGridException {
    public DataGridFileAlreadyExistsException(String message) {
        super(message);
    }
}
