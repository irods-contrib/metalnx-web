 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.machine.util;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;

import java.util.Comparator;

public class DataGridDataObjectReplicaComparator implements Comparator<DataGridCollectionAndDataObject> {

	@Override
	public int compare(DataGridCollectionAndDataObject do1, DataGridCollectionAndDataObject do2) {		
		return  do1.getReplicaNumber().compareTo(do2.getReplicaNumber());
	}

}
