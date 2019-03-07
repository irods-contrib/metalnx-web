/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.irods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridMetadata;
import com.emc.metalnx.core.domain.entity.DataGridMetadataSearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.MetadataService;
import com.emc.metalnx.services.interfaces.PermissionsService;
import com.emc.metalnx.services.interfaces.SpecQueryService;
import com.emc.metalnx.services.interfaces.UserService;
import com.emc.metalnx.services.machine.util.DataGridUtils;

@Service
@Transactional
public class MetadataServiceImpl implements MetadataService {
	@Autowired
	IRODSServices irodsServices;

	@Autowired
	SpecQueryService specQueryService;

	@Autowired
	UserService userService;

	@Autowired
	PermissionsService permissionsService;

	private static final Logger logger = LoggerFactory.getLogger(MetadataServiceImpl.class);

	@Override
	public List<DataGridCollectionAndDataObject> findByMetadata(final List<DataGridMetadataSearch> searchList,
			final DataGridPageContext pageContext, final int start, final int length) throws DataGridException {

		logger.info("findByMetadata()");

		if (searchList == null || searchList.isEmpty()) {
			throw new IllegalArgumentException("null or empty searchList");
		}

		if (pageContext == null) {
			throw new IllegalArgumentException("null pageContext");
		}

		logger.info("searchList:{}", searchList);
		logger.info("pageContext:{}", pageContext);
		logger.info("start:{}", start);
		logger.info("length:{}", length);
		int endIndex = start + length - 1;
		logger.info("endIndex:{}", endIndex);

		/*
		 * Need to translate from indexes in terms of collections + data objects into
		 * separate indexes for collections (first) and data objects (second)
		 * 
		 * User pages presents as continuous.
		 */

		List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = new ArrayList<>();
		List<DataGridCollectionAndDataObject> dataGridObjects = new ArrayList<>();
		List<DataGridCollectionAndDataObject> dataGridCollections = new ArrayList<>();

		int totalCollections;
		int totalDataObjects;

		int endIndexForDataObjs;
		int endIndexForCollections;

		try {
			String zone = irodsServices.getCurrentUserZone();

			totalCollections = specQueryService.countCollectionsMatchingMetadata(searchList, zone);
			totalDataObjects = specQueryService.countDataObjectsMatchingMetadata(searchList, zone);

			logger.info("totalCollections:{}", totalCollections);
			logger.info("totalDataObjects:{}", totalDataObjects);

			pageContext.setStartItemNumber(start);
			pageContext.setTotalNumberOfItems(totalCollections + totalDataObjects);

			if (start + 1 < totalCollections) {
				logger.info("have some collections to query");

				// looking for collections
				SpecificQueryResultSet resultSetColls = specQueryService.searchByMetadata(searchList, zone, true,
						pageContext, start, length);
				dataGridCollections = DataGridUtils.mapMetadataResultSetToDataGridCollections(resultSetColls);
				endIndexForCollections = dataGridCollections.size();
				dataGridCollectionAndDataObjects.addAll(dataGridCollections);
			}

			/*
			 * See if I need to add data objects, if I've found some collections, make up
			 * the remainder of the length in data objects
			 */

			int dataObjectRemainder = length - dataGridCollections.size();

			if (dataObjectRemainder > 0) {

				// looking for data objects
				logger.info("looking for data objects, second branch...");

				SpecificQueryResultSet resultSetDataObjs = specQueryService.searchByMetadata(searchList, zone, false,
						pageContext, start, dataObjectRemainder);
				dataGridObjects = DataGridUtils.mapMetadataResultSetToDataGridObjects(resultSetDataObjs);
				dataGridCollectionAndDataObjects.addAll(dataGridObjects);
			}

			dataGridCollectionAndDataObjects.addAll(dataGridCollections);
			dataGridCollectionAndDataObjects.addAll(dataGridObjects);

			pageContext.setEndItemNumber(start + dataGridCollectionAndDataObjects.size() - 1);

		} catch (DataGridConnectionRefusedException e) {
			logger.error("data grid connection refused exception", e);
			throw e;
		} catch (Exception e) {
			logger.error("Could not find data objects by metadata.", e);
			throw new DataGridException("error finding by metadata", e);
		}

		populateVisibilityForCurrentUser(dataGridCollectionAndDataObjects);
		return dataGridCollectionAndDataObjects;
	}

