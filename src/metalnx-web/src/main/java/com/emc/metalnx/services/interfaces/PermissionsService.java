/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.interfaces;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;

import com.emc.metalnx.core.domain.entity.DataGridFilePermission;
import com.emc.metalnx.core.domain.entity.DataGridGroupPermission;
import com.emc.metalnx.core.domain.entity.DataGridUserPermission;
import com.emc.metalnx.core.domain.entity.enums.DataGridPermType;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;

public interface PermissionsService {

	/**
	 * Finds the most restrictive permission from a list of paths.
	 *
	 * @return string containing the most restrictive permission ("none", "read",
	 *         "write", or "own")
	 * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the
	 *                                            grid
	 */
	DataGridPermType findMostRestrictivePermission(String... paths) throws DataGridConnectionRefusedException;

	/**
	 * Retrieves all permissions information about a given path for a given user.
	 * This path can be a collection or a data object. It also parses the results
	 * down to groups and users so there are no duplicate results.
	 *
	 * @param path     path to retrieve permissions from
	 * @param username user name to check permissions on the given path
	 * @return list of {@link DataGridFilePermission} instances
	 */
	List<DataGridFilePermission> getPathPermissionDetails(String path, String username)
			throws JargonException, DataGridConnectionRefusedException;

	/**
	 * Retrieves all the permissions information about a given object that can be a
	 * collection or a data object. It also parses the results down to groups and
	 * users so there are no duplicate results.
	 *
	 * @param path
	 * @return list of {@link DataGridFilePermission} instances
	 */
	List<DataGridFilePermission> getPathPermissionDetails(String path)
			throws JargonException, DataGridConnectionRefusedException;

	/**
	 * Gets all the groups with some kind of permission on the permissions list
	 *
	 * @param ufps
	 * @return list of {@link DataGridGroupPermission}
	 */
	List<DataGridGroupPermission> getGroupsWithPermissions(List<DataGridFilePermission> ufps);

	/**
	 * Gets all the users (not including groups) with some kind of permission on the
	 * list
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
	 * @param userOrGroupName user or group name to give permissions
	 * @param recursive       flag that says whether or not the given permission
	 *                        should be applied recursively
	 * @param inAdminMode     if true, tries to set permission in admin mode (-M
	 *                        option) if false, tries to set permission normally (no
	 *                        additional options)
	 * @param paths           to apply permission
	 * @return a {@link boolean} indicating the status of the request
	 * @throws DataGridConnectionRefusedException
	 */
	boolean setPermissionOnPath(DataGridPermType permType, String userOrGroupName, boolean recursive,
			boolean inAdminMode, String... paths) throws DataGridConnectionRefusedException;

	/**
	 * Find the most permissive access for a user, based on user or group
	 * permissions. This is specifically built to handle cross-zone permissions,
	 * where the user id will be properly interpreted as it would be present on the
	 * given zone. This means that user 'pam' logged into 'zone1' will be queried as
	 * 'pam#zone1' when interrogating a federated zone
	 * 
	 * @param irodsAbsolutePath {@code String} with the target iRODS path
	 * @param userName          {@code String} with the user name in plain form,
	 *                          expecting that federated zones will be queried in
	 *                          user#homezone format
	 * @return{@code String} with the highest level of permission
	 * @throws DataGridException {@link DataGridException}
	 */
	String resolveMostPermissiveAccessForUser(String irodsAbsolutePath, String userName) throws DataGridException;
}
