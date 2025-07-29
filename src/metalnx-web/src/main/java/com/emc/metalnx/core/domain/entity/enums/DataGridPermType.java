 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity.enums;

/*
 * Defines all permission types that exist in the data grid.
 * */

public enum DataGridPermType {
	
	NONE("NONE"),
	READ_METADATA("READ_METADATA"),
	READ_OBJECT("READ_OBJECT"),
	READ("READ"),
	CREATE_METADATA("CREATE_METADATA"),
	MODIFY_METADATA("MODIFY_METADATA"),
	DELETE_METADATA("DELETE_METADATA"),
	CREATE_OBJECT("CREATE_OBJECT"),
	MODIFY_OBJECT("MODIFY_OBJECT"),
	WRITE("WRITE"),
	DELETE_OBJECT("DELETE_OBJECT"),
	OWN("OWN");

    private String stringType;

    DataGridPermType(String type) {
        this.stringType = type.toUpperCase();
    }

    @Override
    public String toString() {
        return this.stringType;
    }

}