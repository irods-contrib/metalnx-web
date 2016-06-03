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

import org.irods.jargon.core.exception.JargonException;

import com.emc.metalnx.core.domain.entity.DataGridGroup;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.UserProfile;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

public interface UserService {

    /**
     * Returns all the users existing on iRODS
     *
     * @return
     */
    public List<DataGridUser> findAll();

    /**
     * Returns the list of DataGridUsers matching data grid ID.
     *
     * @param ids
     * @return
     */
    List<DataGridUser> findByDataGridIds(String[] ids);

    /**
     * Create the user on local DB and on iRODS
     *
     * @param user
     * @return confirmation
     * @throws DataGridConnectionRefusedException
     */
    public boolean createUser(DataGridUser user, String password) throws JargonException, DataGridConnectionRefusedException;

    /**
     * Delete user by username
     *
     * @param username
     * @return a confirmation that the user has been removed
     * @throws DataGridConnectionRefusedException
     */
    public boolean deleteUserByUsername(String username) throws DataGridConnectionRefusedException;

    /**
     * Modify user by username
     *
     * @param username
     * @return a confirmation that the user has been modified
     * @throws DataGridConnectionRefusedException
     */
    public boolean modifyUser(DataGridUser modifyUser) throws DataGridConnectionRefusedException;

    /**
     * Finds users by username
     *
     * @param username
     *            name of the user(s) to be found
     * @return List of users matching {@code username}
     */
    public List<DataGridUser> findByUsername(String username);

    /**
     * Return a user whose name is 'username' and additional info is 'additionalInfo'
     *
     * @param username
     * @param additionalInfo
     * @return
     */
    public DataGridUser findByUsernameAndAdditionalInfo(String username, String additionalInfo);

    /**
     * Returns the quantity of users persisted on our database.
     *
     * @return
     */
    public int countAll();

    /**
     * Finds users matching the specified query.
     *
     * @param query
     * @param page
     * @return list of users
     */
    public List<DataGridUser> findByQueryString(String query);

    /**
     * Returns the list of IDs of each group the user belongs to.
     *
     * @param user
     * @return list of data grid IDs
     * @throws DataGridConnectionRefusedException
     */
    public String[] getGroupIdsForUser(DataGridUser user) throws DataGridConnectionRefusedException;

    /**
     * Returns the list of IDs of each group the user belongs to.
     *
     * @param username
     *            name of the user
     * @param additionalInfo
     *            zone name where the user is
     * @return list of data grid IDs
     * @throws DataGridConnectionRefusedException
     */
    public String[] getGroupIdsForUser(String username, String additionalInfo) throws DataGridConnectionRefusedException;

    /**
     * Updates the list of groups the user belongs to.
     *
     * @param user
     * @param groups
     * @return the confirmation of the update.
     * @throws DataGridConnectionRefusedException
     */
    public boolean updateGroupList(DataGridUser user, List<DataGridGroup> groups) throws DataGridConnectionRefusedException;

    /**
     * Applies a profile to a user
     *
     * @param profile
     * @param user
     * @return
     */
    public boolean applyProfileToUser(UserProfile profile, DataGridUser user);

    /**
     * Updates the list of collections the user has read permission
     *
     * @param group
     * @param addCollectionsToRead
     * @param removeCollectionsToRead
     * @param recursive
     *            flag that says whether or not the permission is applied recursively
     * @return True, if permissions were updated. False, otherwise.
     * @throws DataGridConnectionRefusedException
     */
    public boolean updateReadPermissions(DataGridUser group, Map<String, Boolean> addCollectionsToRead, Map<String, Boolean> removeCollectionsToRead)
            throws DataGridConnectionRefusedException;

    /**
     * Updates the list of collections the user has write permission
     *
     * @param group
     * @param addCollectionsToWrite
     * @param removeCollectionsToWrite
     * @param recursive
     *            flag that says whether or not the permission is applied recursively
     * @return True, if permissions were updated. False, otherwise.
     * @throws DataGridConnectionRefusedException
     */
    public boolean updateWritePermissions(DataGridUser group, Map<String, Boolean> addCollectionsToWrite,
            Map<String, Boolean> removeCollectionsToWrite) throws DataGridConnectionRefusedException;

    /**
     * Updates the list of collections the user owns
     *
     * @param group
     * @param addCollectionsToOwn
     * @param removeCollectionsToOwn
     * @param recursive
     *            flag that says whether or not the permission is applied recursively
     * @return True, if permissions were updated. False, otherwise.
     * @throws DataGridConnectionRefusedException
     */
    public boolean updateOwnership(DataGridUser group, Map<String, Boolean> addCollectionsToOwn, Map<String, Boolean> removeCollectionsToOwn)
            throws DataGridConnectionRefusedException;

    /**
     * Lists all user types available in the data grid
     *
     * @return List of all user types
     */
    public List<String> listUserTypes();

    /**
     * Removes access permission on a set of paths for a particular user.
     *
     * @param user
     *            user who permission will be removed
     * @param paths
     *            paths to collections/data objects on which the user will lose access
     * @param recursive
     *            flag that says whether or not the permission is applied recursively
     * @throws JargonException
     * @throws DataGridConnectionRefusedException
     */
    void removeAccessPermissionForUserAsAdmin(DataGridUser user, Map<String, Boolean> paths) throws JargonException,
            DataGridConnectionRefusedException;

}
