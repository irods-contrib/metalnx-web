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

package com.emc.metalnx.modelattribute.metadatatemplate;

import java.util.List;

public class MetadataTemplateForm {
	private Long id;
	private String templateName;
	private String description;
	private String usageInformation;
	private String accessType;
	private String owner;
	private Integer version;
	private List<String> avuPositions;
	private List<String> avuAttributes;
	private List<String> avuValues;
	private List<String> avuUnits;
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	public String getTemplateName() {
		return templateName;
	}
	
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getUsageInformation() {
		return usageInformation;
	}
	
	public void setUsageInformation(String usageInformation) {
		this.usageInformation = usageInformation;
	}
	
	public List<String> getAvuPositions() {
		return avuPositions;
	}
	
	public void setAvuPositions(List<String> avuPositions) {
		this.avuPositions = avuPositions;
	}
	
	public List<String> getAvuValues() {
		return avuValues;
	}
	
	public void setAvuValues(List<String> avuValues) {
		this.avuValues = avuValues;
	}
	
	public List<String> getAvuAttributes() {
		return avuAttributes;
	}
	
	public void setAvuAttributes(List<String> avuAttributes) {
		this.avuAttributes = avuAttributes;
	}
	
	public List<String> getAvuUnits() {
		return avuUnits;
	}
	
	public void setAvuUnits(List<String> avuUnits) {
		this.avuUnits = avuUnits;
	}	
	public String getAccessType() {
		return this.accessType;
	}
	
	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}
	
	public String getOwner() {
		return this.owner;
	}
	
	public void setOwner(String owner) {
		this.owner = owner;
	}

	/**
	 * @return the version
	 */
	public Integer getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(Integer version) {
		this.version = version;
	}
}
