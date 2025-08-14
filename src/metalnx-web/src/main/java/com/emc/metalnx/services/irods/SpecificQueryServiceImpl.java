 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


/**
 * 
 */
package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.entity.DataGridSpecificQuery;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.SpecificQueryService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.domain.SpecificQueryDefinition;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.SpecificQuery;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SpecificQueryServiceImpl implements SpecificQueryService {

	@Autowired
	private IRODSServices irodsServices;
	
	private static final Logger logger = LogManager.getLogger(SpecificQueryServiceImpl.class);
	
	private final DataGridSpecificQuery createDataGridSpecificQuery(SpecificQueryDefinition specificQueryFromIrods) {
		DataGridSpecificQuery newQuery = new DataGridSpecificQuery();
		newQuery.setAlias(specificQueryFromIrods.getAlias());
		newQuery.setQuery(specificQueryFromIrods.getSql());
		return newQuery;
	}
	
	private final List<DataGridSpecificQuery> createDataGridSpecificQueryList(List<SpecificQueryDefinition> specificQueriesFromIrods) {
		List<DataGridSpecificQuery> specificQueries = new ArrayList<DataGridSpecificQuery>();
		for (SpecificQueryDefinition specificQueryFromIrods : specificQueriesFromIrods) {
			DataGridSpecificQuery newQuery = createDataGridSpecificQuery(specificQueryFromIrods);
			specificQueries.add(newQuery);
		}
		return specificQueries;
	}
	
	@Override
	public List<DataGridSpecificQuery> findAll() throws DataGridConnectionRefusedException {
		return findByAliasLike("%");
	}

	@Override
	public DataGridSpecificQuery findByAlias(String alias) throws DataGridConnectionRefusedException {
		SpecificQueryAO specificQueryAO = irodsServices.getSpecificQueryAO();
		SpecificQueryDefinition query;
		try {
			query = specificQueryAO.findSpecificQueryByAlias(alias);
			return createDataGridSpecificQuery(query);
		} catch (JargonException e) {
			logger.error("Could not retrieve specific query with alias {}", alias, e);
		}
		return null;
	}

	@Override
	public List<DataGridSpecificQuery> findByAliasLike(String like) 
		throws DataGridConnectionRefusedException {
		
		SpecificQueryAO specificQueryAO = irodsServices.getSpecificQueryAO();
		
		try {
			List<SpecificQueryDefinition> specificQueriesFromIrods = specificQueryAO.listSpecificQueryByAliasLike(like);
			return createDataGridSpecificQueryList(specificQueriesFromIrods);
		} catch (Exception e) {
			logger.error("Could not find any specific query definition", e);
		}
		
		return null;
	}

	@Override
	public SpecificQueryResultSet executeSpecificQuery(DataGridSpecificQuery specificQuery, 
		String zone) throws DataGridConnectionRefusedException {
		
		SpecificQueryAO specificQueryAO = irodsServices.getSpecificQueryAO();
		
		try {
			SpecificQuery query = SpecificQuery.instanceWithNoArguments(specificQuery.getQuery(), 0, zone);
			return specificQueryAO.executeSpecificQueryUsingSql(query, 1000);
		} catch (JargonException | JargonQueryException e) {
			logger.error("Could not execute specific query {}", specificQuery.getAlias(), e);
		}
		
		return null;
	}

	@Override
	public boolean createSpecificQuery(DataGridSpecificQuery specificQuery) 
		throws DataGridConnectionRefusedException {
		
		SpecificQueryAO specificQueryAO = irodsServices.getSpecificQueryAO();
		try {
			SpecificQueryDefinition newQuery = new SpecificQueryDefinition(specificQuery.getAlias(), specificQuery.getQuery());
			specificQueryAO.addSpecificQuery(newQuery);
			return true;
		} catch (JargonException e) {
			logger.error("Could not create specific query {}", specificQuery.getAlias(), e);
		}
		
		return false;
	}
	
	@Override
	public boolean updateSpecificQuery(DataGridSpecificQuery specificQuery) 
		throws DataGridConnectionRefusedException {
		
		SpecificQueryAO specificQueryAO = irodsServices.getSpecificQueryAO();
		try {
			
			// Jargon does not support SpecificQueries update, so we'll remove and create
			// a new one.
			specificQueryAO.removeSpecificQueryByAlias(specificQuery.getAlias());
			this.createSpecificQuery(specificQuery);
			return true;
		} catch (JargonException e) {
			logger.error("Could not create specific query {}", specificQuery.getAlias(), e);
		}
		return false;
	}

	@Override
	public boolean removeSpecificQueryByAlias(DataGridSpecificQuery specificQuery) 
		throws DataGridConnectionRefusedException {
		
		SpecificQueryAO specificQueryAO = irodsServices.getSpecificQueryAO();
		try {
			specificQueryAO.removeSpecificQueryByAlias(specificQuery.getAlias());
			return true;
		} catch (JargonException e) {
			logger.error("Could not remove specific query {}", specificQuery.getAlias(), e);
		}
		return false;
	}

}
