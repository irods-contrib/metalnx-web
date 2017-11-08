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

package com.emc.metalnx.integration.test.group;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.test.generic.UiTestUtilities;

@Deprecated
@Ignore
public class GroupTest {

	private static final Logger logger = LoggerFactory.getLogger(GroupTest.class);
	private static WebDriver driver = null;

	@BeforeClass
	public static void setUpBeforeClass() {
		// UITest.setUpBeforeClass();
		driver = UiTestUtilities.getDriver();
	}

	/**
	 * Logs in before each test.
	 */
	@Before
	public void setUp() throws Exception {
		UiTestUtilities.login();
	}

	/**
	 * After each test the user is logged out.
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
	 * *************** ******************************* GROUP MANAGEMENT
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

		try {
			driver.get(UiTestUtilities.GROUPS_URL);

			WebElement addGroupBtn = driver.findElement(By.linkText("Add Group"));
			addGroupBtn.click();

			// checks if add group button brings the group to the add group page
			Assert.assertEquals(UiTestUtilities.ADD_GROUPS_URL, driver.getCurrentUrl());
		} catch (Exception e) {
			logger.error("Could not run test properly");
		}
	}

	/*
	 * *****************************************************************************
	 * *************** ***************************************** ADD GROUP
	 * ****************************************
	 * *****************************************************************************
	 * ***************
	 */

	/**
	 * Test method for adding a brand new group to the system. It verifies if after
	 * adding group, the group is redirected to the group management page; if this
	 * group now exists in the groups list; and if the number of groups existing was
	 * incremented by one.
	 */
	@Test
	public void testAddGroup() {
		logger.info("Testing add a brand new group");

		try {
			driver.get(UiTestUtilities.ADD_GROUPS_URL);

			WebElement inputGroupname = driver.findElement(By.id("inputGroupname"));
			WebElement btnEditUsers = driver.findElement(By.id("showUsersListBtn"));
			WebElement submitGroupFormBtn = driver.findElement(By.id("submitGroupFormBtn"));

			String newGroupname = "webdriver_group" + System.currentTimeMillis();

			inputGroupname.sendKeys(newGroupname);
			btnEditUsers.click();

			List<WebElement> cbUsersList = driver.findElements(By.name("idsList"));
			for (WebElement checkbox : cbUsersList) {
				checkbox.click();
			}

			submitGroupFormBtn.click();

			// checks after adding a Group, if the system returns to previous
			// screen (Group mgmt.)
			assertEquals(UiTestUtilities.GROUPS_URL, driver.getCurrentUrl());

			// checks if a success message is displayed
			WebElement divAlertSucess = driver.findElement(By.className("alert-success"));

			// check if success message is displayed
			assertTrue(divAlertSucess.isDisplayed());

			// check if the group was successfully added
			assertTrue(divAlertSucess.getText().contains(newGroupname));
		} catch (Exception e) {
			logger.error("Could not run the test properly: {}", e.getMessage());
		}
	}

	/**
	 * Test method that checks when adding a user with an empty groupname if the UI
	 * gets this exception and shows an error message.
	 */
	@Test
	public void testAddGroupWithEmptyName() {
		logger.info("Testing add a group with an empty groupname");

		try {

			driver.get(UiTestUtilities.ADD_GROUPS_URL);

			WebElement inputGroupname = driver.findElement(By.id("inputGroupname"));
			WebElement submitGroupFormBtn = driver.findElement(By.id("submitGroupFormBtn"));

			inputGroupname.sendKeys("");
			submitGroupFormBtn.click();

			WebElement emptyGroupnameMsg = driver.findElement(By.id("emptyGroupnameMsg"));

			assertTrue(emptyGroupnameMsg.isDisplayed());
		} catch (Exception e) {
			logger.error("Could not run the test properly: {}", e.getMessage());
		}
	}

	/**
	 * Test method that removes the group created by other test methods. It leaves
	 * the Group Management as it was before the test session started.
	 */
	@Test
	public void testDeleteGroupCreatedByTests() {
		logger.info("Testing removing all groups created by tests");
		WebElement deleteBtn = null;
		WebElement deleteGroupConfBtn = null;

		try {

			driver.get(UiTestUtilities.GROUPS_URL);

			List<WebElement> groupsList = driver.findElements(By.className("col-groupname"));

			// looking for a webdriver group. If found, the group will be removed
			for (WebElement group : groupsList) {
				if (group.getText().startsWith("webdriver_group")) {
					// clicking on the delete button
					deleteBtn = driver.findElement(By.id("deleteBtn_" + group.getText()));
					deleteBtn.click();

					driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);

					// confirmation modal
					deleteGroupConfBtn = driver.findElement(By.id("deleteGroupConfBtn"));
					deleteGroupConfBtn.click();

					// checking if a feedback message is shown after deleting a group
					WebElement feedbackMsg = driver.findElement(By.className("alert"));
					assertTrue(feedbackMsg.isDisplayed());
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Could not run the test properly: {}", e.getMessage());
		}
	}
}
