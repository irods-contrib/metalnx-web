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

package com.emc.metalnx.modelattribute.enums;

public enum MetadataTemplateAccessType {

	SYSTEM("system"),
	PRIVATE("private");

	private String accessType;
	
	MetadataTemplateAccessType(String accessType) {
		this.accessType = accessType;
	}
	
	/**
	 * Returns the String access type corresponding to this value
	 * @return
	 */
	public String getAccessType() {
		return this.accessType;
	}
	
	/**
	 * Finds the correct label associated to the input String
	 * @param accessType
	 * @return
	 */
	public static MetadataTemplateAccessType findByString(String accessType) {
		
		if (accessType == null) {
			return null;
		}
		
		for(MetadataTemplateAccessType at : MetadataTemplateAccessType.values()) {
			if (at.getAccessType().compareTo(accessType) == 0) {
				return at;
			}
		}
		return null;
	}
	
	/**
	 * Returns the string format for this enum
	 */
	public String toString() {
		return this.getAccessType();
	}
}
