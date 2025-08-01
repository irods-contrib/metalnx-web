/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.irods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserService;

@Service("userService")
@Transactional
public class UserServiceImpl implements UserService {

	@Autowired
	UserDao userDao;

	@Autowired
	GroupService groupService;

	@Autowired
	private IRODSServices irodsServices;
	
	@Autowired
	IRODSAccessObjectFactory irodsAccessObjectFactory;

	@Autowired
	private ConfigService configService;

	private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

	/**
	 * Compare the data grid user to the current zone and build a proper fully
	 * qualified user name where indicated
	 * 
	 * @param dataGridUser {@link DataGridUser} to build the full user name
	 * @return {@code String} with the full user name including #zone where needed
	 */
	public String buildConcatUserName(final DataGridUser dataGridUser) {
		logger.info("buildConcatUserName()");
		StringBuilder sb = new StringBuilder();
		sb.append(dataGridUser.getUsername());

		if (dataGridUser.getZone().isEmpty()) {
			// no zone info
		} else if (dataGridUser.getZone().equals(irodsServices.getCurrentUserZone())) {
			// no need for zone, same zone
		} else {
			logger.debug("adding zone");
			sb.append("#");
			sb.append(dataGridUser.getZone());
		}

		return sb.toString();

	}

	@Override
	public List<DataGridUser> findAll() {
		List<DataGridUser> users = userDao.findAll();
		Collections.sort(users);
		return users;
	}

	@Override
	public boolean createUser(DataGridUser user, String password)
			throws JargonException, DataGridConnectionRefusedException {

		logger.info("createUser()");

		UserAO userAO = irodsServices.getUserAO();

		// Translating to iRODS model format
		User irodsUser = new User();
		irodsUser.setName(user.getUsername());
		irodsUser.setZone(user.getZone());

		if (user.getUserType().compareTo(UserTypeEnum.RODS_ADMIN.getTextValue()) == 0) {
			irodsUser.setUserType(UserTypeEnum.RODS_ADMIN);
		} else if (user.getUserType().compareTo(UserTypeEnum.GROUP_ADMIN.getTextValue()) == 0) {
			irodsUser.setUserType(UserTypeEnum.GROUP_ADMIN);
		} else {
			irodsUser.setUserType(UserTypeEnum.RODS_USER);
		}

		logger.debug("adding...");
		// Creating user
		irodsUser = userAO.addUser(irodsUser);
		logger.debug("...added");

		user.setDataGridId(Long.parseLong(irodsUser.getId()));
		user.setEnabled(true);

		logger.info("setting password if provided (may be a PAM user!)");
		if (password == null || password.isEmpty()) {
			logger.info("no password, assume is not standard auth");
		} else {
			logger.info("password was provided, set in irods");
			// Setting password
			userAO.changeAUserPasswordByAnAdmin(user.getUsername(), password);
		}

		return true;

	}

	@Override
	public boolean deleteUserByUsername(String username) throws DataGridConnectionRefusedException {

		UserAO userAO = irodsServices.getUserAO();

		try {

			DataGridUser user = findByUsername(username).get(0);
			String userHomeFolder = String.format("/%s/home/%s", configService.getIrodsZone(), username);

			// Removing user
			userAO.deleteUser(username);
			userDao.deleteByUsername(username);

			return true;
		} catch (Exception e) {
			logger.error("Could not delete user with username [" + username + "]");
		}
		return false;
	}

	@Override
	public boolean modifyUser(DataGridUser modifyUser) throws DataGridException {

		UserAO userAO = irodsServices.getUserAO();

		try {

			User iRodsUser = userAO.findById(String.valueOf(modifyUser.getDataGridId()));

			boolean iRodsFieldsModified = false;

			// check which fields were modified (iRODS)
			if (iRodsUser.getZone().compareTo(modifyUser.getZone()) != 0) {
				iRodsUser.setZone(modifyUser.getZone());
				iRodsFieldsModified = true;
			}

			if (!iRodsUser.getUserType().getTextValue().equals(modifyUser.getUserType())) {
				if (modifyUser.getUserType().compareTo(UserTypeEnum.RODS_ADMIN.getTextValue()) == 0) {
					iRodsUser.setUserType(UserTypeEnum.RODS_ADMIN);
				} else if (modifyUser.getUserType().compareTo(UserTypeEnum.GROUP_ADMIN.getTextValue()) == 0) {
					iRodsUser.setUserType(UserTypeEnum.GROUP_ADMIN);
				} else {
					iRodsUser.setUserType(UserTypeEnum.RODS_USER);
				}

				iRodsFieldsModified = true;
			}

			// updating user in iRODS if any field was modified
			if (iRodsFieldsModified) {
				userAO.updateUser(iRodsUser);
			}

			// Changing password if a new password is set
			String newPassword = modifyUser.getPassword();
			if (newPassword != null && !newPassword.isEmpty()) {
				userAO.changeAUserPasswordByAnAdmin(modifyUser.getUsername(), newPassword);
			}

			return true;
		} catch (JargonException e) {
			logger.error("error modifying user:{}", modifyUser, e);
			throw new DataGridException("unable to modify user", e);
		}
	}

