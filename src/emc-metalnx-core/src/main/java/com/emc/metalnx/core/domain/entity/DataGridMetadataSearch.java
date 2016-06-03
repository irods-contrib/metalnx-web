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
package com.emc.metalnx.core.domain.entity;

import com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum;

/**
 * This is a definition of metadata search criteria. A search criteria is broken into an object to
 * manipulate its attribute, operator, and value.
 *
 */
public class DataGridMetadataSearch {

    private String attribute;
    private DataGridSearchOperatorEnum operator;
    private String value;
    private String unit;
    private String regex = "([^A-Za-z0-9-_.,:=! ]+)";
    private String attrColName = "m.meta_attr_name";
    private String valueColName = "m.meta_attr_value";
    private String unitColName = "m.meta_attr_unit";

    public DataGridMetadataSearch(String attribute, String value, String unit,
            DataGridSearchOperatorEnum operator) {
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
        this.unit = unit;
    }

    /**
     * Builds a SQL query string to look for a piece of metadata (attribute, operator, and value).
     *
     * @return SQL query string
     */
    public String getSpecQueryAsString() {
        String attr = this.attribute.replaceAll(regex, "");
        String opt = this.operator.toString().replaceAll(regex, "");
        String val = this.value.replaceAll(regex, "");
        String unit = this.unit.replaceAll(regex, "");

        boolean hasAttr = !attr.isEmpty();
        boolean hasVal = !val.isEmpty();
        boolean hasUnit = !unit.isEmpty();

        val = addSQLCharToQueryParamBasedOnOperator(val);
        unit = addSQLCharToQueryParamBasedOnOperator(unit);

        String attrQuery = hasAttr ? String.format(" %s = '%s' ", attrColName, attr) : "";
        String valueQuery = hasVal ? String.format(" %s %s %s ", valueColName, opt, val) : "";
        String unitQuery = hasUnit ? String.format(" %s %s %s ", unitColName, opt, unit) : "";

        if (hasAttr && (hasVal || hasUnit)) {
            attrQuery = String.format(" %s AND ", attrQuery);
        }

        if (hasVal && hasUnit) {
            valueQuery = String.format(" %s AND ", valueQuery);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" SELECT map.object_id AS map_object_id ");
        sb.append(" FROM r_objt_metamap map ");
        sb.append(" JOIN ( ");
        sb.append("     SELECT m.meta_id, m.meta_attr_name, m.meta_attr_value");
        sb.append("     FROM r_meta_main m ");
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
        return String.format("%s %s %s", this.attribute, this.operator.toString(), this.value);
    }

    /**
     * Based on the operator applied to the query (LIKE or NOT LIKE), this method checks if any
     * SQL special character needs to be added to the parameter in order for the query to run
     * properly.
     *
     * @param param
     * @return String representing the given parameters along with the proper SQL character for the
     *         query.
     */
    private String addSQLCharToQueryParamBasedOnOperator(String param) {
        if (this.operator == DataGridSearchOperatorEnum.LIKE
                || this.operator == DataGridSearchOperatorEnum.NOT_LIKE) {
            return String.format(" '%%%s%%' ", param);
        }
        return String.format(" '%s' ", param);
    }
}
