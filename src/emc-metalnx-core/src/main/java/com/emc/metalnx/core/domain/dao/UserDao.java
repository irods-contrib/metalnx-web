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
package com.emc.metalnx.core.domain.dao;

import java.util.List;

import com.emc.metalnx.core.domain.dao.generic.GenericDao;
import com.emc.metalnx.core.domain.entity.DataGridUser;

public interface UserDao extends GenericDao<DataGridUser, Long> {
	
	/**
	 * Find a user by his ID
	 * @param ids
	 * @return DataGridUser 
	 */
	DataGridUser findByDataGridId(long id);
	
	/**
	 * Find a list of users by their IDs
	 * @param ids
	 * @return List of users
	 */
	List<DataGridUser> findByDataGridIdList(String[] ids);
	
	/**
	 * Find a user by its username
	 * @param username
	 * @return List of users
	 */
	List<DataGridUser> findByUsername(String username);
	
	/**
	 * Find user by username and zone
	 * @param username
	 * @param zone
	 * @return
	 */
	DataGridUser findByUsernameAndZone(String username, String zone);
	
	/**
	 * Deletes a user by hid id
	 * @param username
	 * @param zone
	 * @return
	 */
	boolean deleteByDataGridId(long id); 
	
	/**
	 * Deletes a user by its username
	 * @param username
	 * @param zone
	 * @return
	 */
	boolean deleteByUsername(String username);
	
	/**
	 * Finds users matching the specified query
	 * @param query
	 * @return list of users
	 */
	public List<DataGridUser> findByQueryString(String query);
}
