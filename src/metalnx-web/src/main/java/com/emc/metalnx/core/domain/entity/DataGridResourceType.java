 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity;

import com.emc.metalnx.core.domain.entity.enums.DataGridResourceTypeEnum;

public class DataGridResourceType implements Comparable<DataGridResourceType> {
	private DataGridResourceTypeEnum dataGridType;
	private String dataGridResourceTypeName;
	private String dataGridResourceTypeNamePrettified;

	public DataGridResourceType() {

	}

	public DataGridResourceType(String dataGridResourceTypeNamePrettified, String dataGridResourceTypeName,
			DataGridResourceTypeEnum dataGridType) {
		this.dataGridResourceTypeNamePrettified = dataGridResourceTypeNamePrettified;
		this.dataGridType = dataGridType;
		this.dataGridResourceTypeName = dataGridResourceTypeName;
	}

	@Override
	public int compareTo(DataGridResourceType dgrt) {
		return this.getDataGridResourceTypeName().compareTo(dgrt.getDataGridResourceTypeName());
	}

	/**
	 * @return the dataGridType
	 */
	public DataGridResourceTypeEnum getDataGridType() {
		return dataGridType;
	}

	/**
	 * @param dataGridType
	 *            the dataGridType to set
	 */
	public void setDataGridType(DataGridResourceTypeEnum dataGridType) {
		this.dataGridType = dataGridType;
	}

	/**
	 * @return the dataGridTypeName
	 */
	public String getDataGridResourceTypeName() {
		return dataGridResourceTypeName;
	}

	/**
	 * @param dataGridTypeName
	 *            the dataGridTypeName to set
	 */
	public void setDataGridResourceTypeName(String dataGridResourceTypeName) {
		this.dataGridResourceTypeName = dataGridResourceTypeName;
	}

	/**
	 * @return the dataGridResourceTypeNamePrettified
	 */
	public String getDataGridResourceTypeNamePrettified() {
		return dataGridResourceTypeNamePrettified;
	}

	/**
	 * @param dataGridResourceTypeNamePrettified
	 *            the dataGridResourceTypeNamePrettified to set
	 */
	public void setDataGridResourceTypeNamePrettified(String dataGridResourceTypeNamePrettified) {
		this.dataGridResourceTypeNamePrettified = dataGridResourceTypeNamePrettified;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridResourceType [");
		if (dataGridType != null) {
			builder.append("dataGridType=").append(dataGridType).append(", ");
		}
		if (dataGridResourceTypeName != null) {
			builder.append("dataGridResourceTypeName=").append(dataGridResourceTypeName).append(", ");
		}
		if (dataGridResourceTypeNamePrettified != null) {
			builder.append("dataGridResourceTypeNamePrettified=").append(dataGridResourceTypeNamePrettified);
		}
		builder.append("]");
		return builder.toString();
	}
}
