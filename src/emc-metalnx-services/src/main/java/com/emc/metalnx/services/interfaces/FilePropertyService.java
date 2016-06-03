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

package com.emc.metalnx.services.interfaces;

import java.util.List;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridFilePropertySearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

public interface FilePropertyService {

	/**
	 * Get all collections and data objects that match any metadata search criteria given as a
	 * parameters
	 * @param searchList
	 * 			list of metadata search criteria 
	 * @param pageContext
	 * 			pagination context for proper counting display at the front end
	 * @param pageNum
	 * 			page required
	 * @param pageSize
	 * 			max number of items to display in a page
	 * @return list of collections and data objects
	 * @throws DataGridConnectionRefusedException 
	 */
	public List<DataGridCollectionAndDataObject> findByFileProperties(List<DataGridFilePropertySearch> searchList,
		DataGridPageContext pageContext, int pageNum, int pageSize) throws DataGridConnectionRefusedException;
	
}
