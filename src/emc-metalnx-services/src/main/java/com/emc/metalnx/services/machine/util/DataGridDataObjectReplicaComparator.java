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

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;

public class DataGridDataObjectReplicaComparator implements Comparator<DataGridCollectionAndDataObject> {

	@Override
	public int compare(DataGridCollectionAndDataObject do1, DataGridCollectionAndDataObject do2) {		
		return  do1.getReplicaNumber().compareTo(do2.getReplicaNumber());
	}

}
