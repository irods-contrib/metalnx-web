 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when an attribute field of a template exceed the limit available in the database.
 *
 */
public class DataGridTemplateAttrException extends DataGridException {
    private static final long serialVersionUID = 1L;

    public DataGridTemplateAttrException() {
        super();
    }

    public DataGridTemplateAttrException(String msg) {
        super(msg);
    }

}
