package com.emc.metalnx.services.interfaces;

import java.util.List;

import org.irods.jargon.core.query.SpecificQueryResultSet;

import com.emc.metalnx.core.domain.entity.DataGridSpecificQuery;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

public interface SpecificQueryService {

	/**
	 * Creates a Specific Query object on the underlying datagrid system
	 * @param specificQuery
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	boolean createSpecificQuery(DataGridSpecificQuery specificQuery) throws DataGridConnectionRefusedException;
	
	/**
	 * Updates a Specific Query object on the underlying datagrid system
	 * @param specificQuery
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	boolean updateSpecificQuery(DataGridSpecificQuery specificQuery) throws DataGridConnectionRefusedException;
	
	/**
	 * Lists all the specific queries on the data grid system
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	List<DataGridSpecificQuery> findAll() throws DataGridConnectionRefusedException;
	
	/**
	 * Lists all the specific queries on the data grid system matching
	 * the alias name
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	DataGridSpecificQuery findByAlias(String alias) throws DataGridConnectionRefusedException;
	
	/**
	 * Lists all the specific queries on the data grid system matching
	 * part of the alias with the 'like' string argument.
	 * @param like
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	List<DataGridSpecificQuery> findByAliasLike(String like) throws DataGridConnectionRefusedException;
	
	/**
	 * Executes the specific query on the underlying data grid system.
	 * @param specificQuery
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	SpecificQueryResultSet executeSpecificQuery(DataGridSpecificQuery specificQuery, String zone) 
		throws DataGridConnectionRefusedException;
	
	/**
	 * Removes a specific query object from the underlying data grid system.
	 * @param specificQuery
	 * @return
	 * @throws DataGridConnectionRefusedException 
	 */
	boolean removeSpecificQueryByAlias(DataGridSpecificQuery specificQuery) throws DataGridConnectionRefusedException;
	
}
