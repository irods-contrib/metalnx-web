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
import com.emc.metalnx.core.domain.entity.DataGridUserBookmark;

public interface UserBookmarkService {

    /**
     * Updates the list of bookmarks on a user
     *
     * @param user
     * @param toAdd
     * @param toRemove
     * @return a confirmation that the operation has been successful
     */
    public boolean updateBookmarks(DataGridUser user, Set<String> toAdd, Set<String> toRemove);

    /**
     * Lists all the bookmarks on a given path
     *
     * @param path
     * @return
     */
    public List<DataGridUserBookmark> findBookmarksOnPath(String path);

    /**
     * Lists all the bookmarks on a given path for a given user as Strings
     *
     * @param user
     * @param parentPath
     * @return
     */
    public List<String> findBookmarksForUserAsString(DataGridUser user);

    /**
     * Remove any bookmark associated with the given path.
     *
     * @param path
     * @return True, if there was one or more bookmarks mathching the given path and they were
     *         removed successfully. False,
     *         otherwise.
     */
    public boolean removeBookmarkBasedOnPath(String path);

    /**
     * Remove any bookmark associated with the given user.
     *
     * @param {@link DataGridUser} user
     * @return True, if there was one or more bookmarks mathching the given user and they were
     *         removed successfully. False, otherwise.
     */
    public boolean removeBookmarkBasedOnUser(DataGridUser user);

    /**
     * Remove all bookmarks based on relative paths.
     *
     * @param path
     * @return True, if there was one or more bookmarks mathching the given path and they were
     *         removed successfully. False,
     *         otherwise.
     */
    public boolean removeBookmarkBasedOnRelativePath(String path);

    /**
     * Find list of bookmarks paginated
     *
     * @param user
     * @param start
     *            represents the starting row in the query
     * @param length
     *            how many elements will have the result
     * @param searchString
     *            is different from null if filter is used
     * @param orderBy
     *            order by a column
     * @param orderDir
     *            the direction of the order can be 'desc' or 'asc'
     * @param onlyCollections
     *            indicates if the results should contain only collections
     * @return list of {@link DataGridUserBookmark}
     */
    public List<DataGridUserBookmark> findBookmarksPaginated(DataGridUser user, int start, int length, String searchString, String orderBy,
            String orderDir, boolean onlyCollections);

    /**
     * Find list of bookmarks paginated
     *
     * @param user
     * @param start
     *            represents the starting row in the query
     * @param length
     *            how many elements will have the result
     * @param searchString
     *            is different from null if filter is used
     * @param orderBy
     *            list of columns the database will order the results by
     * @param orderDir
     *            list of directions to be applied to the columns in the order by clause (DESC or
     *            ASC)
     * @param onlyCollections
     *            indicates if the results should contain only collections
     * @return list of {@link DataGridUserBookmark}
     */
    public List<DataGridUserBookmark> findBookmarksPaginated(DataGridUser user, int start, int length, String searchString, List<String> orderBy,
            List<String> orderDir, boolean onlyCollections);

    /**
     * Updates an existing bookmark path to a new one.
     * 
     * @param oldPath
     * 			old path to be updated
     * @param newPath
     * 			new path value
     * @return True, if oldPath was successfully set to newPath. False, otherwise.
     */
	boolean updateBookmark(String oldPath, String newPath);
}
