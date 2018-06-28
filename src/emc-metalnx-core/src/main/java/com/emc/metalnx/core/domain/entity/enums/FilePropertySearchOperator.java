 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.entity.enums;

public enum FilePropertySearchOperator {

	EQUALS("="),
	NOT_EQUALS("!="),
	CONTAINS("ILIKE"),
	NOT_CONTAINS("NOT ILIKE");
	
	private String operator;
	
	FilePropertySearchOperator(String operator) {
		this.operator = operator;
	}
	
	/**
	 * Finds the correct label associated to the input String
	 * @param accessType
	 * @return
	 */
	public static FilePropertySearchOperator findByString(String operatorStr) {
		
		if (operatorStr == null) {
			return null;
		}
		
		for(FilePropertySearchOperator operator : FilePropertySearchOperator.values()) {
			if (operator.getFieldName().compareTo(operatorStr) == 0) {
				return operator;
			}
		}
		return null;
	}

	/**
	 * @return the fieldName
	 */
	public String getFieldName() {
		return this.operator;
	}
	
}
