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

package com.emc.metalnx.integration.test.users;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
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

import com.emc.metalnx.integration.test.utils.UserUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

@Deprecated
@Ignore
public class UserTest {

	private static final Logger logger = LoggerFactory.getLogger(UserTest.class);

	private String uname = "webdriver" + System.currentTimeMillis();
	private String pwd = "webdriver";
	private static WebDriver driver = null;

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
	}

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
	 * *************** ******************************** USER MANAGEMENT
	 * *******************************************
	 * *****************************************************************************
	 * ***************
	 */

	/**
	 * Test method that checks if the add user button in the user management page
	 * always brings the user to the add user page.
	 */
	@Test
	public void testCheckIfAddButtonWorks() {
		logger.info("Testing if add button works");
		driver.get(UiTestUtilities.USERS_URL);

		driver.findElement(By.cssSelector("a[href='add/']")).click();

		// checks if add user button brings the user to the add user page
		Assert.assertEquals(UiTestUtilities.ADD_USERS_URL, driver.getCurrentUrl());
		UiTestUtilities.logout();
	}

	/*
	 * *****************************************************************************
	 * *************** ***************************************** ADD USER
	 * *****************************************
	 * *****************************************************************************
	 * ***************
	 */

	/**
	 * Test method for adding a brand new user to the system. It verifies if after
	 * adding user, the user is redirected to the user management page; if this user
	 * now exists in the users list; and if the number of users existing was
	 * incremented by one.
	 *
	 * @throws Exception
	 */
	@Test
	public void testAddUser() throws Exception {
		logger.info("Testing add a brand new user");

		driver.get(UiTestUtilities.ADD_USERS_URL);
		UserUtils.fillInUserGeneralInformation(uname, pwd, UiTestUtilities.RODS_ADMIN_TYPE, driver);
		UserUtils.fillInPersonalInfo(driver, "web", "driver", "webdriver@testing.com");

		driver.findElement(By.id("showGroupsListBtn")).click();

		List<WebElement> cbGroupIdsList = driver.findElements(By.name("groupIdsList"));
		for (WebElement checkbox : cbGroupIdsList) {
			checkbox.click();
		}

		UserUtils.submitUserForm(driver);
		assertEquals(UiTestUtilities.USERS_URL, driver.getCurrentUrl());
		UserUtils.searchUser(driver, uname);
		isSuccessMessageDisplayed();
		UiTestUtilities.logout();
		UserUtils.removeUser(uname, driver);
	}

	/**
	 * Test method that checks when adding a user with an empty username if the UI
	 * gets this exception and shows an error message.
	 */
	@Test
	public void testAddUserWithEmptyUsername() {
		logger.info("Testing add a user with an empty username");

		driver.get(UiTestUtilities.ADD_USERS_URL);

		// Creating user for testing
		new WebDriverWait(driver, 15).until(ExpectedConditions.elementToBeClickable(UserUtils.ZONE_FOLDER));

		driver.findElement(UserUtils.USERNAME_INPUT).sendKeys("");
		driver.findElement(UserUtils.PWD_INPUT).sendKeys(pwd);
		driver.findElement(UserUtils.PWD_CONF_INPUT).sendKeys(pwd);

		Assert.assertTrue(driver.findElement(By.id("emptyUsernameMsg")).isDisplayed());
	}

	/**
	 * Method that checks if a success message is displayed and if the user was
	 * successfully added
	 */
	private void isSuccessMessageDisplayed() {
		WebElement divAlertSucess = driver.findElement(By.className("alert-success"));
		Assert.assertTrue(divAlertSucess.isDisplayed());
		Assert.assertTrue(divAlertSucess.getText().contains(uname));
	}
}
