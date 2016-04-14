package com.emc.metalnx.core.domain.entity;

public class DataGridGroupPermission {

    private Integer dataGridId;
    private String groupName;
    private String permission;

    /**
     * @return the dataGridId
     */
    public Integer getDataGridId() {
        return dataGridId;
    }

    /**
     * @return the groupName
     */
    public String getGroupName() {
        return groupName;
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
     * @param groupName
     *            the groupName to set
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * @param permission
     *            the permission to set
     */
    public void setPermission(String permission) {
        this.permission = permission;
    }

}
