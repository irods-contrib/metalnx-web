/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.integration.test.profiles;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.integration.test.utils.ProfileUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

import junit.framework.Assert;

@Deprecated
@Ignore
public class RemoveProfileTest {
	private static final Logger logger = LoggerFactory.getLogger(RemoveProfileTest.class);

	private static WebDriver driver = null;

	private static final String PROFILE_NAME = "Profile_Name";

	private static final String PROFILE_DESCRIPTION = "Profile Description";

	/*************************************
	 * TEST SET UP
	 *************************************/

	@BeforeClass
	public static void setUpBeforeClass() {
		// UITest.setUpBeforeClass();
		driver = UiTestUtilities.getDriver();
	}

	@Before
	public void setUp() throws Exception {
		UiTestUtilities.login();
		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("page-wrapper")));

		ProfileUtils.addUserProfile(PROFILE_NAME, PROFILE_DESCRIPTION, null, driver);
	}

	/**
	 * After each test the user created for the test should be removed.
	 */
	@After
	public void tearDown() throws Exception {
		UiTestUtilities.logout();
	}

	/**
	 * After all tests are done, the test must quit the driver. This will close
	 * every window associated with the current driver instance.
	 */

	@AfterClass
	public static void tearDownAfterClass() {
		if (driver != null) {
			driver.quit();
			driver = null;
			UiTestUtilities.setDriver(null);
		}
	}

	/*
	 * *****************************************************************************
	 * *************** *********************************** REMOVE PROFILE
	 * *****************************************
	 * *****************************************************************************
	 * ***************
	 */

	/**
	 * Test method for removing a user profile. It verifies if success message is
	 * displayed and profile does not appear anymore in profile list.
	 */
	@Test
	public void testRemoveUserProfile() {
		logger.info("Test for removing a user profile");

		driver.get(UiTestUtilities.PROFILES_URL);

		ProfileUtils.removeProfile(PROFILE_NAME, driver);

		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("userProfilesListTable")));

		WebElement divAlertSucess = driver.findElement(By.className("alert-success"));
		assertTrue(divAlertSucess.isDisplayed());
		assertTrue(divAlertSucess.getText().contains(PROFILE_NAME));

		Assert.assertTrue(!ProfileUtils.isProfileInList(PROFILE_NAME, driver));
	}

}
