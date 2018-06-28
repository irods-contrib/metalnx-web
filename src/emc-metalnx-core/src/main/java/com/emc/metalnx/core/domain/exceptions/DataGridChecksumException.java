 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception used when no checksum can be calculated on a file in the grid.
 */
public class DataGridChecksumException extends DataGridException {
    public DataGridChecksumException(String msg) {
        super(msg);
    }
}
