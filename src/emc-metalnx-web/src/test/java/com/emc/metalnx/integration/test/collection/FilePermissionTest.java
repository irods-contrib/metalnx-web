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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.CollectionUtils;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.integration.test.utils.MetadataUtils;
import com.emc.metalnx.integration.test.utils.UserUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

@Deprecated
@Ignore
public class FilePermissionTest {

	private static String uname = "collectionPermission" + System.currentTimeMillis();
	private static String pwd = "webdriver";
	private static String publicColl = String.format("/%s/home/public", UiTestUtilities.IRODS_ZONE);

	private static final By COLLECTION_TABLE_ELEMENT = By.cssSelector("#treeViewTable tbody a:nth-child(1)");
	private static WebDriverWait wait = null;
	private static WebDriver driver = null;
	private static final Logger logger = LoggerFactory.getLogger(FilePermissionTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws DataGridException {

		FileUtils.uploadToDirAsAdmin(publicColl, MetadataUtils.METADATA_SEARCH_FILES);
		UserUtils.createUser(uname, pwd, UiTestUtilities.RODS_USER_TYPE, driver);
	}

	@After
	public void tearDown() {
		UiTestUtilities.logout();
	}

	@AfterClass
	public static void tearDownAfterClass() throws DataGridException {
		FileUtils.forceRemoveFilesFromDirAsAdmin(publicColl, MetadataUtils.METADATA_SEARCH_FILES);
		UserUtils.removeUser(uname, driver);

		if (driver != null) {
			driver.quit();
			driver = null;
			UiTestUtilities.setDriver(null);
		}
	}

	/**
	 * Test method to check if it is possible to access a file in public folder that
	 * does not belong to the current user without crashing the application. The
	 * ideal scenario would be to display a message informing that the user does not
	 * have permission access to that file
	 */
	@Test
	public void testOpenFileFromAnotherUser() {
		logger.info("Testing if a 500 error doesn't show on screen");
		By lackOfPermissionLocator = By.cssSelector(".browse-collection-files-content > div > .text-center span");

		// Code below logs in as rodsuser to try and access a file that does not belong
		// to him
		CollectionUtils.goToPublicCollection(driver, UiTestUtilities.RODS_USER_TYPE, uname, pwd);

		wait.until(ExpectedConditions.visibilityOfElementLocated(COLLECTION_TABLE_ELEMENT));
		driver.findElement(COLLECTION_TABLE_ELEMENT).click();

		wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("directoryPath"), "public"));
		wait.until(ExpectedConditions.visibilityOfElementLocated(lackOfPermissionLocator));
		WebElement messageNoPermission = driver.findElement(lackOfPermissionLocator);
		assertTrue(messageNoPermission.getText().contains(
				"You do not have permissions to access information in the target collection or for the selected object."));
	}

	/**
	 * Method to test if giving read permission to another user works fine
	 */
	@Test
	public void testGiveReadPermissions() {
		logger.info("Testing permission on collections");
		CollectionUtils.goToPublicCollection(driver, UiTestUtilities.RODS_ADMIN_TYPE, null, null);

		// giving permissions
		wait.until(ExpectedConditions.presenceOfElementLocated(COLLECTION_TABLE_ELEMENT));
		List<WebElement> files = driver.findElements(COLLECTION_TABLE_ELEMENT);

		/*
		 * Getting first item in the file list
		 */
		WebElement file = files.get(0);
		String fileName = file.findElement(By.tagName("span")).getText();
		file.click();
		// once in the file location, we use the method below to add a permission
		// through
		// permission tab
		addPermissionToCollection(driver, uname, "READ");

		// checking if nookmark exists for the newly created user
		UserUtils.checkIfBookmarksExist(uname, pwd, new String[] { fileName }, driver);

	}

	/**
	 * Method to test if giving write permission to another user works fine
	 */
	@Test
	public void testGiveWritePermissions() {
		logger.info("Testing permission on collections");
		CollectionUtils.goToPublicCollection(driver, UiTestUtilities.RODS_ADMIN_TYPE, null, null);

		// giving permissions
		wait.until(ExpectedConditions.visibilityOfElementLocated(COLLECTION_TABLE_ELEMENT));
		List<WebElement> files = driver.findElements(COLLECTION_TABLE_ELEMENT);

		/*
		 * Getting second item in the file list
		 */
		WebElement file = files.get(0);
		String fileName = file.findElement(By.tagName("span")).getText();
		file.click();
		// once in the file location, we use the method below to add a permission
		// through
		// permission tab
		addPermissionToCollection(driver, uname, "WRITE");

		// checking if nookmark exists for the newly created user
		UserUtils.checkIfBookmarksExist(uname, pwd, new String[] { fileName }, driver);

	}

	/**
	 * Method to test if giving ownership to another user works fine
	 */
	@Test
	public void testGiveOwnerPermissions() {
		logger.info("Testing permission on collections");

		WebDriverWait wait = new WebDriverWait(driver, 5);
		CollectionUtils.goToPublicCollection(driver, UiTestUtilities.RODS_ADMIN_TYPE, null, null);

		// giving permissions
		wait.until(ExpectedConditions.presenceOfElementLocated(COLLECTION_TABLE_ELEMENT));
		List<WebElement> files = driver.findElements(COLLECTION_TABLE_ELEMENT);

		/*
		 * Getting third item in the file list
		 */
		WebElement file = files.get(0);
		String fileName = file.findElement(By.tagName("span")).getText();
		file.click();
		// once in the file location, we use the method below to add a permission
		// through
		// permission tab
		addPermissionToCollection(driver, uname, "OWN");

		// checking if nookmark exists for the newly created user
		UserUtils.checkIfBookmarksExist(uname, pwd, new String[] { fileName }, driver);

	}

	/**
	 * This method below is used so that once the driver is in the file's location,
	 * we navigate through the tabs to add a permission to the current file
	 */
	private void addPermissionToCollection(WebDriver driver, String uname, String permission) {

		// getting to permissions tab
		try {
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("çççç")));
		} catch (Exception e) {
		}
		wait.until(ExpectedConditions.elementToBeClickable(By.id("breadcrumbOptionsMenu")));
		driver.findElement(By.id("breadcrumbOptionsMenu")).click();
		wait.until(ExpectedConditions.elementToBeClickable(By.id("permissions")));
		driver.findElement(By.id("permissions")).click();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("permissionsTabList")));
		assertTrue(driver.findElement(By.id("permissionsTabList")).isDisplayed());

		// getting to user permissions tab
		wait.until(
				ExpectedConditions.elementToBeClickable(By.cssSelector(".nav.nav-tabs a[href=\"#userPermissions\"]")));
		driver.findElement(By.cssSelector(".nav.nav-tabs a[href=\"#userPermissions\"]")).click();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("userPermissions")));
		assertTrue(driver.findElement(By.id("userPermissions")).isDisplayed());

		// open add user permission modal
		By inputName = By.cssSelector("#newUserPermissionModal .tt-input");
		driver.findElement(By.cssSelector("#addPermissionButtonDiv > .dropdown-toggle")).click();
		wait.until(ExpectedConditions.elementToBeClickable(
				By.cssSelector("#addPermissionButtonDiv a[data-target=\"#newUserPermissionModal\"]")));
		driver.findElement(By.cssSelector("#addPermissionButtonDiv a[data-target=\"#newUserPermissionModal\"]"))
				.click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(inputName));
		driver.findElement(inputName).click();
		driver.findElement(inputName).clear();
		for (char letter : uname.toCharArray()) {
			driver.findElement(inputName).sendKeys(Character.toString(letter));
		}
		driver.findElement(inputName).sendKeys(Keys.ARROW_DOWN);
		driver.findElement(inputName).sendKeys(Keys.ENTER);

		// By suggestions = By.cssSelector("#newUserPermissionModal .tt-suggestion");
		// wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(suggestions));
		// wait.until(ExpectedConditions.elementToBeClickable(suggestions));

		// driver.findElement(suggestions).click();
		// choosing read permission
		new Select(driver.findElement(By.id("userPermissionToBeSet"))).selectByValue(permission);

		driver.findElement(By.id("submitUserToBeAddedButton")).click();
		wait.until(ExpectedConditions.textToBePresentInElementLocated(By.id("userPermissionsTable"), uname));
		assertTrue(driver.findElement(By.id("userPermissionsTable")).getText().contains(uname));
	}

}
