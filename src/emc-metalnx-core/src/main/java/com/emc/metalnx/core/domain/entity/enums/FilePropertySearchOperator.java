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

public enum FilePropertySearchOperator {

	EQUALS("="),
	NOT_EQUALS("!="),
	CONTAINS("ILIKE"),
	NOT_CONTAINS("NOT ILIKE");
	
	private String operator;
	
	FilePropertySearchOperator(String operator) {
		this.operator = operator;
	}
	
	/**
	 * Finds the correct label associated to the input String
	 * @param accessType
	 * @return
	 */
	public static FilePropertySearchOperator findByString(String operatorStr) {
		
		if (operatorStr == null) {
			return null;
		}
		
		for(FilePropertySearchOperator operator : FilePropertySearchOperator.values()) {
			if (operator.getFieldName().compareTo(operatorStr) == 0) {
				return operator;
			}
		}
		return null;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return this.operator;
	}
	
}
