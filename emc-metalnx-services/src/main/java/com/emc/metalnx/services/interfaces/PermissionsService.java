package com.emc.metalnx.services.interfaces;

import java.util.List;

import com.emc.metalnx.core.domain.entity.*;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

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
    public List<DataGridFilePermission> getPathPermissionDetails(String path, String username) throws JargonException,
            DataGridConnectionRefusedException;

    /**
     * Retrieves all the permissions information about a given object
     * that can be a collection or a data object. It also parses the
     * results down to groups and users so there are no duplicate results.
     *
     * @param path
     * @return list of {@link DataGridFilePermission} instances
     */
    public List<DataGridFilePermission> getPathPermissionDetails(String path) throws JargonException,
            DataGridConnectionRefusedException;

    /**
     * Gets all the groups with some kind of permission on the permissions list
     *
     * @param ufps
     * @return list of {@link DataGridGroupPermission}
     */
    public List<DataGridGroupPermission> getGroupsWithPermissions(List<DataGridFilePermission> ufps);

    /**
     * Gets all the users (not including groups) with some kind of permission on the list
     *
     * @param ufps
     * @return list of {@link DataGridUserPermission}
     */
    public List<DataGridUserPermission> getUsersWithPermissions(List<DataGridFilePermission> ufps);

    /**
     * Checks if the logged user can modify a given path
     *
     * @param path
     * @return boolean
     */
    public boolean canLoggedUserModifyPermissionOnPath(String path) throws JargonException, DataGridConnectionRefusedException;

    /**
     * Updates permission on the given path for the given user or group.
     *
     * @param permission
     * @param userOrGroupName
     *            user or group name to give permissions
     * @param path
     *            that can be a collection or a data object
     * @param recursive
     *            flag that says whether or not the given permission should be applied recursively
     * @return a {@link boolean} indicating the status of the request
     * @throws FileNotFoundException
     * @throws JargonException
     * @throws DataGridConnectionRefusedException
     */
    public boolean setPermissionOnPath(String permission, String userOrGroupName, String path, boolean recursive) throws
            JargonException, DataGridConnectionRefusedException;

    /**
     * Finds resulting most permissive permission for a given user
     * @param obj
     * @param user
     */
    public void resolveMostPermissiveAccessForUser(DataGridCollectionAndDataObject obj, DataGridUser user) throws
            DataGridException;
}
