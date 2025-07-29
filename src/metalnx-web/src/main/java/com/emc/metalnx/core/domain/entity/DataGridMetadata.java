 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity;

public class DataGridMetadata implements Comparable<DataGridMetadata> {

	private String attribute;

	private String value;

	private String unit;

	public DataGridMetadata() {

	}

	public DataGridMetadata(String attribute, String value, String unit) {
		this.attribute = attribute;
		this.value = value;
		this.unit = unit;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof DataGridMetadata))
			return false;

		DataGridMetadata m = (DataGridMetadata) obj;
		return attribute.equals(m.getAttribute()) && value.equals(m.getValue()) && unit.equals(m.getUnit());
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	@Override
	public int compareTo(DataGridMetadata o) {
		return this.attribute.compareToIgnoreCase(o.getAttribute());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridMetadata [");
		if (attribute != null) {
			builder.append("attribute=").append(attribute).append(", ");
		}
		if (value != null) {
			builder.append("value=").append(value).append(", ");
		}
		if (unit != null) {
			builder.append("unit=").append(unit);
		}
		builder.append("]");
		return builder.toString();
	}

}