	@Override
	public List<DataGridUser> findByDataGridIds(String[] ids) {
		logger.info("findByDataGridIds()");
		List<DataGridUser> users = userDao.findByDataGridIdList(ids);
		Collections.sort(users);
		return users;
	}

	@Override
	public List<DataGridUser> findByUsername(String username) {
		List<DataGridUser> users = userDao.findByUsername(username);
		Collections.sort(users);
		return users;
	}

	@Override
	public DataGridUser findByUsernameAndZone(String username, String zone) {
		return userDao.findByUsernameAndZone(username, zone);
	}

	@Override
	public int countAll() {
		return userDao.findAll().size();
	}

	@Override
	public List<DataGridUser> findByQueryString(String query) {
		List<DataGridUser> users = userDao.findByQueryString(query);
		Collections.sort(users);
		return users;
	}

	@Override
	public String[] getGroupIdsForUser(DataGridUser user) throws DataGridConnectionRefusedException {
		UserGroupAO userGroupAO = irodsServices.getGroupAO();

		try {
			// Getting list of groups the user belongs to.
			List<UserGroup> groups = userGroupAO.findUserGroupsForUser(user.getUsername());

			// Building Data Grid IDs list
			String[] ids = new String[groups.size()];
			for (int i = 0; i < groups.size(); i++) {
				ids[i] = groups.get(i).getUserGroupId();
			}

			// Returning results
			return ids;
		} catch (JargonException e) {
			logger.error("Could not find group list for user [" + user.getUsername() + "], ", e);
		}

		// If something goes wrong, return empty list.
		return new String[0];
	}

	@Override
	public String[] getGroupIdsForUser(String username, String zone)
			throws DataGridConnectionRefusedException {

		DataGridUser user = findByUsernameAndZone(username, zone);

		if (user == null) {
			return new String[0];
		}

		return getGroupIdsForUser(user);
	}

	@Override
	public boolean updateGroupList(DataGridUser user, List<UserGroup> groups) throws DataGridException {

		logger.info("updateGroupList()");

		if (user == null) {
			throw new IllegalArgumentException("null user");
		}

		if (groups == null) {
			throw new IllegalArgumentException("null groups");
		}

		logger.info("user:{}", user);
		logger.info("groups:{}", groups);

		UserGroupAO groupAO = irodsServices.getGroupAO();

		try {

			// List current groups for user
			List<UserGroup> groupsFromIrods = groupAO.findUserGroupsForUser(user.getUsername());

			Map<String, UserGroup> groupsFromUiMap = new HashMap<>();
			Map<String, UserGroup> groupsFromIrodsMap = new HashMap<>();

			for (UserGroup userGroup : groups) {
				groupsFromUiMap.put(userGroup.getUserGroupName(), userGroup);
			}

			for (UserGroup userGroup : groupsFromIrods) {
				groupsFromIrodsMap.put(userGroup.getUserGroupName(), userGroup);
			}

			// Committing changes to iRODS

			// for every ui key not in irods put the user in that group

			for (String key : groupsFromUiMap.keySet()) {
				if (groupsFromIrodsMap.get(key) == null) {
					logger.info("adding group:{}", key);
					// groupAO.addUserToGroup(key, this.buildConcatUserName(user), "");
					groupAO.addUserToGroup(key, user.getUsername(), user.getZone());
				}
			}

			// for every irods key not in ui remove from irods

			for (String key : groupsFromIrodsMap.keySet()) {
				if (groupsFromUiMap.get(key) == null) {
					logger.info("removing group:{}", key);
					groupAO.removeUserFromGroup(key, user.getUsername(), user.getZone());
				}
			}

			logger.info("done!");

			return true;
		} catch (Exception e) {
			logger.error("Could not update [" + user.getUsername() + "] group list: ", e);
			throw new DataGridException("error updating user groups", e);
		}
	}

