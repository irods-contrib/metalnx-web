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
import java.util.Map;

import com.emc.metalnx.core.domain.entity.DataGridGroup;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

public interface GroupService {

    /**
     * Lists all groups existing on iRODS
     *
     * @return all groups existing in iRODS
     */
    public List<DataGridGroup> findAll();

    /**
     * Find an specific group whose name is equal to groupname
     *
     * @return all groups existing in iRODS
     */
    public List<DataGridGroup> findByGroupname(String groupname);

    /**
     * Find an specific group whose name is equal to groupname and zone
     *
     * @return
     */
    public DataGridGroup findByGroupnameAndZone(String groupname, String zone);

    /**
     * Creates a group in iRODS
     *
     * @param newGroup
     * @return true if the group was created successfully, false otherwise
     * @throws DataGridConnectionRefusedException
     */
    public boolean createGroup(DataGridGroup newGroup, List<DataGridUser> usersToBeAttached) throws DataGridConnectionRefusedException;

    /**
     * Removes a group from iRODS
     *
     * @param groupname
     * @return True, if the group was successfully deleted. False, otherwise.
     * @throws DataGridConnectionRefusedException
     */
    public boolean deleteGroupByGroupname(String groupname) throws DataGridConnectionRefusedException;

    /**
     * Attach the user to the group
     *
     * @param user
     * @param group
     * @return
     * @throws DataGridConnectionRefusedException
     */
    public boolean attachUserToGroup(DataGridUser user, DataGridGroup group) throws DataGridConnectionRefusedException;

    /**
     * Remove the user from the group
     *
     * @param user
     * @param group
     * @return
     * @throws DataGridConnectionRefusedException
     */
    public boolean removeUserFromGroup(DataGridUser user, DataGridGroup group) throws DataGridConnectionRefusedException;

    /**
     * Updates the list of users belonging to a given group
     *
     * @param user
     * @param group
     * @return the confirmation
     * @throws DataGridConnectionRefusedException
     */
    public boolean updateMemberList(DataGridGroup group, List<DataGridUser> users) throws DataGridConnectionRefusedException;

    /**
     * Updates the list of collections the group has read permission
     *
     * @return the confirmation
     * @throws DataGridConnectionRefusedException
     */
    public boolean updateReadPermissions(DataGridGroup group, Map<String, Boolean> addCollectionsToRead, Map<String, Boolean> removeCollectionsToRead)
            throws DataGridConnectionRefusedException;

    /**
     * Updates the list of collections the group has write permission
     *
     * @return the confirmation
     * @throws DataGridConnectionRefusedException
     */
    public boolean updateWritePermissions(DataGridGroup group, Map<String, Boolean> addCollectionsToWrite,
            Map<String, Boolean> removeCollectionsToWrite) throws DataGridConnectionRefusedException;

    /**
     * Updates the list of collections the group owns
     *
     * @return the confirmation
     * @throws DataGridConnectionRefusedException
     */
    public boolean updateOwnership(DataGridGroup group, Map<String, Boolean> addCollectionsToOwn, Map<String, Boolean> removeCollectionsToOwn)
            throws DataGridConnectionRefusedException;

    /**
     * Returns the list of Data Grid IDs for the members of the group.
     *
     * @param group
     * @return list of Data Grid IDs of members
     * @throws DataGridConnectionRefusedException
     */
    public String[] getMemberList(DataGridGroup group) throws DataGridConnectionRefusedException;

    /**
     * Finds users matching the specified query.
     *
     * @param query
     * @param page
     * @return list of users
     */
    public List<DataGridGroup> findByQueryString(String query);

    /**
     * Finds users whose ids match the list of ids.
     *
     * @param ids
     * @return list of users
     */
    public List<DataGridGroup> findByDataGridIdList(String[] ids);

    /**
     * Finds groups whose names match the list of names.
     *
     * @param groupNames
     * @return list of users
     */
    public List<DataGridGroup> findByGroupNameList(String[] groupNames);

    /**
     * Finds users whose ids match the list of ids.
     *
     * @param ids
     * @return list of users
     */
    public List<DataGridGroup> findByIdList(String[] ids);

    /**
     * Calculates the number of existing groups
     *
     * @return the number of groups
     */
    public int countAll();

    /**
     * Gets the group's home collection path in the grid.
     *
     * Be aware that this method, for performance issues, does not call the data grid
     * to verify whether or not the group exists.
     *
     * @param groupName
     *            name of the group to find the home path
     * @return
     *         path to the group's home collection
     */
    public String getGroupCollectionPath(String groupName);

}
