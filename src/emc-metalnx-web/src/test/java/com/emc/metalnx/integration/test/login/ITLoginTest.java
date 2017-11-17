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

package com.emc.metalnx.integration.test.login;

import static org.junit.Assert.assertEquals;

import java.util.Properties;

import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.test.generic.UiTestUtilities;

public class ITLoginTest {

	private static final Logger logger = LoggerFactory.getLogger(ITLoginTest.class);

	private static Properties testingProperties = new Properties();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "ITLoginTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	private static WebDriver driver = null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
		UiTestUtilities.init();
		driver = UiTestUtilities.getDriver();

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
			irodsFileSystem.closeAndEatExceptions();
		}
	}

	/**
	 * Tests a valid username and password for login, and checks if it moves to the
	 * dashboard page
	 *
	 * @throws Exception
	 */
	@Test
	public void testValidUsernameAndPasswordForLogin() throws Exception {
		logger.info("Testing valid username and password for login");
		UiTestUtilities.login(UiTestUtilities.testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY),
				UiTestUtilities.testingProperties.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));

		// check if after login, the user is redirected to the dashboard page
		assertEquals(UiTestUtilities.DASHBOARD_URL, driver.getCurrentUrl());
		UiTestUtilities.logout();
	}

	/**
	 * Tests an invalid username and password for login and checks if an error is
	 * shown
	 *
	 * @throws Exception
	 */
	@Test
	public void testInvalidUsernameAndPasswordForLogin() throws Exception {
		logger.info("Testing invalid username and password for login");
		UiTestUtilities.login("ThisIsAnInvalidUsername", "ThisIsAnInvalidPassword");

		/*
		 * There is no error message for invalid login WebElement errorMsg =
		 * driver.findElement(By.className("errorMsg")); check if after entering invalid
		 * login credentials (username and password), an error message is shown
		 */
		Assert.assertEquals(UiTestUtilities.LOGINERROR_URL, driver.getCurrentUrl());
	}
}
