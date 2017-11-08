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
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.integration.test.utils.TemplateUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

import junit.framework.Assert;

/**
 * Class that tests the application of a template with metadata fields on files.
 */
@Deprecated
@Ignore
public class ApplyTemplateOnFilesTest {
	private static final Logger logger = LoggerFactory.getLogger(TemplateTest.class);

	private static WebDriver driver = null;
	private static List<String> attributes = null;
	private static List<String> values = null;
	private static List<String> units = null;
	private static String templateName = null;

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

		FileUtils.forceRemoveFilesFromHomeAsAdmin(TemplateUtils.TEST_FILES);
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
		FileUtils.forceRemoveFilesFromHomeAsAdmin(TemplateUtils.TEST_FILES);

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
	 * Method that tests if a template can be applied to a file. The template is
	 * applied as it is.
	 */
	@Test
	public void testApplyTemplate() {
		logger.info("Apply template with metadata fields");

		driver.get(UiTestUtilities.COLLECTIONS_URL);
		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
		Assert.assertEquals(UiTestUtilities.COLLECTIONS_URL, driver.getCurrentUrl());

		TemplateUtils.findTemplateToApply(driver, templateName, TemplateUtils.TEST_FILES);
		TemplateUtils.submitApplyTemplateForm(driver);
		TemplateUtils.isSuccessMessageShown(driver);
		TemplateUtils.checkIfMetadataWasAdded(driver, false, attributes, values, units, TemplateUtils.TEST_FILES);
	}

	/**
	 * Method that tests if a template can be applied to a file. The template values
	 * are changed.
	 */
	@Test
	public void testApplyTemplateChangingValues() {
		logger.info("Apply template with metadata fields changing metadata values");

		driver.get(UiTestUtilities.COLLECTIONS_URL);
		Assert.assertEquals(UiTestUtilities.COLLECTIONS_URL, driver.getCurrentUrl());

		TemplateUtils.findTemplateToApply(driver, templateName, TemplateUtils.TEST_FILES);
		List<String> changedValues = changeValuesOfTemplateBeforeApplying();
		TemplateUtils.submitApplyTemplateForm(driver);
		TemplateUtils.isSuccessMessageShown(driver);
		TemplateUtils.checkIfMetadataWasAdded(driver, false, attributes, changedValues, units,
				TemplateUtils.TEST_FILES);
	}

	/**
	 * Auxiliar method that changes the attribute values of a template just before
	 * it is applied to a file. It assumes the template was already found previously
	 * and the driver is on the right page.
	 */
	private List<String> changeValuesOfTemplateBeforeApplying() {
		List<WebElement> oldValues = driver.findElements(By.name("avuValues"));
		List<String> changedValues = new ArrayList<String>();

		if (oldValues != null) {
			for (WebElement inputValue : oldValues) {
				String newValue = "applyTemplate" + Math.round(Math.random());
				int index = values.lastIndexOf(inputValue.getAttribute("value"));
				changedValues.add(index, newValue);
				inputValue.click();
				inputValue.clear();
				inputValue.sendKeys(changedValues.get(index));
			}
		}

		return changedValues;
	}
}
