 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.dao;

import com.emc.metalnx.core.domain.entity.DataGridUser;

import java.util.List;

public interface UserDao {
	
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

	List<DataGridUser> findAll();	
}
