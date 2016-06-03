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

public class DataGridUserPermission {

    private Integer dataGridId;
    private String userName;
    private String userSystemRole;
    private String permission;

    /**
     * @return the dataGridId
     */
    public Integer getDataGridId() {
        return dataGridId;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @return the permission
     */
    public String getPermission() {
        return permission;
    }

    /**
     * @param dataGridId
     *            the dataGridId to set
     */
    public void setDataGridId(Integer dataGridId) {
        this.dataGridId = dataGridId;
    }

    /**
     * @param userName
     *            the userName to set
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @param permission
     *            the permission to set
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * @return the userSystemRole
     */
    public String getUserSystemRole() {
        return userSystemRole;
    }

    /**
     * @param userSystemRole
     *            the userSystemRole to set
     */
    public void setUserSystemRole(String userSystemRole) {
        this.userSystemRole = userSystemRole;
    }

}
