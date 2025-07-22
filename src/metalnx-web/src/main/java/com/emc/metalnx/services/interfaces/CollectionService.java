/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.interfaces;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.IRODSDomainObject;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.extensions.dataprofiler.DataProfile;
import org.irods.jargon.zipservice.api.exception.ZipServiceException;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.IconObject;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridDataNotFoundException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridQueryException;
import com.emc.metalnx.core.domain.exceptions.FileSizeTooLargeException;

public interface CollectionService {

	/**
	 * Verifies whether or not a file already exists in a collection
	 * 
	 * @param filename       name of the file to be checked
	 * @param collectionPath path to the collection where the file may or may not
	 *                       exist
	 * @return True, if a file with the exact same name is found in the collection.
	 *         False, otherwise.
	 * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the
	 *                                            data grid.
	 * @throws JargonException
	 */
	boolean isFileInCollection(String filename, String collectionPath)
			throws DataGridConnectionRefusedException, JargonException;

	/**
	 * Checks whether a path is valid in the grid or not.
	 *
	 * @param path file or collection path to be validated
	 * @return True, if the path exists in the grid (path is a file or collection).
	 *         False, otherwise.
	 * @throws JargonException
	 * @throws DataGridConnectionRefusedException
	 */
	boolean isPathValid(String path) throws DataGridConnectionRefusedException, JargonException;

	/**
	 * Checks whether or not a given path is a path for a collection.
	 *
	 * @param path
	 * @return True, if the given path is a collection path. False, otherwise.
	 * @throws DataGridException
	 * @throws JargonException
	 */
	boolean isCollection(String path) throws DataGridException, JargonException;

	/**
	 * Checks whether or not a given path is a path for a data object.
	 *
	 * @param path
	 * @return True, if the given path is a data object path. False, otherwise.
	 * @throws DataGridException
	 * @throws JargonException
	 */
	boolean isDataObject(String path) throws DataGridException, JargonException;

	/**
	 * Retrieves all collections and data objects that match a search term. All
	 * results are paginated. Any collection or data object where the term appears
	 * in the beginning, middle, and the end of its name will be retrieved.
	 *
	 * @param path        path that we will be looking for collections and data
	 *                    objects that match the search term
	 * @param searchText  term to be matched
	 * @param pageNum     the page the user is currently seeing
	 * @param pageSize    number of items shown by page
	 * @param pageContext object that contains information about the number of items
	 *                    diplayed and the total number of items found
	 * @return List of collections and data objects where the term appears.
	 * @throws DataGridDataNotFoundException
	 * @throws DataGridQueryException
	 * @throws DataGridException
	 */
	List<DataGridCollectionAndDataObject> getSubCollectionsAndDataObjectsUnderPathThatMatchSearchTextPaginated(
			String path, String searchText, int pageNum, int pageSize, int orderColumn, String orderDir,
			DataGridPageContext pageContext)
			throws DataGridDataNotFoundException, DataGridException, DataGridQueryException;

	/**
	 * Gets what kind of permission(s) the logged user has on a certain path
	 *
	 * @param path path to collection or data object in the data grid
	 * @return permission type (read, write, own)
	 * @throws DataGridConnectionRefusedException
	 */
	String

			getPermissionsForPath(String path) throws DataGridConnectionRefusedException;

	/**
	 * Gets the total number of replicas for a specific data object
	 *
	 * @param path path to the data object
	 * @return the total number of replicas for the data object given as a parameter
	 * @throws DataGridConnectionRefusedException
	 * @throws DataGridException
	 */
	int getTotalNumberOfReplsForDataObject(String path) throws DataGridException;

	/**
	 * Lists all replicas of a data object by resource.
	 *
	 * @param path path to the data object
	 * @return <DataObject, Resource> Map, containing the replica of the given path
	 *         and the resource where this replica is located
	 * @throws DataGridConnectionRefusedException
	 */
	Map<DataGridCollectionAndDataObject, DataGridResource> listReplicasByResource(String path)
			throws DataGridConnectionRefusedException;

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
	 * @param path path to the collection where we will retrieve sub collections and
	 *             data objects
	 * @return list of collections and data objects existing under a path
	 * @throws DataGridConnectionRefusedException
	 * @throws FileNotFoundException
	 * @throws JargonException
	 */
	List<DataGridCollectionAndDataObject> getSubCollectionsAndDataObjectsUnderPath(String path)
			throws DataGridConnectionRefusedException, FileNotFoundException, JargonException;

	/**
	 * Create a collection in iRODS
	 *
	 * @param newCollection
	 * @throws DataGridException
	 */
	boolean createCollection(DataGridCollectionAndDataObject newCollection) throws DataGridException;
	
	/**
	 * Create a collection in iRODS
	 *
	 * @param newCollection
	 * @param createParentCollections 
	 * @throws DataGridException
	 */
	boolean createCollection(DataGridCollectionAndDataObject newCollection, boolean createParentCollections) throws DataGridException;

