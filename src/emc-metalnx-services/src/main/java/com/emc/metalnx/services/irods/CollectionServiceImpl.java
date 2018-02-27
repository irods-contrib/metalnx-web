/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.services.irods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.BulkFileOperationsAO;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.domain.ClientHints;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.ObjStat;
import org.irods.jargon.core.pub.domain.SpecificQueryDefinition;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridFilePermission;
import com.emc.metalnx.core.domain.entity.DataGridGroupPermission;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridUserPermission;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridDataNotFoundException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridQueryException;
import com.emc.metalnx.core.domain.exceptions.UnsupportedDataGridFeatureException;
import com.emc.metalnx.services.interfaces.AdminServices;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FileOperationService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.PermissionsService;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.irods.utils.SpecificQueryProvider;
import com.emc.metalnx.services.irods.utils.SpecificQueryProviderFactory;
import com.emc.metalnx.services.irods.utils.SpecificQueryProviderFactoryImpl;
import com.emc.metalnx.services.machine.util.DataGridUtils;

@Service
@Transactional
public class CollectionServiceImpl implements CollectionService {

	private static final String IRODS_PATH_SEPARATOR = "/";
	private static final String SQL_LIST_COLLS_MATCHING_SEARCH_TEXT_ALIAS_WITH_ORDERING = "metalnxListOfCollectionsThatMatchSearchTextWithOrdering";
	private static final String SQL_LIST_DATA_OBJECTS_MATCHING_SEARCH_TEXT_ALIAS_WITH_ORDERING = "metalnxListOfDataObjectsThatMatchSearchTextWithOrdering";
	private static final String SQL_TOTAL_NUMBER_OF_DATA_OBJECTS_MATCHING_SEARCH_TEXT_ALIAS = "metalnxTotalNumberOfDataObjectsThatMatchSearchText";
	private static final String SQL_TOTAL_NUMBER_OF_COLLS_MATCHING_SEARCH_TEXT_ALIAS = "metalnxTotalNumberOfCollectionsThatMatchSearchText";
	private static final int MAX_RESULTS_PER_PAGE = 200;
	private static final Logger logger = LoggerFactory.getLogger(CollectionServiceImpl.class);

	private SpecificQueryProviderFactory specificQueryProviderFactory = new SpecificQueryProviderFactoryImpl();

	@Autowired
	AdminServices adminServices;
	@Autowired
	IRODSServices irodsServices;
	@Autowired
	ResourceService resourceService;
	@Autowired
	PermissionsService permissionsService;
	@Autowired
	FileOperationService fileOperationService;

	@Override
	public boolean isFileInCollection(String filename, String collectionPath)
			throws DataGridConnectionRefusedException, JargonException {
		logger.info("isFileInCollection()");
		if (filename == null || collectionPath == null)
			return false;

		List<DataGridCollectionAndDataObject> items = getSubCollectionsAndDataObjectsUnderPath(collectionPath);

		for (DataGridCollectionAndDataObject i : items) {
			if (!i.isCollection() && filename.equals(i.getName()))
				return true;
		}

		return false;
	}

	@Override
	public boolean isPathValid(String path) throws DataGridConnectionRefusedException, JargonException {
		logger.info("isPathValid()");

		boolean isValid = false;

		try {
			irodsServices.getCollectionAndDataObjectListAndSearchAO().retrieveObjectStatForPath(path);
			isValid = true;
		} catch (FileNotFoundException fnf) {
			logger.warn("path not valid:{}", fnf);
		} catch (DataGridConnectionRefusedException | JargonException e1) {
			logger.error("error obtaining objStat for path:{}", path, e1);
			throw e1;
		}

		return isValid;
	}

	@Override
	public boolean isCollection(String path) throws DataGridConnectionRefusedException, JargonException {
		logger.info("isCollection()");

		if (path == null || path.isEmpty())
			return false;

		ObjStat objStat = irodsServices.getCollectionAndDataObjectListAndSearchAO().retrieveObjectStatForPath(path);
		return objStat.isSomeTypeOfCollection();

	}

	@Override
	public boolean isDataObject(String path) throws DataGridConnectionRefusedException, JargonException {
		logger.info("isDataObject()");

		return !isCollection(path);
	}

	@Override
	public List<DataGridCollectionAndDataObject> getSubCollectionsAndDataObjectsUnderPathThatMatchSearchTextPaginated(
			String parentPath, String searchText, int pageNum, int pageSize, int orderColumn, String orderDir,
			DataGridPageContext pageContext)
			throws DataGridDataNotFoundException, DataGridQueryException, DataGridException {

		logger.info("getSubCollectionsAndDataObjectsUnderPathThatMatchSearchTextPaginated()");

		List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = new ArrayList<>();

		List<DataGridCollectionAndDataObject> dataGridCollections = null;
		List<DataGridCollectionAndDataObject> dataGridObjects = null;

		int totalCollections = 0;
		int totalDataObjects = 0;
		int startIndex = (pageNum - 1) * pageSize;
		int endIndex = pageNum * pageSize - 1;
		int endIndexForDataObjs;
		int endIndexForCollections;

		try {
			totalCollections = getTotalCollectionsUnderPathThatMatchSearchText(parentPath, searchText);
			totalDataObjects = getTotalDataObjectsUnderPathThatMatchSearchText(parentPath, searchText);

			pageContext.setStartItemNumber(startIndex + 1);
			pageContext.setTotalNumberOfItems(totalCollections + totalDataObjects);

			if (endIndex + 1 <= totalCollections) {
				dataGridCollections = listCollectionsUnderPathThatMatchSearchText(parentPath, searchText, startIndex,
						pageSize, orderColumn, orderDir);
				int collectionEntriesSize = dataGridCollections.size();

				endIndexForCollections = collectionEntriesSize;

				dataGridCollectionAndDataObjects = dataGridCollections.subList(0, endIndexForCollections);

				pageContext.setEndItemNumber(pageContext.getStartItemNumber() + endIndexForCollections - 1);
			} else if (startIndex + 1 > totalCollections) {
				dataGridObjects = listDataObjectsUnderPathThatMatchSearchText(parentPath, searchText,
						startIndex - totalCollections, pageSize, orderColumn, orderDir);
				pageContext.setEndItemNumber(pageContext.getStartItemNumber() + dataGridObjects.size() - 1);

				dataGridCollectionAndDataObjects.addAll(dataGridObjects);
			} else {
				dataGridCollections = listCollectionsUnderPathThatMatchSearchText(parentPath, searchText, startIndex,
						pageSize, orderColumn, orderDir);
				endIndexForDataObjs = pageSize - totalCollections % pageSize;
				dataGridObjects = listDataObjectsUnderPathThatMatchSearchText(parentPath, searchText, 0,
						endIndexForDataObjs, orderColumn, orderDir);

				endIndexForDataObjs = endIndexForDataObjs > dataGridObjects.size() ? dataGridObjects.size()
						: endIndexForDataObjs;

				dataGridCollectionAndDataObjects.addAll(dataGridCollections);
				dataGridCollectionAndDataObjects.addAll(dataGridObjects);

				pageContext.setEndItemNumber(
						pageContext.getStartItemNumber() + endIndexForDataObjs + dataGridCollections.size() - 1);
			}
		} catch (DataNotFoundException e) {
			logger.error("Could not find items under path: {}", e.getMessage());
			throw new DataGridDataNotFoundException(e.getMessage());
		} catch (JargonQueryException e) {
			logger.error("Could not query catalog for items under path: {}", e.getMessage());
			throw new DataGridQueryException(e.getMessage());
		}
		return dataGridCollectionAndDataObjects;
	}

