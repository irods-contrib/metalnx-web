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

package com.emc.metalnx.integration.test.resources;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.integration.test.utils.ResourceUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

@Deprecated
@Ignore
public class RemoveResourceTest {

	private static final Logger logger = LoggerFactory.getLogger(RemoveResourceTest.class);

	private static WebDriver driver = null;

	private static String RESOURCE_NAME = "resourceName";

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

		ResourceUtils.addResource(UiTestUtilities.RESOURCES_URL, RESOURCE_NAME, ResourceUtils.RESC_COMPOUND, null, null, null,
				null, driver);
	}

	/**
	 * After each test the user created for the test should be removed.
	 */
	@After
	public void tearDown() throws Exception {
		ResourceUtils.removeResource(RESOURCE_NAME, driver);
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
	 * *************** ************************************ REMOVE RESOURCE
	 * ***************************************
	 * *****************************************************************************
	 * ***************
	 */

	/**
	 * Test method for removing resource. It verifies if resource does not remain in
	 * resource list in Resource Management page.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRemoveResource() throws Exception {
		logger.info("Testing remove resource from Resource Management");
		ResourceUtils.removeResource(RESOURCE_NAME, driver);

		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("resourcesListTable")));
		assertTrue(!ResourceUtils.isInResourcesList(UiTestUtilities.RESOURCES_URL, RESOURCE_NAME, null, driver));
	}

	/**
	 * Test method for rejecting remove resource. It verifies if resource remains in
	 * resource list in Resource Management page.
	 */
	@Test
	public void testRejectRemovingResource() {
		logger.info("Testing reject removing resource");
		if (ResourceUtils.isInResourcesList(UiTestUtilities.RESOURCES_URL, RESOURCE_NAME, null, driver)) {
			ResourceUtils.clickOnResouceRemoveIcon(RESOURCE_NAME, driver);
			driver.findElement(By.id("btnConfRescRemoval_No")).click();
		}

		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("resourcesListTable")));
		assertTrue(ResourceUtils.isInResourcesList(UiTestUtilities.RESOURCES_URL, RESOURCE_NAME, null, driver));
	}
}
