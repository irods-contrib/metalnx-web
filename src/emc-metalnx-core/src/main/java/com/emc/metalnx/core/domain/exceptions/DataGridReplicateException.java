 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when a replication of a file fails.
 */
public class DataGridReplicateException extends DataGridException {
    public DataGridReplicateException(String msg) {
        super(msg);
    }
}
