 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.machine.util;

import com.emc.metalnx.core.domain.entity.DataGridServer;

import java.util.Comparator;
import java.util.HashMap;

public class DataGridServerStatusComparator implements Comparator<DataGridServer> {

	@Override
	public int compare(DataGridServer o1, DataGridServer o2) {
		HashMap<String, Integer> values = new HashMap<String, Integer>();
		values.put("normal", 2);
		values.put("warning", 1);
		values.put("error", 0);
		
		if(o1.getMachineStatus() == null || o2.getMachineStatus() == null) {
			return 0;
		}
		
		return values.get(o1.getMachineStatus()).compareTo(values.get(o2.getMachineStatus()));
	}

}
