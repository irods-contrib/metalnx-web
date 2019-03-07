/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

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
	private static final int MAX_QUERY_ROWS = 256

	;

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
	public SpecificQueryResultSet searchByMetadata(final List<DataGridMetadataSearch> metadataSearch, final String zone,
			final boolean searchAgainstColls, final DataGridPageContext pageContext, final int offset, final int limit)
			throws DataGridConnectionRefusedException, JargonException {

		logger.info("searchByMetadata()");

		if (metadataSearch == null || metadataSearch.isEmpty()) {
			throw new IllegalArgumentException("null or empty metadataSearch");
		}

		if (pageContext == null) {
			throw new IllegalArgumentException("null pageContext");
		}

		logger.info("offset:{}", offset);
		logger.info("limit:{}", limit);

		if (offset < 0) {
			throw new IllegalArgumentException("offset cannot be < 0");
		}

		if (limit <= 0) {
			throw new IllegalArgumentException("limit cannot be <= 0");
		}

		logger.info("metadataSearch:{}", metadataSearch);
		logger.info("zone:{}", zone);
		logger.info("searchAgainstColls:{}", searchAgainstColls);
		logger.info("pageContext:{}", pageContext);

		SpecificQueryAO specificQueryAO = null;
		SpecificQuery specQuery = null;
		SpecificQueryResultSet queryResultSet = null;
		String userSQLAlias = "metalnxUserQuery_" + System.currentTimeMillis();

		try {
			specificQueryAO = adminServices.getSpecificQueryAO();

			ClientHints clientHints = this.irodsServices.getEnvironmentalInfoAO().retrieveClientHints(false);
			SpecificQueryProvider provider = specificQueryProviderFactory.instance(clientHints.whatTypeOfIcatIsIt());
			String query = provider.buildSpecificQueryForMetadataSearch(metadataSearch, zone, searchAgainstColls,
					offset, limit);

			// Creating Specific Query instance
			SpecificQueryDefinition queryDef = new SpecificQueryDefinition();
			queryDef.setAlias(userSQLAlias);
			queryDef.setSql(query);

			// Creating spec query on iRODS
			specificQueryAO.addSpecificQuery(queryDef);

			specQuery = SpecificQuery.instanceWithNoArguments(userSQLAlias, 0, zone);

			logger.info("Specific query: {}", query.toString());

			queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery, limit, offset);
		} catch (JargonException e) {
			logger.error("Could not get specific query: ", e);
			throw e;
		} catch (JargonQueryException e) {
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
	 * @param metadataSearch     metadata criteria
	 * @param zone               zone name
	 * @param searchAgainstColls flag set to true when searching collections and
	 *                           false when searching data data objects
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

			queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery, MAX_QUERY_ROWS, 0);

			// after running the user specific query, we need to remove from the database
			specificQueryAO.removeSpecificQueryByAlias(userSQLAlias);

			totalItems = DataGridUtils.mapCountQueryResultSetToInteger(queryResultSet);
		} catch (JargonException e) {
			logger.error("Could not get specific query: ", e);
			throw e;
		} catch (JargonQueryException e) {
			logger.error("Could not get specific query: ", e);
			throw new JargonException(e);
		}

		return totalItems;
	}

	/**
	 * Counts the number of items matching a file properties search criteria.
	 *
	 * @param filePropertiesSearch filePropertiesSearch criteria
	 * @param zone                 zone name
	 * @param searchAgainstColls   flag set to true when searching collections and
	 *                             false when searching data data objects
	 * @return total number of items matching a file properties search criteria
	 * @throws DataGridConnectionRefusedException
	 * @throws UnsupportedDataGridFeatureException
	 * @throws JargonException
	 */
	private int countItemsMatchingFileProperties(List<DataGridFilePropertySearch> filePropertiesSearch, String zone,
			boolean searchAgainstColls)
			throws DataGridConnectionRefusedException, UnsupportedDataGridFeatureException, JargonException {

		logger.info("countItemsMatchingFileProperties()");
		int totalItems = 0;

		SpecificQueryAO specificQueryAO = null;
		SpecificQuery specQuery = null;
		SpecificQueryResultSet queryResultSet = null;
		String userSQLAlias = "metalnxUserQuery_" + System.currentTimeMillis();

		try {
			specificQueryAO = adminServices.getSpecificQueryAO();

			ClientHints clientHints = this.irodsServices.getEnvironmentalInfoAO().retrieveClientHints(false);
			SpecificQueryProvider provider = specificQueryProviderFactory.instance(clientHints.whatTypeOfIcatIsIt());
			String query = provider.buildQueryCountItemsMatchingPropertiesSearch(filePropertiesSearch, zone,
					searchAgainstColls);

			logger.debug("query:{}", query);

			// Creating Specific Query instance
			SpecificQueryDefinition queryDef = new SpecificQueryDefinition();
			queryDef.setAlias(userSQLAlias);
			queryDef.setSql(query);

			// Creating spec query on iRODS
			specificQueryAO.addSpecificQuery(queryDef);
			specQuery = SpecificQuery.instanceWithNoArguments(userSQLAlias, 0, zone);

			logger.info("Specific query: {}", query.toString());

			queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery, MAX_QUERY_ROWS, 0);

			// after running the user specific query, we need to remove from the database
			specificQueryAO.removeSpecificQueryByAlias(userSQLAlias);

			totalItems = DataGridUtils.mapCountQueryResultSetToInteger(queryResultSet);

		} catch (JargonException e) {
			logger.error("Could not get specific query: ", e);
			throw e;
		} catch (JargonQueryException e) {
			logger.error("Could not get specific query: ", e);
			throw new JargonException(e);
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

			logger.info("Specific query: {}", query);

			queryResultSet = specificQueryAO.executeSpecificQueryUsingAlias(specQuery, MAX_QUERY_ROWS, 0);
		} catch (JargonException e) {
			logger.error("Could not get specific query: ", e);
			throw e;
		} catch (JargonQueryException e) {
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
			String zone)
			throws DataGridConnectionRefusedException, UnsupportedDataGridFeatureException, JargonException {

		return countItemsMatchingFileProperties(filePropertiesSearch, zone, true);
	}

	@Override
	public int countDataObjectsMatchingFileProperties(List<DataGridFilePropertySearch> filePropertiesSearch,
			String zone)
			throws DataGridConnectionRefusedException, UnsupportedDataGridFeatureException, JargonException {

		return countItemsMatchingFileProperties(filePropertiesSearch, zone, false);
	}

	/**
	 * @return the irodsServices
	 */
	public IRODSServices getIrodsServices() {
		return irodsServices;
	}

	/**
	 * @param irodsServices the irodsServices to set
	 */
	public void setIrodsServices(IRODSServices irodsServices) {
		this.irodsServices = irodsServices;
	}

	/**
	 * @return the adminServices
	 */
	public AdminServices getAdminServices() {
		return adminServices;
	}

	/**
	 * @param adminServices the adminServices to set
	 */
	public void setAdminServices(AdminServices adminServices) {
		this.adminServices = adminServices;
	}
}
