/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.core.domain.exceptions;

import org.springframework.security.core.AuthenticationException;

public class DataGridDatabaseException extends AuthenticationException {

	private static final long serialVersionUID = 1489175398959184946L;

	public DataGridDatabaseException(String message) {
		super(message);
	}

	public DataGridDatabaseException(String msg, Throwable t) {
		super(msg, t);
	}

}
