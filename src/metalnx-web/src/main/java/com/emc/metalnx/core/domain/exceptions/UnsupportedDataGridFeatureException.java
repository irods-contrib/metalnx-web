/**
 * 
 */
package com.emc.metalnx.core.domain.exceptions;

/**
 * Signals an attempt at an operation not supported on the target data grid (due
 * to version difference, plugin availability difference etc.
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class UnsupportedDataGridFeatureException extends DataGridException {

	private static final long serialVersionUID = 386934177608694036L;

	/**
	 * 
	 */
	public UnsupportedDataGridFeatureException() {
	}

	/**
	 * @param msg
	 */
	public UnsupportedDataGridFeatureException(String msg) {
		super(msg);
	}

}
