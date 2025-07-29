 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.exceptions;

import org.springframework.security.core.AuthenticationException;

public class DataGridAuthenticationException extends AuthenticationException {
	private static final long serialVersionUID = 1L;

	public DataGridAuthenticationException(String msg) {
		super(msg);
	}

	public DataGridAuthenticationException(String msg, Throwable t) {
		super(msg, t);
	}
}
