 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity.enums;

public enum DataGridSearchOperatorEnum {

	EQUAL("="), 
	NOT_EQUAL("!="), 
	LIKE("ILIKE"), 
	NOT_LIKE("NOT ILIKE"),
	BIGGER_THAN(">"),
	LESS_THAN("<");

	private String stringType;

	private DataGridSearchOperatorEnum(String type) {
		this.stringType = type;
	}

	public String toString() {
		return this.stringType;
	}

}