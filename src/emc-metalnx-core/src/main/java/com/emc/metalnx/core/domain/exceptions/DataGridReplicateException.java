package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when a replication of a file fails.
 */
public class DataGridReplicateException extends DataGridException {
    public DataGridReplicateException(String msg) {
        super(msg);
    }
}
