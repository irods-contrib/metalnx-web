package com.emc.metalnx.core.domain.exceptions;

import java.util.Optional;

/**
 * A file or bundle size is too large for the given operation (e.g. download)
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class FileSizeTooLargeException extends DataGridException {

	private static final long serialVersionUID = -7006504069756007074L;
	
	private Optional<Long> fileSize = Optional.empty();

	public FileSizeTooLargeException() {
	}

	public FileSizeTooLargeException(String msg) {
		super(msg);
	}

	public FileSizeTooLargeException(String msg, long fileSize) {
		super(msg);
		this.fileSize = Optional.of(fileSize);
	}
	
	public Optional<Long> getFileSize() {
		return fileSize;
	}

}
