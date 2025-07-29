/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.core.domain.entity;

import com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum;

/**
 * This is a definition of metadata search criteria. A search criteria is broken
 * into an object to manipulate its attribute, operator, and value.
 *
 */
public class DataGridMetadataSearch {

	private String attribute;
	private DataGridSearchOperatorEnum operator;
	private String value;
	private String unit;
	private String regex = "([^A-Za-z0-9-_.,:=!/ ]+)";
	private String attrColName = "m.meta_attr_name";
	private String valueColName = "m.meta_attr_value";
	private String unitColName = "m.meta_attr_unit";

	public DataGridMetadataSearch(String attribute, String value, String unit, DataGridSearchOperatorEnum operator) {
		this.attribute = attribute;
		this.operator = operator;
		this.value = value;
		this.unit = unit;
	}

	/**
	 * Builds a SQL query string to look for a piece of metadata (attribute,
	 * operator, and value).
	 *
	 * @return SQL query string
	 */
	public String getSpecQueryAsString() {

		boolean hasAttr = !this.attribute.isEmpty();
		boolean hasVal = !this.value.isEmpty();
		boolean hasUnit = !this.unit.isEmpty();

		String val = addSQLCharToQueryParamBasedOnOperator(this.value);
		String theUnit = addSQLCharToQueryParamBasedOnOperator(unit);

		String attrQuery = hasAttr ? String.format(" %s = '%s' ", attrColName, this.attribute) : "";
		String valueQuery = hasVal ? String.format(" %s %s %s ", valueColName, this.operator, val)
				: "";
		String unitQuery = hasUnit ? String.format(" %s %s %s ", unitColName, this.operator, theUnit)
				: "";

		if (hasAttr && (hasVal || hasUnit)) {
			attrQuery = String.format(" %s AND ", attrQuery);
		}

		if (hasVal && hasUnit) {
			valueQuery = String.format(" %s AND ", valueQuery);
		}

		StringBuilder sb = new StringBuilder();
		sb.append(" SELECT map.object_id AS map_object_id ");
		sb.append(" FROM R_OBJT_METAMAP map ");
		sb.append(" JOIN ( ");
		sb.append("     SELECT m.meta_id, m.meta_attr_name, m.meta_attr_value");
		sb.append("     FROM R_META_MAIN m ");
		sb.append(" WHERE ");
		sb.append(attrQuery);
		sb.append(valueQuery);
		sb.append(unitQuery);
		sb.append("  )");
		sb.append("  AS metadata ON (metadata.meta_id = map.meta_id)");
		sb.append("  GROUP BY map.object_id");
		sb.append("  HAVING COUNT(map.meta_id) > 0 ");
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridMetadataSearch [");
		if (attribute != null) {
			builder.append("attribute=").append(attribute).append(", ");
		}
		if (operator != null) {
			builder.append("operator=").append(operator).append(", ");
		}
		if (value != null) {
			builder.append("value=").append(value).append(", ");
		}
		if (unit != null) {
			builder.append("unit=").append(unit).append(", ");
		}
		if (regex != null) {
			builder.append("regex=").append(regex).append(", ");
		}
		if (attrColName != null) {
			builder.append("attrColName=").append(attrColName).append(", ");
		}
		if (valueColName != null) {
			builder.append("valueColName=").append(valueColName).append(", ");
		}
		if (unitColName != null) {
			builder.append("unitColName=").append(unitColName);
		}
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Based on the operator applied to the query (LIKE or NOT LIKE), this method
	 * checks if any SQL special character needs to be added to the parameter in
	 * order for the query to run properly.
	 *
	 * @param param
	 * @return String representing the given parameters along with the proper SQL
	 *         character for the query.
	 */
	private String addSQLCharToQueryParamBasedOnOperator(String param) {
		if (this.operator == DataGridSearchOperatorEnum.LIKE || this.operator == DataGridSearchOperatorEnum.NOT_LIKE) {
			return String.format(" '%%%s%%' ", param);
		}
		return String.format(" '%s' ", param);
	}
}
