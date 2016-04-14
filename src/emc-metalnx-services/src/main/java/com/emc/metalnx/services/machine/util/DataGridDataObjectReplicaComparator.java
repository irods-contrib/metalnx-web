package com.emc.metalnx.services.machine.util;

import java.util.Comparator;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;

public class DataGridDataObjectReplicaComparator implements Comparator<DataGridCollectionAndDataObject> {

	@Override
	public int compare(DataGridCollectionAndDataObject do1, DataGridCollectionAndDataObject do2) {		
		return  do1.getReplicaNumber().compareTo(do2.getReplicaNumber());
	}

}
