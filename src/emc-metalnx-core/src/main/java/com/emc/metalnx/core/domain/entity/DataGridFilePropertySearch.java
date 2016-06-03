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

import java.text.ParseException;
import java.text.SimpleDateFormat;

import com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum;
import com.emc.metalnx.core.domain.entity.enums.FilePropertyField;

public class DataGridFilePropertySearch {

    private FilePropertyField attribute;
    private DataGridSearchOperatorEnum operator;
    private String value;
    private String regex = "([^A-Za-z0-9-_.,=! ]+)";

    // this regex is used to remove quotes, double quotes and semi-colon from value field, so that sql injection becomes harder to happen
    private String regexForValue = "/[^'\";]/g";

    public DataGridFilePropertySearch(FilePropertyField filePropertyField, DataGridSearchOperatorEnum operator, String value) throws ParseException {
        attribute = filePropertyField;
        this.operator = operator;
        if (filePropertyField == FilePropertyField.CREATION_DATE || filePropertyField == FilePropertyField.MODIFICATION_DATE) {
            long timeInMilliseconds = new SimpleDateFormat("MM/dd/yyyy hh:mm aa").parse(value).getTime();
            this.value = String.valueOf(timeInMilliseconds / 1000);
        }
        else {
            this.value = value;
        }
    }

    public String getSelectClauseForDataObjects() {

        StringBuilder query = new StringBuilder();
        query.append("SELECT * 	FROM ( ");
        query.append("	SELECT");
        query.append("  	r_data_main.data_name AS name,");
        query.append("  	r_data_main.data_repl_num AS repl_num,");
        query.append("  	r_data_main.data_owner_name AS owner_name,");
        query.append("  	r_data_main.data_owner_zone AS owner_zone,");
        query.append("  	r_data_main.data_size AS size,");
        query.append("  	r_data_main.resc_name,");
        query.append("  	CASE WHEN r_coll_main.parent_coll_name = '/' THEN '/' || r_data_main.data_name ELSE r_coll_main.coll_name || '/' || r_data_main.data_name END AS path,");
        query.append("  	r_data_main.data_checksum AS checksum,");
        query.append("  	CAST(r_data_main.create_ts AS BIGINT), ");
        query.append("  	CAST(r_data_main.modify_ts AS BIGINT) ");
        query.append("	FROM");
        query.append("  	r_data_main  ");
        query.append("  INNER JOIN  ");
        query.append("  	r_coll_main  ");
        query.append("  ON  ");
        query.append("  	r_data_main.coll_id = r_coll_main.coll_id  ");
        query.append(" ) AS fileProperties ");
        query.append("WHERE ");

        return query.toString();
    }

    public String getSelectClauseForCollections() {

        StringBuilder query = new StringBuilder();
        query.append("SELECT * 	FROM ( ");
        query.append("	SELECT");
        query.append("  	replace(r_coll_main.coll_name, r_coll_main.parent_coll_name || '/', '') AS name, ");
        query.append(" 		0 AS repl_num, ");
        query.append("  	r_coll_main.coll_owner_name AS owner_name, ");
        query.append("  	r_coll_main.coll_owner_zone AS owner_zone, ");
        query.append("  	0 AS size, ");
        query.append("  	'' AS resc_name, ");
        query.append("  	r_coll_main.coll_name AS path, ");
        query.append("  	'' AS checksum, ");
        query.append("  	CAST(r_coll_main.create_ts AS BIGINT), ");
        query.append("  	CAST(r_coll_main.modify_ts AS BIGINT) ");
        query.append("	FROM");
        query.append("  	r_coll_main ");
        query.append(" ) AS fileProperties ");
        query.append("WHERE ");

        return query.toString();
    }

    public String getWhereClause() {
        String whereClause = new String();

        String attribute = this.attribute.getFieldName().replaceAll(regex, "");
        String operator = this.operator.toString();
        String value = this.value.replaceAll(regexForValue, "");
        boolean isAttributeEqualsDate = this.attribute == FilePropertyField.CREATION_DATE || this.attribute == FilePropertyField.MODIFICATION_DATE;

        if (this.operator == DataGridSearchOperatorEnum.LIKE || this.operator == DataGridSearchOperatorEnum.NOT_LIKE) {
            whereClause = String.format(" fileProperties.%s %s '%%%s%%'", attribute, operator, value);
        }
        else if (isAttributeEqualsDate && this.operator == DataGridSearchOperatorEnum.EQUAL) {
            whereClause = String.format(" fileProperties.%s BETWEEN %s AND %d", attribute, value, Long.parseLong(value) + 60);
        }
        else if (isAttributeEqualsDate || this.attribute == FilePropertyField.REPLICA_NUMBER || this.attribute == FilePropertyField.SIZE) {
            whereClause = String.format(" fileProperties.%s %s %s", attribute, operator, value);
        }
        else {
            whereClause = String.format(" fileProperties.%s %s '%s'", attribute, operator, value);
        }

        return whereClause;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s", attribute.getFieldName(), operator.toString(), value);
    }

}
