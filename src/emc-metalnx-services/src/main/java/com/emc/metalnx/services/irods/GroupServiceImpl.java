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

import com.emc.metalnx.services.interfaces.*;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.dao.GroupDao;
import com.emc.metalnx.core.domain.entity.DataGridGroup;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

@Service
@Transactional
public class GroupServiceImpl implements GroupService {

    @Autowired
    CollectionService collectionService;

    @Autowired
    UserBookmarkService userBookmarkService;

    @Autowired
    GroupBookmarkService groupBookmarkService;

    @Autowired
    IRODSServices irodsServices;

    @Autowired
    GroupDao groupDao;

    @Value("${irods.zoneName}")
    private String zoneName;

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public List<DataGridGroup> findAll() {
        List<DataGridGroup> groups = groupDao.findAll(DataGridGroup.class);
        Collections.sort(groups);
        return groups;
    }

    @Override
    public List<DataGridGroup> findByGroupname(String groupname) {
        List<DataGridGroup> groups = groupDao.findByGroupname(groupname);
        Collections.sort(groups);
        return groups;
    }

    @Override
    public DataGridGroup findByGroupnameAndZone(String groupname, String zone) {
        return groupDao.findByGroupnameAndZone(groupname, zone);
    }

    @Override
    public boolean createGroup(DataGridGroup newGroup, List<DataGridUser> usersToBeAttached) throws DataGridConnectionRefusedException {

        UserGroupAO groupAO = irodsServices.getGroupAO();

        // Translating to iRODS model format
        UserGroup irodsGroup = new UserGroup();

        irodsGroup.setUserGroupName(newGroup.getGroupname());
        irodsGroup.setZone(newGroup.getAdditionalInfo());

        try {

            irodsGroup.setUserGroupName(newGroup.getGroupname());
            irodsGroup.setZone(newGroup.getAdditionalInfo());

            // creating group in iRODS
            groupAO.addUserGroup(irodsGroup);

            // Recovering the recently created group to get the data grid id.
            irodsGroup = groupAO.findByName(irodsGroup.getUserGroupName());
            newGroup.setDataGridId(Long.parseLong(irodsGroup.getUserGroupId()));

            // Persisting the new group into our database
            groupDao.save(newGroup);

            // attaching users to this group
            updateMemberList(newGroup, usersToBeAttached);

            return true;
        }
        catch (DuplicateDataException e) {
            logger.error("UserGroup " + newGroup.getGroupname() + " already exists: ", e);
        }
        catch (JargonException e) {
            logger.error("Could not execute createGroup() on UserGroupAO class: ", e);
        }

        return false;
    }

    @Override
    public boolean deleteGroupByGroupname(String groupname) throws DataGridConnectionRefusedException {
        boolean groupDeleted = false;

        UserGroupAO groupAO = irodsServices.getGroupAO();

        try {

            DataGridGroup group = groupDao.findByGroupnameAndZone(groupname, zoneName);

            // remove group from the data grid
            groupAO.removeUserGroup(groupname);

            // Removing group bookmarks associated to this group
            userBookmarkService.removeBookmarkBasedOnPath(String.format("/%s/home/%s", zoneName, groupname));
            groupBookmarkService.removeBookmarkBasedOnGroup(group);

            // remove user from the Mlx database
            groupDeleted = groupDao.deleteByGroupname(groupname);
        }
        catch (JargonException e) {
            logger.error("Could not execute removeUserGroup(String groupname)/" + "deleteByGroupname(groupname) on UserGroupAO/GroupDao class(es): ",
                    e);
        }
        catch (Exception e) {
            logger.error("Could not execute delete group (dao)");
        }

        return groupDeleted;
    }

    @Override
    public boolean attachUserToGroup(DataGridUser user, DataGridGroup group) throws DataGridConnectionRefusedException {

        UserGroupAO groupAO = irodsServices.getGroupAO();
        try {
            groupAO.addUserToGroup(group.getGroupname(), user.getUsername(), group.getAdditionalInfo());
            return true;
        }
        catch (Exception e) {
            logger.info("Could not attach user [" + user.getUsername() + "] to group [" + group.getGroupname() + "]: ", e);
        }
        return false;
    }

