/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */
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
@Audited
@Table(name = "metadata_fields")
public class DataGridMetadataFields implements Serializable, Comparable<DataGridMetadataFields> {
	
	private static final long serialVersionUID = 1L;

	@Id
	@NotAudited
	@GeneratedValue(strategy=GenerationType.IDENTITY)
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
	
	@ManyToOne(fetch = FetchType.EAGER, optional=true)
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
	 * @param id the id to set
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
	 * @param attribute the attribute to set
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
	 * @param value the value to set
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
	 * @param unit the unit to set
	 */
	public void setUnit(String unit) {
		this.attributeUnit = unit;
	}

	@Override
	public int compareTo(DataGridMetadataFields dgmf) {
		return this.attributeValue.compareTo(dgmf.getValue());
	}
	
	
	
}
