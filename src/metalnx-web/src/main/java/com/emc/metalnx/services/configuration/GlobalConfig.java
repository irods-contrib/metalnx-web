/**
 *
 */
package com.emc.metalnx.services.configuration;

import com.emc.metalnx.services.interfaces.ConfigService;

/**
 * DTO from {@link ConfigService} that can pass global config information to
 * pages and controller
 *
 * @author Mike Conway - NIEHS
 *
 */
public class GlobalConfig {

	/**
	 * Turn on or off the tickets features for all users
	 */
	private boolean ticketsEnabled = false;

	/**
	 * Turn on or off the automatic application of rules during upload
	 */
	private boolean uploadRulesEnabled = false;

	/**
	 * Turn the dashboard admin view on and off
	 */
	private boolean dashboardEnabled = false;

	/**
	 * Turn on or off the pluggable search interface elements
	 */
	private boolean pluggableSearchEnabled = false;

	/**
	 * Turn on or off the pluggable search interface elements
	 */
	private boolean pluggableShoppingCartEnabled = false;

	/**
	 * Turn on or off the original file properties/metadata search menu item, can be
	 * replaced if using the standard search via the iRODS search plugin and
	 * Elasticsearch (this is a work in progress)
	 */
	private boolean classicSearchEnabled = true;

	private boolean publicSidebarLinkEnabled;

	public boolean isTicketsEnabled() {
		return ticketsEnabled;
	}

	public void setTicketsEnabled(boolean ticketsEnabled) {
		this.ticketsEnabled = ticketsEnabled;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GlobalConfig [ticketsEnabled=").append(ticketsEnabled).append(", uploadRulesEnabled=")
				.append(uploadRulesEnabled).append(", dashboardEnabled=").append(dashboardEnabled)
				.append(", pluggableSearchEnabled=").append(pluggableSearchEnabled)
				.append(", classicSearchEnabled=").append(classicSearchEnabled)
				.append(", pluggableShoppingCartEnabled=").append(pluggableShoppingCartEnabled);
		return builder.toString();
	}

	public boolean isUploadRulesEnabled() {
		return uploadRulesEnabled;
	}

	public void setUploadRulesEnabled(boolean uploadRulesEnabled) {
		this.uploadRulesEnabled = uploadRulesEnabled;
	}

	/**
	 * @return the dashboardEnabled
	 */
	public boolean isDashboardEnabled() {
		return dashboardEnabled;
	}

	/**
	 * @param dashboardEnabled the dashboardEnabled to set
	 */
	public void setDashboardEnabled(boolean dashboardEnabled) {
		this.dashboardEnabled = dashboardEnabled;
	}

	public boolean isPluggableSearchEnabled() {
		return pluggableSearchEnabled;
	}

	public void setPluggableSearchEnabled(boolean pluggableSearchEnabled) {
		this.pluggableSearchEnabled = pluggableSearchEnabled;
	}

	public boolean isPluggableShoppingCartEnabled() {
		return pluggableShoppingCartEnabled;
	}

	public void setPluggableShoppingCartEnabled(boolean pluggableShoppingCartEnabled) {
		this.pluggableShoppingCartEnabled = pluggableShoppingCartEnabled;
	}

	/**
	 * @return the classicSearchEnabled
	 */
	public boolean isClassicSearchEnabled() {
		return classicSearchEnabled;
	}

	/**
	 * @param classicSearchEnabled the classicSearchEnabled to set
	 */
	public void setClassicSearchEnabled(boolean classicSearchEnabled) {
		this.classicSearchEnabled = classicSearchEnabled;
	}

	public void setPublicSidebarLinkEnabled(boolean publicSidebarLinkEnabled) {
		this.publicSidebarLinkEnabled = publicSidebarLinkEnabled;
		
	}
	
	public boolean isPublicSidebarLinkEnabled() {
		return publicSidebarLinkEnabled;
	}


}
