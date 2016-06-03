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

package com.emc.metalnx.modelattribute.template.field;


public class TemplateFieldForm {

	private Long id;
	private String templateName;
	private String attribute;
	private String attributeValue;
	private String attributeUnit;
	private float startRange;
	private float endRange;
	private int order;
	private Integer formListPosition;
	
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
	 * @return the attributeValue
	 */
	public String getValue() {
		return attributeValue;
	}
	
	/**
	 * @param attributeValue the attributeValue to set
	 */
	public void setValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
	
	/**
	 * @return the attributeUnit
	 */
	public String getUnit() {
		return attributeUnit;
	}
	
	/**
	 * @param attributeUnit the attributeUnit to set
	 */
	public void setUnit(String attributeUnit) {
		this.attributeUnit = attributeUnit;
	}
	
	/**
	 * @return the startRange
	 */
	public float getStartRange() {
		return startRange;
	}
	
	/**
	 * @param startRange the startRange to set
	 */
	public void setStartRange(float startRange) {
		this.startRange = startRange;
	}
	
	/**
	 * @return the endRange
	 */
	public float getEndRange() {
		return endRange;
	}
	
	/**
	 * @param endRange the endRange to set
	 */
	public void setEndRange(float endRange) {
		this.endRange = endRange;
	}
	
	/**
	 * @return the order
	 */
	public int getOrder() {
		return order;
	}
	
	/**
	 * @param order the order to set
	 */
	public void setOrder(int order) {
		this.order = order;
	}
	
	public String getTemplateName() {
		return templateName;
	}
	
	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
	
	/**
	 * @return the formListPosition
	 */
	public Integer getFormListPosition() {
		return formListPosition;
	}
	
	/**
	 * @param formListPosition the formListPosition to set
	 */
	public void setFormListPosition(Integer formListPosition) {
		this.formListPosition = formListPosition;
	}

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
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof TemplateFieldForm) {
			TemplateFieldForm templateFieldForm = (TemplateFieldForm) obj;
			//checking if the ID is set
			if(this.getId() != null && templateFieldForm.getId() != null) {
				return this.getId().equals(templateFieldForm.getId());
			}
			else if(this.getAttribute() != null && this.getValue() != null && this.getUnit() != null){
				return this.getAttribute().equals(templateFieldForm.getAttribute()) &&
						this.getValue().equals(templateFieldForm.getValue()) &&
						this.getUnit().equals(templateFieldForm.getUnit());
			}
		}
		
		return false;
	}
	
}
