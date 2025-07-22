/**
 * 
 */
package com.emc.metalnx.services.irods.utils;

/**
 * Misc constants for specific query processing
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class SpecificQueryConstants {

	public static final String propFieldsRegex = "([^A-Za-z0-9-_.,=! ]+)";
	// this regex is used to remove quotes, double quotes and semi-colon from value
	// field, so that sql injection becomes harder to happen
	public static final String regexForValue = "/[^'\";]/g";

}
