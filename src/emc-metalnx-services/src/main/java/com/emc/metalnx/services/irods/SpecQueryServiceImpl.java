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

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.domain.ClientHints;
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
import com.emc.metalnx.core.domain.exceptions.UnsupportedDataGridFeatureException;
import com.emc.metalnx.services.interfaces.AdminServices;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.SpecQueryService;
import com.emc.metalnx.services.irods.utils.SpecificQueryProvider;
import com.emc.metalnx.services.irods.utils.SpecificQueryProviderFactory;
import com.emc.metalnx.services.irods.utils.SpecificQueryProviderFactoryImpl;
import com.emc.metalnx.services.machine.util.DataGridUtils;

@Service
@Transactional
public class SpecQueryServiceImpl implements SpecQueryService {
	@Autowired
	AdminServices adminServices;

	@Autowired
	IRODSServices irodsServices;

	private static final Logger logger = LoggerFactory.getLogger(SpecQueryServiceImpl.class);
	/*
	 * This will switch based on the iCat to create sql statements for various back
	 * end databases
	 */
	private final SpecificQueryProviderFactory specificQueryProviderFactory = new SpecificQueryProviderFactoryImpl();

	@Override
	public int countCollectionsMatchingMetadata(List<DataGridMetadataSearch> metadataSearch, String zone)
			throws DataGridConnectionRefusedException, JargonException {
		return countItemsMatchingMetadata(metadataSearch, zone, true);
	}

	@Override
	public int countDataObjectsMatchingMetadata(List<DataGridMetadataSearch> metadataSearch, String zone)
			throws DataGridConnectionRefusedException, JargonException {
		return countItemsMatchingMetadata(metadataSearch, zone, false);
	}

	@Override
	public void deleteSpecQueryByAlias(String specQueryAlias) throws DataGridConnectionRefusedException {

		SpecificQueryAO specificQueryAO = null;

		try {
			specificQueryAO = adminServices.getSpecificQueryAO();

			specificQueryAO.removeSpecificQueryByAlias(specQueryAlias);
		} catch (JargonException e) {
			logger.error("Could not get specific query: ", e.getMessage());
		}
	}

	@Override
	public SpecificQueryResultSet searchByMetadata(List<DataGridMetadataSearch> metadataSearch, String zone,
			boolean searchAgainstColls, DataGridPageContext pageContext, int offset, int limit)
			throws DataGridConnectionRefusedException, JargonException {

		SpecificQueryAO specificQueryAO = null;
		SpecificQuery specQuery = null;
		SpecificQueryResultSet queryResultSet = null;
		String userSQLAlias = "metalnxUserQuery_" + System.currentTimeMillis();

		try {
			specificQueryAO = adminServices.getSpecificQueryAO();

			ClientHints clientHints = this.irodsServices.getEnvironmentalInfoAO().retrieveClientHints(false);
			SpecificQueryProvider provider = specificQueryProviderFactory.instance(clientHints.whatTypeOfIcatIsIt());
			String query = provider.buildSpecificQueryForMetadataSearch(metadataSearch, zone, searchAgainstColls);

			// Creating Specific Query instance
			SpecificQueryDefinition queryDef = new SpecificQueryDefinition();
			queryDef.setAlias(userSQLAlias);
			queryDef.setSql(query);

			// Creating spec query on iRODS
			specificQueryAO.addSpecificQuery(queryDef);

			specQuery = SpecificQuery.instanceWithNoArguments(userSQLAlias, 0, zone);

			logger.info("Specific query: {}", query.toString());

			queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery, 99999, 0);
		} catch (JargonException e) {
			logger.error("Could not get specific query: ", e);
			throw e;
		} catch (JargonQueryException e) {
			logger.error("Could not get specific query: ", e);
			throw new JargonException(e);
		} catch (UnsupportedDataGridFeatureException e) {
			logger.error("Could not get specific query: ", e);
			throw new JargonException(e);
		} finally {
			try {
				// after running the user specific query, we need to remove from the database
				specificQueryAO.removeSpecificQueryByAlias(userSQLAlias);
			} catch (JargonException e) {
				logger.error("Could not remove specific query {}: ", userSQLAlias, e.getMessage());
			}
		}

