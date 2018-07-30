 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.modelattribute.preferences;

public class UserPreferences {

	private String localeLanguage;
	private boolean forceFileOverwriting;
	private boolean advancedView;
	private boolean metadataUnitView;

	/**
	 * Returns whether forceFileOverwriting is set or not
	 * 
	 * @return
	 */
	public boolean isForceFileOverwriting() {
		return forceFileOverwriting;
	}

	/**
	 * Sets forceFileOverwriting
	 * 
	 * @param forceFileOverwriting
	 */
	public void setForceFileOverwriting(final boolean forceFileOverwriting) {
		this.forceFileOverwriting = forceFileOverwriting;
	}

	/**
	 * @return the localeLanguage
	 */
	public String getLocaleLanguage() {
		return localeLanguage;
	}

	/**
	 * @param localeLanguage
	 *            the localeLanguage to set
	 */
	public void setLocaleLanguage(final String localeLanguage) {
		this.localeLanguage = localeLanguage;
	}

	/**
	 * @return the <code>boolean</code> that is true for advanced view, otherwise is
	 *         normal view
	 */
	public boolean isAdvancedView() {
		return advancedView;
	}
	
	/**
	 * @return the <code>boolean</code> that is false for metadata's Unit visibility by default
	 *         
	 */
	public boolean isMetadataUnitView() {
		return metadataUnitView;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserPreferences [");
		if (localeLanguage != null) {
			builder.append("localeLanguage=").append(localeLanguage).append(", ");
		}
		builder.append("forceFileOverwriting=").append(forceFileOverwriting).append(", advancedView=")
				.append(advancedView).append(", metadataUnitView=")
				.append(metadataUnitView).append("]");
		return builder.toString();
	}

	public void setAdvancedView(boolean advancedView) {
		this.advancedView = advancedView;
	}
	
	public void setMetadataUnitView(boolean metadataUnitView) {
		this.metadataUnitView = metadataUnitView;
	}
}
