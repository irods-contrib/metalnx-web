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

package com.emc.metalnx.services.interfaces;

import java.util.List;
import java.util.Set;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.DataGridUserFavorite;

public interface FavoritesService {

    /**
     * Updates the favorites table for a user, whether be it to remove or add a path
     *
     * @param user
     * @param set
     *            of paths to be added
     * @param set
     *            of paths to be removed
     * @return True, if operation is successful. False, otherwise.
     */
    boolean updateFavorites(DataGridUser user, Set<String> toAdd, Set<String> toRemove);

    /**
     * Returns a list of strings with each of them representing a path marked as favorite
     *
     * @param user
     * @return List of paths marked as favorites by the user.
     */
    List<String> findFavoritesForUserAsString(DataGridUser user);

    /**
     * Removes path from database. This operation is used when the corresponding collection or file
     * is deleted from the grid
     *
     * @param path
     * @return True, if operation is successful. False, otherwise.
     */
    boolean removeFavoriteBasedOnPath(String path);

    /**
     * Removes path from database. This operation is used when the corresponding collection or file
     * is deleted from the grid
     *
     * @param {@link DataGridUser} user
     * @return True, if operation is successful. False, otherwise.
     */
    boolean removeFavoriteBasedOnUser(DataGridUser user);

    /**
     * Removes path from database. This operation is used when the corresponding collection or file
     * is deleted from the grid
     *
     * @param path
     * @return True, if operation is successful. False, otherwise.
     */
    boolean removeFavoriteBasedOnRelativePath(String path);

    /**
     * Checks whether the parameter path is a favorite for parameter user
     *
     * @param user
     * @param path
     * @return True, if path is a favorite for user. False, otherwise.
     */
    boolean isPathFavoriteForUser(DataGridUser user, String path);

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
     *            indicates if the results should contain only collections
     * @return list of {@link DataGridUserFavorite}
     */
    List<DataGridUserFavorite> findFavoritesPaginated(DataGridUser user, int offset, int limit, String searchString, String orderBy, String orderDir,
            boolean onlyCollections);

}
