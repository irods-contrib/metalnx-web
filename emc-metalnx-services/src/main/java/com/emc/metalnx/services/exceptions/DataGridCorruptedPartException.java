package com.emc.metalnx.services.exceptions;

public class DataGridCorruptedPartException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DataGridCorruptedPartException(String msg) {
		super(msg);
	}

	public DataGridCorruptedPartException(String msg, Throwable t) {
		super(msg, t);
	}
}
