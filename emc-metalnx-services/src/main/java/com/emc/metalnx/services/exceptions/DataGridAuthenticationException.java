package com.emc.metalnx.services.exceptions;

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
