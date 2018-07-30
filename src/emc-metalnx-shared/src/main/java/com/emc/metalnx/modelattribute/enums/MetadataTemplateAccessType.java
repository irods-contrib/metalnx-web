 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.modelattribute.enums;

public enum MetadataTemplateAccessType {

	SYSTEM("system"), PRIVATE("private");

	private String accessType;

	MetadataTemplateAccessType(final String accessType) {
		this.accessType = accessType;
	}

	/**
	 * Returns the String access type corresponding to this value
	 * 
	 * @return
	 */
	public String getAccessType() {
		return accessType;
	}

	/**
	 * Finds the correct label associated to the input String
	 * 
	 * @param accessType
	 * @return
	 */
	public static MetadataTemplateAccessType findByString(final String accessType) {

		if (accessType == null) {
			return null;
		}

		for (MetadataTemplateAccessType at : MetadataTemplateAccessType.values()) {
			if (at.getAccessType().compareTo(accessType) == 0) {
				return at;
			}
		}
		return null;
	}

	/**
	 * Returns the string format for this enum
	 */
	@Override
	public String toString() {
		return getAccessType();
	}
}
