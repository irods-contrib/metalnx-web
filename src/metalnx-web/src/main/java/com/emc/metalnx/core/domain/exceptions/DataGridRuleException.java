 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when the data grid cannot execute a rule.
 */
public class DataGridRuleException extends DataGridException {

    public DataGridRuleException(String msg) {
        super(msg);
    }
}
