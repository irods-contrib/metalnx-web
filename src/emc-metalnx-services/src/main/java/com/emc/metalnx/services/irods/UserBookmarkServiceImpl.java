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
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.dao.UserBookmarkDao;
import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.DataGridUserBookmark;
import com.emc.metalnx.services.interfaces.AdminServices;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserBookmarkService;
import com.emc.metalnx.services.interfaces.UserService;

@Service
@Transactional
public class UserBookmarkServiceImpl implements UserBookmarkService {

    @Autowired
    UserDao userDao;

    @Autowired
    UserBookmarkDao userBookmarkDao;

    @Autowired
    IRODSServices irodsServices;

    @Autowired
    AdminServices adminServices;

    @Autowired
    UserService userService;

    @Autowired
    CollectionService collectionService;

    private static final Logger logger = LoggerFactory.getLogger(UserBookmarkServiceImpl.class);

    @Override
    public boolean updateBookmarks(DataGridUser user, Set<String> toAdd, Set<String> toRemove) {

        boolean operationResult = true;

        try {
            if (toAdd != null) {
                for (String path : toAdd) {
                    if (!findBookmarksForUserAsString(user).contains(path)) {
                        userBookmarkDao.addByUserAndPath(user, path, collectionService.isCollection(path));
                    }
                }
            }

            if (toRemove != null) {
                for (String path : toRemove) {
                    userBookmarkDao.removeByUserAndPath(user, path);
                }
            }
        }
        catch (Exception e) {
            operationResult = false;
            logger.error("Could not modify user bookmark for {}", user.getUsername(), e);
        }

        return operationResult;
    }

    @Override
    public List<String> findBookmarksForUserAsString(DataGridUser user) {
        List<DataGridUserBookmark> bookmarks = userBookmarkDao.findByUser(user);
        List<String> strings = new ArrayList<String>();

        for (DataGridUserBookmark bookmark : bookmarks) {
            strings.add(bookmark.getPath());
        }

        return strings;
    }

    @Override
    public List<DataGridUserBookmark> findBookmarksOnPath(String path) {
        return userBookmarkDao.findBookmarksByPath(path);
    }

    @Override
    public boolean removeBookmarkBasedOnPath(String path) {
        return userBookmarkDao.removeByPath(path);
    }

    @Override
    public boolean removeBookmarkBasedOnRelativePath(String path) {
        return userBookmarkDao.removeByParentPath(path);
    }

    @Override
    public List<DataGridUserBookmark> findBookmarksPaginated(DataGridUser user, int start, int length, String searchString, String orderBy,
            String orderDir, boolean onlyCollections) {
        return userBookmarkDao.findByUserPaginated(user, start, length, searchString, orderBy, orderDir, onlyCollections);
    }

    @Override
    public List<DataGridUserBookmark> findBookmarksPaginated(DataGridUser user, int start, int length, String searchString, List<String> orderBy,
            List<String> orderDir, boolean onlyCollections) {
        return userBookmarkDao.findByUserPaginated(user, start, length, searchString, orderBy, orderDir, onlyCollections);
    }

    @Override
    public boolean removeBookmarkBasedOnUser(DataGridUser user) {
        return userBookmarkDao.removeByUser(user);
    }
    
    @Override
    public boolean updateBookmark(String oldPath, String newPath) {
    	return userBookmarkDao.updateBookmark(oldPath, newPath);
    }

}
