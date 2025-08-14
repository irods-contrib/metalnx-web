 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.machine.util;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.services.auth.UserTokenDetails;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.core.pub.domain.Resource;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry;
import org.irods.jargon.core.query.CollectionAndDataObjectListingEntry.ObjectType;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.irods.jargon.core.utils.IRODSDataConversionUtil;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

public class DataGridUtils {
    private static final Logger logger = LogManager.getLogger(DataGridUtils.class);

    /**
     * Maps a query result set coming from a metadata search into a list of collections.
     *
     * @param queryResultSet
     *            sql result set returned from the execution of a specific query
     * @return List of collections
     * @throws JargonException
     */
    public static List<DataGridCollectionAndDataObject> mapMetadataResultSetToDataGridCollections(SpecificQueryResultSet queryResultSet)
            throws JargonException {

        List<DataGridCollectionAndDataObject> colls = new ArrayList<>();

        if (queryResultSet != null) {
            List<IRODSQueryResultRow> results = queryResultSet.getResults();

            for (IRODSQueryResultRow row : results) {
                DataGridCollectionAndDataObject c = new DataGridCollectionAndDataObject();

                String collPath = row.getColumn("obj_name");
                String collName = collPath.substring(collPath.lastIndexOf("/") + 1, collPath.length());

                c.setName(collName);
                c.setPath(collPath);
                c.setParentPath(row.getColumn("parent_path"));
                c.setOwner(row.getColumn("obj_owner"));
                c.setCollection(true);
                c.setReplicaNumber("-");
                c.setCreatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn("create_ts")));
                c.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn("modify_ts")));
                c.setNumberOfMatches(Integer.valueOf(row.getColumn("totalMatches")));
                c.setResourceName(row.getColumn("resc_name"));

                colls.add(c);
            }
        }

        return colls;
    }

    /**
     * Maps a query result set coming from a metadata search into a list of data objects.
     *
     * @param queryResultSet
     *            sql result set returned from the execution of a specific query
     * @return List of data objects
     * @throws JargonException
     */
    public static List<DataGridCollectionAndDataObject> mapMetadataResultSetToDataGridObjects(SpecificQueryResultSet queryResultSet)
            throws JargonException {

        List<DataGridCollectionAndDataObject> objs = new ArrayList<DataGridCollectionAndDataObject>();

        if (queryResultSet != null) {
            List<IRODSQueryResultRow> results = queryResultSet.getResults();

            for (IRODSQueryResultRow row : results) {
                String objName = row.getColumn("obj_name");
                String parentPath = row.getColumn("parent_path");
                String dataObjDisplaySize = MiscIRODSUtils.humanReadableByteCount(Long.valueOf(row.getColumn("size")));

                String path = parentPath + "/" + objName;
                if (parentPath.compareTo("/") == 0) path = parentPath + objName;

                DataGridCollectionAndDataObject obj = new DataGridCollectionAndDataObject();
                obj.setName(objName);
                obj.setPath(path);
                obj.setParentPath(parentPath);
                obj.setSize(Long.valueOf(row.getColumn("size")));
                obj.setDisplaySize(dataObjDisplaySize);
                obj.setOwner(row.getColumn("obj_owner"));
                obj.setCollection(false);
                obj.setReplicaNumber(row.getColumn("repl_num"));
                obj.setCreatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn("create_ts")));
                obj.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(row.getColumn("modify_ts")));
                obj.setResourceName(row.getColumn("resc_name"));
                obj.setNumberOfMatches(Integer.valueOf(row.getColumn("totalMatches")));

                objs.add(obj);
            }
        }

        return objs;
    }

    /**
     * Maps a query result set coming from a file properties search into a list of data objects.
     *
     * @param queryResultSet
     *            sql result set returned from the execution of a specific query
     * @return List of data objects
     * @throws JargonException
     */
    public static List<DataGridCollectionAndDataObject> mapPropertiesResultSetToDataGridObjects(SpecificQueryResultSet queryResultSet)
            throws JargonException {

        List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = new ArrayList<DataGridCollectionAndDataObject>();

        if (queryResultSet != null) {
            List<IRODSQueryResultRow> results = queryResultSet.getResults();

            for (IRODSQueryResultRow irodsQueryResultRow : results) {
                DataGridCollectionAndDataObject dataGridObj = new DataGridCollectionAndDataObject();

                String objName = irodsQueryResultRow.getColumn(0);

                String dataObjDisplaySize = MiscIRODSUtils.humanReadableByteCount(Long.valueOf(irodsQueryResultRow.getColumn(4)));

                dataGridObj.setName(objName);
                dataGridObj.setPath(irodsQueryResultRow.getColumn(6));
                dataGridObj.setSize(Long.valueOf(irodsQueryResultRow.getColumn(4)));
                dataGridObj.setDisplaySize(dataObjDisplaySize);
                dataGridObj.setOwner(irodsQueryResultRow.getColumn(2));
                dataGridObj.setCollection(dataGridObj.getSize() == 0 ? true : false);
                dataGridObj.setReplicaNumber(irodsQueryResultRow.getColumn(1));
                dataGridObj.setCreatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(irodsQueryResultRow.getColumn(8)));
                dataGridObj.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(irodsQueryResultRow.getColumn(9)));
                dataGridObj.setNumberOfMatches(0);
                dataGridObj.setResourceName(irodsQueryResultRow.getColumn(5));
                dataGridObj.setChecksum(irodsQueryResultRow.getColumn(7));

                dataGridCollectionAndDataObjects.add(dataGridObj);
            }
        }

        return dataGridCollectionAndDataObjects;
    }

    /**
     * Checks if the user logged is admin or nor.
     *
     * @return true, if the user is a rods admin. False, otherwise.
     */
    public static boolean isUserLoggedAdmin() {
        // Checking if user is Admin or normal user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof UsernamePasswordAuthenticationToken) {
            UserTokenDetails userDetails = (UserTokenDetails) auth.getDetails();

            // If the user is admin
            if (userDetails.getUser().isAdmin()) {
                return true;
            }
        }

        return false;
    }

    /**
     * Maps the COUNT from the specific query into an integer
     * for collections
     *
     * @param queryResultSet
     *            result set returned from a query
     * @return list of data grid objects
     * @throws JargonException
     */
    public static int mapCountQueryResultSetToInteger(SpecificQueryResultSet queryResultSet) throws JargonException {
        int totalNumberOfItems = 0;

        if (queryResultSet != null && !queryResultSet.getResults().isEmpty()) {
            IRODSQueryResultRow irodsQueryResultRow = queryResultSet.getResults().get(0);

            totalNumberOfItems = Integer.valueOf(irodsQueryResultRow.getColumnAsIntOrZero(0));
        }

        return totalNumberOfItems;
    }

    /**
     * Maps the results from the specific query for listing files in a collection into data objects
     * for collections
     *
     * @param queryResultSet
     *            result set returned from a query
     * @return list of data grid objects
     * @throws JargonException
     */
    public static List<CollectionAndDataObjectListingEntry> mapCollectionQueryResultSetToDataGridObjects(SpecificQueryResultSet queryResultSet)
            throws JargonException {
        List<CollectionAndDataObjectListingEntry> dataGridCollectionAndDataObjects = new ArrayList<CollectionAndDataObjectListingEntry>();

        List<IRODSQueryResultRow> results = queryResultSet.getResults();

        for (IRODSQueryResultRow irodsQueryResultRow : results) {
            CollectionAndDataObjectListingEntry collectionAndDataObject = new CollectionAndDataObjectListingEntry();

            collectionAndDataObject.setParentPath(irodsQueryResultRow.getColumn("c.parent_coll_name"));
            collectionAndDataObject.setPathOrName(irodsQueryResultRow.getColumn("c.coll_name"));
            collectionAndDataObject.setOwnerName(irodsQueryResultRow.getColumn("c.coll_owner_name"));
            collectionAndDataObject.setOwnerZone(irodsQueryResultRow.getColumn("c.coll_owner_zone"));
            collectionAndDataObject.setObjectType(ObjectType.COLLECTION);
            collectionAndDataObject.setCreatedAt(IRODSDataConversionUtil.getDateFromIRODSValue(irodsQueryResultRow.getColumn("c.create_ts")));
            collectionAndDataObject.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(irodsQueryResultRow.getColumn("c.modify_ts")));

            dataGridCollectionAndDataObjects.add(collectionAndDataObject);
        }

        return dataGridCollectionAndDataObjects;
    }

    /**
     * Maps the results from the specific query for listing files in a collection into DataGrid
     * objects
     *
     * @param queryResultSet
     * @return
     * @throws JargonException
     */
    public static List<CollectionAndDataObjectListingEntry> mapQueryResultSetToDataGridObjects(SpecificQueryResultSet queryResultSet)
            throws JargonException {
        List<CollectionAndDataObjectListingEntry> dataGridCollectionAndDataObjects = new ArrayList<CollectionAndDataObjectListingEntry>();

        List<IRODSQueryResultRow> results = queryResultSet.getResults();

        for (IRODSQueryResultRow irodsQueryResultRow : results) {
            CollectionAndDataObjectListingEntry collectionAndDataObject = new CollectionAndDataObjectListingEntry();

            collectionAndDataObject.setParentPath(irodsQueryResultRow.getColumn("c.coll_name"));
            collectionAndDataObject.setPathOrName(irodsQueryResultRow.getColumn("d.data_name"));
            collectionAndDataObject.setDataSize(Long.parseLong(irodsQueryResultRow.getColumn("d.data_size")));
            collectionAndDataObject.setOwnerName(irodsQueryResultRow.getColumn("d.data_owner_name"));
            collectionAndDataObject.setOwnerZone(irodsQueryResultRow.getColumn("d.data_owner_zone"));
            collectionAndDataObject.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(irodsQueryResultRow.getColumn("d.modify_ts")));

            dataGridCollectionAndDataObjects.add(collectionAndDataObject);
        }

        return dataGridCollectionAndDataObjects;
    }

    /**
     * Maps the results from the specific query for listing files in a collection into DataGrid
     * objects
     *
     * @param queryResultSet
     * @return
     * @throws JargonException
     */
    public static List<CollectionAndDataObjectListingEntry> mapQueryResultSetToDataGridObjectsForSearch(SpecificQueryResultSet queryResultSet)
            throws JargonException {
        List<CollectionAndDataObjectListingEntry> dataGridCollectionAndDataObjects = new ArrayList<CollectionAndDataObjectListingEntry>();

        List<IRODSQueryResultRow> results = queryResultSet.getResults();

        for (IRODSQueryResultRow irodsQueryResultRow : results) {
            CollectionAndDataObjectListingEntry collectionAndDataObject = new CollectionAndDataObjectListingEntry();

            collectionAndDataObject.setId(Integer.valueOf(irodsQueryResultRow.getColumn("data_id")));
            collectionAndDataObject.setPathOrName(irodsQueryResultRow.getColumn("data_name"));
            collectionAndDataObject.setParentPath(irodsQueryResultRow.getColumn("coll_name"));
            collectionAndDataObject.setObjectType(ObjectType.DATA_OBJECT);
            collectionAndDataObject.setDataSize(Long.valueOf(irodsQueryResultRow.getColumn("data_size")));
            collectionAndDataObject.setOwnerName(irodsQueryResultRow.getColumn("data_owner_name"));
            collectionAndDataObject.setOwnerZone(irodsQueryResultRow.getColumn("data_owner_zone"));
            collectionAndDataObject.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(irodsQueryResultRow.getColumn("create_ts")));
            collectionAndDataObject.setModifiedAt(IRODSDataConversionUtil.getDateFromIRODSValue(irodsQueryResultRow.getColumn("modify_ts")));

            dataGridCollectionAndDataObjects.add(collectionAndDataObject);
        }

        return dataGridCollectionAndDataObjects;
    }

    /**
     * Maps a Data Object object from Jargon to the Metalnx representation of Data object
     *
     * @param dataObject
     *            data object instance to be mapped
     * @return DataGridCollectionAndDataObject instance the given Data object
     */
    public static DataGridCollectionAndDataObject getDataGridCollectionAndDataObject(DataObject dataObject) {
        DataGridCollectionAndDataObject dataGridCollectionAndDataObject = new DataGridCollectionAndDataObject();

        dataGridCollectionAndDataObject.setName(dataObject.getDataName());
        dataGridCollectionAndDataObject.setPath(dataObject.getDataPath());
        dataGridCollectionAndDataObject.setReplicaNumber(String.valueOf(dataObject.getDataReplicationNumber()));
        dataGridCollectionAndDataObject.setChecksum(dataObject.getChecksum());
        dataGridCollectionAndDataObject.setCreatedAt(dataObject.getCreatedAt());
        dataGridCollectionAndDataObject.setModifiedAt(dataObject.getUpdatedAt());
        dataGridCollectionAndDataObject.setOwner(dataObject.getDataOwnerName());
        dataGridCollectionAndDataObject.setCollection(false);
        dataGridCollectionAndDataObject.setDisplaySize(dataObject.getDisplayDataSize());
        dataGridCollectionAndDataObject.setSize(dataObject.getDataSize());
        dataGridCollectionAndDataObject.setResourceName(dataObject.getResourceName());

        return dataGridCollectionAndDataObject;
    }

    public static HashMap<String, String> buildMapForResourcesNamesAndMountPoints(List<Resource> resourceList) {
        HashMap<String, String> resourceMap = new HashMap<String, String>();
        for (Resource resource : resourceList) {
            resourceMap.put(resource.getName(), resource.getVaultPath());
        }
        return resourceMap;
    }

    /**
     * Sort Replica-Resource Map by replica number
     *
     * @param map
     *            Replica-Resource Map to be sorted
     * @return
     *         Replica-Resource Map sorted
     */
    public static Map<DataGridCollectionAndDataObject, DataGridResource> sortReplicaResourceMap(
            Map<DataGridCollectionAndDataObject, DataGridResource> map) {

        Map<DataGridCollectionAndDataObject, DataGridResource> sortedMap = new TreeMap<DataGridCollectionAndDataObject, DataGridResource>(
                new Comparator<DataGridCollectionAndDataObject>() {

                    @Override
                    public int compare(DataGridCollectionAndDataObject do1, DataGridCollectionAndDataObject do2) {
                        Integer replicaNumberDO1 = new Integer(do1.getReplicaNumber());
                        Integer replicaNumberDO2 = new Integer(do2.getReplicaNumber());
                        return replicaNumberDO1.compareTo(replicaNumberDO2);
                    }

                });

        sortedMap.putAll(map);

        return sortedMap;
    }

    /**
     * Maps a CollectionAndDataObjectListingEntry list into a DataGridCollectionAndDataObject list
     *
     * @param entries
     *            CollectionAndDataObjectListingEntry objects to map
     * @return list of DataGridCollectionAndDataObject objects
     */
    public static List<DataGridCollectionAndDataObject> mapListingEntryToDataGridCollectionAndDataObject(
            List<CollectionAndDataObjectListingEntry> entries) {
        logger.debug("Mapping a CollectionAndDataObjectListingEntry list into a " + "DataGridCollectionAndDataObject list");

        List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = new ArrayList<DataGridCollectionAndDataObject>();

        for (CollectionAndDataObjectListingEntry entry : entries) {
            if("/".equals(entry.getPathOrName())) continue;
            DataGridCollectionAndDataObject dataGridCollectionAndDataObject = mapListingEntryToDataGridCollectionAndDataObject(entry);
            dataGridCollectionAndDataObjects.add(dataGridCollectionAndDataObject);
        }

        return dataGridCollectionAndDataObjects;
    }

    /**
     * Maps a CollectionAndDataObjectListingEntry object into a DataGridCollectionAndDataObject
     * object
     *
     * @param entry
     *            CollectionAndDataObjectListingEntry object to map into DataGridCollectionAndDataObject
     * @return instance of DataGridCollectionAndDataObject
     */
    public static DataGridCollectionAndDataObject mapListingEntryToDataGridCollectionAndDataObject(CollectionAndDataObjectListingEntry entry) {
        logger.debug("Mapping a CollectionAndDataObjectListingEntry into a " + "DataGridCollectionAndDataObject");

        String entryPath = "";

        if (entry.isCollection()) {
            entryPath = entry.getPathOrName();
        }
        else {
            entryPath = entry.getParentPath() + "/" + entry.getPathOrName();
        }

        String nodeLabelDisplayValue = entry.getNodeLabelDisplayValue();
        String entryName = nodeLabelDisplayValue == null || nodeLabelDisplayValue.isEmpty() ? entryPath : nodeLabelDisplayValue;
        
        DataGridCollectionAndDataObject dataGridCollectionAndDataObject = new DataGridCollectionAndDataObject(entryPath,
        		entryName, entry.getParentPath(), entry.isCollection());

        dataGridCollectionAndDataObject.setCreatedAt(entry.getCreatedAt());
        dataGridCollectionAndDataObject.setModifiedAt(entry.getModifiedAt());
        dataGridCollectionAndDataObject.setOwner(entry.getOwnerName());
        dataGridCollectionAndDataObject.setDisplaySize(entry.getDisplayDataSize());
        dataGridCollectionAndDataObject.setSize(entry.getDataSize());

        return dataGridCollectionAndDataObject;
    }

}
