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
 * @author Mike Conway - NIEHS
 *
 */
public class MysqlSpecificQueryProviderImpl implements SpecificQueryProvider {

	/**
	 * 
	 */
	public MysqlSpecificQueryProviderImpl() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.emc.metalnx.services.irods.utils.SpecificQueryProvider#
	 * buildSpecificQueryForMetadataSearch(java.util.List, java.lang.String,
	 * boolean)
	 */
	@Override
	public String buildSpecificQueryForMetadataSearch(List<DataGridMetadataSearch> metadataSearch, String zone,
			boolean searchAgainstColls, final int offset, final int limit) {
		// This is the first part of the query. It selects all objects (collections or
		// data objects)
		// that has a piece of metadata matching the query
		StringBuilder objQuery = new StringBuilder();

		// This is the query that actually looks for metadata tags in the data grid.
		StringBuilder metadataSearchQuery = new StringBuilder();

		// This is the last part of the query. It groups the objects by their attributes
		// in order to
		// have unique results.
		StringBuilder gb = new StringBuilder();

		// This is the final query that will be sent to the data grid database
		StringBuilder q = new StringBuilder();

		if (searchAgainstColls) {
			objQuery.append(" SELECT obj_name, parent_path, obj_owner, create_ts, modify_ts, resc_name, totalMatches");
			objQuery.append(" FROM (");
			objQuery.append(" SELECT c.coll_name as obj_name,");
			objQuery.append("      c.parent_coll_name as parent_path,");
			objQuery.append("      c.coll_owner_name as obj_owner,");
			objQuery.append("      c.create_ts as create_ts,");
			objQuery.append("      c.modify_ts as modify_ts,");
			objQuery.append("      '' as resc_name,");
			objQuery.append("      c.coll_inheritance,");
			objQuery.append("      COUNT(c.coll_name) as totalMatches");
			objQuery.append(" FROM ");
			objQuery.append("      R_COLL_MAIN c ");
			objQuery.append(" JOIN ( ");

			gb.append(" ) AS coll_metadata ON (c.coll_id = map_object_id) ");
			gb.append(" GROUP BY ");
			gb.append("      c.coll_name,");
			gb.append("      c.parent_coll_name,");
			gb.append("      c.coll_owner_name,");
			gb.append("      c.create_ts,");
			gb.append("      c.modify_ts,");
			gb.append("      resc_name,");
			gb.append("      c.coll_inheritance");
			gb.append(" ORDER BY totalMatches DESC, c.coll_name ");
			gb.append(" ) AS ms ");

			if (limit > 0) {
				gb.append(" LIMIT ");
				gb.append(limit);
			}

			if (offset > 0) {
				gb.append(" OFFSET ");
				gb.append(offset);
			}

		} else {
			objQuery.append(
					" SELECT obj_name, size, obj_owner, repl_num, create_ts, modify_ts, resc_name, parent_path, totalMatches");
			objQuery.append(" FROM (");
			objQuery.append(" SELECT ");
			objQuery.append("      d.data_name as obj_name, ");
			objQuery.append("      d.data_size as size,  ");
			objQuery.append("      d.data_owner_name as obj_owner, ");
			objQuery.append("      d.data_repl_num as repl_num, ");
			objQuery.append("      d.create_ts as create_ts,");
			objQuery.append("      d.modify_ts as modify_ts, ");
			objQuery.append("      d.resc_name as resc_name, ");
			objQuery.append("      c.coll_name as parent_path,");
			objQuery.append("      COUNT(d.data_name) as totalMatches");
			objQuery.append(" FROM R_DATA_MAIN d ");
			objQuery.append(" JOIN R_COLL_MAIN c ON (d.coll_id = c.coll_id) ");
			objQuery.append(" JOIN ( ");

			gb.append(" ) AS data_obj_metadata ON (d.data_id = map_object_id) ");
			gb.append(" GROUP BY ");
			gb.append("      d.data_name,");
			gb.append("      d.data_size,");
			gb.append("      d.data_owner_name,");
			gb.append("      d.data_repl_num,");
			gb.append("      d.create_ts,");
			gb.append("      d.modify_ts,");
			gb.append("      d.resc_name, ");
			gb.append("      c.coll_name");
			gb.append(" ORDER BY totalMatches DESC, d.data_name ");
			gb.append(" ) AS ms ");

			if (limit > 0) {
				gb.append(" LIMIT ");
				gb.append(limit);
			}

			if (offset > 0) {
				gb.append(" OFFSET ");
				gb.append(offset);
			}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.emc.metalnx.services.irods.utils.SpecificQueryProvider#
	 * buildWhereClauseForDataGridPropertySearch(com.emc.metalnx.core.domain.entity.
	 * enums.FilePropertyField,
	 * com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum,
	 * java.lang.String)
	 */
	@Override
	public String buildWhereClauseForDataGridPropertySearch(FilePropertyField inAttribute,
			DataGridSearchOperatorEnum inOperator, String inValue) {

		String whereClause = new String();

		String attribute = inAttribute.getFieldName().replaceAll(SpecificQueryConstants.propFieldsRegex, "");
		String operator = inOperator.toString();
		String value = inValue.replaceAll(SpecificQueryConstants.regexForValue, "");
		boolean isAttributeEqualsDate = inAttribute == FilePropertyField.CREATION_DATE
				|| inAttribute == FilePropertyField.MODIFICATION_DATE;

		if (inOperator == DataGridSearchOperatorEnum.LIKE || inOperator == DataGridSearchOperatorEnum.NOT_LIKE) {
			whereClause = String.format(" fileProperties.%s %s '%%%s%%'", attribute, operator, value);
		} else if (isAttributeEqualsDate && inOperator == DataGridSearchOperatorEnum.EQUAL) {
			whereClause = String.format(" fileProperties.%s BETWEEN %s AND %d", attribute, value,
					Long.parseLong(value) + 60);
		} else if (isAttributeEqualsDate || inAttribute == FilePropertyField.REPLICA_NUMBER
				|| inAttribute == FilePropertyField.SIZE) {
			whereClause = String.format(" fileProperties.%s %s %s", attribute, operator, value);
		} else {
			whereClause = String.format(" fileProperties.%s %s '%s'", attribute, operator, value);
		}

		return whereClause;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.emc.metalnx.services.irods.utils.SpecificQueryProvider#
	 * addOffsetAndLimitToQuery(java.lang.String, int, int)
	 */
	@Override
	public String addOffsetAndLimitToQuery(String query, int offset, int limit) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.emc.metalnx.services.irods.utils.SpecificQueryProvider#
	 * buildQueryForCountOfItemsMatchingMetadataSearch(java.util.List,
	 * java.lang.String, boolean)
	 */
	@Override
	public String buildQueryForCountOfItemsMatchingMetadataSearch(List<DataGridMetadataSearch> metadataSearch,
			String zone, boolean searchAgainstColls) {
		StringBuilder query = new StringBuilder();

		query.append("SELECT COUNT(*) FROM (");
		query.append(this.buildSpecificQueryForMetadataSearch(metadataSearch, zone, searchAgainstColls, 0, 0));
		query.append(") AS searchMetadata");
		return query.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.emc.metalnx.services.irods.utils.SpecificQueryProvider#
	 * buildQueryCountItemsMatchingPropertiesSearch(java.util.List,
	 * java.lang.String, boolean)
	 */
	@Override
	public String buildQueryCountItemsMatchingPropertiesSearch(List<DataGridFilePropertySearch> filePropertiesSearch,
			String zone, boolean searchAgainstColls) {

		StringBuilder query = new StringBuilder();
		StringBuilder selStringBuilder = new StringBuilder();

		if (searchAgainstColls) {
			selStringBuilder.append(buildSelectClauseForCountCollectionsForPropertiesSearch());
		} else {
			selStringBuilder.append(buildSelectClauseForCountDataObjectsForPropertiesSearch());
		}

		query.append("SELECT COUNT(*) FROM	( ");
		query.append(selStringBuilder);
		query.append(" ) AS fileProperties  WHERE ");

		for (int i = 0; i < filePropertiesSearch.size(); i++) {

			query.append(buildWhereClauseForDataGridPropertySearch(filePropertiesSearch.get(i).getAttribute(),
					filePropertiesSearch.get(i).getOperator(), filePropertiesSearch.get(i).getValue()));

			if (i < filePropertiesSearch.size() - 1) {
				query.append(" AND ");
			}
		}
		return query.toString();
	}

	@Override
	public String buildQueryForFilePropertiesSearch(List<DataGridFilePropertySearch> filePropertiesSearches,
			String zone, boolean searchAgainstColls, int offset, int limit) {

		StringBuilder query = new StringBuilder();

		if (filePropertiesSearches.size() > 0) {
			if (searchAgainstColls) {
				query.append(buildSelectClauseForCollectionsForPropertiesSearch());
			} else {
				query.append(buildSelectClauseForDataObjectsForPropertiesSearch());
			}

			for (DataGridFilePropertySearch filePropertiesSearch : filePropertiesSearches) {

				// where clause - conditions
				query.append(buildWhereClauseForDataGridPropertySearch(filePropertiesSearch.getAttribute(),
						filePropertiesSearch.getOperator(), filePropertiesSearch.getValue()));

				// appending conditions
				if (filePropertiesSearches.indexOf(filePropertiesSearch) != filePropertiesSearches.size() - 1) {
					query.append(" AND ");
				}
			}
		}

		if (offset == 0 && limit == 0) {
			// ignored
		} else {
			query.append(" LIMIT ");
			query.append(limit);
			query.append(" OFFSET ");
			query.append(offset);
		}

		return query.toString();

	}

	/**
	 * select clause for count data objects matching file properties query
	 * 
	 * @return <code>String</code> with select
	 */
	private String buildSelectClauseForCountDataObjectsForPropertiesSearch() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT R_DATA_MAIN.data_name as name, R_DATA_MAIN.data_repl_num as repl_num,");
		sb.append("	R_DATA_MAIN.data_owner_name as owner_name, R_DATA_MAIN.data_owner_zone as owner_zone, ");
		sb.append("	R_DATA_MAIN.data_size as size, R_DATA_MAIN.resc_name, ");
		sb.append(
				" CASE WHEN R_COLL_MAIN.parent_coll_name = '/' THEN CONCAT('/', R_DATA_MAIN.data_name) ELSE CONCAT(R_COLL_MAIN.coll_name, '/', R_DATA_MAIN.data_name) END as path, ");
		sb.append("	R_DATA_MAIN.data_checksum as checksum, CAST(R_DATA_MAIN.create_ts AS DECIMAL),  ");
		sb.append("	CAST(R_DATA_MAIN.modify_ts AS DECIMAL) FROM R_DATA_MAIN INNER JOIN R_COLL_MAIN ON ");
		sb.append("	R_DATA_MAIN.coll_id = R_COLL_MAIN.coll_id");
		return sb.toString();

	}

	/**
	 * select clause for count collections matching file properties query
	 * 
	 * @return <code>String</code> with select
	 */
	private String buildSelectClauseForCountCollectionsForPropertiesSearch() {
		StringBuilder sb = new StringBuilder();

		sb.append(
				"SELECT replace(R_COLL_MAIN.coll_name, CONCAT(R_COLL_MAIN.parent_coll_name, '/'), '') AS name, 0 AS repl_num,");
		sb.append("	R_COLL_MAIN.coll_owner_name AS owner_name, R_COLL_MAIN.coll_owner_zone AS owner_zone, 0 AS size, ");
		sb.append("	'' AS resc_name, R_COLL_MAIN.coll_name AS path, '' AS checksum, ");
		sb.append("	CAST(R_COLL_MAIN.create_ts AS DECIMAL), CAST(R_COLL_MAIN.modify_ts AS DECIMAL) FROM R_COLL_MAIN  ");
		return sb.toString();
	}

	private String buildSelectClauseForDataObjectsForPropertiesSearch() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * 	FROM ( ");
		query.append("	SELECT");
		query.append("  	R_DATA_MAIN.data_name AS name,");
		query.append("  	R_DATA_MAIN.data_repl_num AS repl_num,");
		query.append("  	R_DATA_MAIN.data_owner_name AS owner_name,");
		query.append("  	R_DATA_MAIN.data_owner_zone AS owner_zone,");
		query.append("  	R_DATA_MAIN.data_size AS size,");
		query.append("  	R_DATA_MAIN.resc_name,");
		query.append(
				"  	CASE WHEN R_COLL_MAIN.parent_coll_name = '/' THEN CONCAT('/', R_DATA_MAIN.data_name) ELSE CONCAT(R_COLL_MAIN.coll_name, '/', R_DATA_MAIN.data_name) END AS path,");
		query.append("  	R_DATA_MAIN.data_checksum AS checksum,");
		query.append("  	CAST(R_DATA_MAIN.create_ts AS DECIMAL), ");
		query.append("  	CAST(R_DATA_MAIN.modify_ts AS DECIMAL) ");
		query.append("	FROM");
		query.append("  	R_DATA_MAIN  ");
		query.append("  INNER JOIN  ");
		query.append("  	R_COLL_MAIN  ");
		query.append("  ON  ");
		query.append("  	R_DATA_MAIN.coll_id = R_COLL_MAIN.coll_id  ");
		query.append(" ) AS fileProperties ");
		query.append("WHERE ");

		return query.toString();
	}

	private String buildSelectClauseForCollectionsForPropertiesSearch() {
		StringBuilder query = new StringBuilder();
		query.append("SELECT * 	FROM ( ");
		query.append("	SELECT");
		query.append("  	replace(R_COLL_MAIN.coll_name, CONCAT(R_COLL_MAIN.parent_coll_name, '/'), '') AS name, ");
		query.append(" 		0 AS repl_num, ");
		query.append("  	R_COLL_MAIN.coll_owner_name AS owner_name, ");
		query.append("  	R_COLL_MAIN.coll_owner_zone AS owner_zone, ");
		query.append("  	0 AS size, ");
		query.append("  	'' AS resc_name, ");
		query.append("  	R_COLL_MAIN.coll_name AS path, ");
		query.append("  	'' AS checksum, ");
		query.append("  	CAST(R_COLL_MAIN.create_ts AS DECIMAL), ");
		query.append("  	CAST(R_COLL_MAIN.modify_ts AS DECIMAL) ");
		query.append("	FROM");
		query.append("  	R_COLL_MAIN ");
		query.append(" ) AS fileProperties ");
		query.append("WHERE ");

		return query.toString();
	}

	@Override
	public String buildSelectTotalDataObjectsUnderPathThatMatchSearchText(String parentPath, String searchText) {
		StringBuilder query = new StringBuilder();
		query.append(" SELECT(*) FROM ( ");
		query.append(" select distinct on (d.data_name) ");
		query.append("  d.data_name,");
		query.append("  d.data_id,");
		query.append("  d.data_repl_num,");
		query.append("  d.data_version,");
		query.append("  d.data_type_name,");
		query.append("  d.data_size,");
		query.append("  d.resc_group_name,");
		query.append("  d.resc_name,");
		query.append("  d.data_path,");
		query.append("  d.data_owner_name,");
		query.append("  d.data_owner_zone,");
		query.append("  d.data_is_dirty,");
		query.append("  d.data_status,");
		query.append("  d.data_checksum,");
		query.append("  d.data_expiry_ts,");
		query.append("  d.data_map_id,");
		query.append("  d.data_mode,");
		query.append("  d.r_comment,");
		query.append("  d.create_ts,");
		query.append("  d.modify_ts,");
		query.append("  d.resc_hier ");
		query.append("from ");
		query.append("  R_DATA_MAIN d, ");
		query.append("  R_COLL_MAIN c ");
		query.append("where ");
		query.append("  c.coll_id = d.coll_id ");
		query.append("  AND ");
		query.append("  c.coll_name = ? ");
		query.append("  AND ");
		query.append("  d.data_name LIKE ? ");
		query.append("  ) AS countDataObjectsThatMatchSearchText");
		return query.toString();
	}

	@Override
	public String buildSelectTotalCollectionsUnderPathThatMatchSearchText(String parentPath, String searchText) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT COUNT(*) FROM (");
		query.append("	select ");
		query.append("  c.coll_id,");
		query.append("  c.coll_name,");
		query.append("  c.parent_coll_name,");
		query.append("  c.coll_owner_name,");
		query.append("  c.coll_owner_zone,");
		query.append("  c.coll_inheritance,");
		query.append("  c.coll_type,");
		query.append("  c.r_comment,");
		query.append("  c.create_ts,");
		query.append("  c.modify_ts ");
		query.append("from ");
		query.append("  R_COLL_MAIN c ");
		query.append("where ");
		query.append("  c.coll_name LIKE ?");
		query.append("    and ");
		query.append("  c.parent_coll_name = ? ");
		query.append(") AS searchMatchForCollections");
		return query.toString();
	}

	@Override
	public String buildSelectDataObjectsUnderPathThatMatchSearchText(String parentPath, String searchText, int offset,
			int limit, int orderColumn, String orderDir) {
		StringBuilder query = new StringBuilder();
		query.append(" select ");
		query.append("    data_name,");
		query.append("    coll_name,");
		query.append("    data_id,");
		query.append("    data_size,");
		query.append("    data_path,");
		query.append("    data_owner_name,");
		query.append("    data_owner_zone,");
		query.append("    create_ts,");
		query.append("    modify_ts ");
		query.append(" from ( ");
		query.append("  select distinct on (d.data_name) ");
		query.append("    d.* , ");
		query.append("    c.coll_name ");
		query.append("  from ");
		query.append("    R_DATA_MAIN d ");
		query.append("    left join ");
		query.append("    R_COLL_MAIN c ");
		query.append("  on (c.coll_id = d.coll_id) ");
		query.append("  where ");
		query.append("    c.coll_name = ? ");
		query.append("    AND ");
		query.append("    d.data_name LIKE ? ");
		query.append(" ) searchDataObjsByMetadata ");
		query.append(" order by " + MiscSpecificQueryUtils.getMapColumnsForDataObjects().get(orderColumn) + " "
				+ orderDir + " ");
		query.append(" offset ? ");
		query.append(" limit ? ");
		return query.toString();
	}

	@Override
	public String buildSelectCollectionsUnderPathThatMatchSearchText(String parentPath, String searchText, int offset,
			int limit, int orderColumn, String orderDir) {
		// Build specific query SQL command to retrieve data objects
		// on the collection taking the offset and limit into account.
		StringBuilder query = new StringBuilder();
		query.append("select ");
		query.append("   c.coll_id,");
		query.append("   c.coll_name,");
		query.append("   c.parent_coll_name,");
		query.append("   c.coll_owner_name,");
		query.append("   c.coll_owner_zone,");
		query.append("   c.coll_inheritance,");
		query.append("   c.coll_type,");
		query.append("   c.r_comment,");
		query.append("   c.create_ts,");
		query.append("   c.modify_ts ");
		query.append("from ");
		query.append("   R_COLL_MAIN c ");
		query.append("where ");
		query.append("   c.coll_name LIKE ?");
		query.append("   and ");
		query.append("   c.parent_coll_name = ? ");
		query.append("order by " + MiscSpecificQueryUtils.getMapColumnsForCollections().get(orderColumn) + " "
				+ orderDir + " ");
		query.append("offset ? ");
		query.append("limit ? ");
		return query.toString();

	}

}
