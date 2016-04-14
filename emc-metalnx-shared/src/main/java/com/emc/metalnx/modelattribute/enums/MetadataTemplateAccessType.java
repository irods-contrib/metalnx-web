package com.emc.metalnx.modelattribute.enums;

public enum MetadataTemplateAccessType {

	SYSTEM("system"),
	PRIVATE("private");

	private String accessType;
	
	MetadataTemplateAccessType(String accessType) {
		this.accessType = accessType;
	}
	
	/**
	 * Returns the String access type corresponding to this value
	 * @return
	 */
	public String getAccessType() {
		return this.accessType;
	}
	
	/**
	 * Finds the correct label associated to the input String
	 * @param accessType
	 * @return
	 */
	public static MetadataTemplateAccessType findByString(String accessType) {
		
		if (accessType == null) {
			return null;
		}
		
		for(MetadataTemplateAccessType at : MetadataTemplateAccessType.values()) {
			if (at.getAccessType().compareTo(accessType) == 0) {
				return at;
			}
		}
		return null;
	}
	
	/**
	 * Returns the string format for this enum
	 */
	public String toString() {
		return this.getAccessType();
	}
}