	@Override
	public String getPermissionsForPath(String path) throws DataGridConnectionRefusedException {

		logger.info("getPermissionsForPath()");

		FilePermissionEnum filePermissionEnum = null;
		String permissionType = "none";

		try {
			String user = irodsServices.getCurrentUser();
			String zone = irodsServices.getCurrentUserZone();
			Object obj = irodsServices.getCollectionAndDataObjectListAndSearchAO().getFullObjectForType(path);
			if (obj instanceof Collection) {
				CollectionAO collectionAO = irodsServices.getCollectionAO();
				filePermissionEnum = collectionAO.getPermissionForCollection(path, user, zone);
			} else {
				DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
				filePermissionEnum = dataObjectAO.getPermissionForDataObject(path, user, zone);
			}

			if (filePermissionEnum != null) {
				permissionType = filePermissionEnum.toString().toLowerCase();
			}

		} catch (FileNotFoundException e) {
			logger.error("Could not find path: {}", e.getMessage());
		} catch (JargonException e) {
			logger.error("Could not get permission for path: {}", e.getMessage());
		}

		return permissionType;
	}

	@Override
	public int getTotalNumberOfReplsForDataObject(String path) throws DataGridException {

		logger.info("getTotalNumberOfReplsForDataObject()");

		int totalNumberOfRepls = 0;

		if (path == null || path.isEmpty()) {
			return 0;
		}

		String parentPath = path.substring(0, path.lastIndexOf(IRODS_PATH_SEPARATOR));
		String dataObjectName = path.substring(path.lastIndexOf(IRODS_PATH_SEPARATOR), path.length());

		DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();

		// getting total number of replicas
		try {
			totalNumberOfRepls = dataObjectAO.getTotalNumberOfReplsForDataObject(parentPath, dataObjectName);
		} catch (JargonException e) {
			logger.error("Could not get number of replicas of a data obj: {}", e.getMessage());
			throw new DataGridException(e.getMessage());
		}

		return totalNumberOfRepls;
	}

	@Override
	public Map<DataGridCollectionAndDataObject, DataGridResource> listReplicasByResource(String path)
			throws DataGridConnectionRefusedException {

		logger.info("listReplicasByResource()");

		logger.info("Listing all replicas of " + path);
		Map<DataGridCollectionAndDataObject, DataGridResource> map = new HashMap<DataGridCollectionAndDataObject, DataGridResource>();

		String collectionAbsPath = null;
		String fileName = null;
		DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();

		try {
			collectionAbsPath = path.substring(0, path.lastIndexOf(IRODS_PATH_SEPARATOR));
			fileName = path.substring(path.lastIndexOf(IRODS_PATH_SEPARATOR) + 1, path.length());
			List<DataObject> replicas = dataObjectAO.listReplicationsForFile(collectionAbsPath, fileName);

			for (DataObject replica : replicas) {
				DataGridCollectionAndDataObject dataGridCollectionAndDataObject = DataGridUtils
						.getDataGridCollectionAndDataObject(replica);
				DataGridResource dataGridResource = resourceService.find(replica.getResourceName());
				map.put(dataGridCollectionAndDataObject, dataGridResource);
			}
		} catch (JargonException e) {
			logger.error("Could not list replicas by resource for " + path);
		}

		return DataGridUtils.sortReplicaResourceMap(map);
	}

	@Override
	public List<DataGridCollectionAndDataObject> getSubCollectionsAndDataObjectsUnderPath(String parent)
			throws DataGridConnectionRefusedException, JargonException {

		logger.info("getSubCollectionsAndDataObjectsUnderPath()");

		if (parent == null || parent.isEmpty()) {
			throw new IllegalArgumentException("null or empty parent");
		}

		logger.info("parent:{}", parent);

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices
				.getCollectionAndDataObjectListAndSearchAO();
		List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = null;

		try {
			List<CollectionAndDataObjectListingEntry> collectionAndDataObjects = collectionAndDataObjectListAndSearchAO
					.listDataObjectsAndCollectionsUnderPath(parent);
			dataGridCollectionAndDataObjects = new ArrayList<DataGridCollectionAndDataObject>();
			dataGridCollectionAndDataObjects = this
					.mapListingEntryToDataGridCollectionAndDataObject(collectionAndDataObjects);
			return dataGridCollectionAndDataObjects;

		} catch (FileNotFoundException e) {
			logger.error("Could not locate file: ", e);
			throw e;
		} catch (JargonException e) {
			logger.error("Error: ", e);
			throw e;
		}

	}

