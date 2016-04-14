package com.emc.metalnx.core.domain.dao;

import java.util.List;

import com.emc.metalnx.core.domain.dao.generic.GenericDao;
import com.emc.metalnx.core.domain.entity.DataGridGroup;

public interface GroupDao extends GenericDao<DataGridGroup, Long> {
	
	/**
	 * Find a group by its name
	 * @param groupname
	 * @return List of groups
	 */
	List<DataGridGroup> findByGroupname(String groupname);
	
	/**
	 * Find group by group and zone
	 * @param groupname
	 * @param zone
	 * @return
	 */
	DataGridGroup findByGroupnameAndZone(String groupname, String zone);
	
	/**
	 * Deletes a group by its group
	 * @param groupname
	 * @return
	 */
	boolean deleteByGroupname(String groupname);	
	
	/**
	 * Deletes a group by its id
	 * @param id
	 * @return true if a group whose id matches with the id parameter
	 */
	boolean deleteByDataGridGroupId(long id);
	
	/**
	 * Finds groups that match the specified query
	 * @param query
	 * @return list of groups
	 */
	public List<DataGridGroup> findByQueryString(String query);
	
	/**
	 * Finds all groups that match the input Data Grid IDs.
	 * @param ids
	 * @return list of groups
	 */
	public List<DataGridGroup> findByDataGridIdList(String[] ids);
	
	/**
     * Finds all groups that match the input Data Grid Group names.
     * @param ids
     * @return list of group names
     */
    public List<DataGridGroup> findByGroupNameList(String[] groupNames);
	
	/**
	 * Finds all groups that match the input Data Grid IDs.
	 * @param ids
	 * @return list of groups
	 */
	public List<DataGridGroup> findByIdList(Long[] ids);
	
	/**
	 * Finds a group that matches the input Data Grid ID.
	 * @param id
	 * @return DataGridGroup
	 */
	public DataGridGroup findByDataGridId(long id);
}