		return queryResultSet;
	}

	/**
	 * Counts the number of items matching a metadata search criteria.
	 *
	 * @param metadataSearch
	 *            metadata criteria
	 * @param zone
	 *            zone name
	 * @param searchAgainstColls
	 *            flag set to true when searching collections and false when
	 *            searching data data objects
	 * @return total number of items matching a metadata search criteria
	 * @throws DataGridConnectionRefusedException
	 * @throws JargonException
	 */
	private int countItemsMatchingMetadata(List<DataGridMetadataSearch> metadataSearch, String zone,
			boolean searchAgainstColls) throws DataGridConnectionRefusedException, JargonException {

		int totalItems = 0;

		SpecificQueryAO specificQueryAO = null;
		SpecificQuery specQuery = null;
		SpecificQueryResultSet queryResultSet = null;
		String userSQLAlias = "metalnxUserQuery_" + System.currentTimeMillis();

		try {
			specificQueryAO = adminServices.getSpecificQueryAO();

			ClientHints clientHints = this.irodsServices.getEnvironmentalInfoAO().retrieveClientHints(false);
			SpecificQueryProvider provider = specificQueryProviderFactory.instance(clientHints.whatTypeOfIcatIsIt());
			String query = provider.buildQueryForCountOfItemsMatchingMetadataSearch(metadataSearch, zone,
					searchAgainstColls);

			// Creating Specific Query instance
			SpecificQueryDefinition queryDef = new SpecificQueryDefinition();
			queryDef.setAlias(userSQLAlias);
			queryDef.setSql(query);

			// Creating spec query on iRODS
			specificQueryAO.addSpecificQuery(queryDef);
			specQuery = SpecificQuery.instanceWithNoArguments(userSQLAlias, 0, zone);
			logger.info("Specific query: {}", query.toString());

			queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery, 99999, 0);

			// after running the user specific query, we need to remove from the database
			specificQueryAO.removeSpecificQueryByAlias(userSQLAlias);

			totalItems = DataGridUtils.mapCountQueryResultSetToInteger(queryResultSet);
		} catch (JargonException e) {
			logger.error("Could not get specific query: ", e);
			throw e;
		} catch (JargonQueryException e) {
			logger.error("Could not get specific query: ", e);
			throw new JargonException(e);
		} catch (UnsupportedDataGridFeatureException e) {
			logger.error("Could not get specific query: ", e);
			throw new JargonException(e);
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
	 *            flag set to true when searching collections and false when
	 *            searching data data objects
	 * @return total number of items matching a file properties search criteria
	 * @throws DataGridConnectionRefusedException
	 */
	private int countItemsMatchingFileProperties(List<DataGridFilePropertySearch> filePropertiesSearch, String zone,
			boolean searchAgainstColls) throws DataGridConnectionRefusedException {

		int totalItems = 0;

		SpecificQueryAO specificQueryAO = null;
		SpecificQuery specQuery = null;
		SpecificQueryResultSet queryResultSet = null;
		String userSQLAlias = "metalnxUserQuery_" + System.currentTimeMillis();

		// TODO: put into factory to create statement

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
				query.append(filePropertiesSearch.get(i).getWhereClause()); // this is now in spec query provider as
																			// buildQueryForFilePropertiesSearch

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

		} catch (JargonException e) {
			logger.error("Could not get specific query: ", e);
		} catch (JargonQueryException e) {
			logger.error("Could not get specific query: ", e);
		}

		return totalItems;
	}

	@Override
	public SpecificQueryResultSet searchByFileProperties(List<DataGridFilePropertySearch> filePropertySearch,
			String zone, boolean searchAgainstColls, DataGridPageContext pageContext, int offset, int limit)
			throws DataGridConnectionRefusedException, JargonException {

		SpecificQueryAO specificQueryAO = null;
		SpecificQuery specQuery = null;
		SpecificQueryResultSet queryResultSet = null;
		String userSQLAlias = "metalnxUserQuery_" + System.currentTimeMillis();

		try {
			specificQueryAO = adminServices.getSpecificQueryAO();

			ClientHints clientHints = this.irodsServices.getEnvironmentalInfoAO().retrieveClientHints(false);
			SpecificQueryProvider provider = specificQueryProviderFactory.instance(clientHints.whatTypeOfIcatIsIt());
			String query = provider.buildQueryForFilePropertiesSearch(filePropertySearch, zone, searchAgainstColls,
					offset, limit);

			// Creating Specific Query instance
			SpecificQueryDefinition queryDef = new SpecificQueryDefinition();
			queryDef.setAlias(userSQLAlias);
			queryDef.setSql(query);

			// Creating spec query on iRODS
			specificQueryAO.addSpecificQuery(queryDef);

			specQuery = SpecificQuery.instanceWithNoArguments(userSQLAlias, 0, zone);

			logger.info("Specific query: {}", query.toString());

			queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery, 99999, 0);
		} catch (JargonException e) {
			logger.error("Could not get specific query: ", e);
			throw e;
		} catch (JargonQueryException e) {
			logger.error("Could not get specific query: ", e);
			throw new JargonException(e);
		} catch (UnsupportedDataGridFeatureException e) {
			logger.error("Could not get specific query: ", e);
			throw new JargonException(e);
		} finally {
			try {
				// after running the user specific query, we need to remove from the database
				specificQueryAO.removeSpecificQueryByAlias(userSQLAlias);
			} catch (JargonException e) {
				logger.error("Could not remove specific query {}: ", userSQLAlias, e.getMessage());
			}
		}

		return queryResultSet;
	}

	@Override
	public int countCollectionsMatchingFileProperties(List<DataGridFilePropertySearch> filePropertiesSearch,
			String zone) throws DataGridConnectionRefusedException {

		return countItemsMatchingFileProperties(filePropertiesSearch, zone, true);
	}

	@Override
	public int countDataObjectsMatchingFileProperties(List<DataGridFilePropertySearch> filePropertiesSearch,
			String zone) throws DataGridConnectionRefusedException {

		return countItemsMatchingFileProperties(filePropertiesSearch, zone, false);
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
}
