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

package com.emc.metalnx.integration.test.collection;

import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.CollectionUtils;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

@Deprecated
@Ignore
public class ReplicateFileTest {
	private static int NUMBER_OF_ITERATIONS = 100;
	private static WebDriver driver = null;
	private static final String TEST_FILE = "1SeleniumTestMetadataSearch.png";
	private static String irodsCollectionAbsolutePath = String.format("/%s/home/%s/", UiTestUtilities.IRODS_ZONE,
			UiTestUtilities.RODS_USERNAME);
	private static String irodsFileAbsolutePath = String.format("%s%s", irodsCollectionAbsolutePath, TEST_FILE);
	private static String targetResource = "targetResource";
	private static WebDriverWait wait;

	@BeforeClass
	public static void setUpBeforeClass() {
		// UITest.setUpBeforeClass();
		driver = UiTestUtilities.getDriver();
		wait = new WebDriverWait(driver, 10);
	}

	@Before
	public void setUp() throws Exception {
		FileUtils.removeFilesFromHomeAsAdmin(TEST_FILE);
		UiTestUtilities.login();
	}

	/**
	 * After each test the user created for the test should be removed.
	 */
	@After
	public void tearDown() throws Exception {
		FileUtils.removeFilesFromHomeAsAdmin(TEST_FILE);
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

	@Test
	public void testUploadAFileAndReplicateIt() throws DataGridException {
		driver.get(UiTestUtilities.COLLECTIONS_URL);

		int i;
		for (i = 0; i < NUMBER_OF_ITERATIONS; i++) {
			FileUtils.uploadToHomeDirAsAdmin(TEST_FILE);
			CollectionUtils.replicateFile(driver, targetResource, TEST_FILE);
			Assert.assertTrue(isDataObjectReplicated(targetResource, TEST_FILE));
			FileUtils.removeFilesFromHomeAsAdmin(TEST_FILE);
		}

		Assert.assertTrue(NUMBER_OF_ITERATIONS - i == 0);
	}

	@Test
	public void testReplicateFile() throws DataGridException {
		FileUtils.uploadToHomeDirAsAdmin(TEST_FILE);

		driver.get(UiTestUtilities.COLLECTIONS_URL);

		int i;
		for (i = 0; i < NUMBER_OF_ITERATIONS; i++) {
			CollectionUtils.goToHomeCollection(driver);
			CollectionUtils.replicateFile(driver, targetResource, TEST_FILE);
			CollectionUtils.waitForSuccessMessage(driver);
			CollectionUtils.deleteFileReplicas(driver, irodsFileAbsolutePath);
		}

		Assert.assertTrue(NUMBER_OF_ITERATIONS - i == 0);
	}

	@Test
	public void testReplicateFileAfterUploadUsingJargonAPIDirectly() throws DataGridException, JargonException {
		DataObjectAO dataObectAO = FileUtils.getDataObjectAO(UiTestUtilities.RODS_USERNAME, UiTestUtilities.RODS_PASSWORD);
		driver.get(UiTestUtilities.COLLECTIONS_URL);

		int i;
		for (i = 0; i < NUMBER_OF_ITERATIONS; i++) {
			FileUtils.uploadToHomeDirAsAdmin(TEST_FILE);
			dataObectAO.replicateIrodsDataObject(irodsFileAbsolutePath, targetResource);
			FileUtils.removeFilesFromHomeAsAdmin(TEST_FILE);
		}

		Assert.assertTrue(NUMBER_OF_ITERATIONS - i == 0);
	}

	private boolean isDataObjectReplicated(String resource, String filename) {
		CollectionUtils.goToHomeCollection(driver);

		By file = By.cssSelector("a[name=\"" + irodsFileAbsolutePath + "\"]");
		CollectionUtils.goToHomeCollection(driver);

		wait.until(ExpectedConditions.elementToBeClickable(file));
		driver.findElement(file).click();

		CollectionUtils.waitForReplicasTable(driver);

		boolean isReplicated = false;
		List<WebElement> replicas = driver.findElements(By.cssSelector("#replicaAndChecksumInfo table tbody tr td"));
		for (WebElement r : replicas) {
			if (resource.equalsIgnoreCase(r.getText())) {
				isReplicated = true;
				break;
			}
		}

		CollectionUtils.goToHomeCollection(driver);

		return isReplicated;
	}
}
