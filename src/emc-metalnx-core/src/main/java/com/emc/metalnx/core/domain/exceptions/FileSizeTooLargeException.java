package com.emc.metalnx.core.domain.exceptions;

/**
 * A file or bundle size is too large for the given operation (e.g. download)
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class FileSizeTooLargeException extends DataGridException {

	private static final long serialVersionUID = -7006504069756007074L;

	public FileSizeTooLargeException() {
	}

	public FileSizeTooLargeException(String msg) {
		super(msg);
	}

}
