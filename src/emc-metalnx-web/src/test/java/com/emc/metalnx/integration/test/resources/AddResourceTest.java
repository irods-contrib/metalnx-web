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
public class AddResourceTest {

	private static final Logger logger = LoggerFactory.getLogger(AddResourceTest.class);

	private static WebDriver driver = null;

	private static final String RESOURCE_NAME = "resourceName";

	private static final String RESOURCE_NAME_BLANK_SPACE = "resource name";

	private static final String CHILD_RESOURCE_NAME = "child_resourceName";

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
	}

	/**
	 * After each test the user created for the test should be removed.
	 */
	@After
	public void tearDown() throws Exception {
		ResourceUtils.removeResource(RESOURCE_NAME, driver);
		ResourceUtils.removeResource(CHILD_RESOURCE_NAME, driver);
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
	 * *************** ************************************ ADD RESOURCE
	 * ******************************************
	 * *****************************************************************************
	 * ***************
	 */

	/**
	 * Test method for adding a brand new resource into the system. It verifies
	 * after adding a resource if the user is redirected to the resource management
	 * page and a successful message is displayed.
	 */
	@Test
	public void testAddUnixFileSystemResource() {
		logger.info("Testing add a brand new resource from resource management");
		driver.get(UiTestUtilities.RESOURCES_URL);
		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addResourceButton")));
		driver.findElement(By.id("addResourceButton")).click();

		ResourceUtils.addResource(UiTestUtilities.RESOURCES_URL, RESOURCE_NAME, ResourceUtils.RESC_UNIX_FILE_SYSTEM, null, null,
				UiTestUtilities.IRODS_HOST, ResourceUtils.RESOURCE_PATH, driver);
	}

	/**
	 * Test method for adding a resource with same name of another already created.
	 * It verifies if it remains in same page and does not add new resource.
	 *
	 * @throws Exception
	 */
	@Test
	public void testAddSameResourceName() throws Exception {
		logger.info("Testing add a resource name that already exists");

		ResourceUtils.addResource(UiTestUtilities.RESOURCES_URL, RESOURCE_NAME, ResourceUtils.RESC_COMPOUND, null, null, null,
				null, driver);
		ResourceUtils.accessAddResourceFormFrom(UiTestUtilities.RESOURCES_URL, null, driver);
		ResourceUtils.fillAndSubmitResourceForm(RESOURCE_NAME, ResourceUtils.RESC_COMPOUND, null, null, null, null,
				driver);

		assertEquals(UiTestUtilities.ADD_RESOURCES_URL, driver.getCurrentUrl());

		assertTrue(ResourceUtils.errorMessageIsDisplayed("invalidResourceNameMsg", driver));
	}

	/**
	 * Test method for adding a new resource in resource map. It adds parent
	 * resource through management page, adds its resource child through map page
	 * and verifies with both are displayed.
	 */
	@Test
	@Ignore
	// TODO2 Test ignored when executing HTMLUnit driver. There is no support for
	// d3.select() function in SVG element.
	public void testAddResourceFromResourceMap() {
		logger.info("Testing add a brand new resource from resource map");
		ResourceUtils.addResource(UiTestUtilities.RESOURCES_URL, RESOURCE_NAME, ResourceUtils.RESC_COMPOUND, null, null, null,
				null, driver);
		ResourceUtils.addResource(UiTestUtilities.RESOURCES_MAP_URL, CHILD_RESOURCE_NAME, ResourceUtils.RESC_COMPOUND,
				RESOURCE_NAME, null, null, null, driver);

		assertTrue(ResourceUtils.isInResourcesList(UiTestUtilities.RESOURCES_URL, RESOURCE_NAME, null, driver));
		assertTrue(ResourceUtils.isInResourcesList(UiTestUtilities.RESOURCES_MAP_URL, CHILD_RESOURCE_NAME, null, driver));
	}

	/**
	 * Test method for adding a new resource in servers view. It verifies if
	 * resource is displayed in servers view after adding.
	 */
	@Test
	public void testAddResourceFromServersView() {
		logger.info("Testing add a brand new resource from servers view");

		ResourceUtils.addResource(UiTestUtilities.RESOURCES_SERVERS_URL, RESOURCE_NAME, ResourceUtils.RESC_DEFERRED, null, null,
				UiTestUtilities.IRODS_HOST, ResourceUtils.RESOURCE_PATH, driver);

		assertTrue(ResourceUtils.isInResourcesList(UiTestUtilities.RESOURCES_SERVERS_URL, RESOURCE_NAME, UiTestUtilities.IRODS_HOST,
				driver));
	}

	/**
	 * Test method for trying to add a brand new resource into the system with
	 * resource name empty. It verifies if system does not add it and throw a error
	 * message.
	 */
	@Test
	public void testAddEmptyResourceName() {
		logger.info("Testing add empty resource name");
		ResourceUtils.accessAddResourceFormFrom(UiTestUtilities.RESOURCES_URL, null, driver);
		ResourceUtils.fillAndSubmitResourceForm("", ResourceUtils.RESC_COMPOUND, null, null, null, null, driver);

		assertEquals(UiTestUtilities.ADD_RESOURCES_URL, driver.getCurrentUrl());

		assertTrue(ResourceUtils.errorMessageIsDisplayed("emptyResourceNameMsg", driver));
	}

	/**
	 * Test method for adding a resource with a invalid name, with blank spaces. It
	 * verifies if it remains in same page and does not add new resource.
	 */
	@Test
	public void testAddResourceNameWithBlankSpace() {
		logger.info("Testing add resource name with blank space");
		ResourceUtils.accessAddResourceFormFrom(UiTestUtilities.RESOURCES_URL, null, driver);
		ResourceUtils.fillAndSubmitResourceForm(RESOURCE_NAME_BLANK_SPACE, ResourceUtils.RESC_COMPOUND, null, null,
				null, null, driver);

		assertEquals(UiTestUtilities.ADD_RESOURCES_URL, driver.getCurrentUrl());

		assertTrue(ResourceUtils.errorMessageIsDisplayed("invalidResourceNameMsg", driver));
	}

	/**
	 * Test method for adding a storage resource with empty host. It verifies if it
	 * remains in same page, displays error message and does not add new resource.
	 */
	@Test
	public void testAddStorageResourceWithEmptyHost() {
		logger.info("Testing add resource with empty host");
		ResourceUtils.accessAddResourceFormFrom(UiTestUtilities.RESOURCES_URL, null, driver);
		ResourceUtils.fillAndSubmitResourceForm(RESOURCE_NAME, ResourceUtils.RESC_UNIX_FILE_SYSTEM, null, null, "",
				ResourceUtils.RESOURCE_PATH, driver);

		assertEquals(UiTestUtilities.ADD_RESOURCES_URL, driver.getCurrentUrl());

		assertTrue(ResourceUtils.errorMessageIsDisplayed("emptyResourceHostMsg", driver));
	}

	/**
	 * Test method for adding a storage resource with empty path. It verifies if it
	 * remains in same page, displays error message and does not add new resource.
	 */
	@Test
	public void testAddStorageResourceWithEmptyPath() {
		logger.info("Testing add resource with empty path");
		ResourceUtils.accessAddResourceFormFrom(UiTestUtilities.RESOURCES_URL, null, driver);
		ResourceUtils.fillAndSubmitResourceForm(RESOURCE_NAME, ResourceUtils.RESC_UNIX_FILE_SYSTEM, null, null,
				UiTestUtilities.IRODS_HOST, "", driver);

		assertEquals(UiTestUtilities.ADD_RESOURCES_URL, driver.getCurrentUrl());

		assertTrue(ResourceUtils.errorMessageIsDisplayed("emptyResourcePathMsg", driver));
	}

	/**
	 * Test method for canceling add resource. It verifies if it redirects to
	 * Resources Management page and does not add new resource.
	 */
	@Test
	public void testConfirmCancelingAddResource() {
		logger.info("Testing cancel a resource creation");
		ResourceUtils.accessAddResourceFormFrom(UiTestUtilities.RESOURCES_URL, null, driver);
		ResourceUtils.fillAndCancelResourceForm(RESOURCE_NAME, true, driver);
		assertTrue(!ResourceUtils.isInResourcesList(UiTestUtilities.RESOURCES_URL, RESOURCE_NAME, null, driver));
	}

	/**
	 * Test method for not canceling add resource. It verifies if it remains in add
	 * resource form page.
	 */
	@Test
	public void testNotConfirmCancelingAddResource() {
		logger.info("Testing cancel a resource creation");
		ResourceUtils.accessAddResourceFormFrom(UiTestUtilities.RESOURCES_URL, null, driver);
		ResourceUtils.fillAndCancelResourceForm(RESOURCE_NAME, false, driver);

		WebDriverWait wait = new WebDriverWait(driver, 8);
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("cancelModal")));

		assertEquals(UiTestUtilities.ADD_RESOURCES_URL, driver.getCurrentUrl());
	}
}
