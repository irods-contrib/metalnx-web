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

import com.emc.metalnx.core.domain.entity.enums.DataGridResourceTypeEnum;

public class DataGridResourceType implements Comparable<DataGridResourceType> {
    private DataGridResourceTypeEnum dataGridType;
    private String dataGridResourceTypeName;
    private String dataGridResourceTypeNamePrettified;

    public DataGridResourceType() {

    }

    public DataGridResourceType(String dataGridResourceTypeNamePrettified,
            String dataGridResourceTypeName, DataGridResourceTypeEnum dataGridType) {
        this.dataGridResourceTypeNamePrettified = dataGridResourceTypeNamePrettified;
        this.dataGridType = dataGridType;
        this.dataGridResourceTypeName = dataGridResourceTypeName;
    }

    @Override
    public int compareTo(DataGridResourceType dgrt) {
        return this.getDataGridResourceTypeName().compareTo(dgrt.getDataGridResourceTypeName());
    }

    /**
     * @return the dataGridType
     */
    public DataGridResourceTypeEnum getDataGridType() {
        return dataGridType;
    }

    /**
     * @param dataGridType
     *            the dataGridType to set
     */
    public void setDataGridType(DataGridResourceTypeEnum dataGridType) {
        this.dataGridType = dataGridType;
    }

    /**
     * @return the dataGridTypeName
     */
    public String getDataGridResourceTypeName() {
        return dataGridResourceTypeName;
    }

    /**
     * @param dataGridTypeName
     *            the dataGridTypeName to set
     */
    public void setDataGridResourceTypeName(String dataGridResourceTypeName) {
        this.dataGridResourceTypeName = dataGridResourceTypeName;
    }

    /**
     * @return the dataGridResourceTypeNamePrettified
     */
    public String getDataGridResourceTypeNamePrettified() {
        return dataGridResourceTypeNamePrettified;
    }

    /**
     * @param dataGridResourceTypeNamePrettified
     *            the dataGridResourceTypeNamePrettified to set
     */
    public void setDataGridResourceTypeNamePrettified(String dataGridResourceTypeNamePrettified) {
        this.dataGridResourceTypeNamePrettified = dataGridResourceTypeNamePrettified;
    }
}
