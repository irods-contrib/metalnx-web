 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

public class DataGridConnectionRefusedException extends DataGridException {

    private static final long serialVersionUID = 1L;

    public DataGridConnectionRefusedException() {
        super();
    }

    public DataGridConnectionRefusedException(String message) {
        super(message);
    }
}
