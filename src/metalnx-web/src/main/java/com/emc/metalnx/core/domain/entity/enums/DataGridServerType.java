 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity.enums;

public enum DataGridServerType {

	ICAT_SERVER("ICAT_SERVER"), 
	RESOURCE_SERVER("RESOURCE_SERVER"), 
	ISILON("ISILON");

	private String stringType;

	private DataGridServerType(String type) {
		this.stringType = type;
	}

	public String toString() {
		return this.stringType;
	}

}