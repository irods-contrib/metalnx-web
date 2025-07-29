/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.core.domain.exceptions;

import org.springframework.security.core.AuthenticationException;

public class DataGridServerException extends AuthenticationException {

	private static final long serialVersionUID = 5499933817074925047L;

	public DataGridServerException(String message) {
		super(message);
	}

	public DataGridServerException(String msg, Throwable t) {
		super(msg, t);
	}
}
