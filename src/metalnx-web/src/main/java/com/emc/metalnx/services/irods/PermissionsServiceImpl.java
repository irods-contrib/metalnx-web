/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.irods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.entity.DataGridFilePermission;
import com.emc.metalnx.core.domain.entity.DataGridGroupPermission;
import com.emc.metalnx.core.domain.entity.DataGridUserPermission;
import com.emc.metalnx.core.domain.entity.enums.DataGridPermType;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.PermissionsService;

@Service
@Transactional
public class PermissionsServiceImpl implements PermissionsService {

	/*
	 * PERMISSIONS in iRODS READ: Download, Copy, and Replicate WRITE: Download,
	 * Copy, Replicate, Metadata (templates) OWN: Download, Copy, Replicate,
	 * Metadata (templates), Move, Edit, and Delete
	 */

	@Autowired
	private IRODSServices irodsServices;

	@Autowired
	private CollectionService collectionService;

	// String representing the rodsgroup type on the UserFilePermission enum
	private static final String RODS_GROUP = "rodsgroup";

	private static final Logger logger = LogManager.getLogger(PermissionsServiceImpl.class);

	@Override
	public DataGridPermType findMostRestrictivePermission(String... paths) throws DataGridConnectionRefusedException {
		logger.info("Find most restrictive permission");

		DataGridPermType mostRestrictivePermission = DataGridPermType.NONE;
		Set<String> permissions = new HashSet<>();

		for (String path : paths) {
			logger.info("Get permission for {}", path);
			permissions.add(collectionService.getPermissionsForPath(path));
		}

		if (permissions.contains("none")) {
			mostRestrictivePermission = DataGridPermType.NONE;
		} else if (permissions.contains("read_metadata")) {
			mostRestrictivePermission = DataGridPermType.READ_METADATA;
		} else if (permissions.contains("read")) {
			mostRestrictivePermission = DataGridPermType.READ;
		} else if (permissions.contains("read_object")) {
			mostRestrictivePermission = DataGridPermType.READ_OBJECT;
		} else if (permissions.contains("create_metadata")) {
			mostRestrictivePermission = DataGridPermType.CREATE_METADATA;
		} else if (permissions.contains("modify_metadata")) {
			mostRestrictivePermission = DataGridPermType.MODIFY_METADATA;
		} else if (permissions.contains("delete_metadata")) {
			mostRestrictivePermission = DataGridPermType.DELETE_METADATA;
		} else if (permissions.contains("create_object")) {
			mostRestrictivePermission = DataGridPermType.CREATE_OBJECT;
		} else if (permissions.contains("write") || permissions.contains("modify_object")) {
			mostRestrictivePermission = DataGridPermType.MODIFY_OBJECT;
		} else if (permissions.contains("delete_object")) {
			mostRestrictivePermission = DataGridPermType.DELETE_OBJECT;
		} else if (permissions.contains("own")) {
			mostRestrictivePermission = DataGridPermType.OWN;
		}

		logger.info("Most restrictive permission: {}", mostRestrictivePermission);

		return mostRestrictivePermission;
	}

	@Override
	public List<DataGridFilePermission> getPathPermissionDetails(String path, String username)
			throws JargonException, DataGridConnectionRefusedException {

		logger.debug("Getting permissions details for object {}", path);
		List<UserFilePermission> filePermissionList = this.getFilePermissionListForObject(path, username);
		return mapListToListDataGridFilePermission(filePermissionList);
	}

	@Override
	public List<DataGridFilePermission> getPathPermissionDetails(String path)
			throws JargonException, DataGridConnectionRefusedException {

		logger.info("Getting permissions details for object {}", path);
		List<UserFilePermission> filePermissionList = this.getFilePermissionListForObject(path);
		return mapListToListDataGridFilePermission(filePermissionList);
	}

	@Override
	public boolean canLoggedUserModifyPermissionOnPath(String path) throws DataGridConnectionRefusedException {

		String userName = irodsServices.getCurrentUser();
		final IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();
		final IRODSFileSystemAO irodsFileSystemAO = irodsServices.getIRODSFileSystemAO();

		try {
			int resultingPermission;
			final IRODSFile fileObj = irodsFileFactory.instanceIRODSFile(path);

			if (irodsFileSystemAO.isDirectory(fileObj)) {
				resultingPermission = irodsFileSystemAO.getDirectoryPermissionsForGivenUser(fileObj, userName);
			} else {
				resultingPermission = irodsFileSystemAO.getFilePermissionsForGivenUser(fileObj, userName);
			}

			return resultingPermission > FilePermissionEnum.DELETE_OBJECT.getPermissionNumericValue();

		} catch (final Exception e) {
			logger.error("Could not get permissions for current user: {}", e.getMessage());
		}

		return false;
	}

