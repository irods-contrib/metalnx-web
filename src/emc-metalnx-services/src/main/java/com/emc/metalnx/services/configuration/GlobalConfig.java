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
				.append(uploadRulesEnabled).append(", dashboardEnabled=").append(dashboardEnabled).append("]");
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
	 * @param dashboardEnabled
	 *            the dashboardEnabled to set
	 */
	public void setDashboardEnabled(boolean dashboardEnabled) {
		this.dashboardEnabled = dashboardEnabled;
	}

}
