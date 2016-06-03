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
import java.util.List;
import java.util.Set;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.dao.GroupBookmarkDao;
import com.emc.metalnx.core.domain.dao.GroupDao;
import com.emc.metalnx.core.domain.entity.DataGridGroup;
import com.emc.metalnx.core.domain.entity.DataGridGroupBookmark;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.AdminServices;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.GroupBookmarkService;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserService;

@Service
@Transactional
public class GroupBookmarkServiceImpl implements GroupBookmarkService {

    @Autowired
    GroupDao groupDao;

    @Autowired
    GroupBookmarkDao groupBookmarkDao;

    @Autowired
    IRODSServices irodsServices;

    @Autowired
    AdminServices adminServices;

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    @Autowired
    CollectionService collectionService;

    private static final Logger logger = LoggerFactory.getLogger(GroupBookmarkServiceImpl.class);

    @Override
    public List<DataGridGroup> getGroupsBookmarks(String user, String additionalInfo) throws DataGridConnectionRefusedException,
            DataNotFoundException, JargonException {

        List<DataGridGroup> groups = new ArrayList<DataGridGroup>();

        if (user == null || additionalInfo == null || user.isEmpty() || additionalInfo.isEmpty()) {
            logger.error("Could not get groups bookmarks. Username or zone empty");
            return groups;
        }

        logger.info("Get groups bookmarks for {}", user);

        UserAO userAO = adminServices.getUserAO();
        User iRodsUser = userAO.findByName(user);

        if (iRodsUser != null) {
            String[] groupIds = userService.getGroupIdsForUser(user, additionalInfo);
            groups = groupService.findByDataGridIdList(groupIds);
        }

        return groups;
    }

    @Override
    public boolean updateBookmarks(DataGridGroup group, Set<String> toAdd, Set<String> toRemove) {

        boolean operationResult = true;

        try {
            if (toAdd != null) {
                for (String path : toAdd) {
                    if (!findBookmarksForGroupAsString(group).contains(path)) {
                        groupBookmarkDao.addByGroupAndPath(group, path, collectionService.isCollection(path));
                    }
                }
            }

            if (toRemove != null) {
                for (String path : toRemove) {
                    groupBookmarkDao.removeByGroupAndPath(group, path);
                }
            }
        }
        catch (Exception e) {
            operationResult = false;
            logger.error("Could not modify group bookmark for {}", group.getGroupname(), e);
        }

        return operationResult;
    }

    @Override
    public List<String> findBookmarksForGroupAsString(DataGridGroup group) {
        List<DataGridGroupBookmark> bookmarks = groupBookmarkDao.findByGroup(group);
        List<String> strings = new ArrayList<String>();

        for (DataGridGroupBookmark bookmark : bookmarks) {
            strings.add(bookmark.getPath());
        }

        Collections.sort(strings);

        return strings;
    }

    @Override
    public List<DataGridGroupBookmark> getGroupsBookmarksPaginated(String user, String additionalInfo, int offset, int limit, String searchString,
            String orderBy, String orderDir, boolean onlyCollections) throws DataGridConnectionRefusedException, DataNotFoundException,
            JargonException {

        List<DataGridGroupBookmark> groupBookmarks = new ArrayList<DataGridGroupBookmark>();

        if (user == null || additionalInfo == null || user.isEmpty() || additionalInfo.isEmpty()) {
            logger.error("Could not get groups bookmarks. Username or zone empty");
            return groupBookmarks;
        }

        logger.info("Get groups bookmarks for {}", user);

        UserAO userAO = adminServices.getUserAO();
        User iRodsUser = userAO.findByName(user);

        if (iRodsUser != null) {
            String[] groupIds = userService.getGroupIdsForUser(user, additionalInfo);
            groupBookmarks = groupBookmarkDao
                    .findGroupBookmarksByGroupsIds(groupIds, offset, limit, searchString, orderBy, orderDir, onlyCollections);
        }

        return groupBookmarks;
    }

    @Override
    public Integer countTotalGroupBookmarks(String user, String additionalInfo) throws DataGridConnectionRefusedException, DataNotFoundException,
            JargonException {
        Integer totalGroupBookmarks = 0;
        if (user == null || additionalInfo == null || user.isEmpty() || additionalInfo.isEmpty()) {
            logger.error("Could not get groups bookmarks. Username or zone empty");
            return 0;
        }

        logger.info("Get groups bookmarks for {}", user);

        UserAO userAO = adminServices.getUserAO();
        User iRodsUser = userAO.findByName(user);

        if (iRodsUser != null) {
            String[] groupIds = userService.getGroupIdsForUser(user, additionalInfo);
            totalGroupBookmarks = groupBookmarkDao.countGroupBookmarksByGroupsIds(groupIds).intValue();
        }
        return totalGroupBookmarks;
    }

    @Override
    public List<DataGridGroupBookmark> findBookmarksOnPath(String path) {
        return groupBookmarkDao.findBookmarksByPath(path);
    }

    @Override
    public boolean removeBookmarkBasedOnPath(String path) {
        return groupBookmarkDao.removeByPath(path);
    }

    @Override
    public boolean removeBookmarkBasedOnRelativePath(String path) {
        return groupBookmarkDao.removeByParentPath(path);
    }

    @Override
    public boolean removeBookmarkBasedOnGroup(DataGridGroup group) {
        return groupBookmarkDao.removeByGroup(group);
    }
    
    @Override
    public boolean updateBookmark(String oldPath, String newPath) {
    	return groupBookmarkDao.updateBookmark(oldPath, newPath);
    }

}
