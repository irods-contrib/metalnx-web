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
package com.emc.metalnx.core.domain.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.emc.metalnx.core.domain.dao.UserBookmarkDao;
import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.dao.generic.GenericDaoImpl;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.DataGridUserBookmark;

@Repository
@SuppressWarnings("unchecked")
public class UserBookmarkDaoImpl extends GenericDaoImpl<DataGridUserBookmark, Long> implements UserBookmarkDao {

    private static final Logger logger = LoggerFactory.getLogger(UserBookmarkDaoImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    UserDao userDao;

    @Override
    public DataGridUserBookmark findByUserAndPath(DataGridUser user, String path) {
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUserBookmark where user_id = :user_id and path = :path");
        q.setLong("user_id", user.getId());
        q.setString("path", path);
        return (DataGridUserBookmark) q.uniqueResult();
    }

    @Override
    public Long addByUserAndPath(DataGridUser user, String path, boolean isCollection) {

        String parentPath = path.substring(0, path.lastIndexOf("/"));
        if (parentPath.isEmpty()) {
            parentPath = "/";
        }

        String fileName = path != null ? path : "";
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());

        DataGridUserBookmark bookmark = new DataGridUserBookmark();
        bookmark.setUser(user);
        bookmark.setPath(path);
        bookmark.setName(fileName);
        bookmark.setCreateTs(new Date());
        bookmark.setIsNotified(false);
        bookmark.setIsCollection(isCollection);
        return save(bookmark);
    }

    @Override
    public boolean removeByUserAndPath(DataGridUser user, String path) {

        boolean operationResult = true;

        logger.info("Attempting to remove bookmark on {} from user {}", path, user.getUsername());
        try {
            DataGridUserBookmark bookmark = findByUserAndPath(user, path);
            delete(bookmark);
            logger.info("Successfully removed bookmark {} from user{}", path, user.getUsername());
        }
        catch (Exception e) {
            operationResult = false;
            logger.error("Could not remove bookmark on {} from user {}: {}", path, user.getUsername(), e.getMessage());
        }

        return operationResult;
    }

    @Override
    public List<DataGridUserBookmark> findByUser(DataGridUser user) {
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUserBookmark where user_id = :user_id");
        q.setLong("user_id", user.getId());
        return q.list();
    }

    @Override
    public List<DataGridUserBookmark> findBookmarksByPath(String path) {
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUserBookmark where path = :path");
        q.setString("path", path);
        return q.list();
    }

    @Override
    public boolean removeByPath(String path) {
        logger.debug("Removing bookmarks by path: {} ", path);
        boolean removalSuccessful = false;

        try {
            List<DataGridUserBookmark> bookmarks = findBookmarksByPath(path);
            Iterator<DataGridUserBookmark> it = bookmarks.iterator();
            while (it.hasNext()) {
                DataGridUserBookmark bookmark = it.next();
                logger.debug("Removing bookmark {} from database", bookmark.getPath());
                delete(bookmark);
            }

            removalSuccessful = true;
        }
        catch (Exception e) {
            logger.error("Could not remove bookmark for path {} ", path);
        }

        return removalSuccessful;
    }

    @Override
    public boolean removeByParentPath(String parentPath) {
        logger.debug("Removing bookmarks by relative path: {} ", parentPath);
        boolean removalSuccessful = false;

        try {
            Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUserBookmark where path LIKE :path");
            q.setString("path", parentPath + "%");

            List<DataGridUserBookmark> bookmarks = q.list();

            Iterator<DataGridUserBookmark> bookmarksIterator = bookmarks.iterator();
            while (bookmarksIterator.hasNext()) {
                DataGridUserBookmark currBookmark = bookmarksIterator.next();
                logger.debug("Removing relative bookmark {} from database", currBookmark.getPath());
                delete(currBookmark);
            }
        }
        catch (Exception e) {
            logger.error("Could not relative paths on bookmarks for path {} ", parentPath);
        }

        return removalSuccessful;
    }

    @Override
    public List<DataGridUserBookmark> findByUserPaginated(DataGridUser user, int start, int length, String searchString, String orderBy,
            String orderDir, boolean onlyCollections) {
        List<String> orderByList = new ArrayList<String>();
        orderByList.add(orderBy);

        List<String> orderDirList = new ArrayList<String>();
        orderDirList.add(orderDir);

        return this.findByUserPaginated(user, start, length, searchString, orderByList, orderDirList, onlyCollections);
    }

    @Override
    public List<DataGridUserBookmark> findByUserPaginated(DataGridUser user, int start, int length, String searchString, List<String> orderBy,
            List<String> orderDir, boolean onlyCollections) {

        if (orderBy.size() != orderDir.size()) {
            return null;
        }

        StringBuilder query = new StringBuilder();
        query.append("from DataGridUserBookmark ");
        query.append("where ");
        query.append("user_id = :user_id ");
        query.append("and ");
        query.append("path like :path ");
        if (onlyCollections) {
            query.append("and is_collection = true ");
        }
        query.append("order by ");

        for (int i = 0; i < orderBy.size(); i++) {
            query.append(orderBy.get(i));
            query.append(" ");
            query.append(orderDir.get(i));
            query.append(" ");
            if (i + 1 < orderBy.size()) {
                query.append(",");
            }
        }

        Query q = sessionFactory.getCurrentSession().createQuery(query.toString());
        q.setLong("user_id", user.getId());
        q.setString("path", '%' + searchString + '%');
        q.setFirstResult(start);
        q.setMaxResults(length);
        return q.list();
    }

    @Override
    public boolean removeByUser(DataGridUser user) {

        boolean operationResult = true;
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUserBookmark where user_id = :user_id");
        q.setLong("user_id", user.getId());
        List<DataGridUserBookmark> bookmarks = q.list();
        Iterator<DataGridUserBookmark> it = bookmarks.iterator();

        try {
            while (it.hasNext()) {
                delete(it.next());
            }
        }
        catch (Exception e) {
            operationResult = false;
        }

        return operationResult;
    }

	@Override
	public boolean updateBookmark(String oldPath, String newPath) {
		logger.debug("Updating bookmark");
		
		if (oldPath == null || newPath == null) {
			logger.debug("Could not update bookmark. Null values provided");
			return false;
		}
		
		if (oldPath.equals(newPath)) {
			logger.debug("Old bookmark is the same as the new one. No need for an update.");
			return false;
		}
		
		String newBookmarkName = newPath.substring(newPath.lastIndexOf("/") + 1, newPath.length());
        
        Query q = sessionFactory.getCurrentSession().createQuery("update DataGridUserBookmark set path = :newPath, name = :name where path = :oldPath");
        q.setString("newPath", newPath);
        q.setString("name", newBookmarkName);
        q.setString("oldPath", oldPath);
        return q.executeUpdate() > 0;		
	}
}