	@Override
	public boolean updateReadPermissions(DataGridUser user, Map<String, Boolean> addCollectionsToRead,
			Map<String, Boolean> removeCollectionsToRead) throws DataGridConnectionRefusedException {

		CollectionAO collectionAO = null;
		DataObjectAO dataObjectAO = null;
		IRODSFile irodsFile = null;
		IRODSFileFactory irodsFileFactory = null;
		boolean readPermissionsUpdated = false;

		try {
			collectionAO = irodsServices.getCollectionAO();
			dataObjectAO = irodsServices.getDataObjectAO();
			irodsFileFactory = irodsServices.getIRODSFileFactory();

			for (String path : addCollectionsToRead.keySet()) {
				irodsFile = irodsFileFactory.instanceIRODSFile(path);
				if (irodsFile.isDirectory()) {
					// applying read permissions on a collection (not recursively)
					collectionAO.setAccessPermissionReadAsAdmin(user.getZone(), path, user.getUsername(),
							addCollectionsToRead.get(path));
				} else {
					// applying read permissions on a data object
					dataObjectAO.setAccessPermissionReadInAdminMode(user.getZone(), path, user.getUsername());
				}
			}
			removeAccessPermissionForUserAsAdmin(user, removeCollectionsToRead);
			readPermissionsUpdated = true;
		} catch (JargonException e) {
			logger.error("Could not set read permission:", e);
		}

		return readPermissionsUpdated;
	}

	@Override
	public boolean updateWritePermissions(DataGridUser user, Map<String, Boolean> addCollectionsToWrite,
			Map<String, Boolean> removeCollectionsToWrite) throws DataGridConnectionRefusedException {

		CollectionAO collectionAO = null;
		DataObjectAO dataObjectAO = null;
		IRODSFile irodsFile = null;
		IRODSFileFactory irodsFileFactory = null;
		boolean writePermissionsUpdated = false;

		try {
			collectionAO = irodsServices.getCollectionAO();
			dataObjectAO = irodsServices.getDataObjectAO();
			irodsFileFactory = irodsServices.getIRODSFileFactory();

			for (String path : addCollectionsToWrite.keySet()) {
				irodsFile = irodsFileFactory.instanceIRODSFile(path);
				if (irodsFile.isDirectory()) {
					collectionAO.setAccessPermissionWriteAsAdmin(user.getZone(), path, user.getUsername(),
							addCollectionsToWrite.get(path));
				} else {
					dataObjectAO.setAccessPermissionWriteInAdminMode(user.getZone(), path,
							user.getUsername());
				}
			}

			removeAccessPermissionForUserAsAdmin(user, removeCollectionsToWrite);
			writePermissionsUpdated = true;
		} catch (JargonException e) {
			logger.error("Could not set write permission:", e);
		}

		return writePermissionsUpdated;
	}

	@Override
	public boolean updateOwnership(DataGridUser user, Map<String, Boolean> addCollectionsToOwn,
			Map<String, Boolean> removeCollectionsToOwn) throws DataGridConnectionRefusedException {

		CollectionAO collectionAO = null;
		DataObjectAO dataObjectAO = null;
		IRODSFile irodsFile = null;
		IRODSFileFactory irodsFileFactory = null;
		boolean ownPermissionsUpdated = false;

		try {
			collectionAO = irodsServices.getCollectionAO();
			dataObjectAO = irodsServices.getDataObjectAO();
			irodsFileFactory = irodsServices.getIRODSFileFactory();

			for (String path : addCollectionsToOwn.keySet()) {
				irodsFile = irodsFileFactory.instanceIRODSFile(path);
				if (irodsFile.isDirectory()) {
					collectionAO.setAccessPermissionOwnAsAdmin(user.getZone(), path, user.getUsername(),
							addCollectionsToOwn.get(path));
				} else {
					dataObjectAO.setAccessPermissionOwnInAdminMode(user.getZone(), path, user.getUsername());
				}
			}

			removeAccessPermissionForUserAsAdmin(user, removeCollectionsToOwn);
			ownPermissionsUpdated = true;
		} catch (JargonException e) {
			logger.error("Could not set ownership permission:", e);
		}

		return ownPermissionsUpdated;
	}

	@Override
	public List<String> listUserTypes() {
		List<String> userTypes = new ArrayList<String>();

		userTypes.add(UserTypeEnum.RODS_USER.getTextValue());
		userTypes.add(UserTypeEnum.RODS_ADMIN.getTextValue());
		userTypes.add(UserTypeEnum.GROUP_ADMIN.getTextValue());

		return userTypes;
	}

	@Override
	public void removeAccessPermissionForUserAsAdmin(DataGridUser user, Map<String, Boolean> paths)
			throws JargonException, DataGridConnectionRefusedException {

		if (paths == null || paths.isEmpty()) {
			return;
		}

		IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();
		CollectionAO collectionAO = irodsServices.getCollectionAO();
		DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
		IRODSFile irodsFile = null;

		for (String path : paths.keySet()) {
			irodsFile = irodsFileFactory.instanceIRODSFile(path);
			if (irodsFile.isDirectory()) {
				collectionAO.removeAccessPermissionForUserAsAdmin(user.getZone(), path, user.getUsername(),
						paths.get(path));
			} else {
				dataObjectAO.removeAccessPermissionsForUserInAdminMode(user.getZone(), path,
						user.getUsername());
			}
		}
	}

}
