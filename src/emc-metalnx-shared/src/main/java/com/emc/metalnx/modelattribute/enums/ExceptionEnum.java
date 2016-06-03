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

import java.util.ArrayList;
import java.util.List;

/**
 * Maps all exceptions to an ID
 * @author guerra
 *
 */

public enum ExceptionEnum {
	
	JARGON_EXCEPTION(1), 
	USERS_DATA_DUPLICATE_EXCEPTION(2);

	private int code;

	ExceptionEnum(final int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static List<Integer> getExceptionTypeList() {

		List<Integer> exceptionTypes = new ArrayList<Integer>();
		for (ExceptionEnum exceptionEnum : ExceptionEnum.values()) {
			exceptionTypes.add(exceptionEnum.code);
		}
		return exceptionTypes;
	}

	public static ExceptionEnum findTypeByString(final int exceptionType) {
		ExceptionEnum exceptionTypeEnumValue = null;
		for (ExceptionEnum exceptionTypeEnum : ExceptionEnum.values()) {
			if (exceptionTypeEnum.getCode() == exceptionType) {
				exceptionTypeEnumValue = exceptionTypeEnum;
				break;
			}
		}
		if (exceptionTypeEnumValue == null) {
			exceptionTypeEnumValue = ExceptionEnum.JARGON_EXCEPTION;
		}
		return exceptionTypeEnumValue;

	}
}
