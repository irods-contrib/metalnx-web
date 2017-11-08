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
import org.junit.Assert;
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

import com.emc.metalnx.integration.test.utils.TemplateUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

/**
 * Class created in order to create a template with a name that is already in
 * use by another template.
 *
 */
@Deprecated
@Ignore
public class DuplicatedNameTemplateTest {

	private static final Logger logger = LoggerFactory.getLogger(DuplicatedNameTemplateTest.class);

	private static WebDriver driver = null;
	private String templateName = null;

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
		templateName = TemplateUtils.TEMPLATE_TEST_NAME + System.currentTimeMillis();
		UiTestUtilities.login();
		TemplateUtils.createTemplateWithNoFields(driver, templateName, TemplateUtils.PRIVATE_TEMPLATE_TYPE);
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

	/****************************************
	 * TESTS
	 ****************************************/

	/**
	 * Test method for adding a brand new private template with no metadata fields
	 * with an existing name;
	 */
	@Test
	public void testAddPrivateTemplateWithDuplicatedName() {
		logger.info("Testing add a new private template with an existing name");
		addTemplateWithDuplicatedNameAndCheckErrorMsg(templateName, TemplateUtils.PRIVATE_TEMPLATE_TYPE);
	}

	/**
	 * Test method for adding a brand new system template with no metadata fields
	 * with an existing name;
	 */
	@Test
	public void testAddSystemTemplateWithDuplicatedName() {
		logger.info("Testing add a new system template with an existing name");
		addTemplateWithDuplicatedNameAndCheckErrorMsg(templateName, TemplateUtils.SYSTEM_TEMPLATE_TYPE);
	}

	/**
	 * Generic method used for testing template creation failure message due to
	 * duplicated-template-name error.
	 *
	 * @param type
	 */
	private void addTemplateWithDuplicatedNameAndCheckErrorMsg(String template, String type) {
		driver.get(UiTestUtilities.ADD_TEMPLATES_URL);
		TemplateUtils.fillInTemplateInformation(template, TemplateUtils.TEMPLATE_TEST_DESC,
				TemplateUtils.TEMPLATE_USE_INFO, type, driver);

		new WebDriverWait(driver, 15)
				.until(ExpectedConditions.visibilityOfElementLocated(By.id("invalidTemplateNameMsg")));

		Assert.assertTrue(driver.findElement(By.id("invalidTemplateNameMsg")).isDisplayed());
	}
}
