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

package com.emc.metalnx.integration.test.templates;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.CollectionUtils;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.integration.test.utils.TemplateUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

import junit.framework.Assert;

/**
 * Class that tests the application of an empty template (template with no
 * metadata fields) on files and collections.
 */
@Deprecated
@Ignore
public class ApplyEmptyTemplateTest {
	private static final Logger logger = LoggerFactory.getLogger(TemplateTest.class);

	private static WebDriver driver = null;
	private String templateName = null;

	/*************************************
	 * TEST SET UP
	 *************************************/

	@BeforeClass
	public static void setUpBeforeClass() throws DataGridException {
		// UITest.setUpBeforeClass();
		driver = UiTestUtilities.getDriver();

		// Environment clean up
		FileUtils.removeFilesFromHomeAsAdmin(TemplateUtils.TEST_FILES);
	}

	@Before
	public void setUp() throws Exception {
		templateName = TemplateUtils.TEMPLATE_TEST_NAME + System.currentTimeMillis();
		UiTestUtilities.login();
		TemplateUtils.createTemplateWithNoFields(driver, templateName, TemplateUtils.SYSTEM_TEMPLATE_TYPE);
	}

	@After
	public void tearDown() throws Exception {
		TemplateUtils.removeTemplate(templateName, driver);
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

	/*************************************
	 * TESTS
	 *************************************/

	/**
	 * Method that tests if Metalnx prevents an empty template from being applied to
	 * files.
	 *
	 * @throws DataGridException
	 */
	@Test
	public void testApplyTemplateWithNoMetadataFieldsOnFile() throws DataGridException {
		logger.info("Apply template with no metadata fields");

		// Upload test files
		FileUtils.uploadToHomeDirAsAdmin(TemplateUtils.TEST_FILES);

		driver.get(UiTestUtilities.COLLECTIONS_URL);
		Assert.assertEquals(UiTestUtilities.COLLECTIONS_URL, driver.getCurrentUrl());

		TemplateUtils.findTemplateToApply(driver, templateName, TemplateUtils.TEST_FILES);

		new WebDriverWait(driver, 10)
				.until(ExpectedConditions.visibilityOfElementLocated(TemplateUtils.SUBMIT_TEMPLATE_BTN));
		Assert.assertFalse(driver.findElement(TemplateUtils.SUBMIT_TEMPLATE_BTN).isEnabled());

		// Remove test files
		FileUtils.removeFilesFromHomeAsAdmin(TemplateUtils.TEST_FILES);
	}

	/**
	 * Method that tests if Metalnx prevents an empty template from being applied to
	 * collections.
	 */
	@Test
	public void testApplyTemplateWithNoMetadataFieldsOnColl() {
		logger.info("Apply template with no metadata fields");

		CollectionUtils.createCollection(driver, TemplateUtils.TEST_COLLS[0]);

		driver.get(UiTestUtilities.COLLECTIONS_URL);

		Assert.assertEquals(UiTestUtilities.COLLECTIONS_URL, driver.getCurrentUrl());

		TemplateUtils.findTemplateToApply(driver, templateName, TemplateUtils.TEST_COLLS);

		new WebDriverWait(driver, 10)
				.until(ExpectedConditions.visibilityOfElementLocated(TemplateUtils.SUBMIT_TEMPLATE_BTN));
		Assert.assertFalse(driver.findElement(TemplateUtils.SUBMIT_TEMPLATE_BTN).isEnabled());

		CollectionUtils.removeColl(driver, TemplateUtils.TEST_COLLS[0]);
	}
}
