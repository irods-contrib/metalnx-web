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
