package com.emc.metalnx.modelattribute.group;


public class GroupForm {

	private String groupname;
	private Long dataGridId;
	private String additionalInfo;

	public String getGroupname() {
		return groupname;
	}

	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public Long getDataGridId() {
		return dataGridId;
	}

	public void setDataGridId(Long dataGridId) {
		this.dataGridId = dataGridId;
	}
	
}
