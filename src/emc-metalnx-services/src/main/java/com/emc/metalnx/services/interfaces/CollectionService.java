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

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridDataNotFoundException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridQueryException;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CollectionService {

    /**
     * Verifies whether or not a file already exists in a collection
     * @param filename name of the file to be checked
     * @param collectionPath path to the collection where the file may or may not exist
     * @return True, if a file with the exact same name is found in the collection. False, otherwise.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid.
     */
    boolean isFileInCollection(String filename, String collectionPath) throws DataGridConnectionRefusedException;

    /**
     * Checks whether a path is valid in the grid or not.
     *
     * @param path
     *            file or collection path to be validated
     * @return True, if the path exists in the grid (path is a file or collection). False, otherwise.
     * @throws DataGridConnectionRefusedException
     */
    boolean isPathValid(String path) throws DataGridConnectionRefusedException;

    /**
     * Checks whether or not a given path is a path for a collection.
     *
     * @param path
     * @return True, if the given path is a collection path. False, otherwise.
     * @throws DataGridConnectionRefusedException
     */
    boolean isCollection(String path) throws DataGridConnectionRefusedException;

    /**
     * Checks whether or not a given path is a path for a data object.
     *
     * @param path
     * @return True, if the given path is a data object path. False, otherwise.
     * @throws DataGridConnectionRefusedException
     */
    boolean isDataObject(String path) throws DataGridConnectionRefusedException;

    /**
     * Retrieves all collections and data objects that match a search term. All results are
     * paginated. Any collection or data object where the term appears in
     * the beginning, middle, and the end of its name will be retrieved.
     *
     * @param path
     *            path that we will be looking for collections and data objects that match the
     *            search
     *            term
     * @param searchText
     *            term to be matched
     * @param pageNum
     *            the page the user is currently seeing
     * @param pageSize
     *            number of items shown by page
     * @param pageContext
     *            object that contains information about the number of items diplayed and the total
     *            number of items found
     * @return List of collections and data objects where the term appears.
     * @throws DataGridDataNotFoundException
     * @throws DataGridQueryException
     * @throws DataGridException
     */
    List<DataGridCollectionAndDataObject> getSubCollectionsAndDataObjetsUnderPathThatMatchSearchTextPaginated(String path, String searchText,
            int pageNum, int pageSize, DataGridPageContext pageContext) throws DataGridDataNotFoundException, DataGridException,
            DataGridQueryException;

    /**
     * Gets what kind of permission(s) the logged user has on a certain path
     *
     * @param path
     *            path to collection or data object in the data grid
     * @return permission type (read, write, own)
     * @throws DataGridConnectionRefusedException
     */
    String getPermissionsForPath(String path) throws DataGridConnectionRefusedException;

    /**
     * Gets the total number of replicas for a specific data object
     *
     * @param path
     *            path to the data object
     * @return the total number of replicas for the data object given as a parameter
     * @throws DataGridConnectionRefusedException
     * @throws DataGridException
     */
    int getTotalNumberOfReplsForDataObject(String path) throws DataGridException;

    /**
     * Lists all replicas of a data object by resource.
     *
     * @param path
     *            path to the data object
     * @return <DataObject, Resource> Map, containing the replica of the given
     *         path and the resource where this replica is located
     * @throws DataGridConnectionRefusedException
     */
    Map<DataGridCollectionAndDataObject, DataGridResource> listReplicasByResource(String path) throws DataGridConnectionRefusedException;

    /**
     * Gets the current user's home directory
     *
     * @return string with the path to the the current user's home directory
     * @throws DataGridException
     */
    String getHomeDirectyForCurrentUser() throws DataGridException;

    /**
     * Gets all collections and data objects existing under a specific path
     *
     * @param path
     *            path to the collection where we will retrieve sub collections and data objects
     * @return list of collections and data objects existing under a path
     * @throws DataGridConnectionRefusedException
     */
    List<DataGridCollectionAndDataObject> getSubCollectionsAndDataObjetsUnderPath(String path) throws DataGridConnectionRefusedException;

    /**
     * Create a collection in iRODS
     *
     * @param newCollection
     * @throws DataGridException
     */
    boolean createCollection(DataGridCollectionAndDataObject newCollection) throws DataGridException;

    /**
     * Find collections that match the parameter name
     *
     * @param name
     *            name that will be looked for in the grid
     * @return CollectionAndDataObjectListingEntry List
     *         list of collections that match a given name
     * @throws DataGridConnectionRefusedException
     */
    List<DataGridCollectionAndDataObject> searchCollectionAndDataObjectsByName(String name) throws DataGridConnectionRefusedException;

    /**
     * Find collections and data objects that match the parameter name
     *
     * @param path
     *            path that will be looked for in the grid
     * @return DataGridCollectionAndDataObject of the given path
     *         null if no collections or data objects were found
     * @throws DataGridException
     */
    DataGridCollectionAndDataObject findByName(String path) throws DataGridException;

    /**
     * Changes collection's name
     *
     * @param previousPath
     *            collection path that will be modified
     * @param newPath
     *            new collection path where the previous collection path will be moved to
     * @param inheritOption
     *            boolean that says if we need to enable inheritance on a collection or not
     * @throws DataGridConnectionRefusedException
     */
    boolean modifyCollectionAndDataObject(String previousPath, String newPath, boolean inheritOption)
            throws DataGridConnectionRefusedException;

    /**
     * Lists all the collection that have read permissions on the specified path
     * for the given user.
     *
     * @param path
     * @param userName
     * @return
     * @throws DataGridConnectionRefusedException
     */
    Set<String> listReadPermissionsForPathAndUser(String path, String userName) throws DataGridConnectionRefusedException;

    /**
     * Lists all the collection that have write permissions on the specified path
     * for the given user.
     *
     * @param path
     * @param userName
     * @return
     * @throws DataGridConnectionRefusedException
     */
    Set<String> listWritePermissionsForPathAndUser(String path, String userName) throws DataGridConnectionRefusedException;

    /**
     * Lists all the collection that have ownership on the specified path
     * for the given user.
     *
     * @param path
     * @param userName
     * @return
     * @throws DataGridConnectionRefusedException
     */
    Set<String> listOwnershipForPathAndUser(String path, String userName) throws DataGridConnectionRefusedException;

    /**
     * Lists all the collection that have read permissions on the specified path
     * for the given group.
     *
     * @param path
     * @param groupName
     * @return
     * @throws DataGridConnectionRefusedException
     */
    Set<String> listReadPermissionsForPathAndGroup(String path, String groupName) throws DataGridConnectionRefusedException;

    /**
     * Lists all the collection that have write permissions on the specified path
     * for the given group.
     *
     * @param path
     * @param groupName
     * @return
     * @throws DataGridConnectionRefusedException
     */
    Set<String> listWritePermissionsForPathAndGroup(String path, String groupName) throws DataGridConnectionRefusedException;

    /**
     * Lists all the collection that have ownership on the specified path
     * for the given group.
     *
     * @param path
     * @param groupName
     * @return
     * @throws DataGridConnectionRefusedException
     */
    Set<String> listOwnershipForPathAndGroup(String path, String groupName) throws DataGridConnectionRefusedException;

    /**
     * List the collections that have inheritance enabled
     *
     * @param path
     *            the parent path
     * @return list of collections that have the inheritance option enabled
     * @throws DataGridConnectionRefusedException
     */
    Set<String> listInheritanceForPath(String path) throws DataGridConnectionRefusedException;

    /**
     * Update the inheritance options on collections
     *
     * @param toAdd
     * @param toRemove
     * @return confirmation
     * @throws DataGridConnectionRefusedException
     */
    boolean updateInheritanceOptions(Map<String, Boolean> toAdd, Map<String, Boolean> toRemove, String zoneName)
            throws DataGridConnectionRefusedException;

    /**
     * Calculates all files existing in the data grid
     *
     * @return the number of existing collections
     * @throws DataGridConnectionRefusedException
     */
    int countAll() throws DataGridConnectionRefusedException;

    /**
     * Lists all the collection that have write permissions on the specified path recursively
     * for the given group.
     *
     * @param path
     * @param groupName
     * @return
     * @throws DataGridConnectionRefusedException
     */
    Set<String> listWritePermissionsForPathAndGroupRecursive(String path, String groupName) throws DataGridConnectionRefusedException;

    /**
     * Prepares files to be downloaded by compressing them into a single file.
     *
     * @param sourcePaths
     *            list of files to compress into a single file
     * @return Path to the compressed file, if any. Empty string, otherwise.
     * @throws IOException
     * @throws DataGridException
     */
    String prepareFilesForDownload(List<String> sourcePaths) throws IOException, DataGridException;

    /**
     * Returns the inheritance option value for a given collection
     *
     * @param collPath
     * @return the boolean
     * @throws DataGridConnectionRefusedException
     */
    boolean getInheritanceOptionForCollection(String collPath) throws DataGridConnectionRefusedException;

    /**
     * Gets the replica number of a collection or data object in the grid.
     *
     * @param path
     *            path to the collection/object
     * @return int with the replica number
     *         0, if path does not exist
     * @throws DataGridConnectionRefusedException
     */
    int getReplicationNumber(String path) throws DataGridConnectionRefusedException;

    /**
     * Gets the data grid checksum for a given path.
     *
     * @param path
     *            path to the collection/object in the grid
     * @return String with the checksum
     * @throws DataGridConnectionRefusedException
     */
    String getChecksum(String path) throws DataGridConnectionRefusedException;

    /**
     * Maps a CollectionAndDataObjectListingEntry list into a DataGridCollectionAndDataObject list
     *
     * @param entries
     *            CollectionAndDataObjectListingEntry objects to map
     * @return list of DataGridCollectionAndDataObject objects
     */
    List<DataGridCollectionAndDataObject> mapListingEntryToDataGridCollectionAndDataObject(List<CollectionAndDataObjectListingEntry> entries);

    /**
     * Maps a CollectionAndDataObjectListingEntry object into a DataGridCollectionAndDataObject
     * object
     *
     * @param entry
     *            CollectionAndDataObjectListingEntry objects to map
     * @return instance of DataGridCollectionAndDataObject
     */
    DataGridCollectionAndDataObject mapListingEntryToDataGridCollectionAndDataObject(CollectionAndDataObjectListingEntry entry);

    /**
     * Gets the public directory
     *
     * @return string with the path to the the public directory
     */
    String getHomeDirectyForPublic();

    /**
     * Retrieve only the collections under a parent collection
     *
     * @param parent
     * @return
     * @throws DataGridConnectionRefusedException
     */
    List<DataGridCollectionAndDataObject> getSubCollectionsUnderPath(String parent) throws DataGridConnectionRefusedException;

    /**
     * Get trash path related to the current path
     * @param path
     * @return correspondent trash for given path
     * */
    String getTrashForPath(String path);

}
