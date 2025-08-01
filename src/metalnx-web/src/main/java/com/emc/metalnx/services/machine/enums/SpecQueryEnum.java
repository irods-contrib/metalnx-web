 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.machine.enums;

public enum SpecQueryEnum {
	
	SQL_ATTR_EQUAL_VALUE_1_COND("metalnxMetadataAttrEqualValue1Cond"),
	SQL_ATTR_NOT_EQUAL_VALUE_1_COND("metalnxMetadataAttrNotEqualValue1Cond"),
	SQL_ATTR_LIKE_VALUE_1_COND("metalnxMetadataAttrLikeValue1Cond"),
	SQL_ATTR_NOT_LIKE_VALUE_1_COND("metalnxMetadataAttrNotLikeValue1Cond");
	
	private String specQueryAlias;
	
	private SpecQueryEnum(String specQueryAlias) {
		this.specQueryAlias = specQueryAlias;
	}
	
	public String toString() {
		return this.specQueryAlias;
	}
}
