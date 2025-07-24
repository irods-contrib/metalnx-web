/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.irods;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.InvalidGroupException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.IRODSServices;

@Service
@Transactional
public class GroupServiceImpl implements GroupService {

	@Autowired
	CollectionService collectionService;

	@Autowired
	IRODSServices irodsServices;

	@Autowired
	private ConfigService configService;

	private static final Logger logger = LogManager.getLogger(GroupServiceImpl.class);

	@Override
	public List<UserGroup> findAll() throws DataGridException {
		try {
			UserGroupAO groupAO = irodsServices.getGroupAO();
			List<UserGroup> groups = groupAO.findAll();
			return groups;
		} catch (JargonException e) {
			logger.error("error finding groups", e);
			throw new DataGridException(e);
		}
	}

	@Override
	public UserGroup findById(String groupId) throws DataGridException {
		logger.info("findById()");
		if (groupId == null || groupId.isEmpty()) {
			throw new IllegalArgumentException("null groupId");
		}
		UserGroupAO userGroupAO = irodsServices.getGroupAO();
		try {
			return userGroupAO.find(groupId);
		} catch (JargonException e) {
			logger.error("error finding group by id:{}", groupId, e);
			throw new DataGridException("error finding group", e);
		}
	}

	@Override
	public List<UserGroup> findByGroupname(String groupname) throws DataGridException {
		try {
			UserGroupAO userGroupAO = irodsServices.getGroupAO();
			List<UserGroup> groups = userGroupAO.findUserGroups(groupname);
			return groups;
		} catch (JargonException e) {
			logger.error("error finding groups", e);
			throw new DataGridException(e);
		}
	}

	@Override
	public UserGroup findByGroupnameAndZone(String groupname, String zone) throws DataGridException {

		logger.info("findByGroupnameAndZone()");
		if (groupname == null || groupname.isEmpty()) {
			throw new IllegalArgumentException("null or empty groupname");
		}

		if (zone == null) {
			throw new IllegalArgumentException("null zone");
		}

		logger.info("groupname:{}", groupname);
		logger.info("zone:{}", zone);

		UserGroupAO userGroupAO = irodsServices.getGroupAO();
		StringBuilder sb = new StringBuilder();
		sb.append(groupname);
		if (!zone.isEmpty()) {
			if (!zone.equals(userGroupAO.getIRODSAccount().getZone())) {
				sb.append('#');
				sb.append(zone);
			}
		}

		try {
			UserGroup group = userGroupAO.findByName(sb.toString());

			return group;
		} catch (JargonException e) {
			logger.error("error finding groups", e);
			throw new DataGridException(e);
		}
	}

	@Override
	public boolean createGroup(UserGroup newGroup, List<DataGridUser> usersToBeAttached) throws DataGridException {

		UserGroupAO groupAO = irodsServices.getGroupAO();

		try {

			// creating group in iRODS
			groupAO.addUserGroup(newGroup);

			// attaching users to this group
			updateMemberList(newGroup, usersToBeAttached);

			return true;
		} catch (DuplicateDataException e) {
			logger.error("UserGroup " + newGroup.getUserGroupName() + " already exists: ", e);
			return false;
		} catch (JargonException e) {
			logger.error("Could not execute createGroup() on UserGroupAO class: ", e);
			throw new DataGridException("error creating group", e);
		}

	}

	@Override
	public void deleteGroup(UserGroup userGroup) throws DataGridException {

		logger.info("deleteGroup()");

		if (userGroup == null) {
			throw new IllegalArgumentException("null userGroup");
		}

		logger.info("userGroup:{}", userGroup);

		UserGroupAO groupAO = irodsServices.getGroupAO();
		try {
			groupAO.removeUserGroup(userGroup);
		} catch (JargonException e) {
			logger.error("exception removing user group", e);
			throw new DataGridException("error removing user group", e);
		}

	}

