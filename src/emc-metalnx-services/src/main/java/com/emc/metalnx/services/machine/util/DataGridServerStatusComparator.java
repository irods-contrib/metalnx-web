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

package com.emc.metalnx.services.machine.util;

import java.util.Comparator;
import java.util.HashMap;

import com.emc.metalnx.core.domain.entity.DataGridServer;

public class DataGridServerStatusComparator implements Comparator<DataGridServer> {

	@Override
	public int compare(DataGridServer o1, DataGridServer o2) {
		HashMap<String, Integer> values = new HashMap<String, Integer>();
		values.put("normal", 2);
		values.put("warning", 1);
		values.put("error", 0);
		
		if(o1.getMachineStatus() == null || o2.getMachineStatus() == null) {
			return 0;
		}
		
		return values.get(o1.getMachineStatus()).compareTo(values.get(o2.getMachineStatus()));
	}

}
