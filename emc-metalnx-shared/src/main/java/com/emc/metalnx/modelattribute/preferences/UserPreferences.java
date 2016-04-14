package com.emc.metalnx.modelattribute.preferences;

public class UserPreferences {

	private String localeLanguage;
	private boolean forceFileOverwriting;

	/**
	 * Returns whether forceFileOverwriting is set or not
	 * @return
	 */
	public boolean isForceFileOverwriting() {
		return forceFileOverwriting;
	}

	/**
	 * Sets forceFileOverwriting
	 * @param forceFileOverwriting
	 */
	public void setForceFileOverwriting(boolean forceFileOverwriting) {
		this.forceFileOverwriting = forceFileOverwriting;
	}

	/**
	 * @return the localeLanguage
	 */
	public String getLocaleLanguage() {
		return localeLanguage;
	}

	/**
	 * @param localeLanguage the localeLanguage to set
	 */
	public void setLocaleLanguage(String localeLanguage) {
		this.localeLanguage = localeLanguage;
	}

}