	@Override
	public void attachUserToGroup(String userName, String userZone, UserGroup userGroup) throws DataGridException {

		logger.info("attachUserToGroup()");

		if (userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("null or empty userName");
		}

		if (userZone == null) {
			throw new IllegalArgumentException("null userZone");
		}

		if (userGroup == null) {
			throw new IllegalArgumentException("null userGroup");
		}

		logger.info("userName:{}", userName);
		logger.info("userZone:{}", userZone);
		logger.info("userGroup:{}", userGroup);

		UserGroupAO groupAO = irodsServices.getGroupAO();

		try {
			groupAO.addUserToGroup(userGroup.getUserGroupName(), userName, userZone); // FIXME: needs support for user
																						// zone and group zone
		} catch (Exception e) {
			logger.error("unable to add user to group", e);
			throw new DataGridException("unable to add user to group", e);
		}
	}

	@Override
	public void removeUserFromGroup(String userName, String userZone, UserGroup userGroup) throws DataGridException {

		UserGroupAO groupAO = irodsServices.getGroupAO(); // FIXME: user#zone and group#zone support
		try {
			groupAO.removeUserFromGroup(userGroup.getUserGroupName(), userName, userZone);
		} catch (Exception e) {
			logger.info("Could not remove user [" + userName + "] from group [" + userGroup + "]: ", e);
		}
	}

	// FIXME: why is this done this way? Why not just track new users added?

	@Override
	public void updateMemberList(UserGroup group, List<DataGridUser> users) throws DataGridException {

		logger.info("updateMemberList()");

		if (group == null) {
			throw new IllegalArgumentException("null group");
		}

		if (users == null) {
			throw new IllegalArgumentException("null users");
		}

		logger.info("group:{}", group);
		logger.info("users:{}", users);

		try {

			UserGroupAO groupAO = irodsServices.getGroupAO();
			// FIXME: incorporate user and group zone, include changes to userGroupAO
			// Users that are currently on this group
			List<User> usersFromIrods = groupAO.listUserGroupMembers(group.getUserGroupName());

			// Building set with iRODS IDs already on this group
			HashMap<Long, User> idsFromIrods = new HashMap<>();
			for (User userFromIrods : usersFromIrods) {
				idsFromIrods.put(Long.decode(userFromIrods.getId()), userFromIrods);
				logger.info("userFromIrods:{}", userFromIrods);
			}

			// Building set with iRODS IDs coming from UI
			HashMap<Long, DataGridUser> idsFromUi = new HashMap<Long, DataGridUser>();
			for (DataGridUser userFromUi : users) {
				idsFromUi.put(userFromUi.getDataGridId(), userFromUi);
			}

			// Resolving differences from UI to iRODS
			// keysFromUi = irods user ids from iCAT
			Set<Long> keysFromUi = idsFromUi.keySet();
			// keysFromIrods = irods user ids from web page
			Set<Long> keysFromIrods = idsFromIrods.keySet();

			// for each iRODS user in the group from web page
			for (Long keyFromUi : keysFromUi) {
				// if not in irods add it
				if (!keysFromIrods.contains(keyFromUi)) {
					logger.info("adding user:{}", keyFromUi);
					attachUserToGroup(idsFromUi.get(keyFromUi), group);
				}
			}

			for (Long keyFromIrods : keysFromIrods) {
				// if the UI does not have the user from iRODS, remove it from iRODS
				if (!keysFromUi.contains(keyFromIrods)) {
					DataGridUser user = new DataGridUser();
					user.setUsername(idsFromIrods.get(keyFromIrods).getName());
					user.setZone(idsFromIrods.get(keyFromIrods).getZone());
					removeUserFromGroup(user, group);
					logger.info("removing user:{}", user);
				}
			}

		} catch (Exception e) {
			logger.info("error updating user group membership", e);
			throw new DataGridException("error adding group membership", e);
		}
	}

	/**
	 * Remove the given user from the group
	 * 
	 * @param user  {@link DataGridUser} to remove
	 * @param group {@link UserGroup} from which the user will be removed
	 */
	private void removeUserFromGroup(DataGridUser user, UserGroup group) throws DataGridException {
		logger.info("removeUserFromGroup()");
		logger.info("user:{}", user);
		logger.info("group:{}", group);
		UserGroupAO groupAO = irodsServices.getGroupAO();
		try {
			groupAO.removeUserFromGroup(group.getUserGroupName(), user.getUsername(), user.getZone());
		} catch (InvalidUserException e) {
			logger.warn("invalid user, ignored", e);
		} catch (InvalidGroupException e) {
			logger.warn("invalid group, ignored", e);
		} catch (JargonException e) {
			logger.error("error adding user to group", e);
			throw new DataGridException("error removing user from group", e);
		}
	}

