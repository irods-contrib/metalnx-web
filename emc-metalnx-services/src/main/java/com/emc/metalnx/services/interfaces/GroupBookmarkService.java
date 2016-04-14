package com.emc.metalnx.services.interfaces;

import java.util.List;
import java.util.Set;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;

import com.emc.metalnx.core.domain.entity.DataGridGroup;
import com.emc.metalnx.core.domain.entity.DataGridGroupBookmark;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

public interface GroupBookmarkService {

    /**
     * Finds all groups a user belongs to and all bookmarks set for this group to have access to.
     *
     * @param user
     *            name of the user
     * @param additionalInfo
     *            zone name where the user is
     * @return true if the group was created successfully, false otherwise
     * @throws DataGridConnectionRefusedException
     * @throws JargonException
     * @throws DataNotFoundException
     */
    public List<DataGridGroup> getGroupsBookmarks(String user, String additionalInfo) throws DataGridConnectionRefusedException,
            DataNotFoundException, JargonException;

    /**
     * Updates the list of bookmarks on a group
     *
     * @param group
     * @param toAdd
     * @param toRemove
     * @return a confirmation that the operation has been successful
     */
    public boolean updateBookmarks(DataGridGroup group, Set<String> toAdd, Set<String> toRemove);

    /**
     * Lists all the bookmarks on a given path
     *
     * @param path
     * @return
     */
    public List<DataGridGroupBookmark> findBookmarksOnPath(String path);

    /**
     * Lists all the bookmarks on a given path for a given group as Strings
     *
     * @param group
     * @param parentPath
     * @return
     */
    public List<String> findBookmarksForGroupAsString(DataGridGroup group);

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
     * @param {@link DataGridGroup} group
     * @return True, if there was one or more bookmarks mathching the given path and they were
     *         removed successfully. False,
     *         otherwise.
     */
    public boolean removeBookmarkBasedOnGroup(DataGridGroup group);

    /**
     * Remove all bookmarks based on relative paths.
     *
     * @param path
     * @return True, if there was one or more bookmarks mathching the given path and they were
     *         removed successfully. False,
     *         otherwise.
     */
    public boolean removeBookmarkBasedOnRelativePath(String path);

    public List<DataGridGroupBookmark> getGroupsBookmarksPaginated(String user, String additionalInfo, int offset, int limit, String searchString,
            String orderBy, String orderDir, boolean onlyCollections) throws DataGridConnectionRefusedException, DataNotFoundException,
            JargonException;

    public Integer countTotalGroupBookmarks(String user, String additionalInfo) throws DataGridConnectionRefusedException, DataNotFoundException,
            JargonException;

}