	@Override
	public List<DataGridCollectionAndDataObject> getSubCollectionsUnderPath(String parent)
			throws DataGridConnectionRefusedException {

		logger.info("getSubCollectionsUnderPath()");

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices
				.getCollectionAndDataObjectListAndSearchAO();
		List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = null;

		try {
			List<CollectionAndDataObjectListingEntry> collectionAndDataObjects = collectionAndDataObjectListAndSearchAO
					.listCollectionsUnderPath(parent, 0);
			dataGridCollectionAndDataObjects = this
					.mapListingEntryToDataGridCollectionAndDataObject(collectionAndDataObjects);
			return dataGridCollectionAndDataObjects;

		} catch (FileNotFoundException e) {
			logger.error("Could not locate file: ", e);
		} catch (JargonException e) {
			logger.error("Error: ", e);
		}

		return dataGridCollectionAndDataObjects;
	}

	@Override
	public boolean createCollection(DataGridCollectionAndDataObject collection) throws DataGridException {

		logger.info("createCollection()");

		boolean collCreated;

		try {
			IRODSFileSystemAO irodsFileSystemAO = irodsServices.getIRODSFileSystemAO();
			IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();

			IRODSFile newFile = irodsFileFactory.instanceIRODSFile(collection.getParentPath(), collection.getName());
			irodsFileSystemAO.mkdir(newFile, false);

			// Updating inheritance option on the collection, if needed
			CollectionAO collectionAO = irodsServices.getCollectionAO();
			String zoneName = irodsFileSystemAO.getIRODSServerProperties().getRodsZone();
			// enable inheritance for this collection
			if (collection.isInheritanceOption()) {
				collectionAO.setAccessPermissionInherit(zoneName, collection.getPath(), false);
			}
			// disable inheritance for this collection
			else if ("own".equals(getPermissionsForPath(collection.getParentPath()))) {
				collectionAO.setAccessPermissionToNotInherit(zoneName, collection.getPath(), false);
			}
			collCreated = true;
		} catch (JargonException e) {
			logger.debug("Could not create a collection in the data grid: {}", e.getMessage());
			throw new DataGridException(e.getMessage());
		}

		return collCreated;

	}

	@Override
	public List<DataGridCollectionAndDataObject> searchCollectionAndDataObjectsByName(String collectionName)
			throws DataGridConnectionRefusedException {

		logger.info("searchCollectionAndDataObjectsByName()");

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices
				.getCollectionAndDataObjectListAndSearchAO();
		List<CollectionAndDataObjectListingEntry> collectionAndDataObjects = null;
		List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = null;

		try {
			collectionAndDataObjects = collectionAndDataObjectListAndSearchAO
					.searchCollectionsAndDataObjectsBasedOnName(collectionName);
			dataGridCollectionAndDataObjects = this
					.mapListingEntryToDataGridCollectionAndDataObject(collectionAndDataObjects);
			return dataGridCollectionAndDataObjects;
		} catch (JargonException e) {
			logger.error("Could not search collections: {}", e.getMessage());
		}
		return null;
	}

	@Override
	public DataGridCollectionAndDataObject findByName(String path) throws FileNotFoundException, DataGridException {

		logger.info("findByName()");

		if (path == null || path.isEmpty()) {
			logger.info("Could not find collection or data object by name: path is null");
			return null;
		}

		logger.info("Find collection or data object by name: {}", path);

		CollectionAndDataObjectListAndSearchAO objectsAO = irodsServices.getCollectionAndDataObjectListAndSearchAO();
		DataGridCollectionAndDataObject dataGridCollectionAndDataObject;

		try {
			CollectionAndDataObjectListingEntry entry = objectsAO
					.getCollectionAndDataObjectListingEntryAtGivenAbsolutePathWithHeuristicPathGuessing(path);
			dataGridCollectionAndDataObject = this.mapListingEntryToDataGridCollectionAndDataObject(entry);
		} catch (FileNotFoundException fnf) {
			logger.warn("file not found for path:{}", path);
			throw fnf;
		} catch (JargonException e) {
			logger.debug("error finding collection/data object by name: {}", path);
			throw new DataGridException("Could not find path " + path);
		}

		return dataGridCollectionAndDataObject;
	}

	@Override
	public boolean modifyCollectionAndDataObject(String previousPath, String newPath, boolean inheritOption)
			throws DataGridConnectionRefusedException {

		logger.info("modifyCollectionAndDataObject()");

		IRODSFileSystemAO irodsFileSystemAO = irodsServices.getIRODSFileSystemAO();
		IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();
		CollectionAO collectionAO = irodsServices.getCollectionAO();

		logger.debug("Modify Collection/DataObject {} to {}", previousPath, newPath);
		try {

			logger.debug("Locating {} in iRODS", previousPath);
			IRODSFile previousFile = irodsFileFactory.instanceIRODSFile(previousPath);

			logger.info("Creating new IRODSFile instance for {}", newPath);
			IRODSFile newFile = irodsFileFactory.instanceIRODSFile(newPath);

			boolean isDirectory = irodsFileSystemAO.isDirectory(previousFile);
			if (previousPath.compareTo(newPath) != 0) {

				// checking if the path given is a collection
				if (isDirectory) {
					logger.debug("{} is a collection", previousPath);
					irodsFileSystemAO.renameDirectory(previousFile, newFile);
				}

				// the path given is a data object
				else {
					logger.debug("{} is a data object", previousPath);
					irodsFileSystemAO.renameFile(previousFile, newFile);
				}
			}

			// Updating inheritance option on the collection, if needed
			String zoneName = irodsFileSystemAO.getIRODSServerProperties().getRodsZone();
			if (isDirectory) {
				boolean isInheritanceSetForNewCollection = collectionAO
						.isCollectionSetForPermissionInheritance(newPath);

				if (inheritOption != isInheritanceSetForNewCollection) {
					// enable inheritance for this collection
					if (inheritOption) {
						logger.debug("Setting inheritance option on {}", newPath);
						collectionAO.setAccessPermissionInherit(zoneName, newPath, false);
					}
					// disable inheritance for this collection
					else {
						logger.debug("Removing inheritance setting on {}", newPath);
						collectionAO.setAccessPermissionToNotInherit(zoneName, newPath, false);
					}
				}
			}
			return true;
		} catch (JargonException e) {
			logger.error("Could not edit Collection/DataObject {} to {}: ", previousPath, newPath, e);
		}
		return false;
	}

	@Override
	public Set<String> listReadPermissionsForPathAndUser(String path, String userName)
			throws DataGridConnectionRefusedException {
		return listCollectionsWithPermissionsForUser(path, userName, FilePermissionEnum.READ);
	}

