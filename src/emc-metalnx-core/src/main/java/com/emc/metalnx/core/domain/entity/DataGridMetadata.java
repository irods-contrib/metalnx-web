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

public class DataGridMetadata implements Comparable<DataGridMetadata> {

	private String attribute;
	
	private String value;
	
	private String unit;
	
	public DataGridMetadata(){
		
	}
	
	public DataGridMetadata(String attribute, String value, String unit){
		this.attribute = attribute;
		this.value = value;
		this.unit = unit;
	}
	
	public boolean equals(DataGridMetadata dataGridAVU){
		if(this.attribute.equals(dataGridAVU.getAttribute()) && this.value.equals(dataGridAVU.getValue()) && this.unit.equals(dataGridAVU.getUnit()))
			return true;
		return false;
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
	
}
