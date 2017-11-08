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

import java.util.UUID;

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

import junit.framework.Assert;

@Deprecated
@Ignore
public class SearchResourceTest {

	private static final Logger logger = LoggerFactory.getLogger(SearchResourceTest.class);

	private static WebDriver driver = null;

	private static final String RESC_NAME_1 = "demoResc1";
	private static final String RESC_NAME_2 = "demo_resc_2";
	private static final String RESC_NAME_3 = "demo_RESC_3";
	private static final String RESC_NAME_4 = "DEMORESC_4";

	private static final String ONE_LETTER_SEARCH_ALL = "e";
	private static final String MORE_LETTER_SEARCH_ALL = "resc";
	private static final String MORE_LETTER_SEARCH_1 = "_3";
	private static final String MORE_LETTER_SEARCH_0 = UUID.randomUUID().toString();

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

		// Adding resources to test search
		ResourceUtils.addResource(UiTestUtilities.RESOURCES_URL, RESC_NAME_1, ResourceUtils.RESC_COMPOUND, null, null, null,
				null, driver);
		ResourceUtils.addResource(UiTestUtilities.RESOURCES_URL, RESC_NAME_2, ResourceUtils.RESC_COMPOUND, null, null, null,
				null, driver);
		ResourceUtils.addResource(UiTestUtilities.RESOURCES_URL, RESC_NAME_3, ResourceUtils.RESC_COMPOUND, null, null, null,
				null, driver);
		ResourceUtils.addResource(UiTestUtilities.RESOURCES_URL, RESC_NAME_4, ResourceUtils.RESC_COMPOUND, null, null, null,
				null, driver);
	}

	/**
	 * After each test the user created for the test should be removed.
	 */
	@After
	public void tearDown() throws Exception {
		driver.get(UiTestUtilities.RESOURCES_URL);
		WebDriverWait wait = new WebDriverWait(driver, 15);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("resourcesListTable")));

		ResourceUtils.removeResource(RESC_NAME_1, driver);
		ResourceUtils.removeResource(RESC_NAME_2, driver);
		ResourceUtils.removeResource(RESC_NAME_3, driver);
		ResourceUtils.removeResource(RESC_NAME_4, driver);

		UiTestUtilities.logout();
	}

	/**
	 * After all tests are done, the test must quit the driver. This will close
	 * every window associated with the current driver instance.
	 *
	 * @throws Exception
	 */

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if (driver != null) {
			driver.quit();
			driver = null;
			UiTestUtilities.setDriver(null);
		}
	}

	/*
	 * *****************************************************************************
	 * *************** *********************************** SEARCH RESOURCE
	 * ****************************************
	 * *****************************************************************************
	 * ***************
	 */

	/**
	 * Test method to make a search by empty content in search box. It verifies if
	 * same resources before search are displayed after type ENTER in search box.
	 */
	@Test
	public void testSearchEmptyContent() {
		logger.info("Testing search with none search content");

		int initialCount = ResourceUtils.getResourcesCount(driver);
		int resultCount = ResourceUtils.searchResource("", initialCount, driver);

		Assert.assertEquals(initialCount, resultCount);
	}

	/**
	 * Test method to execute a search by one letter in upper case in search box. It
	 * verifies if no filtering is dispatched once that it will only be triggered by
	 * typing more than one letter.
	 */
	@Test
	public void testSearchByOneLetterUpperCase() {
		logger.info("Testing search by one letter upper case");

		int initialCount = ResourceUtils.getResourcesCount(driver);
		int resultCount = ResourceUtils.searchResource(ONE_LETTER_SEARCH_ALL.toUpperCase(), initialCount, driver);

		Assert.assertEquals(initialCount, resultCount);
	}

	/**
	 * Test method to execute a search by one letter in lower case in search box. It
	 * verifies if no filtering is dispatched once that it will only be triggered by
	 * typing more than one letter.
	 */
	@Test
	public void testSearchByOneLetterLowerCase() {
		logger.info("Testing search by one letter lower case");

		int initialCount = ResourceUtils.getResourcesCount(driver);
		int resultCount = ResourceUtils.searchResource(ONE_LETTER_SEARCH_ALL.toLowerCase(), initialCount, driver);

		Assert.assertEquals(initialCount, resultCount);
	}

	/**
	 * Test method to execute a search by more than one letter in upper case in
	 * search box. It verifies if search is case insensitive and filters resources
	 * correctly.
	 */
	@Test
	public void testSearchByMoreThanOneLetterUpperCase() {
		logger.info("Testing search by more letters upper case");

		int initialCount = ResourceUtils.getResourcesCount(driver);
		int resultCount = ResourceUtils.searchResource(MORE_LETTER_SEARCH_ALL.toUpperCase(), initialCount, driver);

		Assert.assertEquals(initialCount, resultCount);
	}

	/**
	 * Test method to execute a search by more than one letter in lower case in
	 * search box. It verifies if search is case insensitive and filters resources
	 * correctly.
	 */
	@Test
	public void testSearchByMoreThanOneLetterLowerCase() {
		logger.info("Testing search by more letters lower case");

		int initialCount = ResourceUtils.getResourcesCount(driver);
		int resultCount = ResourceUtils.searchResource(MORE_LETTER_SEARCH_ALL.toLowerCase(), initialCount, driver);
		Assert.assertEquals(initialCount, resultCount);
	}

	/**
	 * Test method to execute a search by more than one letter. It verifies if only
	 * one resource will be show after search.
	 */
	@Test
	public void testSearchByMoreThanOneLetterResultOne() {
		logger.info("Testing search by more letters resulting only one result");

		int expectedCount = 1;
		int resultCount = ResourceUtils.searchResource(MORE_LETTER_SEARCH_1, expectedCount, driver);

		Assert.assertEquals(expectedCount, resultCount);
	}

	/**
	 * Test method to execute a search by more than one letter. It verifies if no
	 * resources will be show after search.
	 */
	@Test
	public void testSearchByMoreThanOneLetterEmptyResult() {
		logger.info("Testing search by more letters resulting only one result");

		int expectedCount = 0;
		int resultCount = ResourceUtils.searchResource(MORE_LETTER_SEARCH_0, expectedCount, driver);

		Assert.assertEquals(expectedCount, resultCount);
	}
}
