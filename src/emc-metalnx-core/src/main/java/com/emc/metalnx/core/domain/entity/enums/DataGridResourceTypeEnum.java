 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity.enums;

public enum DataGridResourceTypeEnum {

    IRODS_COORDINATING("COORDINATING"), IRODS_STORAGE("STORAGE");

    private String stringType;

    DataGridResourceTypeEnum(String type) {
        this.stringType = type;
    }

    @Override
    public String toString() {
        return this.stringType;
    }

}