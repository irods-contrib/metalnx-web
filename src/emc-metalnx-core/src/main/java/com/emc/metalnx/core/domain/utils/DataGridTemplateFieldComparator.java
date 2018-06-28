 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.utils;

import com.emc.metalnx.core.domain.entity.DataGridTemplateField;

import java.util.Comparator;

public class DataGridTemplateFieldComparator implements Comparator<DataGridTemplateField> {

	@Override
	public int compare(DataGridTemplateField o1, DataGridTemplateField o2) {
		if (o1.getAttribute().compareTo(o2.getAttribute()) != 0) {
			return o1.getAttribute().compareTo(o2.getAttribute());
		}
		
		else if (o1.getValue().compareTo(o2.getValue()) != 0) {
			return o1.getValue().compareTo(o2.getValue());
		}
		
		else if (o1.getUnit().compareTo(o2.getUnit()) != 0) {
			return o1.getUnit().compareTo(o2.getUnit());
		}
		
		return 0;
	}

}
