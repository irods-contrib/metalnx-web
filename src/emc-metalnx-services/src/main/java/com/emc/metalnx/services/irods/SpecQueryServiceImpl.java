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

package com.emc.metalnx.services.irods;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.domain.SpecificQueryDefinition;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.entity.DataGridFilePropertySearch;
import com.emc.metalnx.core.domain.entity.DataGridMetadataSearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.AdminServices;
import com.emc.metalnx.services.interfaces.SpecQueryService;
import com.emc.metalnx.services.machine.util.DataGridUtils;

@Service
@Transactional
public class SpecQueryServiceImpl implements SpecQueryService {
    @Autowired
    AdminServices adminServices;

    private static final Logger logger = LoggerFactory.getLogger(SpecQueryServiceImpl.class);

    @Override
    public int countCollectionsMatchingMetadata(List<DataGridMetadataSearch> metadataSearch, String zone) throws DataGridConnectionRefusedException {

        return countItemsMatchingMetadata(metadataSearch, zone, true);
    }

    @Override
    public int countDataObjectsMatchingMetadata(List<DataGridMetadataSearch> metadataSearch, String zone) throws DataGridConnectionRefusedException {
        return countItemsMatchingMetadata(metadataSearch, zone, false);
    }

    @Override
    public void deleteSpecQueryByAlias(String specQueryAlias) throws DataGridConnectionRefusedException {

        SpecificQueryAO specificQueryAO = null;

        try {
            specificQueryAO = adminServices.getSpecificQueryAO();

            specificQueryAO.removeSpecificQueryByAlias(specQueryAlias);
        }
        catch (JargonException e) {
            logger.error("Could not get specific query: ", e.getMessage());
        }
    }

    @Override
    public SpecificQueryResultSet searchByMetadata(List<DataGridMetadataSearch> metadataSearch, String zone, boolean searchAgainstColls,
            DataGridPageContext pageContext, int offset, int limit) throws DataGridConnectionRefusedException {

        SpecificQueryAO specificQueryAO = null;
        SpecificQuery specQuery = null;
        SpecificQueryResultSet queryResultSet = null;
        String userSQLAlias = "metalnxUserQuery_" + System.currentTimeMillis();

        try {
            specificQueryAO = adminServices.getSpecificQueryAO();

            StringBuilder query = new StringBuilder();
            query.append(getSpecificQueryForMetadataSearch(metadataSearch, zone, searchAgainstColls));
            query.append(" OFFSET ");
            query.append(offset);
            query.append(" LIMIT ");
            query.append(limit);

            // Creating Specific Query instance
            SpecificQueryDefinition queryDef = new SpecificQueryDefinition();
            queryDef.setAlias(userSQLAlias);
            queryDef.setSql(query.toString());

            // Creating spec query on iRODS
            specificQueryAO.addSpecificQuery(queryDef);

            specQuery = SpecificQuery.instanceWithNoArguments(userSQLAlias, 0, zone);

            logger.info("Specific query: {}", query.toString());

            queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery, 99999, 0);
        }
        catch (JargonException e) {
            logger.error("Could not get specific query: ", e);
        }
        catch (JargonQueryException e) {
            logger.error("Could not get specific query: ", e);
        }
        finally {
            try {
                // after running the user specific query, we need to remove from the database
                specificQueryAO.removeSpecificQueryByAlias(userSQLAlias);
            }
            catch (JargonException e) {
                logger.error("Could not remove specific query {}: ", userSQLAlias, e.getMessage());
            }
        }

