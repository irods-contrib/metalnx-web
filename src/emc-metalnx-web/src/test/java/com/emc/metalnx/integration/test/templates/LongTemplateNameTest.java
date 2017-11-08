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

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.TemplateUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

import junit.framework.Assert;

@Deprecated
@Ignore
public class LongTemplateNameTest {
	private static WebDriver driver = null;
	private String longTemplateName = null;

	/*************************************
	 * TEST SET UP
	 *************************************/

	@BeforeClass
	public static void setUpBeforeClass() throws DataGridException {
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
		UiTestUtilities.login();
		driver.get(UiTestUtilities.TEMPLATES_URL);
		TemplateUtils.removeAllTemplates(driver);
		UiTestUtilities.logout();

		if (driver != null) {
			driver.quit();
			driver = null;
			UiTestUtilities.setDriver(null);
		}
	}

	/*************************************
	 * TESTS
	 *************************************/
	@Test
	public void testSystemTemplateNameWith60Characters() {
		longTemplateName = RandomStringUtils.randomAlphanumeric(60);
		TemplateUtils.createTemplateWithNoFields(driver, longTemplateName, TemplateUtils.SYSTEM_TEMPLATE_TYPE);
		TemplateUtils.assertTemplateSuccessfulCreation(driver, longTemplateName);

		TemplateUtils.searchByTemplateName(driver, longTemplateName);
		Assert.assertNotNull(driver
				.findElement(By.cssSelector("#templatesListTable tbody tr td[title='" + longTemplateName + "']")));
	}

	@Test
	public void testPrivateTemplateNameWith60Characters() {
		longTemplateName = RandomStringUtils.randomAlphanumeric(60);
		TemplateUtils.createTemplateWithNoFields(driver, longTemplateName, TemplateUtils.PRIVATE_TEMPLATE_TYPE);
		TemplateUtils.assertTemplateSuccessfulCreation(driver, longTemplateName);

		TemplateUtils.searchByTemplateName(driver, longTemplateName);
		Assert.assertNotNull(driver
				.findElement(By.cssSelector("#templatesListTable tbody tr td[title='" + longTemplateName + "']")));
	}

	@Test
	public void testSystemTemplateNameWith100Characters() {
		longTemplateName = RandomStringUtils.randomAlphanumeric(100);
		TemplateUtils.createTemplateWithNoFields(driver, longTemplateName, TemplateUtils.SYSTEM_TEMPLATE_TYPE);
		TemplateUtils.assertTemplateSuccessfulCreation(driver, longTemplateName);

		TemplateUtils.searchByTemplateName(driver, longTemplateName);
		Assert.assertNotNull(driver
				.findElement(By.cssSelector("#templatesListTable tbody tr td[title='" + longTemplateName + "']")));
	}

	@Test
	public void testPrivateTemplateNameWith100Characters() {
		longTemplateName = RandomStringUtils.randomAlphanumeric(100);
		TemplateUtils.createTemplateWithNoFields(driver, longTemplateName, TemplateUtils.PRIVATE_TEMPLATE_TYPE);
		TemplateUtils.assertTemplateSuccessfulCreation(driver, longTemplateName);

		TemplateUtils.searchByTemplateName(driver, longTemplateName);
		Assert.assertNotNull(driver
				.findElement(By.cssSelector("#templatesListTable tbody tr td[title='" + longTemplateName + "']")));
	}

	@Test
	public void testSystemTemplateNameWith200Characters() {
		longTemplateName = RandomStringUtils.randomAlphanumeric(200);
		String allowedTemplatedName = (String) longTemplateName.subSequence(0, 100);

		TemplateUtils.createTemplateWithNoFields(driver, longTemplateName, TemplateUtils.SYSTEM_TEMPLATE_TYPE);
		TemplateUtils.assertTemplateSuccessfulCreation(driver, allowedTemplatedName);

		TemplateUtils.searchByTemplateName(driver, longTemplateName);
		Assert.assertNotNull(driver.findElement(By.className("dataTables_empty")));

		TemplateUtils.searchByTemplateName(driver, allowedTemplatedName);
		Assert.assertNotNull(driver
				.findElement(By.cssSelector("#templatesListTable tbody tr td[title='" + allowedTemplatedName + "']")));

		longTemplateName = allowedTemplatedName;
	}

	@Test
	public void testPrivateTemplateNameWith200Characters() {
		longTemplateName = RandomStringUtils.randomAlphanumeric(200);
		String allowedTemplatedName = (String) longTemplateName.subSequence(0, 100);

		TemplateUtils.createTemplateWithNoFields(driver, longTemplateName, TemplateUtils.PRIVATE_TEMPLATE_TYPE);
		TemplateUtils.assertTemplateSuccessfulCreation(driver, allowedTemplatedName);

		TemplateUtils.searchByTemplateName(driver, longTemplateName);
		Assert.assertNotNull(driver.findElement(By.className("dataTables_empty")));

		TemplateUtils.searchByTemplateName(driver, allowedTemplatedName);
		Assert.assertNotNull(driver
				.findElement(By.cssSelector("#templatesListTable tbody tr td[title='" + allowedTemplatedName + "']")));

		longTemplateName = allowedTemplatedName;
	}
}
