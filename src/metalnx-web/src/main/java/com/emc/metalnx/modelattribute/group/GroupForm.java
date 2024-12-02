 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.modelattribute.group;


public class GroupForm {

	private String groupname;
	private Long dataGridId;
	private String zone;

	public String getGroupname() {
		return groupname;
	}

	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}

	public String getZone() {
		return zone;
	}

	public void setZone(String zone) {
		this.zone = zone;
	}

	public Long getDataGridId() {
		return dataGridId;
	}

	public void setDataGridId(Long dataGridId) {
		this.dataGridId = dataGridId;
	}
	
}
