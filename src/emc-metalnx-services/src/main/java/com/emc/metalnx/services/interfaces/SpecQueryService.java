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

/**
 * 
 */
package com.emc.metalnx.services.interfaces;

import java.util.List;

import org.irods.jargon.core.query.SpecificQueryResultSet;

import com.emc.metalnx.core.domain.entity.DataGridFilePropertySearch;
import com.emc.metalnx.core.domain.entity.DataGridMetadataSearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

public interface SpecQueryService {
	
	/**
	 * Searches data objects or collections by metadata.
	 * @param metadataSearch
	 * 			list of metadata criteria to apply the search
	 * @param zone
	 * 			zone to be looking for collections and data objects
	 * @param searchAgainstColls
	 * 			flag set to true when looking for collections and set to false when looking for 
	 * 			data objects
	 * @param pageContext
	 * 			pagination context
	 * @param offset
	 * 			offset for pagination
	 * @param limit
	 * 			max number of items shown in a page
	 * @return Query result set from a metadata search
	 * @throws DataGridConnectionRefusedException 
	 */
	public SpecificQueryResultSet searchByMetadata(List<DataGridMetadataSearch> metadataSearch, 
		String zone, boolean searchAgainstColls, DataGridPageContext pageContext, int offset, 
		int limit) throws DataGridConnectionRefusedException;
	
	/**
	 * Searches data objects or collections by file properties.
	 * @param metadataSearch
	 * 			list of file properties criteria to apply the search
	 * @param zone
	 * 			zone to be looking for collections and data objects
	 * @param searchAgainstColls
	 * 			flag set to true when looking for collections and set to false when looking for 
	 * 			data objects
	 * @param pageContext
	 * 			pagination context
	 * @param offset
	 * 			offset for pagination
	 * @param limit
	 * 			max number of items shown in a page
	 * @return Query result set from a file properties search
	 * @throws DataGridConnectionRefusedException 
	 */
	public SpecificQueryResultSet searchByFileProperties(List<DataGridFilePropertySearch> filePropertySearch, 
		String zone, boolean searchAgainstColls, DataGridPageContext pageContext, int offset, 
		int limit) throws DataGridConnectionRefusedException;

	/**
	 * Removes a specific query from the data grid by its alias
	 * @param specQueryAlias
	 * @throws DataGridConnectionRefusedException 
	 */
	public void deleteSpecQueryByAlias(String specQueryAlias) 
		throws DataGridConnectionRefusedException;
	
	/**
	 * Counts the total number of Collections matching a specific metadata criteria.
	 * @param metadataSearch
	 * 			list of metadata criteria to apply the search
	 * @param zone
	 * 			zone to be looking for collections 
	 * @return total number of collections matching a metadata search criteria
	 * @throws DataGridConnectionRefusedException 
	 */
	public int countCollectionsMatchingMetadata(List<DataGridMetadataSearch> metadataSearch, 
		String zone) throws DataGridConnectionRefusedException;
	
	/**
	 * Counts the total number of data objects matching a specific metadata criteria.
	 * @param metadataSearch
	 * 			list of metadata criteria to apply the search
	 * @param zone
	 * 			zone to be looking for data objects 
	 * @return total number of data objects matching a metadata search criteria
	 * @throws DataGridConnectionRefusedException 
	 */
	public int countDataObjectsMatchingMetadata(List<DataGridMetadataSearch> metadataSearch, 
		String zone) throws DataGridConnectionRefusedException;
	
	/**
	 * Counts the total number of Collections matching a specific file properties criteria.
	 * @param filePropertiesSearch
	 * 			list of file properties criteria to apply the search
	 * @param zone
	 * 			zone to be looking for collections 
	 * @return total number of collections matching a file properties search criteria
	 * @throws DataGridConnectionRefusedException 
	 */
	public int countCollectionsMatchingFileProperties(List<DataGridFilePropertySearch> filePropertiesSearch, String zone) throws DataGridConnectionRefusedException;
	
	/**
	 * Counts the total number of data objects matching a specific file properties criteria.
	 * @param filePropertiesSearch
	 * 			list of file properties criteria to apply the search
	 * @param zone
	 * 			zone to be looking for data objects 
	 * @return total number of data objects matching a file properties search criteria
	 * @throws DataGridConnectionRefusedException 
	 */
	public int countDataObjectsMatchingFileProperties(List<DataGridFilePropertySearch> filePropertiesSearch, String zone) throws DataGridConnectionRefusedException;
}
