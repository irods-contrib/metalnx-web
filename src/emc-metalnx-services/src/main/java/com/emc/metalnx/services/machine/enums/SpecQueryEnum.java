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