	@Override
	public Set<String> listWritePermissionsForPathAndUser(String path, String userName)
			throws DataGridConnectionRefusedException {
		return listCollectionsWithPermissionsForUser(path, userName, FilePermissionEnum.WRITE);
	}

	@Override
	public Set<String> listOwnershipForPathAndUser(String path, String userName)
			throws DataGridConnectionRefusedException {
		return listCollectionsWithPermissionsForUser(path, userName, FilePermissionEnum.OWN);
	}

	@Override
	public Set<String> listReadPermissionsForPathAndGroup(String path, String groupName)
			throws DataGridConnectionRefusedException {
		return listCollectionsWithPermissionsForGroup(path, groupName, FilePermissionEnum.READ);
	}

	@Override
	public Set<String> listWritePermissionsForPathAndGroup(String path, String groupName)
			throws DataGridConnectionRefusedException {
		return listCollectionsWithPermissionsForGroup(path, groupName, FilePermissionEnum.WRITE);
	}

	@Override
	public Set<String> listOwnershipForPathAndGroup(String path, String groupName)
			throws DataGridConnectionRefusedException {
		return listCollectionsWithPermissionsForGroup(path, groupName, FilePermissionEnum.OWN);
	}

	@Override
	public Set<String> listInheritanceForPath(String path) throws DataGridConnectionRefusedException {

		logger.info("listInheritanceForPath()");

		Set<String> collections = new HashSet<String>();

		CollectionAO collectionAO = irodsServices.getCollectionAO();
		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices
				.getCollectionAndDataObjectListAndSearchAO();

		try {
			List<CollectionAndDataObjectListingEntry> collList = collectionAndDataObjectListAndSearchAO
					.listCollectionsUnderPathWithPermissions(path, 0);
			for (CollectionAndDataObjectListingEntry collEntry : collList) {
				String currentCollPath = collEntry.getPathOrName();
				if (collectionAO.isCollectionSetForPermissionInheritance(currentCollPath)) {
					collections.add(currentCollPath);
				}
			}
			return collections;
		} catch (JargonException e) {
			logger.error("Could not get collections with inheritance option enabled: ", e);
		}
		return null;
	}

	@Override
	public boolean updateInheritanceOptions(Map<String, Boolean> toAdd, Map<String, Boolean> toRemove, String zoneName)
			throws DataGridConnectionRefusedException {

		logger.info("updateInheritanceOptions()");

		CollectionAO collectionAO = irodsServices.getCollectionAO();
		try {
			for (String addColl : toAdd.keySet()) {
				collectionAO.setAccessPermissionInheritAsAdmin(zoneName, addColl, toAdd.get(addColl));
			}
			for (String removeColl : toRemove.keySet()) {
				collectionAO.setAccessPermissionToNotInheritInAdminMode(zoneName, removeColl, toRemove.get(removeColl));
			}

			return true;
		} catch (JargonException e) {
			logger.error("Could not set inheritance: ", e);
		}

		return false;
	}

	@Override
	public int countAll() throws DataGridConnectionRefusedException {
		logger.info("countAll()");

		CollectionAO collectionAO = irodsServices.getCollectionAO();

		try {
			return collectionAO.countAllFilesUnderneathTheGivenCollection(IRODS_PATH_SEPARATOR);
		} catch (JargonException e) {
			logger.error("Could not count all files in the data grid: ", e);
		}

		return 0;
	}

	@Override
	public Set<String> listWritePermissionsForPathAndGroupRecursive(String path, String groupName)
			throws DataGridConnectionRefusedException, JargonException {

		logger.info("listWritePermissionsForPathAndGroupRecursive()");

		CollectionAO collectionAO = irodsServices.getCollectionAO();
		List<DataGridCollectionAndDataObject> children = getSubCollectionsAndDataObjectsUnderPath(path);
		Set<String> list = new HashSet<String>();

		for (DataGridCollectionAndDataObject child : children) {
			try {
				if (collectionAO.getPermissionForUserName(child.getPath(), groupName) != null) {
					list.add(child.getPath());
				} else {
					if (listWritePermissionsForPathAndGroupRecursive(child.getPath(), groupName).isEmpty()) {
						list.add(child.getPath());
					}
				}
			} catch (JargonException e) {
				logger.error("Could not list write permissions for path " + path + "and group: " + groupName, e);
			}
		}

		return list;
	}

	/*
	 * *****************************************************************************
	 * *************** ******************************** DATA TRANSFER OPERATIONS
	 * **********************************
	 * *****************************************************************************
	 * ***************
	 */

	@Override
	public String prepareFilesForDownload(String[] paths) throws IOException, DataGridException, JargonException {
		logger.info("prepareFilesForDownload()");

		return prepareFilesForDownload(Arrays.asList(paths));
	}

	@Override
	public String prepareFilesForDownload(List<String> sourcePaths)
			throws IOException, DataGridException, JargonException {
		logger.info("prepareFilesForDownload()");

		logger.info("Preparing files for download");
		if (sourcePaths == null || sourcePaths.isEmpty()) {
			return "";
		}

		Date currentDate = new Date();
		String tempCollectionName = "mlx_download_" + System.currentTimeMillis();
		String tempCollectionPath = getHomeDirectyForCurrentUser() + IRODS_PATH_SEPARATOR + tempCollectionName;
		String filePathToBeBundled = tempCollectionPath;
		String compressedFilePath = getHomeDirectyForCurrentUser() + IRODS_PATH_SEPARATOR + tempCollectionName + ".tar";
		String path = "";

		// if a single file was selected, it will be transferred directly
		// through the HTTP response
		if (sourcePaths.size() == 1 && isDataObject(sourcePaths.get(0))) {
			path = sourcePaths.get(0);
		}
		// if two or more files will be downloaded, then a compressed file needs
		// to be created in order to place all these files
		else {
			// creating temporary collection for download
			DataGridCollectionAndDataObject tempCollection = new DataGridCollectionAndDataObject(tempCollectionPath,
					getHomeDirectyForCurrentUser(), true);

			tempCollection.setCreatedAt(currentDate);
			tempCollection.setModifiedAt(currentDate);
			tempCollection.setInheritanceOption(false);

			logger.debug("Creating temporary collection for download");

			boolean isTempCollectionCreated = createCollection(tempCollection);

			if (isTempCollectionCreated && !sourcePaths.isEmpty()) {
				logger.info("Copying files to be downloaded to the temporary collection");

				// copying all files and collections to be downloaded to the temporary
				// collection
				fileOperationService.copy(sourcePaths, tempCollectionPath, false);

				// creating the compressed file (tar) into the temporary collection
				logger.info("Compressing temporary collection");
				compressTempFolderIntoDataGrid(filePathToBeBundled, compressedFilePath, "");
				path = compressedFilePath;
			}
		}

		return path;
	}

