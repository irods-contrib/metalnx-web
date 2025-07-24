package com.emc.metalnx.services.interfaces;

import org.irods.jargon.core.exception.JargonException;

/**
 * Exception in processing metadata templates
 * 
 * @author conwaymc
 *
 */
public class MetadataTemplateException extends JargonException {

	private static final long serialVersionUID = 408138518224024455L;

	public MetadataTemplateException(String message) {
		super(message);
	}

	public MetadataTemplateException(String message, Throwable cause) {
		super(message, cause);
	}

	public MetadataTemplateException(Throwable cause) {
		super(cause);
	}

	public MetadataTemplateException(String message, Throwable cause, int underlyingIRODSExceptionCode) {
		super(message, cause, underlyingIRODSExceptionCode);
	}

	public MetadataTemplateException(Throwable cause, int underlyingIRODSExceptionCode) {
		super(cause, underlyingIRODSExceptionCode);
	}

	public MetadataTemplateException(String message, int underlyingIRODSExceptionCode) {
		super(message, underlyingIRODSExceptionCode);
	}

}