        return queryResultSet;
    }

    /**
     * Creates a specific query based on the metadata search criteria.
     *
     * @param metadataSearch
     *            search criteria
     * @param zone
     *            zone to be looking for data objects or collections
     * @param searchAgainstColls
     *            Flag set to true when looking for collections. False when looking for data
     *            objects.
     * @return Specific query string
     */
    private String getSpecificQueryForMetadataSearch(List<DataGridMetadataSearch> metadataSearch, String zone, boolean searchAgainstColls) {

        // This is the first part of the query. It selects all objects (collections or data objects)
        // that has a piece of metadata matching the query
        StringBuilder objQuery = new StringBuilder();

        // This is the query that actually looks for metadata tags in the data grid.
        StringBuilder metadataSearchQuery = new StringBuilder();

        // This is the last part of the query. It groups the objects by their attributes in order to
        // have unique results.
        StringBuilder gb = new StringBuilder();

        // This is the final query that will be sent to the data grid database
        StringBuilder q = new StringBuilder();

        if (searchAgainstColls) {
            objQuery.append(" SELECT obj_name, parent_path, obj_owner, create_ts, modify_ts, totalMatches");
            objQuery.append(" FROM (");
            objQuery.append(" SELECT c.coll_name as obj_name,");
            objQuery.append("      c.parent_coll_name as parent_path,");
            objQuery.append("      c.coll_owner_name as obj_owner,");
            objQuery.append("      c.create_ts as create_ts,");
            objQuery.append("      c.modify_ts as modify_ts,");
            objQuery.append("      c.coll_inheritance,");
            objQuery.append("      COUNT(c.coll_name) as totalMatches");
            objQuery.append(" FROM ");
            objQuery.append("      r_coll_main c ");
            objQuery.append(" JOIN ( ");

            gb.append(" ) AS coll_metadata ON (c.coll_id = map_object_id) ");
            gb.append(" GROUP BY ");
            gb.append("      c.coll_name,");
            gb.append("      c.parent_coll_name,");
            gb.append("      c.coll_owner_name,");
            gb.append("      c.create_ts,");
            gb.append("      c.modify_ts,");
            gb.append("      c.coll_inheritance");
            gb.append(" ORDER BY totalMatches DESC, c.coll_name ");
            gb.append(" ) AS ms ");
        }
        else {
            objQuery.append(" SELECT obj_name, size, obj_owner, repl_num, create_ts, modify_ts, parent_path, totalMatches");
            objQuery.append(" FROM (");
            objQuery.append(" SELECT ");
            objQuery.append("      d.data_name as obj_name, ");
            objQuery.append("      d.data_size as size,  ");
            objQuery.append("      d.data_owner_name as obj_owner, ");
            objQuery.append("      d.data_repl_num as repl_num, ");
            objQuery.append("      d.create_ts as create_ts,");
            objQuery.append("      d.modify_ts as modify_ts, ");
            objQuery.append("      c.coll_name as parent_path,");
            objQuery.append("      COUNT(d.data_name) as totalMatches");
            objQuery.append(" FROM r_data_main d ");
            objQuery.append(" JOIN r_coll_main c ON (d.coll_id = c.coll_id) ");
            objQuery.append(" JOIN ( ");

            gb.append(" ) AS data_obj_metadata ON (d.data_id = map_object_id) ");
            gb.append(" GROUP BY ");
            gb.append("      d.data_name,");
            gb.append("      d.data_size,");
            gb.append("      d.data_owner_name,");
            gb.append("      d.data_repl_num,");
            gb.append("      d.create_ts,");
            gb.append("      d.modify_ts,");
            gb.append("      c.coll_name");
            gb.append(" ORDER BY totalMatches DESC, d.data_name ");
            gb.append(" ) AS ms ");
        }

        for (DataGridMetadataSearch d : metadataSearch) {
            metadataSearchQuery.append(d.getSpecQueryAsString());

            // appending conditions
            if (metadataSearch.indexOf(d) != metadataSearch.size() - 1) {
                metadataSearchQuery.append(" UNION ALL ");
            }
        }

        // combining the three parts of the metadata query into a single SQL query
        q.append(objQuery.toString());
        q.append(metadataSearchQuery.toString());
        q.append(gb.toString());

        return q.toString();
    }

    /**
     * Creates a specific query based on the file properties search criteria.
     *
     * @param filePropertiesSearches search criteria
     * @param zone
     *            zone to be looking for data objects or collections
     * @param searchAgainstColls
     *            Flag set to true when looking for collections. False when looking for data
     *            objects.
     * @return Specific query string
     */
    private String getSpecificQueryForFilePropertySearch(List<DataGridFilePropertySearch> filePropertiesSearches, String zone,
            boolean searchAgainstColls) {

        StringBuilder query = new StringBuilder();

        if (filePropertiesSearches.size() > 0) {
            DataGridFilePropertySearch first = filePropertiesSearches.get(0);
            if (searchAgainstColls) {
                query.append(first.getSelectClauseForCollections());
            }
            else {
                query.append(first.getSelectClauseForDataObjects());
            }

            for (DataGridFilePropertySearch filePropertiesSearch : filePropertiesSearches) {

                // where clause - conditions
                query.append(filePropertiesSearch.getWhereClause());

                // appending conditions
                if (filePropertiesSearches.indexOf(filePropertiesSearch) != filePropertiesSearches.size() - 1) {
                    query.append(" AND ");
                }
            }
        }

        return query.toString();
    }

    /**
     * Counts the number of items matching a metadata search criteria.
     *
     * @param metadataSearch
     *            metadata criteria
     * @param zone
     *            zone name
     * @param searchAgainstColls
     *            flag set to true when searching collections and false when searching data
     *            data objects
     * @return total number of items matching a metadata search criteria
     * @throws DataGridConnectionRefusedException
     */
    private int countItemsMatchingMetadata(List<DataGridMetadataSearch> metadataSearch, String zone, boolean searchAgainstColls)
            throws DataGridConnectionRefusedException {

        int totalItems = 0;

        SpecificQueryAO specificQueryAO = null;
        SpecificQuery specQuery = null;
        SpecificQueryResultSet queryResultSet = null;
        String userSQLAlias = "metalnxUserQuery_" + System.currentTimeMillis();

        try {
            specificQueryAO = adminServices.getSpecificQueryAO();

            StringBuilder query = new StringBuilder();

            query.append("WITH searchMetadata AS (");
            query.append(getSpecificQueryForMetadataSearch(metadataSearch, zone, searchAgainstColls));
            query.append(") ");
            query.append("SELECT COUNT(*) ");
            query.append("FROM searchMetadata ");

            // Creating Specific Query instance
            SpecificQueryDefinition queryDef = new SpecificQueryDefinition();
            queryDef.setAlias(userSQLAlias);
            queryDef.setSql(query.toString());

            // Creating spec query on iRODS
            specificQueryAO.addSpecificQuery(queryDef);

            specQuery = SpecificQuery.instanceWithNoArguments(userSQLAlias, 0, zone);

            logger.info("Specific query: {}", query.toString());

            queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery, 99999, 0);

            // after running the user specific query, we need to remove from the database
            specificQueryAO.removeSpecificQueryByAlias(userSQLAlias);

            totalItems = DataGridUtils.mapCountQueryResultSetToInteger(queryResultSet);
        }
        catch (JargonException e) {
            logger.error("Could not get specific query: ", e);
        }
        catch (JargonQueryException e) {
            logger.error("Could not get specific query: ", e);
        }

        return totalItems;
    }

    /**
     * Counts the number of items matching a file properties search criteria.
     *
     * @param filePropertiesSearch
     *            filePropertiesSearch criteria
     * @param zone
     *            zone name
     * @param searchAgainstColls
     *            flag set to true when searching collections and false when searching data
     *            data objects
     * @return total number of items matching a file properties search criteria
     * @throws DataGridConnectionRefusedException
     */
    private int countItemsMatchingFileProperties(List<DataGridFilePropertySearch> filePropertiesSearch, String zone, boolean searchAgainstColls)
            throws DataGridConnectionRefusedException {

        int totalItems = 0;

        SpecificQueryAO specificQueryAO = null;
        SpecificQuery specQuery = null;
        SpecificQueryResultSet queryResultSet = null;
        String userSQLAlias = "metalnxUserQuery_" + System.currentTimeMillis();

        try {
            specificQueryAO = adminServices.getSpecificQueryAO();

            StringBuilder query = new StringBuilder();
            String tableName = "SELECT r_data_main.data_name as name, r_data_main.data_repl_num as repl_num,"
                    + "	r_data_main.data_owner_name as owner_name, r_data_main.data_owner_zone as owner_zone, "
                    + "	r_data_main.data_size as size, r_data_main.resc_name, "
                    + " CASE WHEN r_coll_main.parent_coll_name = '/' THEN '/' || r_data_main.data_name ELSE r_coll_main.coll_name || '/' || r_data_main.data_name END as path, "
                    + "	r_data_main.data_checksum as checksum, CAST(r_data_main.create_ts AS BIGINT),  "
                    + "	CAST(r_data_main.modify_ts AS BIGINT) FROM r_data_main INNER JOIN r_coll_main ON "
                    + "	r_data_main.coll_id = r_coll_main.coll_id";
            if (searchAgainstColls) {
                tableName = "SELECT replace(r_coll_main.coll_name, r_coll_main.parent_coll_name || '/', '') AS name, 0 AS repl_num,"
                        + "	r_coll_main.coll_owner_name AS owner_name, r_coll_main.coll_owner_zone AS owner_zone, 0 AS size, "
                        + "	'' AS resc_name, r_coll_main.coll_name AS path, '' AS checksum, "
                        + "	CAST(r_coll_main.create_ts AS BIGINT), CAST(r_coll_main.modify_ts AS BIGINT) FROM r_coll_main  ";
            }
            query.append("SELECT COUNT(*) FROM	( " + tableName + " ) AS fileProperties  WHERE");

            for (int i = 0; i < filePropertiesSearch.size(); i++) {
                query.append(filePropertiesSearch.get(i).getWhereClause());

                if (i < filePropertiesSearch.size() - 1) {
                    query.append(" AND ");
                }
            }

            // Creating Specific Query instance
            SpecificQueryDefinition queryDef = new SpecificQueryDefinition();
            queryDef.setAlias(userSQLAlias);
            queryDef.setSql(query.toString());

            // Creating spec query on iRODS
            specificQueryAO.addSpecificQuery(queryDef);

            specQuery = SpecificQuery.instanceWithNoArguments(userSQLAlias, 0, zone);

            logger.info("Specific query: {}", query.toString());

            queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery, 99999, 0);

            // after running the user specific query, we need to remove from the database
            specificQueryAO.removeSpecificQueryByAlias(userSQLAlias);

            totalItems = DataGridUtils.mapCountQueryResultSetToInteger(queryResultSet);

        }
        catch (JargonException e) {
            logger.error("Could not get specific query: ", e);
        }
        catch (JargonQueryException e) {
            logger.error("Could not get specific query: ", e);
        }

        return totalItems;
    }

    @Override
    public SpecificQueryResultSet searchByFileProperties(List<DataGridFilePropertySearch> filePropertySearch, String zone,
            boolean searchAgainstColls, DataGridPageContext pageContext, int offset, int limit) throws DataGridConnectionRefusedException {

        SpecificQueryAO specificQueryAO = null;
        SpecificQuery specQuery = null;
        SpecificQueryResultSet queryResultSet = null;
        String userSQLAlias = "metalnxUserQuery_" + System.currentTimeMillis();

        try {
            specificQueryAO = adminServices.getSpecificQueryAO();

            StringBuilder query = new StringBuilder();
            query.append(getSpecificQueryForFilePropertySearch(filePropertySearch, zone, searchAgainstColls));
            query.append(" OFFSET ");
            query.append(offset);
            query.append(" LIMIT ");
            query.append(limit);

            // Creating Specific Query instance
            SpecificQueryDefinition queryDef = new SpecificQueryDefinition();
            queryDef.setAlias(userSQLAlias);
            queryDef.setSql(query.toString());

            // Creating spec query on iRODS
            specificQueryAO.addSpecificQuery(queryDef);

            specQuery = SpecificQuery.instanceWithNoArguments(userSQLAlias, 0, zone);

            logger.info("Specific query: {}", query.toString());

            queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery, 99999, 0);
        }
        catch (JargonException e) {
            logger.error("Could not get specific query: ", e);
        }
        catch (JargonQueryException e) {
            logger.error("Could not get specific query: ", e);
        }
        finally {
            try {
                // after running the user specific query, we need to remove from the database
                specificQueryAO.removeSpecificQueryByAlias(userSQLAlias);
            }
            catch (JargonException e) {
                logger.error("Could not remove specific query {}: ", userSQLAlias, e.getMessage());
            }
        }

        return queryResultSet;
    }

    @Override
    public int countCollectionsMatchingFileProperties(List<DataGridFilePropertySearch> filePropertiesSearch, String zone)
            throws DataGridConnectionRefusedException {

        return countItemsMatchingFileProperties(filePropertiesSearch, zone, true);
    }

    @Override
    public int countDataObjectsMatchingFileProperties(List<DataGridFilePropertySearch> filePropertiesSearch, String zone)
            throws DataGridConnectionRefusedException {

        return countItemsMatchingFileProperties(filePropertiesSearch, zone, false);
    }
}