	@Override
	public boolean getInheritanceOptionForCollection(String collPath)
			throws DataGridConnectionRefusedException, JargonException {
		logger.info("getInheritanceOptionForCollection()");

		if (collPath == null || collPath.isEmpty()) {
			throw new IllegalArgumentException("null or empty collPath");
		}

		logger.info("collPath:{}", collPath);

		CollectionAO collectionAO = irodsServices.getCollectionAO();
		try {
			return collectionAO.isCollectionSetForPermissionInheritance(collPath);
		} catch (FileNotFoundException e) {
			logger.warn("Collection {} does not exist: {}", collPath, e.getMessage());
		} catch (JargonException e) {
			logger.error("Could not retrieve inheritance option value for", collPath, e.getMessage());
			throw e;
		}
		return false;
	}

	@Override
	public int getReplicationNumber(String path) throws DataGridConnectionRefusedException {
		logger.info("getReplicationNumber()");

		logger.debug("Getting replication number of " + path);

		DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
		DataObject dataObject = null;

		try {
			dataObject = dataObjectAO.findByAbsolutePath(path);
		} catch (JargonException e) {
			logger.error("Could not find a data object matching " + path, e);
		}

		if (dataObject != null) {
			return dataObject.getDataReplicationNumber();
		}

		return 0;
	}

	@Override
	public String getChecksum(String path) throws DataGridConnectionRefusedException {
		logger.info("getChecksum()");

		logger.debug("Getting checksum of " + path);
		DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
		DataObject dataObject = null;

		try {
			dataObject = dataObjectAO.findByAbsolutePath(path);
		} catch (JargonException e) {
			logger.error("Could not find a data object matching " + path, e);
		}

		if (dataObject != null) {
			return dataObject.getChecksum();
		}

		return new String();
	}

	@Override
	public List<DataGridCollectionAndDataObject> mapListingEntryToDataGridCollectionAndDataObject(
			List<CollectionAndDataObjectListingEntry> entries) {
		logger.info("mapListingEntryToDataGridCollectionAndDataObject()");

		logger.debug("Mapping a CollectionAndDataObjectListingEntry list into a DataGridCollectionAndDataObject list");

		List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = new ArrayList<DataGridCollectionAndDataObject>();
		for (CollectionAndDataObjectListingEntry entry : entries) {
			dataGridCollectionAndDataObjects.add(mapListingEntryToDataGridCollectionAndDataObject(entry));
		}
		return dataGridCollectionAndDataObjects;
	}

	@Override
	public DataGridCollectionAndDataObject mapListingEntryToDataGridCollectionAndDataObject(
			CollectionAndDataObjectListingEntry entry) {
		logger.info("mapListingEntryToDataGridCollectionAndDataObject()");

		logger.debug("Mapping a CollectionAndDataObjectListingEntry into a " + "DataGridCollectionAndDataObject");

		String entryPath = "";
		String entryName = "";
		DataGridCollectionAndDataObject dgObj = null;

		if (entry.isCollection()) {
			entryPath = entry.getPathOrName();
		} else {
			entryPath = entry.getParentPath() + IRODS_PATH_SEPARATOR + entry.getPathOrName();
			if (entry.getParentPath().compareTo(IRODS_PATH_SEPARATOR) == 0) {
				entryPath = IRODS_PATH_SEPARATOR + entry.getPathOrName();
			}
		}

		entryName = entry.getNodeLabelDisplayValue();
		if (entryName == null || entryName.isEmpty()) {
			entryName = entryPath;
		}

		dgObj = new DataGridCollectionAndDataObject(entryPath, entryName, entry.getParentPath(), entry.isCollection());
		dgObj.setCreatedAt(entry.getCreatedAt());
		dgObj.setModifiedAt(entry.getModifiedAt());
		dgObj.setOwner(entry.getOwnerName());
		dgObj.setProxy(entry.getObjectType() == ObjectType.COLLECTION_HEURISTIC_STANDIN);
		return dgObj;
	}

	@Override
	public String getHomeDirectyForCurrentUser() throws DataGridException {
		logger.debug("Getting current user's home directory");

		String currentUser = irodsServices.getCurrentUser();
		String zone = irodsServices.getCurrentUserZone();

		String homeDirectory = String.format("/%s/home/%s", zone, currentUser);
		return homeDirectory; // it's safe to say if you have a user account you have a home or something is
								// really wrong - mcc
	}

	@Override
	public String getHomeDirectyForPublic() {
		logger.debug("Getting public directory");
		return IRODS_PATH_SEPARATOR + irodsServices.getCurrentUserZone() + "/home/public";
	}

	/*
	 * *************************************************************************
	 * *************************** PRIVATE METHODS *****************************
	 * *************************************************************************
	 */

	/**
	 * Creates a tar file into iRODS based on the path given
	 *
	 * @param filePathToBeBundled
	 * @param bundleFilePathTobeCreated
	 * @param resource
	 * @return
	 * @throws DataGridConnectionRefusedException
	 * @throws JargonException
	 */
	private boolean compressTempFolderIntoDataGrid(String filePathToBeBundled, String bundleFilePathTobeCreated,
			String resource) throws DataGridConnectionRefusedException {

		logger.info("compressTempFolderIntoDataGrid()");

		boolean isZipFileCreatedSuccessfully = false;
		BulkFileOperationsAO bulkFileOperationsAO = null;

		try {
			bulkFileOperationsAO = irodsServices.getBulkFileOperationsAO();
			bulkFileOperationsAO.createABundleFromIrodsFilesAndStoreInIrodsWithForceOption(bundleFilePathTobeCreated,
					filePathToBeBundled, resource);
			isZipFileCreatedSuccessfully = true;
		} catch (JargonException e) {
			logger.error("Could not compress temporary folder: {}", e.getMessage());
		}

		return isZipFileCreatedSuccessfully;
	}

