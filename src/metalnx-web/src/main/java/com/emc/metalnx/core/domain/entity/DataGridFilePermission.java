 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity;

import java.io.Serializable;

public class DataGridFilePermission implements Serializable {
	private String userId;
	private String username;
	private String permission;
	private String userType;
	private String userZone;
	private static final long serialVersionUID = 1L;

	public DataGridFilePermission() {

	}

	public String getPermission() {
		return permission;
	}

	public void setPermission(String permission) {
		this.permission = permission;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserZone() {
		return userZone;
	}

	public void setUserZone(String userZone) {
		this.userZone = userZone;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridFilePermission [");
		if (userId != null) {
			builder.append("userId=").append(userId).append(", ");
		}
		if (username != null) {
			builder.append("username=").append(username).append(", ");
		}
		if (permission != null) {
			builder.append("permission=").append(permission).append(", ");
		}
		if (userType != null) {
			builder.append("userType=").append(userType).append(", ");
		}
		if (userZone != null) {
			builder.append("userZone=").append(userZone);
		}
		builder.append("]");
		return builder.toString();
	}

}