	/**
	 * Attach the data grid user to the given UserGroup in iRODS
	 * 
	 * @param dataGridUser {@link DataGridUser} to add to group
	 * @param group        {@link UserGroup} to add user to
	 */
	private void attachUserToGroup(DataGridUser dataGridUser, UserGroup group) throws DataGridException {
		logger.info("attachUserToGroup()");
		logger.info("dataGridUser:{}", dataGridUser);
		logger.info("group:{}", group);
		UserGroupAO groupAO = irodsServices.getGroupAO();
		try {
			groupAO.addUserToGroup(group.getUserGroupName(), dataGridUser.getUsername(),
					dataGridUser.getZone());
		} catch (JargonException e) {
			logger.error("error adding user to group", e);
			throw new DataGridException("error adding user to group", e);
		}
	}

	@Override
	public String[] getMemberList(String groupName, String groupZone) throws DataGridException {

		logger.info("getMemberList()");

		if (groupName == null || groupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty groupName");
		}

		if (groupZone == null) {
			throw new IllegalArgumentException("null groupZone");
		}

		logger.info("groupName:{}", groupName);
		logger.info("groupZone:{}", groupZone);

		UserGroupAO userGroupAO = irodsServices.getGroupAO();

		String queryGroupName;
		if (groupZone.isEmpty()) {
			queryGroupName = groupName;
		} else if (groupZone.equals(userGroupAO.getIRODSAccount().getZone())) {
			queryGroupName = groupName;
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append(groupName);
			sb.append('#');
			sb.append(groupZone);
			queryGroupName = sb.toString();
		}

		logger.info("queryGroupName:{}", queryGroupName);

		try {
			List<User> groupMembers = userGroupAO.listUserGroupMembers(queryGroupName);
			String[] dataGridIds = new String[groupMembers.size()];
			for (int i = 0; i < groupMembers.size(); i++) {
				dataGridIds[i] = groupMembers.get(i).getId();
			}
			return dataGridIds;
		} catch (JargonException e) {
			logger.error("Could not get members list for group ", e);
			throw new DataGridException("error listing group members", e);
		}
	}

	@Override
	public void updateReadPermissions(UserGroup group, Map<String, Boolean> addCollectionsToRead,
			Map<String, Boolean> removeCollectionsToRead) throws DataGridException {

		logger.info("updateReadPermissions()");

		if (group == null) {
			throw new IllegalArgumentException("null group");
		}

		if (addCollectionsToRead == null) {
			throw new IllegalArgumentException("null addCollectionsToRead");
		}

		if (removeCollectionsToRead == null) {
			throw new IllegalArgumentException("null removeCollectionsToRead");
		}

		logger.info("group:{}", group);
		logger.info("addCollectionsToRead:{}", addCollectionsToRead);
		logger.info("removeCollectionsToRead:{}", removeCollectionsToRead);

		CollectionAO collectionAO = irodsServices.getCollectionAO();
		DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();

		try {
			for (String path : addCollectionsToRead.keySet()) {
				if (collectionService.isCollection(path)) {
					collectionAO.setAccessPermissionReadAsAdmin(group.getZone(), path, group.getUserGroupName(),
							addCollectionsToRead.get(path));
				} else {
					dataObjectAO.setAccessPermissionReadInAdminMode(group.getZone(), path, group.getUserGroupName());
				}
			}
			for (String path : removeCollectionsToRead.keySet()) {
				if (collectionService.isCollection(path)) {
					collectionAO.removeAccessPermissionForUserAsAdmin(group.getZone(), path, group.getUserGroupName(),
							removeCollectionsToRead.get(path));
				} else {
					dataObjectAO.setAccessPermissionReadInAdminMode(group.getZone(), path, group.getUserGroupName());
				}
			}
		} catch (JargonException e) {
			logger.error("Could not set read permission:", e);
			throw new DataGridException("exception setting permission", e);
		}
	}

