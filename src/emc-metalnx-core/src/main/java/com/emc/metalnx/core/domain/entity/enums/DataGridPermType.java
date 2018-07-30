 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity.enums;

/*
 * Defines all permission types that exist in the data grid.
 * */

public enum DataGridPermType {

    OWN("OWN"), WRITE("WRITE"), READ("READ"), IRODS_ADMIN("ADMIN"), NONE("NONE");

    private String stringType;

    DataGridPermType(String type) {
        this.stringType = type.toUpperCase();
    }

    @Override
    public String toString() {
        return this.stringType;
    }

}