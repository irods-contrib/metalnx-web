 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

import org.irods.jargon.core.exception.JargonException;

/**
 * Top level Metalnx exception
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class DataGridException extends JargonException {

	private static final long serialVersionUID = 1L;

	public DataGridException() {
		super("general data grid exception");
	}

	public DataGridException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

	public DataGridException(String message, Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public DataGridException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataGridException(String message) {
		super(message);
	}

	public DataGridException(Throwable cause, int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public DataGridException(Throwable cause) {
		super(cause);
	}

}
