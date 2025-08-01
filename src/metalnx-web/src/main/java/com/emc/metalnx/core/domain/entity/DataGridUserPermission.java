 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity;

public class DataGridUserPermission {

	private Integer dataGridId;
	private String userName;
	private String userSystemRole;
	private String permission;

	/**
	 * @return the dataGridId
	 */
	public Integer getDataGridId() {
		return dataGridId;
	}

	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the permission
	 */
	public String getPermission() {
		return permission;
	}

	/**
	 * @param dataGridId
	 *            the dataGridId to set
	 */
	public void setDataGridId(Integer dataGridId) {
		this.dataGridId = dataGridId;
	}

	/**
	 * @param userName
	 *            the userName to set
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * @param permission
	 *            the permission to set
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}

	/**
	 * @return the userSystemRole
	 */
	public String getUserSystemRole() {
		return userSystemRole;
	}

	/**
	 * @param userSystemRole
	 *            the userSystemRole to set
	 */
	public void setUserSystemRole(String userSystemRole) {
		this.userSystemRole = userSystemRole;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridUserPermission [");
		if (dataGridId != null) {
			builder.append("dataGridId=").append(dataGridId).append(", ");
		}
		if (userName != null) {
			builder.append("userName=").append(userName).append(", ");
		}
		if (userSystemRole != null) {
			builder.append("userSystemRole=").append(userSystemRole).append(", ");
		}
		if (permission != null) {
			builder.append("permission=").append(permission);
		}
		builder.append("]");
		return builder.toString();
	}

}
