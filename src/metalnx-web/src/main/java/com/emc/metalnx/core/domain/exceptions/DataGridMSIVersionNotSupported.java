 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when the MSI package installed is not supported by this application.
 */
public class DataGridMSIVersionNotSupported extends DataGridException {
    public DataGridMSIVersionNotSupported(String msg) { super(msg); }
}
