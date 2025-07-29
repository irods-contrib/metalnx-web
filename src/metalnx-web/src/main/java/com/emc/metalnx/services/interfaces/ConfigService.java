/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.interfaces;

import java.util.List;

import org.irods.jargon.core.connection.AuthScheme;

import com.emc.metalnx.services.configuration.AuthTypeMapping;
import com.emc.metalnx.services.configuration.GlobalConfig;

/**
 * Service used to retrieve all configurable parameters from *.properties files.
 */
public interface ConfigService {

	/**
	 * Indicates whether the admin dashboard is enabled
	 *
	 * @return {@code boolean} of {@code true} if the dashboard function is
	 *         implemented
	 */
	boolean isDashboardEnabled();
	/**
	 * Finds the MSI API version supported by the current version of Metalnx.
	 *
	 * @return string representing the version
	 */
	String getMsiAPIVersionSupported();

	/**
	 * Finds the list of all expected Metalnx microservices.
	 *
	 * @return list of all Metalnx microservices.
	 */
	List<String> getMlxMSIsExpected();

	/**
	 * Finds the list of all expected iRODS 4.1.X microservices.
	 *
	 * @return list of all iRODS 4.1.X microservices.
	 */
	List<String> getIrods41MSIsExpected();

	/**
	 * Finds the list of all expected irods 4.2.X microservices.
	 *
	 * @return list of all irods 4.2.X microservices.
	 */
	List<String> getIrods42MSIsExpected();

	/**
	 * Finds the list of all third-party microservices.
	 *
	 * @return list of all third-party microservices.
	 */
	List<String> getOtherMSIsExpected();

	/**
	 * Find the iCAT hostname.
	 *
	 * @return String representing the iCAT machine's hostname.
	 */
	String getIrodsHost();

	/**
	 * Find the irods port number.
	 *
	 * @return String representing irods port number.
	 */
	String getIrodsPort();

	/**
	 * Find the irods default zone.
	 *
	 * @return String representing the irods default zone.
	 */
	String getIrodsZone();

	/**
	 * Find the authentication scheme used for authenticating against iRODS.
	 *
	 * @return String representing the authentication scheme.
	 */
	String getIrodsAuthScheme();

	/**
	 * Find file download limit
	 *
	 * @return long representing the download limit in Megabytes
	 */
	long getDownloadLimit();

	/**
	 * Checks whether or not the populate MSI flag is enabled
	 *
	 * @return True, if populate is enabled. False, otherwise.
	 */
	boolean isPopulateMsiEnabled();

	/**
	 * Get a summary config object of global behavior settings, these can be
	 * injected into templates to control the layout of pages and exposing/hiding
	 * functionality
	 *
	 * @return {@link GlobalConfig} with system-wide optional settings that control
	 *         appearance and behavior
	 */
	GlobalConfig getGlobalConfig();

	/**
	 * Global setting turning on or off the application of rules based on file type
	 * during upload
	 *
	 * @return <code>boolean</code> that is <code>true</code> when upload rules are
	 *         applied based on file type
	 */
	boolean isUploadRulesEnabled();

	/**
	 * Default auth scheme used at login time by the user on the login form
	 *
	 * @return {@code String} with the stringified representation of the jargon
	 *         {@link AuthScheme}
	 */
	String getDefaultIrodsAuthScheme();
	
	/**
	 * Global setting turning on or off the public sidebar link
	 *
	 * @return <code>boolean</code> that is <code>true</code> when the public sidebar
	 *         link is enabled
	 */
	boolean isPublicSidebarLinkEnabled();


	/**
	 * List the {@link AuthTypeMapping} values used to display user login auth type
	 * prompts as configured in the properties
	 * 
	 * @return {@code List} of {@link AuthTypeMapping}
	 */
	List<AuthTypeMapping> listAuthTypeMappings();
	String getJwtAlgo();
	String getJwtSecret();
	String getJwtIssuer();
	void setDownloadLimit(long downloadLimit);
	void setPopulateMsiEnabled(boolean populateMsiEnabled);
	void setIrodsAuthScheme(String irodsAuthScheme);
	void setIrodsZone(String irodsZone);
	void setIrodsPort(String irodsPort);
	void setIrodsHost(String irodsHost);
	void setOtherMSIsExpected(String otherMSIsExpected);
	void setIrods42MSIsExpected(String irods42msIsExpected);
	void setIrods41MSIsExpected(String irods41msIsExpected);
	void setMlxMSIsExpected(String mlxMSIsExpected);
	void setMsiAPIVersionSupported(String msiAPIVersionSupported);
	void setPublicSidebarLinkEnabled(boolean publicSidebarLinkEnabled);
}