	/***********************************************************************************/
	/*************************
	 * PERMISSIONS/GROUPS PROCESSING
	 ***************************/
	/***********************************************************************************/

	@Override
	public List<DataGridGroupPermission> getGroupsWithPermissions(List<DataGridFilePermission> ufps) {

		logger.info("getGroupsWithPermissions()");

		// Maps from data grid ID to DataGridGroupPermission object for retrieving the
		// group names
		List<DataGridGroupPermission> groupPermissions = new ArrayList<>();

		for (DataGridFilePermission ufp : ufps) {

			// Getting only the groups, ignoring users
			if (ufp.getUserType().compareTo(RODS_GROUP) == 0) {
				DataGridGroupPermission groupPermission = new DataGridGroupPermission();
				groupPermission.setDataGridId(0);
				groupPermission.setGroupName(ufp.getUsername());
				groupPermission.setPermission(ufp.getPermission());
				groupPermissions.add(groupPermission);

			}
		}

		return groupPermissions;

	}

	/***********************************************************************************/
	/*************************
	 * PERMISSIONS/USERS PROCESSING
	 ****************************/
	/***********************************************************************************/

	@Override
	public List<DataGridUserPermission> getUsersWithPermissions(List<DataGridFilePermission> ufps) {

		logger.info("getUsersWithPermissions()");
		// List containing all the users with some kind of permissions
		List<DataGridUserPermission> usersWithPermissions = new ArrayList<DataGridUserPermission>();

		for (DataGridFilePermission ufp : ufps) {
			logger.debug("dataGridFilePermission:{}", ufp);

			// Getting only the users, ignoring groups
			if (ufp.getUserType().compareTo(RODS_GROUP) != 0) {
				DataGridUserPermission dgup = new DataGridUserPermission();
				// dgup.setDataGridId(Integer.parseInt(ufp.getUserId()));
				dgup.setUserName(ufp.getUsername());
				dgup.setUserSystemRole(ufp.getUserType());
				dgup.setPermission(ufp.getPermission());
				usersWithPermissions.add(dgup);
			}
		}

		return usersWithPermissions;
	}

	@Override
	public boolean setPermissionOnPath(DataGridPermType permType, String uName, boolean recursive, boolean inAdminMode,
			String... paths) throws DataGridConnectionRefusedException {

		logger.info("Setting {} permission on path {} for user/group {}", permType, paths, uName);

		boolean operationResult = true;

		for (String path : paths) {
			try {
				IRODSFile irodsFile = irodsServices.getIRODSFileFactory().instanceIRODSFile(path);

				if (irodsFile.isDirectory()) {
					operationResult = chmodCollection(permType, path, recursive, uName, inAdminMode);
				} else {
					operationResult = chmodDataObject(permType, path, uName, inAdminMode);
				}
				logger.info("Permission {} for user {} on path {} set successfully", permType, uName, paths);
			} catch (JargonException e) {
				logger.error("Could not set {} permission on path {} for user/group {}", permType, path, uName, e);
				operationResult = false;
			}
		}

		return operationResult;
	}

	private boolean chmodCollection(DataGridPermType permType, String path, boolean recursive, String uName,
			boolean inAdminMode) throws DataGridConnectionRefusedException {
		String currentZone = irodsServices.getCurrentUserZone();
		CollectionAO collectionAO = irodsServices.getCollectionAO();
		boolean isPermissionSet = false;

		try {
			logger.debug("Setting {} permission on collection {} for user/group as ADMIN{}", permType, path, uName);

			if (!inAdminMode) {
				FilePermissionEnum filePermission = FilePermissionEnum.valueOf(permType.toString());
				collectionAO.setAccessPermission(currentZone, path, uName, recursive, filePermission);
			} else {
				FilePermissionEnum filePermission = FilePermissionEnum.valueOf(permType.toString());
				collectionAO.setAccessPermissionAsAdmin(currentZone, path, uName, recursive, filePermission);
			}
			isPermissionSet = true;
		} catch (JargonException e) {
			logger.error("Could not set {} permission on path {} for user/group {}", permType, path, uName, e);
		}

		return isPermissionSet;
	}