	/**
	 * Lists all collections under the given path that match a search term. Any
	 * collection where the term appears in the beginning, middle, and the end of
	 * its name will be retrieved.
	 *
	 * @param parentPath
	 *            path to the parent collection where you are looking for items that
	 *            match a search term
	 * @param searchText
	 *            term to be matched
	 * @param offset
	 *            partial start index
	 * @param limit
	 *            max number of items retrieved
	 * @return list of data objects that match the given search text
	 * @throws DataGridConnectionRefusedException
	 */
	private List<DataGridCollectionAndDataObject> listCollectionsUnderPathThatMatchSearchText(String parentPath,
			String searchText, int offset, int limit, int orderColumn, String orderDir)
			throws DataGridConnectionRefusedException {

		logger.info("listCollectionsUnderPathThatMatchSearchText()");
		logger.info("parentPath:{}", parentPath);
		logger.info("searchText:{}", searchText);
		logger.info("offset:{}", offset);
		logger.info("limit:{}", limit);
		logger.info("orderColumn:{}", orderColumn);
		logger.info("orderDir:{}", orderDir);

		SpecificQueryAO specificQueryAO = adminServices.getSpecificQueryAO();

		SpecificQueryDefinition queryDef = null;
		List<CollectionAndDataObjectListingEntry> itemsListingEntries = null;
		List<DataGridCollectionAndDataObject> dataGridItemsList = null;
		String sqlQueryAlias = "";
		try {
			sqlQueryAlias = SQL_LIST_COLLS_MATCHING_SEARCH_TEXT_ALIAS_WITH_ORDERING;

			ClientHints clientHints = this.irodsServices.getEnvironmentalInfoAO().retrieveClientHints(false);
			SpecificQueryProvider provider = specificQueryProviderFactory.instance(clientHints.whatTypeOfIcatIsIt());
			String query = provider.buildSelectCollectionsUnderPathThatMatchSearchText(parentPath, searchText, offset,
					limit, orderColumn, orderDir);

			// Creating Specific Query instance
			queryDef = new SpecificQueryDefinition();
			queryDef.setAlias(sqlQueryAlias);
			queryDef.setSql(query);

			// Creating spec query on iRODS
			specificQueryAO.addSpecificQuery(queryDef);

			// Executing specific query
			String zone = irodsServices.getCurrentUserZone();

			List<String> args = new ArrayList<String>();
			String collNameParam = null;
			if (IRODS_PATH_SEPARATOR.equals(parentPath)) {
				collNameParam = String.format("%%%s%%%%%s%%", IRODS_PATH_SEPARATOR, searchText);
			} else {
				collNameParam = String.format("%s%s%%%s%%", parentPath, IRODS_PATH_SEPARATOR, searchText);
			}
			args.add(collNameParam);
			args.add(parentPath);
			args.add(String.valueOf(offset));
			args.add(String.valueOf(limit));

			SpecificQuery specQuery = SpecificQuery.instanceArguments(sqlQueryAlias, args, 0, zone);
			logger.debug("specificQuery for text search:{}", specQuery);

			SpecificQueryResultSet queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery,
					MAX_RESULTS_PER_PAGE, 0);

			// Mapping spec query results to DataGrid* objects
			itemsListingEntries = DataGridUtils.mapCollectionQueryResultSetToDataGridObjects(queryResultSet);
			dataGridItemsList = DataGridUtils.mapListingEntryToDataGridCollectionAndDataObject(itemsListingEntries);
		} catch (JargonException | JargonQueryException | UnsupportedDataGridFeatureException e) {
			logger.error("Could not execute specific query to find collections matching a search text. ", e);
		} finally {
			try {
				// after running the user specific query, we need to remove from the database
				specificQueryAO.removeSpecificQueryByAlias(sqlQueryAlias);
			} catch (JargonException e) {
				logger.error("Could not remove specific query {}: ", sqlQueryAlias, e.getMessage());
			}
		}