	@Override
	public void updateWritePermissions(UserGroup group, Map<String, Boolean> addCollectionsToWrite,
			Map<String, Boolean> removeCollectionsToWrite) throws DataGridException {

		logger.info("updateWritePermissions()");

		if (group == null) {
			throw new IllegalArgumentException("null group");
		}

		if (addCollectionsToWrite == null) {
			throw new IllegalArgumentException("null addCollectionsToWrite");
		}

		if (removeCollectionsToWrite == null) {
			throw new IllegalArgumentException("null removeCollectionsToWrite");
		}

		logger.info("group:{}", group);
		logger.info("addCollectionsToWrite:{}", addCollectionsToWrite);
		logger.info("removeCollectionsToWrite:{}", removeCollectionsToWrite);

		DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
		CollectionAO collectionAO = irodsServices.getCollectionAO();

		try {
			for (String path : addCollectionsToWrite.keySet()) {
				if (collectionService.isCollection(path)) {
					collectionAO.setAccessPermissionWriteAsAdmin(group.getZone(), path, group.getUserGroupName(),
							addCollectionsToWrite.get(path));
				} else {
					dataObjectAO.setAccessPermissionWriteInAdminMode(group.getZone(), path, group.getUserGroupName());
				}
			}
			for (String path : removeCollectionsToWrite.keySet()) {
				if (collectionService.isCollection(path)) {
					collectionAO.removeAccessPermissionForUserAsAdmin(group.getZone(), path, group.getUserGroupName(),
							removeCollectionsToWrite.get(path));
				} else {
					dataObjectAO.removeAccessPermissionsForUserInAdminMode(group.getZone(), path,
							group.getUserGroupName());
				}
			}
		} catch (JargonException e) {
			logger.error("Could not set read permission:", e);
			throw new DataGridException("error setting read permission", e);
		}
	}

	@Override
	public void updateOwnership(UserGroup group, Map<String, Boolean> addCollectionsToOwn,
			Map<String, Boolean> removeCollectionsToOwn) throws DataGridException {

		logger.info("updateOwnership()");

		if (group == null) {
			throw new IllegalArgumentException("null group");
		}

		if (addCollectionsToOwn == null) {
			throw new IllegalArgumentException("null addCollectionsToOwn");
		}

		if (removeCollectionsToOwn == null) {
			throw new IllegalArgumentException("null removeCollectionsToOwn");
		}

		logger.info("group:{}", group);
		logger.info("addCollectionsToOwn:{}", addCollectionsToOwn);
		logger.info("removeCollectionsToOwn:{}", removeCollectionsToOwn);

		DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
		CollectionAO collectionAO = irodsServices.getCollectionAO();
		try {
			for (String path : addCollectionsToOwn.keySet()) {
				if (collectionService.isCollection(path)) {
					collectionAO.setAccessPermissionOwnAsAdmin(group.getZone(), path, group.getUserGroupName(),
							addCollectionsToOwn.get(path));
				} else {
					dataObjectAO.setAccessPermissionWriteInAdminMode(group.getZone(), path, group.getUserGroupName());
				}
			}
			for (String path : removeCollectionsToOwn.keySet()) {
				if (collectionService.isCollection(path)) {
					collectionAO.removeAccessPermissionForUserAsAdmin(group.getZone(), path, group.getUserGroupName(),
							removeCollectionsToOwn.get(path));
				} else {
					dataObjectAO.removeAccessPermissionsForUserInAdminMode(group.getZone(), path,
							group.getUserGroupName());
				}
			}

		} catch (JargonException e) {
			logger.error("Could not set ownership:", e);
			throw new DataGridException("error setting own permission", e);

		}
	}

	@Override
	public String getGroupCollectionPath(String groupName) {
		if (groupName == null || groupName.isEmpty()) {
			return "";
		}

		return String.format("/%s/home/%s", configService.getIrodsZone(), groupName);
	}

}
