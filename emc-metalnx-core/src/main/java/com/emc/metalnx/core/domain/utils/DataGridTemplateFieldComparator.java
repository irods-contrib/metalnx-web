package com.emc.metalnx.core.domain.utils;

import java.util.Comparator;

import com.emc.metalnx.core.domain.entity.DataGridTemplateField;

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
