package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when a value field of a template exceed the limit available in the database.
 *
 */
public class DataGridTemplateValueException extends DataGridException {
    private static final long serialVersionUID = 1L;

    public DataGridTemplateValueException() {
        super();
    }

    public DataGridTemplateValueException(String msg) {
        super(msg);
    }

}
