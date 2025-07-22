/**
 * 
 */
package com.emc.metalnx.services.irods.template;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author conwaymc
 *
 */
public class SerializedTemplateField {

	@JsonProperty("id")
	private String identifier = "";
	@JsonProperty("attribute")
	private String attribute = "";
	@JsonProperty("value")
	private String value = "";
	@JsonProperty("unit")
	private String unit = "";
	@JsonProperty("field_order")
	private Integer fieldOrder = 0;

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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

	public Integer getFieldOrder() {
		return fieldOrder;
	}

	public void setFieldOrder(Integer fieldOrder) {
		this.fieldOrder = fieldOrder;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SerializedTemplateField [");
		if (identifier != null) {
			builder.append("identifier=").append(identifier).append(", ");
		}
		if (attribute != null) {
			builder.append("attribute=").append(attribute).append(", ");
		}
		if (value != null) {
			builder.append("value=").append(value).append(", ");
		}
		if (unit != null) {
			builder.append("unit=").append(unit).append(", ");
		}
		if (fieldOrder != null) {
			builder.append("fieldOrder=").append(fieldOrder);
		}
		builder.append("]");
		return builder.toString();
	}

}
