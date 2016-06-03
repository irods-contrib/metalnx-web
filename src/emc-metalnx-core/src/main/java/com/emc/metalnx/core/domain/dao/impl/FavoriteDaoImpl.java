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

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.emc.metalnx.core.domain.dao.FavoriteDao;
import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.dao.generic.GenericDaoImpl;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.DataGridUserFavorite;

@Repository
@SuppressWarnings("unchecked")
public class FavoriteDaoImpl extends GenericDaoImpl<DataGridUserFavorite, Long> implements FavoriteDao {

    private static final Logger logger = LoggerFactory.getLogger(FavoriteDaoImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    UserDao userDao;

    @Override
    public DataGridUserFavorite findByUserAndPath(DataGridUser user, String path) {
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUserFavorite where user_id = :user_id and path = :path");
        q.setLong("user_id", user.getId());
        q.setString("path", path);
        return (DataGridUserFavorite) q.uniqueResult();
    }

    @Override
    public Long addByUserAndPath(DataGridUser user, String path, boolean isCollection) {

        String parentPath = path.substring(0, path.lastIndexOf("/"));
        if (parentPath.isEmpty()) {
            parentPath = "/";
        }
        String fileName = path != null ? path.substring(path.lastIndexOf("/") + 1, path.length()) : "";

        DataGridUserFavorite favorite = new DataGridUserFavorite();
        favorite.setUser(user);
        favorite.setPath(path);
        favorite.setPathHash(path.hashCode());
        favorite.setName(fileName);
        favorite.setCreateTs(new Date());
        favorite.setIsCollection(isCollection);
        return save(favorite);
    }

    @Override
    public boolean removeByUserAndPath(DataGridUser user, String path) {

        boolean operationResult = true;

        logger.info("Attempting to remove favorite on {} from user {}", path, user.getUsername());
        try {
            DataGridUserFavorite favorite = findByUserAndPath(user, path);
            delete(favorite);
            logger.info("Successfully removed favorite {} from user{}", path, user.getUsername());
        }
        catch (Exception e) {
            operationResult = false;
            logger.error("Could not remove favorite on {} from user {}: {}", path, user.getUsername(), e.getMessage());
        }

        return operationResult;
    }

    @Override
    public List<DataGridUserFavorite> findByUser(DataGridUser user) {
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUserFavorite where user_id = :user_id");
        q.setLong("user_id", user.getId());
        return q.list();
    }

    @Override
    public List<DataGridUserFavorite> findByUserPaginated(DataGridUser user, int offset, int limit, String searchString, String orderBy,
            String orderDir, boolean onlyCollections) {
        String queryString = "from DataGridUserFavorite where user_id = :user_id and path like :path ";
        if (onlyCollections) {
            queryString += " and is_collection = true ";
        }
        queryString += " order by " + orderBy + " " + orderDir;
        Query q = sessionFactory.getCurrentSession().createQuery(queryString);
        q.setLong("user_id", user.getId());
        q.setString("path", '%' + searchString + '%');
        q.setFirstResult(offset);
        q.setMaxResults(limit);
        return q.list();
    }

    @Override
    public List<DataGridUserFavorite> findFavoritesByPath(String path) {
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUserFavorite where path = :path");
        q.setString("path", path);
        return q.list();
    }

    @Override
    public boolean removeByPath(String path) {
        logger.debug("Removing favorite by path: {} ", path);
        boolean removalSuccessful = false;

        try {
            List<DataGridUserFavorite> favorites = findFavoritesByPath(path);
            Iterator<DataGridUserFavorite> it = favorites.iterator();
            while (it.hasNext()) {
                DataGridUserFavorite favorite = it.next();
                logger.debug("Removing favorite {} from database", favorite.getPath());
                delete(favorite);
            }

            removalSuccessful = true;
        }
        catch (Exception e) {
            logger.error("Could not remove favorite for path {} ", path);
        }

        return removalSuccessful;
    }

    @Override
    public boolean removeByParentPath(String parentPath) {
        logger.debug("Removing favorite by relative path: {} ", parentPath);
        boolean removalSuccessful = false;

        try {
            Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUserFavorite where path LIKE :path");
            q.setString("path", parentPath + "%");

            List<DataGridUserFavorite> favorites = q.list();

            Iterator<DataGridUserFavorite> favoritesIterator = favorites.iterator();
            while (favoritesIterator.hasNext()) {
                DataGridUserFavorite currFavorite = favoritesIterator.next();
                logger.debug("Removing relative favorite {} from database", currFavorite.getPath());
                delete(currFavorite);
            }
        }
        catch (Exception e) {
            logger.error("Could not relative paths on favorite for path {} ", parentPath);
        }

        return removalSuccessful;
    }

    @Override
    public boolean removeByUser(DataGridUser user) {
        logger.debug("Removing favorite by user: {} ", user.getUsername());
        boolean removalSuccessful = false;

        try {
            List<DataGridUserFavorite> favorites = findByUser(user);
            Iterator<DataGridUserFavorite> it = favorites.iterator();
            while (it.hasNext()) {
                DataGridUserFavorite favorite = it.next();
                logger.debug("Removing favorite {} from database", favorite.getPath());
                delete(favorite);
            }

            removalSuccessful = true;
        }
        catch (Exception e) {
            logger.error("Could not remove favorite for user {} ", user.getUsername());
        }

        return removalSuccessful;
    }

}
