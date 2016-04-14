package com.emc.metalnx.services.irods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emc.metalnx.core.domain.entity.*;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
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

    @Value("${irods.zoneName}")
    private String zoneName;

    private static final Logger logger = LoggerFactory.getLogger(MetadataServiceImpl.class);

    @Override
    public List<DataGridCollectionAndDataObject> findByMetadata(List<DataGridMetadataSearch> searchList, DataGridPageContext pageContext,
            int pageNum, int pageSize) throws DataGridConnectionRefusedException {

        List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = new ArrayList<DataGridCollectionAndDataObject>();
        List<DataGridCollectionAndDataObject> dataGridObjects = null;
        List<DataGridCollectionAndDataObject> dataGridCollections = null;

        int totalCollections = 0;
        int totalDataObjects = 0;
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = (pageNum * pageSize) - 1;
        int endIndexForDataObjs;
        int endIndexForCollections;

        try {
            String zone = irodsServices.getCurrentUserZone();

            totalCollections = specQueryService.countCollectionsMatchingMetadata(searchList, zone);
            totalDataObjects = specQueryService.countDataObjectsMatchingMetadata(searchList, zone);

            pageContext.setStartItemNumber(startIndex + 1);
            pageContext.setTotalNumberOfItems(totalCollections + totalDataObjects);

            if (endIndex + 1 <= totalCollections) {
                // looking for collections
                SpecificQueryResultSet resultSetColls = specQueryService.searchByMetadata(searchList, zone, true, pageContext, startIndex, pageSize);

                dataGridCollections = DataGridUtils.mapMetadataResultSetToDataGridCollections(resultSetColls);

                endIndexForCollections = dataGridCollections.size();

                dataGridCollectionAndDataObjects.addAll(dataGridCollections);

                pageContext.setEndItemNumber(pageContext.getStartItemNumber() + endIndexForCollections - 1);
            } else if (startIndex + 1 > totalCollections) {
                // looking for data objects
                SpecificQueryResultSet resultSetDataObjs = specQueryService.searchByMetadata(searchList, zone, false, pageContext, startIndex
                        - totalCollections, pageSize);

                dataGridObjects = DataGridUtils.mapMetadataResultSetToDataGridObjects(resultSetDataObjs);

                pageContext.setEndItemNumber(pageContext.getStartItemNumber() + dataGridObjects.size() - 1);

                dataGridCollectionAndDataObjects.addAll(dataGridObjects);
            } else {
                // looking for collections
                SpecificQueryResultSet resultSetColls = specQueryService.searchByMetadata(searchList, zone, true, pageContext, startIndex, pageSize);

                dataGridCollections = DataGridUtils.mapMetadataResultSetToDataGridCollections(resultSetColls);

                endIndexForDataObjs = pageSize - (totalCollections % pageSize);

                // looking for data objects
                SpecificQueryResultSet resultSetDataObjs = specQueryService.searchByMetadata(searchList, zone, false, pageContext, 0,
                        endIndexForDataObjs);

                dataGridObjects = DataGridUtils.mapMetadataResultSetToDataGridObjects(resultSetDataObjs);

                endIndexForDataObjs = endIndexForDataObjs > dataGridObjects.size() ? dataGridObjects.size() : endIndexForDataObjs;

                dataGridCollectionAndDataObjects.addAll(dataGridCollections);
                dataGridCollectionAndDataObjects.addAll(dataGridObjects);

                pageContext.setEndItemNumber(pageContext.getStartItemNumber() + endIndexForDataObjs + dataGridCollections.size() - 1);
            }

        }
        catch (DataGridConnectionRefusedException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Could not find data objects by metadata. ", e.getMessage());
        }

        return dataGridCollectionAndDataObjects;
    }

    @Override
    public List<DataGridMetadata> findMetadataValuesByPath(String path) throws DataGridConnectionRefusedException {

        List<MetaDataAndDomainData> metadataList = null;
        List<DataGridMetadata> dataGridMetadataList = new ArrayList<DataGridMetadata>();
        List<MetaDataAndDomainData> resultingList = null;

        CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices.getCollectionAndDataObjectListAndSearchAO();

        try {
            Object obj = collectionAndDataObjectListAndSearchAO.getFullObjectForType(path);

            if (obj instanceof DataObject) {
                DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
                metadataList = dataObjectAO.findMetadataValuesForDataObject(path);
            } else {
                CollectionAO collectionAO = irodsServices.getCollectionAO();
                metadataList = collectionAO.findMetadataValuesForCollection(path);
            }

            // TODO: Making sure all AVUs are unique. Jargon should do that.
            resultingList = new ArrayList<MetaDataAndDomainData>();
            Set<Integer> setOfAlreadyListedAVUs = new HashSet<Integer>();
            for (MetaDataAndDomainData avuForItem : metadataList) {

                int avuId = avuForItem.getAvuId();

                if ( !setOfAlreadyListedAVUs.contains(avuId)) {
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
        }
        catch (JargonQueryException e) {
            logger.error("Error getting metadata info from collection: " + e.toString());
        }
        catch (JargonException e) {
            logger.error("Error getting metadata info from dataobject: " + e.toString());
        }

        return dataGridMetadataList;

    }

    @Override
    public boolean addMetadataToPath(String path, String attribute, String value, String unit) throws DataGridConnectionRefusedException {

        CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices.getCollectionAndDataObjectListAndSearchAO();
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
        }
        catch (JargonException e) {
            logger.error("Error trying to add metadata: " + e);
        }
        return isMetadataAdded;
    }

    @Override
    public boolean modMetadataFromPath(String path, String oldAttribute, String oldValue, String oldUnit, String newAttribute, String newValue,
            String newUnit) throws DataGridConnectionRefusedException {

        CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices.getCollectionAndDataObjectListAndSearchAO();

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
        }
        catch (JargonException e) {
            logger.error("Error trying to modify metadata: " + e.toString());
            return false;
        }
        return true;
    }

    @Override
    public boolean delMetadataFromPath(String path, String attribute, String value, String unit) throws DataGridConnectionRefusedException {

        CollectionAndDataObjectListAndSearchAO collectionAndDataObjectListAndSearchAO = irodsServices.getCollectionAndDataObjectListAndSearchAO();
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
        }
        catch (JargonException e) {
            logger.error("Error trying to delete metadata: " + e.toString());
            return false;
        }
        return true;
    }

    /**
     * Sets whether or not a user can check an object resulting from a metadata search
     *
     * @param objectList
     *            list of data objects/collections
     * @throws DataGridConnectionRefusedException
     */
    public void populateVisibilityForCurrentUser(List<DataGridCollectionAndDataObject> objectList) throws DataGridConnectionRefusedException {

        if (objectList == null || objectList.isEmpty()) {
            return;
        }

        String currentUser = getLoggedDataGridUser().getUsername();

        for (DataGridCollectionAndDataObject obj : objectList) {
            obj.setVisibleToCurrentUser(false);
            try {
                // if the user does not have access to an object an exception is thrown
                List<DataGridFilePermission> permissions = permissionsService.getPathPermissionDetails(obj.getPath(), currentUser);
                for (DataGridFilePermission perm : permissions) {
                    if (perm.getUsername().compareToIgnoreCase(currentUser) == 0) {
                        obj.setVisibleToCurrentUser(true);
                    }
                }
            }
            catch (Exception e) {
                logger.error("Could not get permissions for current user: {}", e.getMessage());
            }

        }
    }

    private DataGridUser getLoggedDataGridUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) auth.getPrincipal();

        return userService.findByUsernameAndAdditionalInfo(username, zoneName);
    }
}
