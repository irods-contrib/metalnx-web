 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */


package com.emc.metalnx.core.domain.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Entity
//@Audited
@Table(name = "metadata_fields")
public class DataGridMetadataFields implements Serializable, Comparable<DataGridMetadataFields> {

	private static final long serialVersionUID = 1L;

	@Id
	@NotAudited
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "attribute", length = 60)
	private String attribute;

	@Column(name = "attribute_value", length = 60)
	private String attributeValue;

	@Column(name = "attribute_unit", length = 60)
	private String attributeUnit;

	@Column(name = "start_range")
	private float startRange;

	@Column(name = "end_range")
	private float endRange;

	@Column(name = "field_order")
	private int order;

	@ManyToOne(fetch = FetchType.EAGER, optional = true)
	@JoinColumn(name = "template_id", nullable = false, updatable = true)
	private DataGridTemplate template;

	public DataGridMetadataFields() {

	}

	public DataGridMetadataFields(String attribute, String value, String unit) {
		this.attribute = attribute;
		this.attributeValue = value;
		this.attributeUnit = unit;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the attribute
	 */
	public String getAttribute() {
		return attribute;
	}

	/**
	 * @param attribute
	 *            the attribute to set
	 */
	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return attributeValue;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(String value) {
		this.attributeValue = value;
	}

	/**
	 * @return the unit
	 */
	public String getUnit() {
		return attributeUnit;
	}

	/**
	 * @param unit
	 *            the unit to set
	 */
	public void setUnit(String unit) {
		this.attributeUnit = unit;
	}

	@Override
	public int compareTo(DataGridMetadataFields dgmf) {
		return this.attributeValue.compareTo(dgmf.getValue());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridMetadataFields [");
		if (id != null) {
			builder.append("id=").append(id).append(", ");
		}
		if (attribute != null) {
			builder.append("attribute=").append(attribute).append(", ");
		}
		if (attributeValue != null) {
			builder.append("attributeValue=").append(attributeValue).append(", ");
		}
		if (attributeUnit != null) {
			builder.append("attributeUnit=").append(attributeUnit).append(", ");
		}
		builder.append("startRange=").append(startRange).append(", endRange=").append(endRange).append(", order=")
				.append(order).append(", ");
		if (template != null) {
			builder.append("template=").append(template);
		}
		builder.append("]");
		return builder.toString();
	}

}