	/**
	 * Find collections that match the parameter name
	 *
	 * @param name name that will be looked for in the grid
	 * @return CollectionAndDataObjectListingEntry List list of collections that
	 *         match a given name
	 * @throws DataGridConnectionRefusedException
	 */
	List<DataGridCollectionAndDataObject> searchCollectionAndDataObjectsByName(String name)
			throws DataGridConnectionRefusedException;

	/**
	 * Find collections and data objects that match the parameter name
	 *
	 * @param path path that will be looked for in the grid
	 * @return DataGridCollectionAndDataObject of the given path null if no
	 *         collections or data objects were found
	 * @throws DataGridException
	 * @throws FileNotFoundException
	 */
	DataGridCollectionAndDataObject findByName(String path) throws DataGridException, FileNotFoundException;

	/**
	 * Changes collection's name
	 *
	 * @param previousPath  collection path that will be modified
	 * @param newPath       new collection path where the previous collection path
	 *                      will be moved to
	 * @param inheritOption boolean that says if we need to enable inheritance on a
	 *                      collection or not
	 * @throws DataGridConnectionRefusedException
	 */
	boolean modifyCollectionAndDataObject(String previousPath, String newPath, boolean inheritOption)
			throws DataGridConnectionRefusedException;

	/**
	 * Lists all the collection that have read permissions on the specified path for
	 * the given user.
	 *
	 * @param path
	 * @param userName
	 * @return
	 * @throws DataGridConnectionRefusedException
	 */
	Set<String> listReadPermissionsForPathAndUser(String path, String userName)
			throws DataGridConnectionRefusedException;

	/**
	 * Lists all the collection that have write permissions on the specified path
	 * for the given user.
	 *
	 * @param path
	 * @param userName
	 * @return
	 * @throws DataGridConnectionRefusedException
	 */
	Set<String> listWritePermissionsForPathAndUser(String path, String userName)
			throws DataGridConnectionRefusedException;

	/**
	 * Lists all the collection that have ownership on the specified path for the
	 * given user.
	 *
	 * @param path
	 * @param userName
	 * @return
	 * @throws DataGridConnectionRefusedException
	 */
	Set<String> listOwnershipForPathAndUser(String path, String userName) throws DataGridConnectionRefusedException;

	/**
	 * Lists all the collection that have read permissions on the specified path for
	 * the given group.
	 *
	 * @param path
	 * @param groupName
	 * @return
	 * @throws DataGridConnectionRefusedException
	 */
	Set<String> listReadPermissionsForPathAndGroup(String path, String groupName)
			throws DataGridConnectionRefusedException;

	/**
	 * Lists all the collection that have write permissions on the specified path
	 * for the given group.
	 *
	 * @param path
	 * @param groupName
	 * @return
	 * @throws DataGridConnectionRefusedException
	 */
	Set<String> listWritePermissionsForPathAndGroup(String path, String groupName)
			throws DataGridConnectionRefusedException;

	/**
	 * Lists all the collection that have ownership on the specified path for the
	 * given group.
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
	 * @param path the parent path
	 * @return list of collections that have the inheritance option enabled
	 * @throws DataGridException {@link DataGridException}
	 */
	Set<String> listInheritanceForPath(String path) throws DataGridConnectionRefusedException, DataGridException;

	/**
	 * Update the inheritance options on collections
	 *
	 * @param toAdd    {@code Map} with the inheritance paths and options to add
	 * @param toRemove {@code Map} with the inheritance paths and options to remove
	 * @param zoneName {@code String} (blank if default} for which the operation is
	 *                 done
	 * @return confirmation {@code boolean} with the confirmation
	 * @throws DataGridConnectionRefusedException {@link DataGridConnectionRefusedException}
	 */
	boolean updateInheritanceOptions(Map<String, Boolean> toAdd, Map<String, Boolean> toRemove, String zoneName)
			throws DataGridConnectionRefusedException;

	/**
	 * Calculates all files existing in the data grid
	 *
	 * @return the number of existing collections
	 * @throws DataGridConnectionRefusedException {@link DataGridConnectionRefusedException}
	 */
	int countAll() throws DataGridConnectionRefusedException;

	/**
	 * Lists all the collection that have write permissions on the specified path
	 * recursively for the given group.
	 *
	 * @param path      {@code String} with the path to list permissions for
	 * @param groupName {@code String} with the group name
	 * @return {@code Set} of {@code String}
	 * @throws DataGridConnectionRefusedException {@link DataGridConnectionRefusedException}
	 * @throws JargonException                    {@link JargonException}
	 */
	Set<String> listWritePermissionsForPathAndGroupRecursive(String path, String groupName)
			throws DataGridConnectionRefusedException, JargonException;

	/**
	 * Prepares files to be downloaded by compressing them into a single file.
	 *
	 * @param paths array of strings that represent all paths that will be
	 *              downloaded
	 * @return Path to the compressed file, if any. Empty string, otherwise.
	 * @throws DataGridException {@link DataGridException}
	 * @throws JargonException   {@link JargonException}
	 */
	String prepareFilesForDownload(String[] paths) throws IOException, DataGridException, JargonException;