	private boolean chmodDataObject(DataGridPermType permType, String path, String uName, boolean inAdminMode)
			throws DataGridConnectionRefusedException {
		String currentZone = irodsServices.getCurrentUserZone();
		DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();

		logger.debug("Setting {} permission on data object {} for user/group {}", permType, path, uName);

		boolean isPermissionSet = false;

		try {
			if (!inAdminMode) {
				FilePermissionEnum filePermission = FilePermissionEnum.valueOf(permType.toString());
				dataObjectAO.setAccessPermission(currentZone, path, uName, filePermission);
			} else {
				FilePermissionEnum filePermission = FilePermissionEnum.valueOf(permType.toString());
				dataObjectAO.setAccessPermissionAsAdmin(currentZone, path, uName, filePermission);
			}

			isPermissionSet = true;
		} catch (JargonException e) {
			logger.error("Could not set {} permission on path {} for user/group {}", permType, path, uName, e);
		}

		return isPermissionSet;
	}

	@Override
	public String resolveMostPermissiveAccessForUser(String irodsAbsolutePath, String userName)
			throws DataGridException {

		logger.info("resolveMostPermissiveAccessForUser()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		logger.info("irodsAbsolutePath:{}", irodsAbsolutePath);
		logger.info("userName:{}", userName);

		/*
		 * The user is characterized by the user name as well as the zone they are
		 * logged into. The iRODS file path may indicate that the user is browsing
		 * across a federation and a zone will be thus added to the user name assuming
		 * user#zone is the subject for which permissions will be sought
		 */

		String targetZone = MiscIRODSUtils.getZoneInPath(irodsAbsolutePath);
		logger.debug("targetZone from path:{}", targetZone);
		String targetUser = null;
		if (targetZone.equals(irodsServices.getCurrentUserZone())) {
			logger.debug("expanding user name for cross-zone query");
			StringBuilder sb = new StringBuilder();
			sb.append(userName);
			sb.append('#');
			sb.append(irodsServices.getCurrentUserZone());
			targetUser = sb.toString();
		} else {
			logger.debug("use existing user name within-zone");
			targetUser = userName;
			targetZone = "";
		}

		List<UserGroup> userGroups;
		List<UserFilePermission> acl;

		try {
			logger.info("obtaining user groups for user:{}", userName);
			userGroups = irodsServices.getGroupAO().findUserGroupsForUserInZone(targetUser, targetZone);
			logger.info("obtaining acls list for object:{}", irodsAbsolutePath);
			acl = getFilePermissionListForObject(irodsAbsolutePath);
			logger.info("acl:{}", acl);
		} catch (JargonException e) {
			logger.error("jargon exception getting permission listing", e);
			throw new DataGridException("error getting permission listing", e);
		}

		// Building set containing group names for current user
		Set<String> userGroupsSet = new HashSet<>();
		for (UserGroup g : userGroups) {
			userGroupsSet.add(g.getUserGroupName());
		}

		// Instantiating comparison matrix for permissions
		List<String> permissions = new ArrayList<>();
		permissions.add("NONE");
		permissions.add("READ");
		permissions.add("WRITE");
		permissions.add("OWN");

		String resultingPermission = "NONE";
		for (UserFilePermission perm : acl) {

			String permUserName = perm.getUserName();

			// Checking if current permission is related to logged user
			if (permUserName.compareTo(targetUser) == 0 || userGroupsSet.contains(permUserName)) {
				String permissionName = perm.getFilePermissionEnum().name();
				int userOrGroupPerm = permissions.indexOf(permissionName);
				int currentPermission = permissions.indexOf(resultingPermission);

				if (userOrGroupPerm > currentPermission) {
					resultingPermission = permissionName;
				}
			}

			if (resultingPermission.compareToIgnoreCase("OWN") == 0) {
				break;
			}
		}

		return resultingPermission.toLowerCase();
	}

	/***********************************************************************************/
	/********************************
	 * PRIVATE METHODS
	 **********************************/
	/***********************************************************************************/

	/**
	 * Gets the list of file permissions on the requested object. The object can be
	 * a collection as a single data object.
	 *
	 * @param path the path to the object
	 * @return list of {@link UserFilePermission}
	 * @throws FileNotFoundException
	 * @throws JargonException
	 * @throws DataGridConnectionRefusedException
	 */
	private List<UserFilePermission> getFilePermissionListForObject(String path)
			throws JargonException, DataGridConnectionRefusedException {

		return this.getFilePermissionListForObject(path, "");
	}

	/**
	 * Gets the list of file permissions on the requested object for a particular
	 * user. The object can be a collection as a single data object.
	 *
	 * @param path     the path to the object
	 * @param username user name to get the permissions on the given path. If no
	 *                 user name is required, an empty String or null should be
	 *                 provided
	 * @return list of {@link UserFilePermission}
	 * @throws FileNotFoundException
	 * @throws JargonException
	 * @throws DataGridConnectionRefusedException
	 */
	private List<UserFilePermission> getFilePermissionListForObject(String path, String username)
			throws DataGridConnectionRefusedException, JargonException {
		Object obj = irodsServices.getCollectionAndDataObjectListAndSearchAO().getFullObjectForType(path);

		List<UserFilePermission> filePermissionList = new ArrayList<UserFilePermission>();
		List<UserFilePermission> dataGridfilePermissionList = null;

		// If the object is a collection
		if (obj instanceof Collection) {
			logger.debug("Getting permission info for collection {}", path);
			dataGridfilePermissionList = irodsServices.getCollectionAO().listPermissionsForCollection(path);
		}

		// If the object is a data object
		else {
			logger.debug("Getting permission info for data object {}", path);
			dataGridfilePermissionList = irodsServices.getDataObjectAO().listPermissionsForDataObject(path);
		}

		// adding to the final list of permissions only permissions related to the user
		// given
		// as the parameter
		if (username != null && !username.isEmpty()) {
			for (UserFilePermission userFilePermission : dataGridfilePermissionList) {
				if (userFilePermission.getUserName().equalsIgnoreCase(username)) {
					filePermissionList.add(userFilePermission);
				}
			}
		} else {
			filePermissionList = dataGridfilePermissionList;
		}

		return filePermissionList;
	}

	/**
	 * Maps an UserFilePermission instance to DataGridFilePermission object
	 *
	 * @param ufp
	 * @return instance of {@link DataGridFilePermission}
	 */
	private DataGridFilePermission mapToDataGridFilePermission(UserFilePermission ufp) {

		logger.debug("\tMapping permissions for permissions controller");

		DataGridFilePermission dgfp = new DataGridFilePermission();
		dgfp.setUserId(ufp.getUserId());
		dgfp.setUsername(ufp.getUserName());
		
		// Translate WRITE and READ to the new strings MODIFY_OBJECT and READ_OBJECT
		if ("WRITE".equals(ufp.getFilePermissionEnum().toString())) {
			dgfp.setPermission("MODIFY_OBJECT");
		} else if ("READ".equals(ufp.getFilePermissionEnum().toString())) {
			dgfp.setPermission("READ_OBJECT");
		} else {
			dgfp.setPermission(ufp.getFilePermissionEnum().toString());
		}
		
		dgfp.setUserType(ufp.getUserType().getTextValue());
		dgfp.setUserZone(ufp.getUserZone());
		return dgfp;
	}

	/**
	 * Maps a list of UserFilePermission instances to a list of
	 * DataGridFilePermission objects.
	 *
	 * @param filePermissionList
	 * @return list of instances of {@link DataGridFilePermission}
	 */
	private List<DataGridFilePermission> mapListToListDataGridFilePermission(
			List<UserFilePermission> filePermissionList) {

		logger.debug("Mapping list of UserFilePermissions to List of DataGridFilePermission");

		List<DataGridFilePermission> dgFilePermissionList = new ArrayList<DataGridFilePermission>();
		for (UserFilePermission ufp : filePermissionList) {
			DataGridFilePermission dgfp = mapToDataGridFilePermission(ufp);
			dgFilePermissionList.add(dgfp);
		}
		logger.debug("mapped permission list:{}", dgFilePermissionList);
		return dgFilePermissionList;
	}
}
