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

import static org.junit.Assert.assertEquals;

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
public class ViewInfoResourceTest {

	private static final Logger logger = LoggerFactory.getLogger(ViewInfoResourceTest.class);

	private static WebDriver driver = null;

	private static String RESOURCE_COORDINATING_NAME = "coordinatingRescName";
	private static String RESOURCE_STORAGE_NAME = "storageRescName";
	private static String RESOURCE_COORDINATING_TYPE = ResourceUtils.RESC_LOAD_BAL;
	private static String RESOURCE_STORAGE_TYPE = ResourceUtils.RESC_DEFERRED;
	private static String RESOURCE_PARENT = UiTestUtilities.IRODS_ZONE;
	private static String RESOURCE_ZONE = UiTestUtilities.IRODS_ZONE;
	private static String RESOURCE_HOST = UiTestUtilities.IRODS_HOST;
	private static String RESOURCE_PATH = ResourceUtils.RESOURCE_PATH;

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
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("user-panel-name")));

		ResourceUtils.addResource(UiTestUtilities.RESOURCES_MAP_URL, RESOURCE_COORDINATING_NAME, RESOURCE_COORDINATING_TYPE,
				RESOURCE_PARENT, RESOURCE_ZONE, RESOURCE_HOST, RESOURCE_PATH, driver);
		ResourceUtils.addResource(UiTestUtilities.RESOURCES_MAP_URL, RESOURCE_STORAGE_NAME, RESOURCE_STORAGE_TYPE,
				RESOURCE_PARENT, RESOURCE_ZONE, RESOURCE_HOST, RESOURCE_PATH, driver);
	}

	/**
	 * After each test the user created for the test should be removed.
	 */
	@After
	public void tearDown() throws Exception {
		ResourceUtils.removeResource(RESOURCE_COORDINATING_NAME, driver);
		ResourceUtils.removeResource(RESOURCE_STORAGE_NAME, driver);
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
	 * *************** ********************************** VIEW INFO RESOURCE
	 * **************************************
	 * *****************************************************************************
	 * ***************
	 */

	/**
	 * Test method for view info in resource management page. It verifies if
	 * information displayed in info modal matches with information added for a
	 * coordinating resource.
	 */
	@Test
	@Ignore
	// TODO2 Test ignored when executing HTMLUnit driver. There is no support for
	// d3.select() function in SVG element.
	public void testViewInfoCoordinatingRescInResourceManagement() {
		logger.info("Testing access view info from resource management page");

		driver.get(UiTestUtilities.RESOURCES_URL);
		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("resourcesListTable")));
		assertEquals(UiTestUtilities.RESOURCES_URL, driver.getCurrentUrl());

		if (ResourceUtils.isInResourcesList(UiTestUtilities.RESOURCES_URL, RESOURCE_COORDINATING_NAME, null, driver)) {
			driver.findElement(By.id("btn_info_" + RESOURCE_COORDINATING_NAME)).click();

			By rescInfoModal = By.id("rescInfoModal");
			wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(rescInfoModal));

			ResourceUtils.assertResourceInfo(RESOURCE_COORDINATING_NAME, RESOURCE_COORDINATING_TYPE, RESOURCE_ZONE,
					RESOURCE_ZONE, RESOURCE_PATH, RESOURCE_PARENT, driver);

			driver.findElement(By.id("rescInfoModal")).findElement(By.tagName("button")).click();
		}
	}

	/**
	 * Test method for view info in resource map page. It verifies if information
	 * displayed in info modal matches with information added for a coordinating
	 * resource.
	 */
	@Test
	@Ignore
	// TODO2 Test ignored when executing HTMLUnit driver. There is no support for
	// d3.select() function in SVG element.
	public void testViewInfoCoordinatingRescInResourceMap() {
		logger.info("Testing access view info from resource map page");

		driver.get(UiTestUtilities.RESOURCES_MAP_URL);
		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("resourceMapPanel")));
		assertEquals(UiTestUtilities.RESOURCES_MAP_URL, driver.getCurrentUrl());

		ResourceUtils.rightClickOnNodeInMap(RESOURCE_COORDINATING_NAME, driver);
		driver.findElement(By.linkText("View Info")).click();

		By resourceInfo = By.id("resourceInfo");
		wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(resourceInfo));

		ResourceUtils.assertResourceInfo(RESOURCE_COORDINATING_NAME, RESOURCE_COORDINATING_TYPE, RESOURCE_ZONE,
				RESOURCE_ZONE, RESOURCE_PATH, RESOURCE_PARENT, driver);

		driver.findElement(resourceInfo).findElement(By.className("close")).click();
	}

	/**
	 * Test method for view info in resource management page. It verifies if
	 * information displayed in info modal matches with information added for a
	 * storage resource.
	 */
	@Test
	@Ignore
	// TODO2 Test ignored when executing HTMLUnit driver. There is no support for
	// d3.select() function in SVG element.
	public void testViewInfoStorageRescInResourceManagement() {
		logger.info("Testing access view info from resource management page");

		driver.get(UiTestUtilities.RESOURCES_URL);
		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("resourcesListTable")));
		assertEquals(UiTestUtilities.RESOURCES_URL, driver.getCurrentUrl());

		if (ResourceUtils.isInResourcesList(UiTestUtilities.RESOURCES_URL, RESOURCE_STORAGE_NAME, null, driver)) {
			driver.findElement(By.id("btn_info_" + RESOURCE_STORAGE_NAME)).click();

			By rescInfoModal = By.id("rescInfoModal");
			wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(rescInfoModal));

			ResourceUtils.assertResourceInfo(RESOURCE_STORAGE_NAME, RESOURCE_STORAGE_TYPE, RESOURCE_ZONE, RESOURCE_HOST,
					RESOURCE_PATH, RESOURCE_PARENT, driver);

			driver.findElement(By.id("rescInfoModal")).findElement(By.tagName("button")).click();
		}
	}

	/**
	 * Test method for view info in resource map page. It verifies if information
	 * displayed in info modal matches with information added for a storage
	 * resource.
	 */
	@Test
	@Ignore
	// TODO2 Test ignored when executing HTMLUnit driver. There is no support for
	// d3.select() function in SVG element.
	public void testViewInfoStorageRescInResourceMap() {
		logger.info("Testing access view info from resource map page");

		driver.get(UiTestUtilities.RESOURCES_MAP_URL);
		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.id("resourceMapPanel")));
		assertEquals(UiTestUtilities.RESOURCES_MAP_URL, driver.getCurrentUrl());

		ResourceUtils.rightClickOnNodeInMap(RESOURCE_STORAGE_NAME, driver);
		driver.findElement(By.linkText("View Info")).click();

		By resourceInfo = By.id("resourceInfo");
		wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(resourceInfo));

		ResourceUtils.assertResourceInfo(RESOURCE_STORAGE_NAME, RESOURCE_STORAGE_TYPE, RESOURCE_ZONE, RESOURCE_HOST,
				RESOURCE_PATH, RESOURCE_PARENT, driver);

		driver.findElement(resourceInfo).findElement(By.className("close")).click();
	}
}
