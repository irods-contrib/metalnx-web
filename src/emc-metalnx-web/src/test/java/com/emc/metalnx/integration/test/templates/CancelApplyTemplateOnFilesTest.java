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

import java.util.ArrayList;
import java.util.List;

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

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.CollectionUtils;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.integration.test.utils.TemplateUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

import junit.framework.Assert;

/**
 * Class that tests the cancel option when the user is applying a template on
 * files.
 */
@Deprecated
@Ignore
public class CancelApplyTemplateOnFilesTest {
	private static final Logger logger = LoggerFactory.getLogger(TemplateTest.class);

	private static WebDriver driver = null;
	private static List<String> attributes = null;
	private static List<String> values = null;
	private static List<String> units = null;
	private String templateName = null;

	/*************************************
	 * TEST SET UP
	 *************************************/

	@BeforeClass
	public static void setUpBeforeClass() throws DataGridException {
		// UITest.setUpBeforeClass();
		driver = UiTestUtilities.getDriver();

		attributes = new ArrayList<String>();
		values = new ArrayList<String>();
		units = new ArrayList<String>();

		for (int i = 0; i < TemplateUtils.TOTAL_METADATA_FIELDS; i++) {
			attributes.add(TemplateUtils.attribute + i);
			values.add(TemplateUtils.value + i);
			units.add(TemplateUtils.unit + i);
		}

		FileUtils.removeFilesFromHomeAsAdmin(TemplateUtils.TEST_FILES);
	}

	@Before
	public void setUp() throws Exception {
		templateName = TemplateUtils.TEMPLATE_TEST_NAME + System.currentTimeMillis();
		UiTestUtilities.login();

		// Upload test files
		FileUtils.uploadToHomeDirAsAdmin(TemplateUtils.TEST_FILES);
		TemplateUtils.createTemplateWithFields(driver, templateName, TemplateUtils.SYSTEM_TEMPLATE_TYPE);
	}

	@After
	public void tearDown() throws Exception {
		TemplateUtils.removeTemplate(templateName, driver);

		// Remove test files
		FileUtils.removeFilesFromHomeAsAdmin(TemplateUtils.TEST_FILES);

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
	 * Test that checks if the cancel button in the step 1 (Template selection)
	 * works. No metadata should be added.
	 */
	@Test
	public void testCancelApplyTemplateStep1() {
		WebDriverWait wait = new WebDriverWait(driver, 10);

		logger.info("Cancel apply template selection");

		driver.get(UiTestUtilities.COLLECTIONS_URL);
		Assert.assertEquals(UiTestUtilities.COLLECTIONS_URL, driver.getCurrentUrl());

		CollectionUtils.waitForItemToLoad(driver, TemplateUtils.TEST_FILES[TemplateUtils.TEST_FILES.length - 1]);
		driver.findElement(By.id(TemplateUtils.TEST_FILES[0])).click();

		CollectionUtils.waitForSelectActionBtnToBeEnabled(driver);
		driver.findElement(CollectionUtils.SELECT_ACTION_BTN).click();

		CollectionUtils.waitForActionsDropdownToBeShown(driver);
		driver.findElement(CollectionUtils.APPLY_TEMPLATE_BTN).click();

		By templateList = By.id("templateList");
		wait.until(ExpectedConditions.visibilityOfElementLocated(templateList));
		Assert.assertTrue(driver.findElement(templateList).isDisplayed());

		driver.findElement(By.cssSelector("#templateList .modal-dialog .modal-footer .cancelBtn")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("treeViewTable")));
		Assert.assertTrue(driver.findElement(By.id("treeViewTable")).isDisplayed());
	}

	/**
	 * Test that checks if the cancel button in the step 2 (Templates' fields list)
	 * works. No metadata should be added.
	 */
	@Test
	public void testCancelApplyTemplateStep2() {
		WebDriverWait wait = new WebDriverWait(driver, 10);

		logger.info("Cancel apply template selection");

		driver.get(UiTestUtilities.COLLECTIONS_URL);
		Assert.assertEquals(UiTestUtilities.COLLECTIONS_URL, driver.getCurrentUrl());

		TemplateUtils.findTemplateToApply(driver, templateName, TemplateUtils.TEST_FILES);

		By templateFieldsList = By.id("templateFieldsList");
		wait.until(ExpectedConditions.visibilityOfElementLocated(templateFieldsList));
		Assert.assertTrue(driver.findElement(templateFieldsList).isDisplayed());

		driver.findElement(By.cssSelector("#templateFieldsList .modal-dialog .modal-footer .cancelBtn")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("treeViewTable")));
		Assert.assertTrue(driver.findElement(By.id("treeViewTable")).isDisplayed());
	}
}
