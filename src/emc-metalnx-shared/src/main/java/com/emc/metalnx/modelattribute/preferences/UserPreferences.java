/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

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
