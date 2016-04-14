package com.emc.metalnx.core.domain.entity.enums;

public enum DataGridResourceTypeEnum {

    IRODS_COORDINATING("COORDINATING"), IRODS_STORAGE("STORAGE");

    private String stringType;

    private DataGridResourceTypeEnum(String type) {
        this.stringType = type;
    }

    @Override
    public String toString() {
        return this.stringType;
    }

}