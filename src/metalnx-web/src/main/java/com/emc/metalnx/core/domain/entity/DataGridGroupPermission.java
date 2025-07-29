 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity;

public class DataGridGroupPermission {

	private Integer dataGridId;
	private String groupName;
	private String permission;

	/**
	 * @return the dataGridId
	 */
	public Integer getDataGridId() {
		return dataGridId;
	}

	/**
	 * @return the groupName
	 */
	public String getGroupName() {
		return groupName;
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
	 * @param groupName
	 *            the groupName to set
	 */
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * @param permission
	 *            the permission to set
	 */
	public void setPermission(String permission) {
		this.permission = permission;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridGroupPermission [");
		if (dataGridId != null) {
			builder.append("dataGridId=").append(dataGridId).append(", ");
		}
		if (groupName != null) {
			builder.append("groupName=").append(groupName).append(", ");
		}
		if (permission != null) {
			builder.append("permission=").append(permission);
		}
		builder.append("]");
		return builder.toString();
	}

}
