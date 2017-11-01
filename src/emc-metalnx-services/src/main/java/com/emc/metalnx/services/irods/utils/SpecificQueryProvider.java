/**
 * 
 */
package com.emc.metalnx.services.irods.utils;

import java.util.List;

import com.emc.metalnx.core.domain.entity.DataGridFilePropertySearch;
import com.emc.metalnx.core.domain.entity.DataGridMetadataSearch;
import com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum;
import com.emc.metalnx.core.domain.entity.enums.FilePropertyField;

/**
 * Represents a provider of specific query sql for a given database flavor,
 * creating sql statements that can be used via specific query
 * 
 * @author Mike Conway - NIEHS
 *
 */
public interface SpecificQueryProvider {

	/**
	 * Creates a specific query based on the metadata search criteria.
	 *
	 * @param metadataSearch
	 *            <code>List</code> of {@link DataGridMetadataSearch} with the user
	 *            supplied search terms search criteria
	 * @param zone
	 *            <code>String</code> zone to be looking for data objects or
	 *            collections
	 * @param searchAgainstColls
	 *            <code>boolean</code> Flag set to <code>true</code> when looking
	 *            for collections. <code>false</code> when looking for data objects.
	 * @return Specific query string
	 */
	String buildSpecificQueryForMetadataSearch(List<DataGridMetadataSearch> metadataSearch, String zone,
			boolean searchAgainstColls);

	/**
	 * For a data grid property search, build a 'where' clause for the appropriate
	 * database flavor
	 * 
	 * @param attribute
	 *            {@link FilePropertyField} specifying the data grid property search
	 * @param operator
	 *            {@link DataGridSearchOperatorEnum} with the operator for the query
	 * @param value
	 *            <code>String</code> with the value for the search
	 * @return <code>String</code> with the SQL text
	 */
	String buildWhereClauseForDataGridPropertySearch(final FilePropertyField attribute,
			final DataGridSearchOperatorEnum operator, final String value);

	/**
	 * Given a query, add the offset and limit sql terms to the end of the query
	 * 
	 * @param query
	 *            <code>String</code> with the database query
	 * @param offset
	 *            <code>int</code> with the offset
	 * @param limit
	 *            <code>int</code> with the limit
	 * @return <code>String</code> with the appended offset and limit bits
	 */
	String addOffsetAndLimitToQuery(final String query, final int offset, final int limit);

	/**
	 * build the query for obtaining a count of items matching a metadata search
	 * 
	 * @param metadataSearch
	 *            <code>List</code> of {@link DataGridMetadataSearch} with the user
	 *            supplied search terms search criteria
	 * @param zone
	 *            <code>String</code> zone to be looking for data objects or
	 *            collections
	 * @param searchAgainstColls
	 *            <code>boolean</code> Flag set to <code>true</code> when looking
	 *            for collections. <code>false</code> when looking for data objects.
	 * @return <code>String</code> with the appropriate sql query
	 */
	String buildQueryForCountOfItemsMatchingMetadataSearch(final List<DataGridMetadataSearch> metadataSearch,
			final String zone, final boolean searchAgainstColls);

	/**
	 * build the query for obtaining a count of items matching a file properties
	 * search
	 * 
	 * @param filePropertiesSearch
	 *            <code>List</code> of {@link DataGridFilePropertySearch} that
	 *            constitute the query
	 * @param zone
	 *            <code>String</code> zone to be looking for data objects or
	 *            collections
	 * @param searchAgainstColls
	 *            <code>boolean</code> Flag set to <code>true</code> when looking
	 *            for collections. <code>false</code> when looking for data objects.
	 * @return <code>String</code> with the appropriate sql query
	 */
	String buildQueryCountItemsMatchingPropertiesSearch(final List<DataGridFilePropertySearch> filePropertiesSearch,
			final String zone, final boolean searchAgainstColls);

	/**
	 * Build the sql select clause for a data objects properties search
	 * 
	 * @return <code>String</code> with the select clause
	 */
	String buildSelectClauseForDataObjectsForPropertiesSearch();

	/**
	 * Build the sql select clause for a collections properties search
	 * 
	 * @return <code>String</code> with the select clause
	 */
	String buildSelectClauseForCollectionsForPropertiesSearch();

}
