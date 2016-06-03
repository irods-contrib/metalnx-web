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
package com.emc.metalnx.core.domain.dao;

import java.util.List;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.DataGridUserFavorite;

public interface FavoriteDao {

    /**
     * Retrieve a given Favorite based on the user and path
     *
     * @param user
     * @param path
     * @return a {@link DataGridUserFavorite}
     */
    DataGridUserFavorite findByUserAndPath(DataGridUser user, String path);

    /**
     * Add a favorite to a user using the path and user entity
     *
     * @param user
     *            data grid user to add the favorite to
     * @param path
     *            path to the collection/data object to be added as a favorite
     * @param isCollection
     *            indicates whether a path is a collection or a file
     * @return a confirmation that the insertion has been successfully
     */
    Long addByUserAndPath(DataGridUser user, String path, boolean isCollection);

    /**
     * Removes a favorite based on the path and the user
     *
     * @param user
     * @param path
     * @return a confirmation that the deletion has been successful
     */
    boolean removeByUserAndPath(DataGridUser user, String path);

    /**
     * Removes a favorite based on the user
     *
     * @param {@link DataGridUser} user
     * @return a confirmation that the deletion has been successful
     */
    boolean removeByUser(DataGridUser user);

    /**
     * Retrieve a given Favorite based on the user
     *
     * @param user
     * @return list of {@link DataGridUserFavorite}
     */
    List<DataGridUserFavorite> findByUser(DataGridUser user);

    /**
     * Retrieves all the favorites on a given path
     *
     * @param path
     * @return list of {@link DataGridUserFavorite}
     */
    List<DataGridUserFavorite> findFavoritesByPath(String path);

    /**
     * Removes a favorite based on the given path
     *
     * @param path
     *            path to remove any favorite
     * @return a confirmation that the deletion has been successful
     */
    boolean removeByPath(String path);

    /**
     * Removes all existing favorites whose parent path is the given path. Basically, if the
     * following favorites exist:
     * a/b/c
     * a/b/c/d
     * x/y/z/a/b/c
     * and the directory "a" gets deleted. Both "a/b/c" and "a/b/c/d" should be removed from
     * favorites since they no longer exist. But "x/y/z/a/b/c" should be kept.
     *
     * @param parentPath
     *            path to remove any favorite
     * @return a confirmation that the deletion has been successful
     */
    boolean removeByParentPath(String parentPath);

    /**
     * Find list of favorites paginated
     *
     * @param user
     * @param offset
     *            represents the starting row in the query
     * @param limit
     *            how many elements will have the result
     * @param searchString
     *            is different from null if filter is used
     * @param orderBy
     *            order by a column
     * @param orderDir
     *            the direction of the order can be 'desc' or 'asc'
     * @param onlyCollections
     *            indicates if results should contain only collections
     * @return list of {@link DataGridUserFavorite}
     */
    List<DataGridUserFavorite> findByUserPaginated(DataGridUser user, int offset, int limit, String searchString, String orderBy, String orderDir,
            boolean onlyCollections);

}