	/**
	 * Prepares files to be downloaded by compressing them into a single file.
	 *
	 * @param sourcePaths list of files to compress into a single file
	 * @return Path to the compressed file, if any. Empty string, otherwise.
	 * @throws FileSizeTooLargeException {@link FileSizeTooLargeException} when
	 *                                   bundle size exceeds max
	 * @throws IOException               {@link IOException}
	 * @throws DataGridException         {@link DataGridException}
	 * @throws ZipServiceException       {@link ZipServiceException}
	 * @throws JargonException           {@link JargonException}
	 * 
	 */
	String prepareFilesForDownload(List<String> sourcePaths)
			throws FileSizeTooLargeException, IOException, DataGridException, JargonException;

	/**
	 * Returns the inheritance option value for a given collection
	 *
	 * @param collPath
	 * @return the boolean
	 * @throws DataGridConnectionRefusedException {@link DataGridConnectionRefusedException}
	 * @throws JargonException                    {@link JargonException}
	 */
	boolean getInheritanceOptionForCollection(String collPath)
			throws DataGridConnectionRefusedException, JargonException;

	/**
	 * Gets the replica number of a collection or data object in the grid.
	 *
	 * @param path path to the collection/object
	 * @return int with the replica number 0, if path does not exist
	 * @throws DataGridConnectionRefusedException {@link DataGridConnectionRefusedException}
	 */
	int getReplicationNumber(String path) throws DataGridConnectionRefusedException;

	/**
	 * Gets the data grid checksum for a given path.
	 *
	 * @param path path to the collection/object in the grid
	 * @return String with the checksum
	 * @throws DataGridConnectionRefusedException {@link DataGridConnectionRefusedException}
	 */
	String getChecksum(String path) throws DataGridConnectionRefusedException;

	/**
	 * Maps a CollectionAndDataObjectListingEntry list into a
	 * DataGridCollectionAndDataObject list
	 *
	 * @param entries CollectionAndDataObjectListingEntry objects to map
	 * @return list of DataGridCollectionAndDataObject objects
	 */
	List<DataGridCollectionAndDataObject> mapListingEntryToDataGridCollectionAndDataObject(
			List<CollectionAndDataObjectListingEntry> entries);

	/**
	 * Maps a CollectionAndDataObjectListingEntry object into a
	 * DataGridCollectionAndDataObject object
	 *
	 * @param entry CollectionAndDataObjectListingEntry objects to map
	 * @return instance of {@link DataGridCollectionAndDataObject}
	 */
	DataGridCollectionAndDataObject mapListingEntryToDataGridCollectionAndDataObject(
			CollectionAndDataObjectListingEntry entry);

	/**
	 * Gets the public directory
	 *
	 * @return {@code Stringa] with the path to the the public directory
	 */
	String getHomeDirectyForPublic();

	/**
	 * Retrieve only the collections under a parent collection
	 *
	 * @param parent {@code String} with the parent path
	 * @return {@code List} of {@link DataGridCollectionAndDataObject}
	 * @throws DataGridConnectionRefusedException {@link DataGridConnectionRefusedException}
	 */
	List<DataGridCollectionAndDataObject> getSubCollectionsUnderPath(String parent)
			throws DataGridConnectionRefusedException;

	/**
	 * Get trash path related to the current path
	 * 
	 * @param path {@code String} with the path
	 * @return correspondent trash for given path
	 */
	String getTrashForPath(String path);

	IconObject getIcon(String mimeType);

	/**
	 * Retrieve a data profile for the path
	 * 
	 * @param path {@code String} with the path
	 * @return {@link DataProfile}
	 * @throws DataGridException     {@link DataGridException}
	 * @throws FileNotFoundException {@link FileNotFoundException}
	 */
	DataProfile<IRODSDomainObject> getCollectionDataProfile(String path)
			throws DataGridException, FileNotFoundException;

	/**
	 * Update the collection inheritance value
	 * 
	 * @param path          {@code String} with the collection path
	 * @param inheritOption {@code boolean} with the inheritance setting
	 * @param recursive     {@code boolean} indicating whether to apply the setting
	 *                      recursively
	 * @throws DataGridException {@link DataGridException}
	 */
	void modifyInheritance(String path, boolean inheritOption, boolean recursive) throws DataGridException;

	/**
	 * Handy method probes user access to a path
	 * 
	 * @param path {@code String} with the path to check access on
	 * @return {@code boolean} of {@code true} if the user has access
	 * @throws DataGridException {@link DataGridException}
	 */
	boolean canUserAccessThisPath(String path) throws DataGridException;

	/**
	 * Retrieve a data profile for the path using the admin account to serve as a
	 * metadata only proxy
	 * 
	 * @param path {@code String} with the path
	 * @return {@link DataProfile}
	 * @throws DataGridException {@link DataGridException}
	 */
	DataProfile<IRODSDomainObject> getCollectionDataProfileAsProxyAdmin(String path)
			throws FileNotFoundException, DataGridException;

}
