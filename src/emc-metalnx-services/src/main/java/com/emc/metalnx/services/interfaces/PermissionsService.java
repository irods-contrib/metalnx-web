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

import com.emc.metalnx.core.domain.entity.*;
import com.emc.metalnx.core.domain.entity.enums.DataGridPermType;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import org.irods.jargon.core.exception.JargonException;

import java.util.List;

public interface PermissionsService {

    /**
     * Retrieves all permissions information about a given path for a given user.
     * This path can be a collection or a data object. It also parses the
     * results down to groups and users so there are no duplicate results.
     *
     * @param path
     *            path to retrieve permissions from
     * @param username
     *            user name to check permissions on the given path
     * @return list of {@link DataGridFilePermission} instances
     */
    List<DataGridFilePermission> getPathPermissionDetails(String path, String username) throws JargonException,
            DataGridConnectionRefusedException;

    /**
     * Retrieves all the permissions information about a given object
     * that can be a collection or a data object. It also parses the
     * results down to groups and users so there are no duplicate results.
     *
     * @param path
     * @return list of {@link DataGridFilePermission} instances
     */
    List<DataGridFilePermission> getPathPermissionDetails(String path) throws JargonException,
            DataGridConnectionRefusedException;

    /**
     * Gets all the groups with some kind of permission on the permissions list
     *
     * @param ufps
     * @return list of {@link DataGridGroupPermission}
     */
    List<DataGridGroupPermission> getGroupsWithPermissions(List<DataGridFilePermission> ufps);

    /**
     * Gets all the users (not including groups) with some kind of permission on the list
     *
     * @param ufps
     * @return list of {@link DataGridUserPermission}
     */
    List<DataGridUserPermission> getUsersWithPermissions(List<DataGridFilePermission> ufps);

    /**
     * Checks if the logged user can modify a given path
     *
     * @param path
     * @return boolean
     * @throws DataGridConnectionRefusedException
     */
    boolean canLoggedUserModifyPermissionOnPath(String path) throws DataGridConnectionRefusedException;

    /**
     * Updates permission on the given path for the given user or group.
     *
     * @param permType
     * @param userOrGroupName
     *            user or group name to give permissions
     * @param path
     *            that can be a collection or a data object
     * @param recursive
     *            flag that says whether or not the given permission should be applied recursively
     * @param inAdminMode
     *          if true, tries to set permission in admin mode (-M option)
     *          if false, tries to set permission normally (no additional options)
     * @return a {@link boolean} indicating the status of the request
     * @throws DataGridConnectionRefusedException
     */
    boolean setPermissionOnPath(DataGridPermType permType, String userOrGroupName, String path, boolean recursive, boolean inAdminMode)
            throws DataGridConnectionRefusedException;

    /**
     * Finds resulting most permissive permission for a given user
     * @param obj
     * @param user
     */
    void resolveMostPermissiveAccessForUser(DataGridCollectionAndDataObject obj, DataGridUser user) throws
            DataGridException;
}
