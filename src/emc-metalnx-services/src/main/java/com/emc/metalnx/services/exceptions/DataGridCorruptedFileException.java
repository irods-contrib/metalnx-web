package com.emc.metalnx.services.exceptions;

public class DataGridCorruptedFileException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public DataGridCorruptedFileException(String msg) {
		super(msg);
	}

	public DataGridCorruptedFileException(String msg, Throwable t) {
		super(msg, t);
	}
}