	@Override
	public List<DataGridMetadata> findMetadataValuesByPath(String path) throws DataGridException {

		List<MetaDataAndDomainData> metadataList;
		List<DataGridMetadata> dataGridMetadataList = new ArrayList<>();
		List<MetaDataAndDomainData> resultingList;

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices
				.getCollectionAndDataObjectListAndSearchAO();

		try {
			Object obj = collectionAndDataObjectListAndSearchAO.getFullObjectForType(path);

			if (obj instanceof DataObject) {
				DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
				metadataList = dataObjectAO.findMetadataValuesForDataObject(path);
			} else {
				CollectionAO collectionAO = irodsServices.getCollectionAO();
				metadataList = collectionAO.findMetadataValuesForCollection(path);
			}

			// TODO2: Making sure all AVUs are unique. Jargon should do that.
			resultingList = new ArrayList<>();
			Set<Integer> setOfAlreadyListedAVUs = new HashSet<>();
			for (MetaDataAndDomainData avuForItem : metadataList) {

				int avuId = avuForItem.getAvuId();

				if (!setOfAlreadyListedAVUs.contains(avuId)) {
					resultingList.add(avuForItem);
					setOfAlreadyListedAVUs.add(avuId);
				}
			}

			for (MetaDataAndDomainData metadata : resultingList) {
				DataGridMetadata dataGridMetadata = new DataGridMetadata();
				dataGridMetadata.setAttribute(metadata.getAvuAttribute());
				dataGridMetadata.setValue(metadata.getAvuValue());
				dataGridMetadata.setUnit(metadata.getAvuUnit());
				dataGridMetadataList.add(dataGridMetadata);
			}

			Collections.sort(dataGridMetadataList);
		} catch (JargonQueryException e) {
			logger.error("Error getting metadata info from collection", e);
		} catch (JargonException e) {
			logger.error("Error getting metadata info from dataobject", e.toString());
		}

		return dataGridMetadataList;
	}

	@Override
	public boolean addMetadataToPath(String path, String attribute, String value, String unit)
			throws DataGridConnectionRefusedException {

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices
				.getCollectionAndDataObjectListAndSearchAO();
		boolean isMetadataAdded = false;

		logger.debug(path + ": " + attribute + " " + value + " " + unit);

		try {
			AvuData avuData = new AvuData(attribute, value, unit);
			Object obj = collectionAndDataObjectListAndSearchAO.getFullObjectForType(path);

			if (obj instanceof DataObject) {
				DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
				dataObjectAO.addAVUMetadata(path, avuData);
			} else {
				CollectionAO collectionAO = irodsServices.getCollectionAO();
				collectionAO.addAVUMetadata(path, avuData);
			}

			isMetadataAdded = true;
		} catch (JargonException e) {
			logger.error("Error trying to add metadata: " + e);
		}
		return isMetadataAdded;
	}

	@Override
	public boolean addMetadataToPath(String path, DataGridMetadata metadata) throws DataGridConnectionRefusedException {
		if (metadata == null)
			return false;
		return addMetadataToPath(path, metadata.getAttribute(), metadata.getValue(), metadata.getUnit());
	}

	@Override
	public boolean addMetadataToPath(String path, List<DataGridMetadata> metadataList)
			throws DataGridConnectionRefusedException {
		if (metadataList == null || metadataList.isEmpty())
			return false;

		boolean isMetadataAdded = false;

		for (DataGridMetadata metadata : metadataList) {
			isMetadataAdded &= addMetadataToPath(path, metadata);
		}

		return isMetadataAdded;
	}

