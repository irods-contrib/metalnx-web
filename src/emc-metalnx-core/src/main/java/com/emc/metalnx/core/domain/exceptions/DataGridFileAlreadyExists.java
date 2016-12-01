package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception classes used when a file is moved, copied, uploaded to a collection, but it already exists there.
 */
public class DataGridFileAlreadyExists extends DataGridException {
    public DataGridFileAlreadyExists(String message) {
        super(message);
    }
}
