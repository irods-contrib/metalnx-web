package com.emc.metalnx.core.domain.dao;

import java.util.List;

import com.emc.metalnx.core.domain.dao.generic.GenericDao;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.DataGridUserBookmark;

public interface UserBookmarkDao extends GenericDao<DataGridUserBookmark, Long> {

    /**
     * Add a bookmark to a user using the path and user entity
     *
     * @param user
     *            data grid user to add the bookmark to
     * @param path
     *            path to the collection/data object to be added as a bookmark
     * @param isCollection
     *            indicates whether a path is a collection or a file
     * @return a confirmation that the insertion has been successfully
     */
    public Long addByUserAndPath(DataGridUser user, String path, boolean isCollection);

    /**
     * Removes a bookmark based on the path and the user
     *
     * @param user
     * @param path
     * @return a confirmation that the deletion has been successful
     */
    public boolean removeByUserAndPath(DataGridUser user, String path);

    /**
     * Removes a bookmark based on the given path
     *
     * @param path
     *            path to remove any bookmark
     * @return a confirmation that the deletion has been successful
     */
    public boolean removeByPath(String path);

    /**
     * Removes a bookmark based on the given user
     *
     * @param path
     *            path to remove any bookmark
     * @return a confirmation that the deletion has been successful
     */
    public boolean removeByUser(DataGridUser user);

    /**
     * Removes all existing bookmarks whose parent path is the given path. Basically, if the
     * following bookmarks exist:
     * a/b/c
     * a/b/c/d
     * x/y/z/a/b/c
     * and the directory "a" gets deleted. Both "a/b/c" and "a/b/c/d" should be removed from
     * bookmarks since they no longer exist. But "x/y/z/a/b/c" should be kept.
     *
     * @param parentPath
     *            path to remove any bookmark
     * @return a confirmation that the deletion has been successful
     */
    public boolean removeByParentPath(String parentPath);

    /**
     * Retrieve a given UserBookmark based on the user and path
     *
     * @param user
     * @param path
     * @return a {@link DataGridUserBookmark}
     */
    public DataGridUserBookmark findByUserAndPath(DataGridUser user, String path);

    /**
     * Retrieve a given UserBookmark based on the user
     *
     * @param user
     * @return a {@link DataGridUserBookmark}
     */
    public List<DataGridUserBookmark> findByUser(DataGridUser user);

    /**
     * Retrieves all the bookmarks on a given path
     *
     * @param path
     * @return list of {@link DataGridUserBookmark}
     */
    public List<DataGridUserBookmark> findBookmarksByPath(String path);

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
    public List<DataGridUserBookmark> findByUserPaginated(DataGridUser user, int start, int length, String searchString, String orderBy,
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
     *            list of directions (ASC or DESC) that will applied to the columns in the order by
     *            clause
     * @param onlyCollections
     *            indicates if the results should contain only collections
     * @return list of {@link DataGridUserBookmark}
     */
    public List<DataGridUserBookmark> findByUserPaginated(DataGridUser user, int start, int length, String searchString, List<String> orderBy,
            List<String> orderDir, boolean onlyCollections);

}
