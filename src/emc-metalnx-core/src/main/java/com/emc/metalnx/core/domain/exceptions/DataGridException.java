/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