    @Override
    public boolean removeUserFromGroup(DataGridUser user, DataGridGroup group) throws DataGridConnectionRefusedException {

        UserGroupAO groupAO = irodsServices.getGroupAO();
        try {
            groupAO.removeUserFromGroup(group.getGroupname(), user.getUsername(), group.getAdditionalInfo());
            return true;
        }
        catch (Exception e) {
            logger.info("Could not remove user [" + user.getUsername() + "] from group [" + group.getGroupname() + "]: ", e);
        }
        return false;
    }

    @Override
    public boolean updateMemberList(DataGridGroup group, List<DataGridUser> users) throws DataGridConnectionRefusedException {

        try {

            UserGroupAO groupAO = irodsServices.getGroupAO();

            // Users that are currently on this group
            List<User> usersFromIrods = groupAO.listUserGroupMembers(group.getGroupname());

            // Building set with iRODS IDs already on this group
            HashMap<Long, User> idsFromIrods = new HashMap<Long, User>();
            for (User userFromIrods : usersFromIrods) {
                idsFromIrods.put(Long.valueOf(userFromIrods.getId()), userFromIrods);
            }

            // Building set with iRODS IDs coming from UI
            HashMap<Long, DataGridUser> idsFromUi = new HashMap<Long, DataGridUser>();
            for (DataGridUser userFromUi : users) {
                idsFromUi.put(userFromUi.getDataGridId(), userFromUi);
            }

            // Resolving differences from UI to iRODS
            Set<Long> keysFromUi = idsFromUi.keySet();
            Set<Long> keysFromIrods = idsFromIrods.keySet();

            for (Long dataGridId : keysFromUi) {
                if (!keysFromIrods.contains(dataGridId)) {
                    attachUserToGroup(idsFromUi.get(dataGridId), group);
                }
            }

            for (Long dataGridId : keysFromIrods) {
                if (!keysFromUi.contains(dataGridId)) {
                    DataGridUser user = new DataGridUser();
                    user.setUsername(idsFromIrods.get(dataGridId).getName());
                    removeUserFromGroup(user, group);
                }
            }

            return true;
        }
        catch (Exception e) {
            logger.info("Could not update [" + group.getGroupname() + "]: ", e);
        }
        return false;
    }

    @Override
    public List<DataGridGroup> findByQueryString(String query) {
        List<DataGridGroup> groups = groupDao.findByQueryString(query);
        Collections.sort(groups);
        return groups;
    }

    @Override
    public String[] getMemberList(DataGridGroup group) throws DataGridConnectionRefusedException {
        UserGroupAO userGroupAO = irodsServices.getGroupAO();
        try {
            List<User> groupMembers = userGroupAO.listUserGroupMembers(group.getGroupname());
            String[] dataGridIds = new String[groupMembers.size()];
            for (int i = 0; i < groupMembers.size(); i++) {
                dataGridIds[i] = groupMembers.get(i).getId();
            }
            return dataGridIds;
        }
        catch (JargonException e) {
            logger.error("Could not get members list for group [" + group.getGroupname() + "]: ", e);
        }
        return new String[0];
    }

    @Override
    public List<DataGridGroup> findByDataGridIdList(String[] ids) {
        List<DataGridGroup> groups = groupDao.findByDataGridIdList(ids);
        Collections.sort(groups);
        return groups;
    }

    @Override
    public List<DataGridGroup> findByGroupNameList(String[] groupNames) {
        List<DataGridGroup> groups = groupDao.findByGroupNameList(groupNames);
        Collections.sort(groups);
        return groups;
    }

    @Override
    public List<DataGridGroup> findByIdList(String[] ids) {

        if (ids != null) {
            Long[] longIds = new Long[ids.length];
            for (int i = 0; i < ids.length; i++) {
                longIds[i] = Long.valueOf(ids[i]);
            }
            List<DataGridGroup> groups = groupDao.findByIdList(longIds);
            Collections.sort(groups);
            return groups;
        }
        return new ArrayList<DataGridGroup>();
    }

