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

package com.emc.metalnx.services.irods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.entity.DataGridGroup;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.UserProfile;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.FavoritesService;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserBookmarkService;
import com.emc.metalnx.services.interfaces.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    UserDao userDao;

    @Autowired
    GroupService groupService;

    @Autowired
    UserBookmarkService userBookmarkService;

    @Autowired
    FavoritesService favoritesService;

    @Autowired
    private IRODSServices irodsServices;

    @Value("${irods.zoneName}")
    private String zoneName;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public List<DataGridUser> findAll() {
        List<DataGridUser> users = userDao.findAll(DataGridUser.class);
        Collections.sort(users);
        return users;
    }

    @Override
    public boolean createUser(DataGridUser user, String password) throws JargonException, DataGridConnectionRefusedException {

        UserAO userAO = irodsServices.getUserAO();

        // Translating to iRODS model format
        User irodsUser = new User();
        irodsUser.setName(user.getUsername());
        irodsUser.setZone(user.getAdditionalInfo());

        if (user.getUserType().compareTo(UserTypeEnum.RODS_ADMIN.getTextValue()) == 0) {
            irodsUser.setUserType(UserTypeEnum.RODS_ADMIN);
        }
        else {
            irodsUser.setUserType(UserTypeEnum.RODS_USER);
        }

        // Creating user
        irodsUser = userAO.addUser(irodsUser);

        user.setDataGridId(Long.parseLong(irodsUser.getId()));
        user.setEnabled(true);
        userDao.save(user);

        // Setting password
        userAO.changeAUserPasswordByAnAdmin(user.getUsername(), password);

        return true;

    }

    @Override
    public boolean deleteUserByUsername(String username) throws DataGridConnectionRefusedException {

        UserAO userAO = irodsServices.getUserAO();

        try {

            DataGridUser user = findByUsername(username).get(0);
            String userHomeFolder = String.format("/%s/home/%s", zoneName, username);

            // Removing user
            userAO.deleteUser(username);
            userDao.deleteByUsername(username);

            // Removing favorites and user bookmarks before removing user
            userBookmarkService.removeBookmarkBasedOnUser(user);
            userBookmarkService.removeBookmarkBasedOnPath(userHomeFolder);

            favoritesService.removeFavoriteBasedOnUser(user);
            favoritesService.removeFavoriteBasedOnPath(userHomeFolder);

            return true;
        }
        catch (Exception e) {
            logger.error("Could not delete user with username [" + username + "]");
        }
        return false;
    }

    @Override
    public boolean modifyUser(DataGridUser modifyUser) throws DataGridConnectionRefusedException {

        UserAO userAO = irodsServices.getUserAO();

        try {

            User iRodsUser = userAO.findById(String.valueOf(modifyUser.getDataGridId()));

            boolean iRodsFieldsModified = false;

            // check which fields were modified (iRODS)
            if (iRodsUser.getZone().compareTo(modifyUser.getAdditionalInfo()) != 0) {
                iRodsUser.setZone(modifyUser.getAdditionalInfo());
                iRodsFieldsModified = true;
            }

            if (!iRodsUser.getUserType().getTextValue().equals(modifyUser.getUserType())) {
                if (modifyUser.getUserType().compareTo(UserTypeEnum.RODS_ADMIN.getTextValue()) == 0) {
                    iRodsUser.setUserType(UserTypeEnum.RODS_ADMIN);
                }
                else {
                    iRodsUser.setUserType(UserTypeEnum.RODS_USER);
                }

                iRodsFieldsModified = true;
            }

            // updating user in iRODS if any field was modified
            if (iRodsFieldsModified) {
                userAO.updateUser(iRodsUser);
            }

            DataGridUser applicationUser = userDao.findByUsernameAndZone(modifyUser.getUsername(), modifyUser.getAdditionalInfo());

            // check which fields were modified (our database)
            if (applicationUser.getAdditionalInfo() == null || applicationUser.getAdditionalInfo().compareTo(modifyUser.getAdditionalInfo()) != 0) {
                applicationUser.setAdditionalInfo(modifyUser.getAdditionalInfo());
            }

            if (applicationUser.getFirstName() == null || applicationUser.getFirstName().compareTo(modifyUser.getFirstName()) != 0) {
                applicationUser.setFirstName(modifyUser.getFirstName());
            }

            if (applicationUser.getLastName() == null || applicationUser.getLastName().compareTo(modifyUser.getLastName()) != 0) {
                applicationUser.setLastName(modifyUser.getLastName());
            }

            if (applicationUser.getEmail() == null || applicationUser.getEmail().compareTo(modifyUser.getEmail()) != 0) {
                applicationUser.setEmail(modifyUser.getEmail());
            }

            if (applicationUser.getCompany() == null || applicationUser.getCompany().compareTo(modifyUser.getCompany()) != 0) {
                applicationUser.setCompany(modifyUser.getCompany());
            }

            if (applicationUser.getDepartment() == null || applicationUser.getDepartment().compareTo(modifyUser.getDepartment()) != 0) {
                applicationUser.setDepartment(modifyUser.getDepartment());
            }

            applicationUser.setUserProfile(modifyUser.getUserProfile());
            applicationUser.setLocale(modifyUser.getLocale());
            applicationUser.setOrganizationalRole(modifyUser.getOrganizationalRole());
            applicationUser.setUserType(modifyUser.getUserType());
            applicationUser.setForceFileOverwriting(modifyUser.isForceFileOverwriting());
            userDao.merge(applicationUser);

            // Changing password if a new password is set
            String newPassword = modifyUser.getPassword();
            if (newPassword != null && !newPassword.isEmpty()) {
                userAO.changeAUserPasswordByAnAdmin(modifyUser.getUsername(), newPassword);
            }

            return true;
        }
        catch (JargonException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<DataGridUser> findByDataGridIds(String[] ids) {
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
    public DataGridUser findByUsernameAndAdditionalInfo(String username, String additionalInfo) {
        return userDao.findByUsernameAndZone(username, additionalInfo);
    }

    @Override
    public int countAll() {
        return userDao.findAll(DataGridUser.class).size();
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
        }
        catch (JargonException e) {
            logger.error("Could not find group list for user [" + user.getUsername() + "], ", e);
        }

        // If something goes wrong, return empty list.
        return new String[0];
    }

    @Override
    public String[] getGroupIdsForUser(String username, String additionalInfo) throws DataGridConnectionRefusedException {

        DataGridUser user = findByUsernameAndAdditionalInfo(username, additionalInfo);

        if (user == null) {
            return new String[0];
        }

        return getGroupIdsForUser(user);
    }

    @Override
    public boolean updateGroupList(DataGridUser user, List<DataGridGroup> groups) throws DataGridConnectionRefusedException {

        UserGroupAO groupAO = irodsServices.getGroupAO();

        try {

            // List current groups for user
            List<UserGroup> groupsFromIrods = groupAO.findUserGroupsForUser(user.getUsername());

            // Building set with iRODS IDs already on this group
            HashMap<Long, UserGroup> idsFromIrods = new HashMap<Long, UserGroup>();
            for (UserGroup groupFromIrods : groupsFromIrods) {
                idsFromIrods.put(Long.valueOf(groupFromIrods.getUserGroupId()), groupFromIrods);
            }

            // Building set with iRODS IDs coming from UI
            HashMap<Long, DataGridGroup> idsFromUi = new HashMap<Long, DataGridGroup>();
            for (DataGridGroup groupFromUi : groups) {
                idsFromUi.put(groupFromUi.getDataGridId(), groupFromUi);
            }

            // Resolving differences from UI to iRODS
            Set<Long> keysFromUi = idsFromUi.keySet();
            Set<Long> keysFromIrods = idsFromIrods.keySet();

            // Committing changes to iRODS
            for (Long dataGridId : keysFromUi) {
                if (!keysFromIrods.contains(dataGridId)) {
                    groupService.attachUserToGroup(user, idsFromUi.get(dataGridId));
                }
            }

            for (Long dataGridId : keysFromIrods) {
                if (!keysFromUi.contains(dataGridId)) {
                    DataGridGroup group = new DataGridGroup();
                    group.setGroupname(idsFromIrods.get(dataGridId).getUserGroupName());

                    if (group.getGroupname().compareTo("public") != 0) {
                        groupService.removeUserFromGroup(user, group);
                    }
                }
            }

            return true;
        }
        catch (Exception e) {
            logger.info("Could not update [" + user.getUsername() + "] group list: ", e);
        }
        return false;
    }

    @Override
    public boolean applyProfileToUser(UserProfile profile, DataGridUser user) {
        Set<DataGridGroup> profileGroups = profile.getGroups();
        for (DataGridGroup dataGridGroup : profileGroups) {
            try {
                groupService.attachUserToGroup(user, dataGridGroup);
            }
            catch (Exception e) {
                logger.info("iCAT already contain the user [" + user.getUsername() + "] on group [" + dataGridGroup.getGroupname() + "] :", e);
            }
        }
        return true;
    }

    @Override
    public boolean updateReadPermissions(DataGridUser user, Map<String, Boolean> addCollectionsToRead, Map<String, Boolean> removeCollectionsToRead)
            throws DataGridConnectionRefusedException {

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
                    collectionAO.setAccessPermissionReadAsAdmin(user.getAdditionalInfo(), path, user.getUsername(), addCollectionsToRead.get(path));
                }
                else {
                    // applying read permissions on a data object
                    dataObjectAO.setAccessPermissionReadInAdminMode(user.getAdditionalInfo(), path, user.getUsername());
                }
            }
            removeAccessPermissionForUserAsAdmin(user, removeCollectionsToRead);
            readPermissionsUpdated = true;
        }
        catch (JargonException e) {
            logger.error("Could not set read permission:", e);
        }

        return readPermissionsUpdated;
    }

    @Override
    public boolean updateWritePermissions(DataGridUser user, Map<String, Boolean> addCollectionsToWrite, Map<String, Boolean> removeCollectionsToWrite)
            throws DataGridConnectionRefusedException {

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
                    collectionAO.setAccessPermissionWriteAsAdmin(user.getAdditionalInfo(), path, user.getUsername(), addCollectionsToWrite.get(path));
                }
                else {
                    dataObjectAO.setAccessPermissionWriteInAdminMode(user.getAdditionalInfo(), path, user.getUsername());
                }
            }

            removeAccessPermissionForUserAsAdmin(user, removeCollectionsToWrite);
            writePermissionsUpdated = true;
        }
        catch (JargonException e) {
            logger.error("Could not set write permission:", e);
        }

        return writePermissionsUpdated;
    }

    @Override
    public boolean updateOwnership(DataGridUser user, Map<String, Boolean> addCollectionsToOwn, Map<String, Boolean> removeCollectionsToOwn)
            throws DataGridConnectionRefusedException {

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
                    collectionAO.setAccessPermissionOwnAsAdmin(user.getAdditionalInfo(), path, user.getUsername(), addCollectionsToOwn.get(path));
                }
                else {
                    dataObjectAO.setAccessPermissionOwnInAdminMode(user.getAdditionalInfo(), path, user.getUsername());
                }
            }

            removeAccessPermissionForUserAsAdmin(user, removeCollectionsToOwn);
            ownPermissionsUpdated = true;
        }
        catch (JargonException e) {
            logger.error("Could not set ownership permission:", e);
        }

        return ownPermissionsUpdated;
    }

    @Override
    public List<String> listUserTypes() {
        List<String> userTypes = new ArrayList<String>();

        userTypes.add(UserTypeEnum.RODS_ADMIN.getTextValue());
        userTypes.add(UserTypeEnum.RODS_USER.getTextValue());

        return userTypes;
    }

    @Override
    public void removeAccessPermissionForUserAsAdmin(DataGridUser user, Map<String, Boolean> paths) throws JargonException,
    DataGridConnectionRefusedException {

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
                collectionAO.removeAccessPermissionForUserAsAdmin(user.getAdditionalInfo(), path, user.getUsername(), paths.get(path));
            }
            else {
                dataObjectAO.removeAccessPermissionsForUserInAdminMode(user.getAdditionalInfo(), path, user.getUsername());
            }
        }
    }

}
