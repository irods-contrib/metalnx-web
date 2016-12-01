package com.emc.metalnx.core.domain.exceptions;

/**
 * Exception thrown when the data grid cannot execute a rule.
 */
public class DataGridRuleException extends DataGridException {

    public DataGridRuleException(String msg) {
        super(msg);
    }
}
