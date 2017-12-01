/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.emc.metalnx.core.domain.entity;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum;
import com.emc.metalnx.core.domain.entity.enums.FilePropertyField;

public class DataGridFilePropertySearch {

	private final FilePropertyField attribute;
	private final DataGridSearchOperatorEnum operator;
	private final String value;

	public DataGridFilePropertySearch(FilePropertyField filePropertyField, DataGridSearchOperatorEnum operator,
			String value) throws ParseException {
		attribute = filePropertyField;
		this.operator = operator;
		if (filePropertyField == FilePropertyField.CREATION_DATE
				|| filePropertyField == FilePropertyField.MODIFICATION_DATE) {
			long timeInMilliseconds = new SimpleDateFormat("MM/dd/yyyy hh:mm aa").parse(value).getTime();
			this.value = String.valueOf(timeInMilliseconds / 1000);
		} else {
			this.value = value;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridFilePropertySearch [");
		if (attribute != null) {
			builder.append("attribute=").append(attribute).append(", ");
		}
		if (operator != null) {
			builder.append("operator=").append(operator).append(", ");
		}
		if (value != null) {
			builder.append("value=").append(value);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the attribute
	 */
	public FilePropertyField getAttribute() {
		return attribute;
	}

	/**
	 * @return the operator
	 */
	public DataGridSearchOperatorEnum getOperator() {
		return operator;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

}
