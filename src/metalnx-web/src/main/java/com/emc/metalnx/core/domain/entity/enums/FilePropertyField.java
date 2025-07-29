 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity.enums;

public enum FilePropertyField {

	FILE_NAME("name"),
	FILE_PATH("path"),
	RESC_NAME("resc_name"),
	OWNER_NAME("owner_name"),
	CREATION_DATE("create_ts"),
	MODIFICATION_DATE("modify_ts"),
	SIZE("size"),
	REPLICA_NUMBER("repl_num"),
	CHECKSUM("checksum"),
	COMMENT("r_comment");
	
	private String fieldName;
	
	FilePropertyField(String fieldName) {
		this.fieldName = fieldName;
	}
	
	/**
	 * Finds the correct label associated to the input String
	 * @param fieldName field name as is written in front-end
	 * @return field name as is used in database
	 */
	public static FilePropertyField findByString(String fieldName) {
		
		if (fieldName == null) {
			return null;
		}
		
		for(FilePropertyField field : FilePropertyField.values()) {
			if (field.getFieldName().compareTo(fieldName) == 0) {
				return field;
			}
		}
		return null;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return fieldName;
	}
	
}
