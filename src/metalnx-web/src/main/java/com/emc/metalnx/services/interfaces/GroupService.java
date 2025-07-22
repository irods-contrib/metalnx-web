/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.interfaces;

import java.util.List;
import java.util.Map;

import org.irods.jargon.core.pub.domain.UserGroup;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridException;

public interface GroupService {

	/**
	 * Lists all groups existing on iRODS
	 *
	 * @return all groups existing in iRODS
	 * @throws DataGridException
	 */
	public List<UserGroup> findAll() throws DataGridException;

	/**
	 * Find an specific group whose name is equal to groupname
	 *
	 * @return all groups existing in iRODS
	 * @throws DataGridException
	 */
	public List<UserGroup> findByGroupname(String groupname) throws DataGridException;

	/**
	 * Find an specific group whose name is equal to groupname and zone
	 *
	 * @return
	 * @throws DataGridException {@link DataGridException}
	 */
	public UserGroup findByGroupnameAndZone(String groupname, String zone) throws DataGridException;

	/**
	 * Creates a group in iRODS
	 *
	 * @param newGroup
	 * @return true if the group was created successfully, false otherwise
	 * @throws DataGridException {@link DataGridException}
	 */
	public boolean createGroup(UserGroup newGroup, List<DataGridUser> usersToBeAttached) throws DataGridException;

	/**
	 * Gets the group's home collection path in the grid.
	 *
	 * Be aware that this method, for performance issues, does not call the data
	 * grid to verify whether or not the group exists.
	 *
	 * @param groupName name of the group to find the home path
	 * @return path to the group's home collection
	 */
	public String getGroupCollectionPath(String groupName);

	/**
	 * Delete the group from the connected zone
	 * 
	 * @param userGroup {@link UserGroup} to be deleted
	 * @throws DataGridException {@link DataGridException}
	 */
	void deleteGroup(UserGroup userGroup) throws DataGridException;

	/**
	 * Attach a user to a group. This group is local to the logged-in iRODS zone.
	 * The user can be a cross-zone user as denoted by the {@code zoneName}
	 * 
	 * 
	 * @throws DataGridException {@code DataGridException}
	 */
	void attachUserToGroup(String userName, String userZone, UserGroup userGroup) throws DataGridException;

	/**
	 * Remove the given user from the group
	 * 
	 * @param userName  {@code String} with the user to add
	 * @param userZone  {@code String} with the name of the user zone
	 * @param userGroup {@code userGroup} with the group#zone
	 * @throws DataGridException
	 */
	void removeUserFromGroup(String userName, String userZone, UserGroup userGroup) throws DataGridException;

	/**
	 * Given the users in the UI and in iRODS compare the delta and apply changes
	 * FIXME: this is dumb
	 * 
	 * @param group {@link UserGroup} being operated on
	 * @param users {@code List} of {@link DataGridUsers} to compute
	 * @throws DataGridException {@link DataGridException}
	 */
	void updateMemberList(UserGroup group, List<DataGridUser> users) throws DataGridException;

	/**
	 * Get a list of members for the given group and optional zone
	 * 
	 * @param groupName {@code String} with the name of the group
	 * @param groupZone {@code String} with an optional zone
	 * @return {@code List[String]} with the name of the members
	 * @throws DataGridException {@link DataGridException}
	 */
	String[] getMemberList(String groupName, String groupZone) throws DataGridException;

	/**
	 * Update permissions with a set of deltas to add and remove
	 * 
	 * @param group                   {@link UserGroup}
	 * @param addCollectionsToRead    {@code List} of collections to add read
	 *                                permissions
	 * @param removeCollectionsToRead {@code List} of collections to remove read
	 *                                permissions
	 * @throws DataGridException {@link DataGridException}
	 */
	void updateReadPermissions(UserGroup group, Map<String, Boolean> addCollectionsToRead,
			Map<String, Boolean> removeCollectionsToRead) throws DataGridException;

	/**
	 * Updates the list of collections the group has write permission
	 * 
	 * @param group                    {@link UserGroup}
	 * @param addCollectionsToWrite    {@code List} of collections to add write
	 *                                 permissions
	 * @param removeCollectionsToWrite {@code List} of collections to remove write
	 *                                 permissions
	 * @throws DataGridException {@link DataGridException}
	 */
	void updateWritePermissions(UserGroup group, Map<String, Boolean> addCollectionsToWrite,
			Map<String, Boolean> removeCollectionsToWrite) throws DataGridException;

	void updateOwnership(UserGroup group, Map<String, Boolean> addCollectionsToOwn,
			Map<String, Boolean> removeCollectionsToOwn) throws DataGridException;

	/**
	 * Locate a user group based on its iCAT id
	 * 
	 * @param groupId {@code String} with the group id
	 * @return {@link UserGroup} associated with the id, or {@code null}
	 * @throws DataGridException {@link DataGridException}
	 */
	UserGroup findById(String groupId) throws DataGridException;

}
