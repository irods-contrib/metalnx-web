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

import com.emc.metalnx.core.domain.dao.GroupBookmarkDao;
import com.emc.metalnx.core.domain.dao.GroupDao;
import com.emc.metalnx.core.domain.dao.generic.GenericDaoImpl;
import com.emc.metalnx.core.domain.entity.DataGridGroup;
import com.emc.metalnx.core.domain.entity.DataGridGroupBookmark;

@Repository
@SuppressWarnings("unchecked")
public class GroupBookmarkDaoImpl extends GenericDaoImpl<DataGridGroupBookmark, Long> implements GroupBookmarkDao {

    private static final Logger logger = LoggerFactory.getLogger(GroupBookmarkDaoImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    GroupDao groupDao;

    @Override
    public Long addByGroupAndPath(DataGridGroup group, String path, boolean isCollection) {

        String parentPath = path.substring(0, path.lastIndexOf("/"));
        if (parentPath.isEmpty()) {
            parentPath = "/";
        }

        DataGridGroupBookmark bookmark = new DataGridGroupBookmark();
        bookmark.setGroup(group);
        bookmark.setPath(path);
        bookmark.setIsCollection(isCollection);
        bookmark.setCreateTs(new Date());
        bookmark.setIsNotified(false);
        return save(bookmark);
    }

    @Override
    public boolean removeByGroupAndPath(DataGridGroup group, String path) {

        boolean madeModifications = false;
        boolean operationResult = true;

        logger.info("Attempting to remove bookmark on {} from group {}", path, group.getGroupname());
        try {
            Iterator<DataGridGroupBookmark> it = group.getGroupBookmarks().iterator();
            while (it.hasNext()) {
                DataGridGroupBookmark bk = it.next();
                if (bk.getPath().compareTo(path) == 0) {
                    madeModifications = true;
                    it.remove();
                }
            }

            if (madeModifications) {
                logger.debug("Attempting to merge group entity [{}]", group.getId());
                groupDao.merge(group);
                logger.info("Successfully removed bookmark {} from group{}", path, group.getGroupname());
            }
        }
        catch (Exception e) {
            operationResult = false;
            logger.error("Could not remove bookmark on {} from group {}", path, group.getGroupname(), e);
        }

        return operationResult;
    }

    @Override
    public List<DataGridGroupBookmark> findByGroup(DataGridGroup group) {
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridGroupBookmark where group_id = :group_id");
        q.setLong("group_id", group.getId());
        return q.list();
    }

    @Override
    public List<DataGridGroupBookmark> findBookmarksByPath(String path) {
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridGroupBookmark where path = :path");
        q.setString("path", path);
        return q.list();
    }

    @Override
    public boolean removeByPath(String path) {
        logger.debug("Removing bookmarks by path: {} ", path);
        boolean removalSuccessful = false;

        try {
            List<DataGridGroupBookmark> bookmarks = findBookmarksByPath(path);
            Iterator<DataGridGroupBookmark> it = bookmarks.iterator();
            while (it.hasNext()) {
                DataGridGroupBookmark bookmark = it.next();
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

            List<DataGridGroupBookmark> bookmarks = q.list();

            Iterator<DataGridGroupBookmark> bookmarksIterator = bookmarks.iterator();
            while (bookmarksIterator.hasNext()) {
                DataGridGroupBookmark currBookmark = bookmarksIterator.next();
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
    public boolean removeByGroup(DataGridGroup group) {
        boolean operationResult = true;

        List<DataGridGroupBookmark> bookmarks = findByGroup(group);
        Iterator<DataGridGroupBookmark> it = bookmarks.iterator();
        while (it.hasNext()) {
            try {
                delete(it.next());
            }
            catch (Exception e) {
                operationResult = false;
            }
        }

        return operationResult;
    }

    @Override
    public List<DataGridGroupBookmark> findGroupBookmarksByGroupsIds(String[] groupIds, int offset, int limit, String searchString, String orderBy,
            String orderDir, boolean onlyCollections) {
        if (groupIds != null && groupIds.length > 0) {
            Long[] groupIdsLong = convertStringsToLongs(groupIds);
            String queryString = "select Dggb from DataGridGroupBookmark Dggb left join Dggb.group Dgg where Dgg.dataGridId in (:groupIds) and (Dggb.path LIKE :path or Dgg.groupname LIKE :groupname) ";
            if (onlyCollections) {
                queryString += "and Dggb.isCollection = true ";
            }
            queryString += "order by " + orderBy + " " + orderDir;
            Query q = sessionFactory.getCurrentSession().createQuery(queryString);
            q.setParameterList("groupIds", groupIdsLong);
            q.setString("path", '%' + searchString + '%');
            q.setString("groupname", '%' + searchString + '%');
            q.setFirstResult(offset);
            q.setMaxResults(limit);

            return q.list();
        }

        // If the input list is null, the method returns null
        return new ArrayList<DataGridGroupBookmark>();
    }

    @Override
    public Long countGroupBookmarksByGroupsIds(String[] groupIds) {
        if (groupIds != null && groupIds.length > 0) {
            Long[] groupIdsLong = convertStringsToLongs(groupIds);
            Query q = sessionFactory.getCurrentSession().createQuery(
                    "select count(*) from DataGridGroupBookmark Dggb left join Dggb.group Dgg where Dgg.dataGridId in (:groupIds)");
            q.setParameterList("groupIds", groupIdsLong);

            return (Long) q.uniqueResult();
        }
        return (long) 0;
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
		
        Query q = sessionFactory.getCurrentSession().createQuery("update DataGridGroupBookmark set path = :newPath where path = :oldPath");
        q.setString("newPath", newPath);
        q.setString("oldPath", oldPath);
        
        return q.executeUpdate() > 0;		
	}

    /**
     * Converts an array of strings into an array of longs.
     *
     * @param strArray
     *            array of strings to be converted
     * @return array of longs
     */
    private Long[] convertStringsToLongs(String[] strArray) {
        Long[] intArray = new Long[strArray.length];

        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Long.valueOf(strArray[i]);
        }

        return intArray;
    }

}
