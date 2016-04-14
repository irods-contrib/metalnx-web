package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when the name of a template exceed the limit available in the database.
 *
 */
public class DataGridTooLongTemplateNameException extends DataGridException {
    private static final long serialVersionUID = 1L;

    public DataGridTooLongTemplateNameException() {
        super();
    }

    public DataGridTooLongTemplateNameException(String msg) {
        super(msg);
    }

}
