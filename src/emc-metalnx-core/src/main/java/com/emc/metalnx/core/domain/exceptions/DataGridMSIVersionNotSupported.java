package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when the MSI package installed is not supported by this application.
 */
public class DataGridMSIVersionNotSupported extends DataGridException {
    public DataGridMSIVersionNotSupported(String msg) { super(msg); }
}
