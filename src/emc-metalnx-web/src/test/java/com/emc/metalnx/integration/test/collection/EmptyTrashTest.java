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

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.CollectionUtils;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

@Deprecated
@Ignore
public class EmptyTrashTest {
	private static final Logger logger = LoggerFactory.getLogger(EmptyTrashTest.class);
	private static WebDriver driver = null;
	private static WebDriverWait wait = null;
	private static final String[] TEST_FILES = { "emptyTrashTest.png" };

	@BeforeClass
	public static void setUpBeforeClass() {

	}

	@Before
	public void setUp() throws Exception {
		UiTestUtilities.login();
	}

	/**
	 * After each test the user created for the test should be removed.
	 */
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
		if (driver != null) {
			driver.quit();
			driver = null;
			UiTestUtilities.setDriver(null);
		}
	}

	/**
	 * Method used for testing if all items are removed from the trash folder once
	 * the user clicks on the empty trash button. No results should be shown.
	 *
	 * TimeoutException is expected since no items should be shown.
	 *
	 * @throws DataGridException
	 */
	@Test
	public void testEmptyTrash() throws DataGridException {
		logger.info("Testing if empty trash button works");

		FileUtils.uploadToHomeDirAsAdmin(TEST_FILES);
		FileUtils.removeFilesFromHomeAsAdmin(TEST_FILES);

		driver.get(UiTestUtilities.TRASH_URL);

		CollectionUtils.clickOnEmptyTrash(driver);
		CollectionUtils.confirmEmptyTrash(driver);

		Assert.assertEquals(UiTestUtilities.TRASH_URL, driver.getCurrentUrl());

		// check if a feedback message of success is displayed
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
		WebElement successMsg = driver.findElement(By.className("alert-success"));
		Assert.assertTrue(successMsg.isDisplayed());

		// wait for items to be deleted and checking if no results were found
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("table tbody tr td[class='dataTables_empty']")));
	}

	/**
	 * Method used for testing if the empty trash button shows up when the user is
	 * in the trash folder.
	 *
	 * NoSuchElementException is expected if no results are shown, then the empty
	 * trash won't show up.
	 */
	@Test(expected = NoSuchElementException.class)
	public void testIfEmptyTrashButtonIsShown() {
		logger.info("Testing if empty trash button shows up");

		driver.get(UiTestUtilities.TRASH_URL);

		// check if there is any item in the trash
		driver.findElement(CollectionUtils.COLLS_TABLE);

		// if there is at least one item in the trash, the empty trash button must be
		// shown
		Assert.assertNotNull(driver.findElement(CollectionUtils.EMPTY_TRASH_BTN));
	}

	/**
	 * Method used for testing if a confirmation modal is displayed after the user
	 * clicks on the empty trash button.
	 *
	 * @throws DataGridException
	 */
	@Test
	public void testIfAfterClikingOnEmptyTrashButtonAConfirmationShowsUp() throws DataGridException {
		logger.info("Testing if empty trash button shows up");

		String trashTestFile = "emptyTrashTest.png";

		FileUtils.uploadToHomeDirAsAdmin(trashTestFile);
		FileUtils.removeFilesFromHomeAsAdmin(trashTestFile);

		driver.get(UiTestUtilities.TRASH_URL);

		wait.until(ExpectedConditions.elementToBeClickable(CollectionUtils.EMPTY_TRASH_BTN));
		driver.findElement(CollectionUtils.EMPTY_TRASH_BTN).click();

		// a confirmation modal should be shown after clicking on the empty trash button
		Assert.assertNotNull(driver.findElement(CollectionUtils.EMPTY_TRASH_MODAL));

		String trashPath = String.format("/%s/trash/home/%s", UiTestUtilities.IRODS_ZONE, UiTestUtilities.RODS_USERNAME);
		FileUtils.forceRemoveFilesFromDirAsAdmin(trashPath, trashTestFile);
	}

	/**
	 * Method used for testing if the empty trash button is NOT shown in the
	 * collection management page.
	 *
	 * NoSuchElementException is expected since the empty trash button is not
	 * supposed to be displayed in the collections mgmt. page
	 */
	@Test(expected = NoSuchElementException.class)
	public void testIfEmptyTrashButtonIsNotShownInTheCollPage() {
		logger.info("Testing if empty trash button is not shown in the collections mgmt. page");

		driver.get(UiTestUtilities.COLLECTIONS_URL);

		// if the user is in the collections mgmt. page, the empty trash button must NOT
		// be shown
		driver.findElement(CollectionUtils.EMPTY_TRASH_BTN);

		// if the test gets to this point, it means the empty trash button is available
		Assert.fail();
	}
}