    @Override
    public boolean updateReadPermissions(DataGridGroup group, Map<String, Boolean> addCollectionsToRead, Map<String, Boolean> removeCollectionsToRead)
            throws DataGridConnectionRefusedException {

        CollectionAO collectionAO = irodsServices.getCollectionAO();
        DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();

        try {
            for (String path : addCollectionsToRead.keySet()) {
                if (collectionService.isCollection(path)) {
                    collectionAO
                            .setAccessPermissionReadAsAdmin(group.getAdditionalInfo(), path, group.getGroupname(), addCollectionsToRead.get(path));
                }
                else {
                    dataObjectAO.setAccessPermissionReadInAdminMode(group.getAdditionalInfo(), path, group.getGroupname());
                }
            }
            for (String path : removeCollectionsToRead.keySet()) {
                if (collectionService.isCollection(path)) {
                    collectionAO.removeAccessPermissionForUserAsAdmin(group.getAdditionalInfo(), path, group.getGroupname(),
                            removeCollectionsToRead.get(path));
                }
                else {
                    dataObjectAO.setAccessPermissionReadInAdminMode(group.getAdditionalInfo(), path, group.getGroupname());
                }
            }
            return true;
        }
        catch (JargonException e) {
            logger.error("Could not set read permission:", e);
        }
        return false;
    }

    @Override
    public boolean updateWritePermissions(DataGridGroup group, Map<String, Boolean> addCollectionsToWrite,
            Map<String, Boolean> removeCollectionsToWrite) throws DataGridConnectionRefusedException {

        DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
        CollectionAO collectionAO = irodsServices.getCollectionAO();

        try {
            for (String path : addCollectionsToWrite.keySet()) {
                if (collectionService.isCollection(path)) {
                    collectionAO.setAccessPermissionWriteAsAdmin(group.getAdditionalInfo(), path, group.getGroupname(),
                            addCollectionsToWrite.get(path));
                }
                else {
                    dataObjectAO.setAccessPermissionWriteInAdminMode(group.getAdditionalInfo(), path, group.getGroupname());
                }
            }
            for (String path : removeCollectionsToWrite.keySet()) {
                if (collectionService.isCollection(path)) {
                    collectionAO.removeAccessPermissionForUserAsAdmin(group.getAdditionalInfo(), path, group.getGroupname(),
                            removeCollectionsToWrite.get(path));
                }
                else {
                    dataObjectAO.removeAccessPermissionsForUserInAdminMode(group.getAdditionalInfo(), path, group.getGroupname());
                }
            }
            return true;
        }
        catch (JargonException e) {
            logger.error("Could not set read permission:", e);
        }
        return false;
    }

    @Override
    public boolean updateOwnership(DataGridGroup group, Map<String, Boolean> addCollectionsToOwn, Map<String, Boolean> removeCollectionsToOwn)
            throws DataGridConnectionRefusedException {

        DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
        CollectionAO collectionAO = irodsServices.getCollectionAO();
        try {
            for (String path : addCollectionsToOwn.keySet()) {
                if (collectionService.isCollection(path)) {
                    collectionAO.setAccessPermissionOwnAsAdmin(group.getAdditionalInfo(), path, group.getGroupname(), addCollectionsToOwn.get(path));
                }
                else {
                    dataObjectAO.setAccessPermissionWriteInAdminMode(group.getAdditionalInfo(), path, group.getGroupname());
                }
            }
            for (String path : removeCollectionsToOwn.keySet()) {
                if (collectionService.isCollection(path)) {
                    collectionAO.removeAccessPermissionForUserAsAdmin(group.getAdditionalInfo(), path, group.getGroupname(),
                            removeCollectionsToOwn.get(path));
                }
                else {
                    dataObjectAO.removeAccessPermissionsForUserInAdminMode(group.getAdditionalInfo(), path, group.getGroupname());
                }
            }
            return true;
        }
        catch (JargonException e) {
            logger.error("Could not set ownership:", e);
        }
        return false;
    }

    @Override
    public int countAll() {
        return groupDao.findAll(DataGridGroup.class).size();
    }

    @Override
    public String getGroupCollectionPath(String groupName) {
        if (groupName == null || groupName.isEmpty()) {
            return "";
        }

        return String.format("/%s/home/%s", zoneName, groupName);
    }

}
