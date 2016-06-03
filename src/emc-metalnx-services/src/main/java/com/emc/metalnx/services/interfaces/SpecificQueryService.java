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

import org.irods.jargon.core.query.SpecificQueryResultSet;

import com.emc.metalnx.core.domain.entity.DataGridSpecificQuery;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

public interface SpecificQueryService {

	/**
	 * Creates a Specific Query object on the underlying datagrid system
	 * @param specificQuery
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	boolean createSpecificQuery(DataGridSpecificQuery specificQuery) throws DataGridConnectionRefusedException;
	
	/**
	 * Updates a Specific Query object on the underlying datagrid system
	 * @param specificQuery
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	boolean updateSpecificQuery(DataGridSpecificQuery specificQuery) throws DataGridConnectionRefusedException;
	
	/**
	 * Lists all the specific queries on the data grid system
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	List<DataGridSpecificQuery> findAll() throws DataGridConnectionRefusedException;
	
	/**
	 * Lists all the specific queries on the data grid system matching
	 * the alias name
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	DataGridSpecificQuery findByAlias(String alias) throws DataGridConnectionRefusedException;
	
	/**
	 * Lists all the specific queries on the data grid system matching
	 * part of the alias with the 'like' string argument.
	 * @param like
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	List<DataGridSpecificQuery> findByAliasLike(String like) throws DataGridConnectionRefusedException;
	
	/**
	 * Executes the specific query on the underlying data grid system.
	 * @param specificQuery
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	SpecificQueryResultSet executeSpecificQuery(DataGridSpecificQuery specificQuery, String zone) 
		throws DataGridConnectionRefusedException;
	
	/**
	 * Removes a specific query object from the underlying data grid system.
	 * @param specificQuery
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	boolean removeSpecificQueryByAlias(DataGridSpecificQuery specificQuery) throws DataGridConnectionRefusedException;
	
}