		return dataGridItemsList;
	}

	/**
	 * Lists all data objects under the given path that match a search term. Any
	 * data object where the term appears in the beginning, middle, and the end of
	 * its name will be retrieved.
	 *
	 * @param parentPath
	 *            path to the parent collection where you are looking for items that
	 *            match a search term
	 * @param searchText
	 *            term to be matched
	 * @param offset
	 *            partial start index
	 * @param limit
	 *            max number of items retrieved
	 * @return list of data objects that match the given search text
	 * @throws DataGridConnectionRefusedException
	 */
	private List<DataGridCollectionAndDataObject> listDataObjectsUnderPathThatMatchSearchText(String parentPath,
			String searchText, int offset, int limit, int orderColumn, String orderDir)
			throws DataNotFoundException, JargonQueryException, DataGridConnectionRefusedException {

		logger.info("listDataObjectsUnderPathThatMatchSearchText()");

		logger.info("parentPath:{}", parentPath);
		logger.info("searchText:{}", searchText);
		logger.info("offset:{}", offset);
		logger.info("limit:{}", limit);
		logger.info("orderColumn:{}", orderColumn);
		logger.info("orderDir:{}", orderDir);

		SpecificQueryAO specificQueryAO = adminServices.getSpecificQueryAO();

		SpecificQueryDefinition queryDef = null;
		List<CollectionAndDataObjectListingEntry> dataGridList = null;
		List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = null;
		String sqlAlias = null;

		try {
			dataGridCollectionAndDataObjects = new ArrayList<DataGridCollectionAndDataObject>();
			sqlAlias = SQL_LIST_DATA_OBJECTS_MATCHING_SEARCH_TEXT_ALIAS_WITH_ORDERING;

			ClientHints clientHints = this.irodsServices.getEnvironmentalInfoAO().retrieveClientHints(false);
			SpecificQueryProvider provider = specificQueryProviderFactory.instance(clientHints.whatTypeOfIcatIsIt());
			String query = provider.buildSelectDataObjectsUnderPathThatMatchSearchText(parentPath, searchText, offset,
					limit, orderColumn, orderDir);

			// Creating Specific Query instance
			queryDef = new SpecificQueryDefinition();
			queryDef.setAlias(sqlAlias);
			queryDef.setSql(query.toString());

			// Creating spec query on iRODS
			specificQueryAO.addSpecificQuery(queryDef);

			// Executing specific query
			String zone = irodsServices.getCurrentUserZone();

			List<String> args = new ArrayList<String>();
			args.add(parentPath);
			args.add("%" + searchText + "%");
			args.add(String.valueOf(offset));
			args.add(String.valueOf(limit));

			SpecificQuery specQuery = SpecificQuery.instanceArguments(sqlAlias, args, 0, zone);
			SpecificQueryResultSet queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery,
					MAX_RESULTS_PER_PAGE, 0);

			// Mapping spec query results to DataGrid* objects
			dataGridList = DataGridUtils.mapQueryResultSetToDataGridObjectsForSearch(queryResultSet);
			dataGridCollectionAndDataObjects
					.addAll(DataGridUtils.mapListingEntryToDataGridCollectionAndDataObject(dataGridList));
		} catch (JargonException | UnsupportedDataGridFeatureException e) {
			logger.error("Could not execute specific query for listing data objects that match a search text", e);
		} finally {
			try {
				// after running the user specific query, we need to remove from the database
				specificQueryAO.removeSpecificQueryByAlias(sqlAlias);
			} catch (JargonException e) {
				logger.error("Could not remove specific query {}: ", sqlAlias, e.getMessage());
			}
		}

		return dataGridCollectionAndDataObjects;
	}

	/**
	 * Calculates the number of collections that match the given search term.
	 *
	 * @param parentPath
	 *            path to the parent collection where you are looking for items that
	 *            match a search term
	 * @param searchText
	 *            term to be matched
	 * @return total number of collections matching the given search text
	 * @throws DataGridConnectionRefusedException
	 * @throws JargonException
	 * @throws DuplicateDataException
	 * @throws JargonQueryException
	 */
	private int getTotalCollectionsUnderPathThatMatchSearchText(String parentPath, String searchText)
			throws DataGridConnectionRefusedException {

		logger.info("getTotalCollectionsUnderPathThatMatchSearchText()");

		SpecificQueryAO specificQueryAO = adminServices.getSpecificQueryAO();

		int totalNumberOfItems = 0;
		SpecificQueryDefinition queryDef = null;
		String sqlQueryAlias = "totalNumberOfCollectionsThatMatchSearchText";

		try {
			sqlQueryAlias = SQL_TOTAL_NUMBER_OF_COLLS_MATCHING_SEARCH_TEXT_ALIAS;

			ClientHints clientHints = this.irodsServices.getEnvironmentalInfoAO().retrieveClientHints(false);
			SpecificQueryProvider provider = specificQueryProviderFactory.instance(clientHints.whatTypeOfIcatIsIt());
			String query = provider.buildSelectTotalCollectionsUnderPathThatMatchSearchText(parentPath, searchText);

			// Creating Specific Query instance
			queryDef = new SpecificQueryDefinition();
			queryDef.setAlias(sqlQueryAlias);
			queryDef.setSql(query);

			// Creating spec query on iRODS
			specificQueryAO.addSpecificQuery(queryDef);

			// Executing specific query
			String zone = irodsServices.getCurrentUserZone();

			List<String> args = new ArrayList<String>();
			String collNameParam = null;
			if (IRODS_PATH_SEPARATOR.equals(parentPath)) {
				collNameParam = String.format("%%%s%%%%%s%%", IRODS_PATH_SEPARATOR, searchText);
			} else {
				collNameParam = String.format("%s%s%%%s%%", parentPath, IRODS_PATH_SEPARATOR, searchText);
			}
			args.add(collNameParam);
			args.add(parentPath);

			SpecificQuery specQuery = SpecificQuery.instanceArguments(sqlQueryAlias, args, 0, zone);
			SpecificQueryResultSet queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery,
					MAX_RESULTS_PER_PAGE, 0);
			totalNumberOfItems = DataGridUtils.mapCountQueryResultSetToInteger(queryResultSet);
		} catch (JargonException | JargonQueryException | UnsupportedDataGridFeatureException e) {
			logger.error("Could not execute specific query to find collections matching a search text. ", e);
		} finally {
			try {
				// after running the user specific query, we need to remove from the database
				specificQueryAO.removeSpecificQueryByAlias(sqlQueryAlias);
			} catch (JargonException e) {
				logger.error("Could not remove specific query {}: ", sqlQueryAlias, e.getMessage());
			}
		}

		return totalNumberOfItems;
	}

	/**
	 * Calculates the number of data objects that match the given search term.
	 *
	 * @param parentPath
	 *            path to the parent collection where you are looking for items that
	 *            match a search term
	 * @param searchText
	 *            term to be matched
	 * @return total number of collections matching the given search text
	 * @throws DataGridConnectionRefusedException
	 */
	private int getTotalDataObjectsUnderPathThatMatchSearchText(String parentPath, String searchText)
			throws DataNotFoundException, JargonQueryException, DataGridConnectionRefusedException {

		logger.info("getTotalCollectionsUnderPathThatMatchSearchText()");

		logger.info("parentPath:{}", parentPath);
		logger.info("searchText:{}", searchText);

		SpecificQueryAO specificQueryAO = adminServices.getSpecificQueryAO();

		SpecificQueryDefinition queryDef = null;
		String sqlAlias = null;
		int totalNumberOfItems = 0;

		try {
			sqlAlias = SQL_TOTAL_NUMBER_OF_DATA_OBJECTS_MATCHING_SEARCH_TEXT_ALIAS;
			ClientHints clientHints = this.irodsServices.getEnvironmentalInfoAO().retrieveClientHints(false);
			SpecificQueryProvider provider = specificQueryProviderFactory.instance(clientHints.whatTypeOfIcatIsIt());
			String query = provider.buildSelectTotalDataObjectsUnderPathThatMatchSearchText(parentPath, searchText);

			// Creating Specific Query instance
			queryDef = new SpecificQueryDefinition();
			queryDef.setAlias(sqlAlias);
			queryDef.setSql(query.toString());

			// Creating spec query on iRODS
			specificQueryAO.addSpecificQuery(queryDef);

			// Executing specific query
			String zone = irodsServices.getCurrentUserZone();

			List<String> args = new ArrayList<String>();
			args.add(parentPath);
			args.add("%" + searchText + "%");

			SpecificQuery specQuery = SpecificQuery.instanceArguments(sqlAlias, args, 0, zone);
			SpecificQueryResultSet queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery,
					MAX_RESULTS_PER_PAGE, 0);

			// Mapping spec query results to DataGrid* objects
			totalNumberOfItems = DataGridUtils.mapCountQueryResultSetToInteger(queryResultSet);
		} catch (JargonException | UnsupportedDataGridFeatureException e) {
			logger.error(
					"Could not execute specific query to get the total number of data objects matching a search text.",
					e);
		} finally {
			try {
				// after running the user specific query, we need to remove from the database
				specificQueryAO.removeSpecificQueryByAlias(sqlAlias);
			} catch (JargonException e) {
				logger.error("Could not remove specific query {}: ", sqlAlias, e.getMessage());
			}
		}

		return totalNumberOfItems;
	}

	private Set<String> listCollectionsWithPermissionsForUser(String path, String entityName,
			FilePermissionEnum permissionType) throws DataGridConnectionRefusedException {
		logger.info("listCollectionsWithPermissionsForUser()");

		return listCollectionsWithPermissionsForEntity(path, entityName, permissionType, "user");
	}

	private Set<String> listCollectionsWithPermissionsForGroup(String path, String entityName,
			FilePermissionEnum permissionType) throws DataGridConnectionRefusedException {
		logger.info("listCollectionsWithPermissionsForGroup()");

		return listCollectionsWithPermissionsForEntity(path, entityName, permissionType, "group");
	}

	/**
	 * Auxiliary method that filters the directories with the specified permission
	 * level for the given entity, that can be an user or a group
	 *
	 * @param path
	 *            The path for which we would like to list the objects with
	 *            permissions
	 * @param entityName
	 *            The entity name
	 * @param permissionType
	 *            The permission stype
	 * @param entityType
	 *            The entity type that can be user or group
	 * @return
	 * @throws DataGridConnectionRefusedException
	 */
	private Set<String> listCollectionsWithPermissionsForEntity(String path, String entityName,
			FilePermissionEnum permissionType, String entityType) throws DataGridConnectionRefusedException {

		logger.info("listCollectionsWithPermissionsForEntity()");

		Set<String> collections = new HashSet<String>();

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices
				.getCollectionAndDataObjectListAndSearchAO();

		try {
			List<CollectionAndDataObjectListingEntry> collList = collectionAndDataObjectListAndSearchAO
					.listDataObjectsAndCollectionsUnderPath(path);

			logger.info("Getting permissions and bookmarks for path {}", path);
			for (CollectionAndDataObjectListingEntry collEntry : collList) {
				String filePath = "";

				if (collEntry.isCollection()) {
					filePath = collEntry.getPathOrName();
				} else {
					filePath = collEntry.getParentPath() + IRODS_PATH_SEPARATOR + collEntry.getPathOrName();
					if (collEntry.getParentPath().compareTo(IRODS_PATH_SEPARATOR) == 0) {
						filePath = collEntry.getParentPath() + collEntry.getPathOrName();
					}
				}

				List<DataGridFilePermission> permissions = permissionsService.getPathPermissionDetails(filePath,
						entityName);

				// Deciding whether we should get permissions for users or groups
				if (entityType.compareTo("user") == 0) {

					// Filtering permissions for users
					List<DataGridUserPermission> userPermissions = permissionsService
							.getUsersWithPermissions(permissions);

					// Adding collections with permissions into the result list
					for (DataGridUserPermission dataGridUserPermission : userPermissions) {
						if (dataGridUserPermission.getPermission().equalsIgnoreCase(permissionType.name())) {
							collections.add(filePath);
						}
					}
				} else {
					// Filtering permissions for groups
					List<DataGridGroupPermission> groupPermissions = permissionsService
							.getGroupsWithPermissions(permissions);

					// Adding collections with permissions into the result list
					for (DataGridGroupPermission dataGridGroupPermission : groupPermissions) {
						if (dataGridGroupPermission.getPermission().equalsIgnoreCase(permissionType.name())) {
							collections.add(filePath);
						}
					}
				}
			}
		} catch (JargonException e) {
			logger.error("Could not get permissions: ", e);
		}
		return collections;
	}

	@Override
	public String getTrashForPath(String path) {
		Pattern pattern = Pattern.compile("^/(\\w+)/trash/home/(\\w+)");
		Matcher matcher = pattern.matcher(path);
		if (matcher.find()) {
			return matcher.group(0);
		}

		// if trash for path above is not found, user trash collection is used instead
		return String.format("/%s/trash/home/%s", irodsServices.getCurrentUserZone(), irodsServices.getCurrentUser());
	}

	/**
	 * @return the adminServices
	 */
	public AdminServices getAdminServices() {
		return adminServices;
	}

	/**
	 * @param adminServices
	 *            the adminServices to set
	 */
	public void setAdminServices(AdminServices adminServices) {
		this.adminServices = adminServices;
	}

	/**
	 * @return the irodsServices
	 */
	public IRODSServices getIrodsServices() {
		return irodsServices;
	}

	/**
	 * @param irodsServices
	 *            the irodsServices to set
	 */
	public void setIrodsServices(IRODSServices irodsServices) {
		this.irodsServices = irodsServices;
	}

	/**
	 * @return the resourceService
	 */
	public ResourceService getResourceService() {
		return resourceService;
	}

	/**
	 * @param resourceService
	 *            the resourceService to set
	 */
	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	/**
	 * @return the permissionsService
	 */
	public PermissionsService getPermissionsService() {
		return permissionsService;
	}

	/**
	 * @param permissionsService
	 *            the permissionsService to set
	 */
	public void setPermissionsService(PermissionsService permissionsService) {
		this.permissionsService = permissionsService;
	}

	/**
	 * @return the fileOperationService
	 */
	public FileOperationService getFileOperationService() {
		return fileOperationService;
	}

	/**
	 * @param fileOperationService
	 *            the fileOperationService to set
	 */
	public void setFileOperationService(FileOperationService fileOperationService) {
		this.fileOperationService = fileOperationService;
	}

}
