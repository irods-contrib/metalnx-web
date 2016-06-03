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

package com.emc.metalnx.core.domain.utils;

import java.util.Comparator;

import com.emc.metalnx.core.domain.entity.DataGridTemplateField;

public class DataGridTemplateFieldComparator implements Comparator<DataGridTemplateField> {

	@Override
	public int compare(DataGridTemplateField o1, DataGridTemplateField o2) {
		if (o1.getAttribute().compareTo(o2.getAttribute()) != 0) {
			return o1.getAttribute().compareTo(o2.getAttribute());
		}
		
		else if (o1.getValue().compareTo(o2.getValue()) != 0) {
			return o1.getValue().compareTo(o2.getValue());
		}
		
		else if (o1.getUnit().compareTo(o2.getUnit()) != 0) {
			return o1.getUnit().compareTo(o2.getUnit());
		}
		
		return 0;
	}

}
