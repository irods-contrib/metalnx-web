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
	 * @param accessType
	 * @return
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
