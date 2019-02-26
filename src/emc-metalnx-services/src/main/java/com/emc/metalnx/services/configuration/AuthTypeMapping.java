/**
 * 
 */
package com.emc.metalnx.services.configuration;

import org.irods.jargon.core.connection.AuthScheme;

/**
 * Maps iRODS auth type to a configurable user-friendly display. In this way
 * "PAM" auth type can display as an option in login as "Joes Corporation
 * Login", etc.
 * <p>
 * This is picked up from metalnx.properties and used in UI components, such as
 * on the login page, providing site-specific customization.
 * 
 * @author conwaymc
 *
 */
public class AuthTypeMapping {

	private final String irodsAuthType;
	private final String userAuthType;

	/**
	 * @param irodsAuthType
	 *            {@code String} with the iRODS Auth type. See the
	 *            {@link AuthScheme} enumeration for values
	 * @param userAuthType
	 *            {@code String} with the site-specific display value for the auth
	 *            type.
	 */
	public AuthTypeMapping(String irodsAuthType, String userAuthType) {
		this.irodsAuthType = irodsAuthType;
		this.userAuthType = userAuthType;
	}

	public String getIrodsAuthType() {
		return irodsAuthType;
	}

	public String getUserAuthType() {
		return userAuthType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AuthTypeMapping [");
		if (irodsAuthType != null) {
			builder.append("irodsAuthType=").append(irodsAuthType).append(", ");
		}
		if (userAuthType != null) {
			builder.append("userAuthType=").append(userAuthType);
		}
		builder.append("]");
		return builder.toString();
	}

}
