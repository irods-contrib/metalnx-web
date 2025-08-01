/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.core.domain.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;


public class DataGridUser implements Serializable, Comparable<DataGridUser> {

	private Long id;

	private long dataGridId;

	private String username;

	private String password;

	private String zone;

	private boolean enabled;

	private String locale = "en";

	private boolean forceFileOverwriting = false;

	private String userType;

	private boolean advancedView;

	private boolean metadataUnitView;

	private static final long serialVersionUID = -500578459147421831L;

	public DataGridUser() {

	}

	public DataGridUser(String username, String password, boolean enabled) {
		this.username = username;
		this.enabled = enabled;
	}

	public String getDisplayName() {
		return username;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the dataGridId
	 */
	public long getDataGridId() {
		return dataGridId;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}

	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * @return the advanced_view
	 */
	public boolean isAdvancedView() {
		return advancedView;
	}

	/**
	 * @return the metadata_unit_view
	 */
	public boolean isMetadataUnitView() {
		return metadataUnitView;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @param dataGridId the dataGridId to set
	 */
	public void setDataGridId(long dataGridId) {
		this.dataGridId = dataGridId;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @param zone the zone to set
	 */
	public void setZone(String zone) {
		this.zone = zone;
	}

	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @param advancedView to set
	 */
	public void setAdvanceView(boolean advancedView) {
		this.advancedView = advancedView;
	}

	/**
	 * @param metadataUnitView to set
	 */
	public void setMetadataUnitView(boolean metadataUnitView) {
		this.metadataUnitView = metadataUnitView;
	}

	/**
	 * @return the locale
	 */
	public String getLocale() {
		return locale;
	}

	/**
	 * @param locale the locale to set
	 */
	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	@Override
	public int compareTo(DataGridUser o) {
		return username.compareTo(o.getUsername());
	}

	public boolean isAdmin() {
		return userType.compareTo("rodsadmin") == 0;
	}

	public boolean isGroupAdmin() {
		return userType.compareTo("groupadmin") == 0;
	}

	/**
	 * @return the forceFileOverwriting
	 */
	public boolean isForceFileOverwriting() {
		return forceFileOverwriting;
	}

	/**
	 * @param forceFileOverwriting the forceFileOverwriting to set
	 */
	public void setForceFileOverwriting(boolean forceFileOverwriting) {
		this.forceFileOverwriting = forceFileOverwriting;
	}

	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridUser [");
		if (id != null) {
			builder.append("id=").append(id).append(", ");
		}
		builder.append("dataGridId=").append(dataGridId).append(", ");
		if (username != null) {
			builder.append("username=").append(username).append(", ");
		}
		if (password != null) {
			builder.append("password=").append(password).append(", ");
		}
		if (zone != null) {
			builder.append("zone=").append(zone).append(", ");
		}
		builder.append("enabled=").append(enabled).append(", ");
		if (locale != null) {
			builder.append("locale=").append(locale).append(", ");
		}
		builder.append("forceFileOverwriting=").append(forceFileOverwriting).append(", ");
		if (userType != null) {
			builder.append("userType=").append(userType).append(", ");
		}
		builder.append("advancedView=").append(advancedView).append(", ");
		builder.append("metadataUnitView=").append(metadataUnitView).append(", ");
		builder.append("]");
		return builder.toString();
	}

}