	@Override
	public boolean modMetadataFromPath(String path, String oldAttribute, String oldValue, String oldUnit,
			String newAttribute, String newValue, String newUnit) throws DataGridConnectionRefusedException {

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices
				.getCollectionAndDataObjectListAndSearchAO();

		try {
			AvuData oldAVUData = new AvuData(oldAttribute, oldValue, oldUnit);
			AvuData newAVUData = new AvuData(newAttribute, newValue, newUnit);
			Object obj = collectionAndDataObjectListAndSearchAO.getFullObjectForType(path);

			if (obj instanceof DataObject) {
				DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
				dataObjectAO.modifyAVUMetadata(path, oldAVUData, newAVUData);
			} else {
				CollectionAO collectionAO = irodsServices.getCollectionAO();
				collectionAO.modifyAVUMetadata(path, oldAVUData, newAVUData);
			}
		} catch (JargonException e) {
			logger.error("Error trying to modify metadata: " + e.toString());
			return false;
		}
		return true;
	}

	@Override
	public boolean delMetadataFromPath(String path, String attribute, String value, String unit)
			throws DataGridConnectionRefusedException {

		CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices
				.getCollectionAndDataObjectListAndSearchAO();
		try {
			AvuData avuData = new AvuData(attribute, value, unit);
			Object obj = collectionAndDataObjectListAndSearchAO.getFullObjectForType(path);

			if (obj instanceof DataObject) {
				DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
				dataObjectAO.deleteAVUMetadata(path, avuData);
			} else {
				CollectionAO collectionAO = irodsServices.getCollectionAO();
				collectionAO.deleteAVUMetadata(path, avuData);
			}
		} catch (JargonException e) {
			logger.error("Error trying to delete metadata: " + e.toString());
			return false;
		}
		return true;
	}

	/**
	 * Sets whether or not a user can check an object resulting from a metadata
	 * search
	 *
	 * @param objectList list of data objects/collections
	 * @throws DataGridConnectionRefusedException
	 */
	@Override
	public void populateVisibilityForCurrentUser(List<DataGridCollectionAndDataObject> objectList)
			throws DataGridException {

		if (objectList == null || objectList.isEmpty()) {
			return;
		}

		final String currentUser = irodsServices.getCurrentUser();
		final IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();
		final IRODSFileSystemAO irodsFileSystemAO = irodsServices.getIRODSFileSystemAO();

		for (final DataGridCollectionAndDataObject obj : objectList) {
			try {
				int resultingPermission;
				final IRODSFile fileObj = irodsFileFactory.instanceIRODSFile(obj.getPath());

				if (obj.isCollection()) {
					resultingPermission = irodsFileSystemAO.getDirectoryPermissionsForGivenUser(fileObj, currentUser);
				} else {
					resultingPermission = irodsFileSystemAO.getFilePermissionsForGivenUser(fileObj, currentUser);
				}

				// By default, the visibility of a user over an object is set to false
				obj.setVisibleToCurrentUser(resultingPermission != FilePermissionEnum.NONE.getPermissionNumericValue());

			} catch (final Exception e) {
				logger.error("Could not get permissions for current user: {}", e.getMessage());
				throw new DataGridException("error getting collection permissions", e);
			}
		}
	}

	@Override
	public boolean copyMetadata(String srcPath, String dstPath) throws DataGridException {
		if (srcPath == null || srcPath.isEmpty() || dstPath == null || dstPath.isEmpty())
			return false;

		logger.info("Copying metadata from {} to {}", srcPath, dstPath);

		boolean isMetadataCopied = true;

		for (DataGridMetadata metadata : findMetadataValuesByPath(srcPath)) {
			isMetadataCopied &= addMetadataToPath(dstPath, metadata);
		}

		return isMetadataCopied;
	}
}
