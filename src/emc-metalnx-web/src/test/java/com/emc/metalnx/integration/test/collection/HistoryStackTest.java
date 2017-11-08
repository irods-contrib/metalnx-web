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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import com.emc.metalnx.integration.test.utils.CollectionUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

@Deprecated
@Ignore
public class HistoryStackTest {
	private static WebDriver driver = null;
	private static WebDriverWait wait = null;

	private static String collectionName1 = "breadcrumbCollectionTestA" + System.currentTimeMillis();
	private static String collectionName2 = "breadcrumbCollectionTestB" + System.currentTimeMillis();
	private static String collectionName3 = "breadcrumbCollectionTestC" + System.currentTimeMillis();
	private static String[] arrayPaths = { CollectionUtils.RODS_COLL_PATH,
			String.format("%s/%s", CollectionUtils.RODS_COLL_PATH, collectionName1),
			String.format("%s/%s/%s", CollectionUtils.RODS_COLL_PATH, collectionName1, collectionName2), String.format(
					"%s/%s/%s/%s", CollectionUtils.RODS_COLL_PATH, collectionName1, collectionName2, collectionName3) };

	private By backButtonLocator = By.cssSelector("#backBtn");
	private By forwardButtonLocator = By.cssSelector("#forwardBtn");
	private By dropdownHistoryButtonLocator = By.cssSelector("#historyBtn");
	private By dropdownHistoryMenuLocator = By.cssSelector("#directoryPath > .btn-group > .dropdown-menu");

	private By inactivePathHistory = By
			.cssSelector("#directoryPath > .btn-group > ul.dropdown-menu > li:not(.active) a");

	@BeforeClass
	public static void setUpBeforeClass() {

		driver = UiTestUtilities.getDriver();
		wait = new WebDriverWait(driver, 5);

		UiTestUtilities.login();
		createCollectionHistoryTree(driver, wait);
		UiTestUtilities.logout();
	}

	@Before
	public void setUp() {
		UiTestUtilities.login();
		driver.get(UiTestUtilities.COLLECTIONS_URL);

		CollectionUtils.goToCollection(driver, collectionName1);
		CollectionUtils.goToCollection(driver, collectionName2);
		CollectionUtils.goToCollection(driver, collectionName3);
	}

	@After
	public void tearDown() {
		UiTestUtilities.logout();
	}

	@AfterClass
	public static void tearDownAfterClass() {
		UiTestUtilities.login();
		driver.get(UiTestUtilities.COLLECTIONS_URL);
		wait.until(ExpectedConditions.elementToBeClickable(By.className("breadcrumb")));
		CollectionUtils.writeOnEditableBreadCrumb(driver, wait, CollectionUtils.RODS_COLL_PATH);
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("#directoryPath .breadcrumb > li:last-of-type > span")));
		CollectionUtils.removeColl(driver, collectionName1);
		UiTestUtilities.logout();

		if (driver != null) {
			driver.quit();
			driver = null;
			UiTestUtilities.setDriver(null);
		}
	}

	/**
	 * Test if stack to go back in collection history is working and putting paths
	 * in the correct order. Creates collection and enters them to fill stack
	 */
	@Test
	public void stackSimpleBackHistory() {

		// Checks if back button is enabled
		Assert.assertTrue(checkButtonEnabled(driver, wait, backButtonLocator));

		// checks if current path is correct
		Assert.assertTrue(checkCurrentPath(driver, wait, arrayPaths[3]));

		// checks if older paths are in the correct order
		List<WebElement> liElements = driver.findElements(inactivePathHistory);
		for (int i = 0; i < liElements.size(); i++) {
			Assert.assertEquals(arrayPaths[liElements.size() - i - 1], liElements.get(i).getText());
		}

	}

	/**
	 * Test forward stack checking is function and order of the list
	 */
	@Test
	public void stackSimpleForwardHistory() {

		// going back in history stack
		wait.until(ExpectedConditions.elementToBeClickable(dropdownHistoryButtonLocator)).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(dropdownHistoryMenuLocator));
		driver.findElement(By.cssSelector("#directoryPath > .btn-group > ul.dropdown-menu > li:last-child a")).click();

		// making sure forward button got enabled
		Assert.assertTrue(checkButtonEnabled(driver, wait, forwardButtonLocator));

		// checks if current path is correct
		Assert.assertTrue(checkCurrentPath(driver, wait, CollectionUtils.RODS_COLL_PATH));

		// checks if older paths are in the correct order
		List<WebElement> liElements = driver.findElements(inactivePathHistory);
		for (int i = 0; i < liElements.size(); i++) {
			Assert.assertEquals(arrayPaths[liElements.size() - i], liElements.get(i).getText());
		}

	}

	/**
	 * Test if a path passed in editable breadcrumb is stored in history stack
	 */
	@Test
	public void stackFromEditableBreadcrumb() {

		// going to: /zone/home/user -> /zone -> /zone/home/user
		wait.until(ExpectedConditions.elementToBeClickable(By.className("breadcrumb")));
		CollectionUtils.writeOnEditableBreadCrumb(driver, wait, CollectionUtils.RODS_COLL_PATH);
		wait.until(ExpectedConditions.elementToBeClickable(By.className("breadcrumb")));
		CollectionUtils.writeOnEditableBreadCrumb(driver, wait, "/" + UiTestUtilities.IRODS_ZONE);
		wait.until(ExpectedConditions.elementToBeClickable(By.className("breadcrumb")));
		CollectionUtils.writeOnEditableBreadCrumb(driver, wait, CollectionUtils.RODS_COLL_PATH);

		// creating the expected stack history
		List<String> historyStackTemp = new ArrayList<String>(Arrays.asList(arrayPaths));
		historyStackTemp.add(CollectionUtils.RODS_COLL_PATH);
		historyStackTemp.add("/" + UiTestUtilities.IRODS_ZONE);

		// checks if current path is correct
		Assert.assertTrue(checkCurrentPath(driver, wait, CollectionUtils.RODS_COLL_PATH));

		// checks if older paths are in the correct order
		List<WebElement> liElements = driver.findElements(inactivePathHistory);
		for (int i = 0; i < liElements.size(); i++) {
			Assert.assertEquals(historyStackTemp.get(liElements.size() - i - 1), liElements.get(i).getText());
		}

	}

	/**
	 * Test if both back and forward stacks are in order if more than 1 step back is
	 * taken
	 */
	@Test
	public void stackJumpStepsBack() {

		// going back in history stack
		clickNthChildInHistory(driver, wait, 3);

		// making sure forward button got enabled
		Assert.assertTrue(checkButtonEnabled(driver, wait, forwardButtonLocator));

		// checks if current path is correct
		Assert.assertTrue(checkCurrentPath(driver, wait, arrayPaths[1]));

		// checks if older paths are in the correct order
		List<WebElement> liElements = driver.findElements(inactivePathHistory);
		String[] expectedPaths = { arrayPaths[3], arrayPaths[2], arrayPaths[0] };
		for (int i = 0; i < liElements.size(); i++) {
			Assert.assertEquals(expectedPaths[i], liElements.get(i).getText());
		}

	}

	/**
	 * Test if back and forward stacks are in order is more than 1 step forward is
	 * taken
	 */
	@Test
	public void stackJumpStepsForward() {
		// going back in history stack
		clickNthChildInHistory(driver, wait, 3);

		wait.until(ExpectedConditions.elementToBeClickable(forwardButtonLocator));

		// going forward in history stack
		clickNthChildInHistory(driver, wait, 2);

		// making sure forward button got enabled
		Assert.assertTrue(checkButtonEnabled(driver, wait, forwardButtonLocator));

		// wait.until(ExpectedConditions.elementToBeClickable(forwardButtonLocator));
		try {
			wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("çççç")));
		} catch (Exception e) {
		}

		// checks if current path is correct
		Assert.assertTrue(checkCurrentPath(driver, wait, arrayPaths[2]));

		// checks if older paths are in the correct order
		List<WebElement> liElements = driver.findElements(inactivePathHistory);
		String[] expectedPaths = { arrayPaths[3], arrayPaths[1], arrayPaths[0] };
		for (int i = 0; i < liElements.size(); i++) {
			Assert.assertEquals(expectedPaths[i], liElements.get(i).getText());
		}

	}

	/**
	 * Creates 3 collections, each a child of the previously created collection
	 *
	 * @param driver
	 * @param wait
	 */
	private static void createCollectionHistoryTree(WebDriver driver, WebDriverWait wait) {
		// creating first level collection
		CollectionUtils.createCollection(driver, collectionName1);

		// entering first level collection
		CollectionUtils.goToCollection(driver, collectionName1);

		// creating second level collection
		CollectionUtils.createCollection(driver, collectionName2);

		// entering second level collection
		CollectionUtils.goToCollection(driver, collectionName2);

		// creating third level collection
		CollectionUtils.createCollection(driver, collectionName3);

		// entering third level collection
		CollectionUtils.goToCollection(driver, collectionName3);
	}

	/**
	 * Check if current path in history stack is the same as the expected path
	 *
	 * @param driver
	 * @param wait
	 * @param expectedPath
	 * @return
	 */
	private boolean checkCurrentPath(WebDriver driver, WebDriverWait wait, String expectedPath) {
		wait.until(ExpectedConditions.elementToBeClickable(dropdownHistoryButtonLocator)).click();
		wait.until(ExpectedConditions.elementToBeClickable(dropdownHistoryMenuLocator));
		String currentPathInStack = driver
				.findElement(By.cssSelector("#directoryPath > .btn-group > .dropdown-menu > .active a")).getText();
		return expectedPath.equals(currentPathInStack);
	}

	/**
	 * Check if the button indicated by the locator is enabled
	 *
	 * @param driver
	 * @param wait
	 * @param buttonLocator
	 * @return
	 */
	private boolean checkButtonEnabled(WebDriver driver, WebDriverWait wait, By buttonLocator) {
		wait.until(ExpectedConditions.elementToBeClickable(buttonLocator));
		String buttonDisabled = driver.findElement(buttonLocator).getAttribute("disabled");
		if (buttonDisabled == null) {
			return true;
		}
		return false;
	}

	/**
	 * Click on the n-th child displayed in the drop down menu in history buttons
	 *
	 * @param driver
	 * @param wait
	 * @param nth
	 */
	private void clickNthChildInHistory(WebDriver driver, WebDriverWait wait, int nth) {
		String selectorString = String.format("#directoryPath > .btn-group > ul.dropdown-menu > li:nth-child(%d) a",
				nth);
		wait.until(ExpectedConditions.elementToBeClickable(dropdownHistoryButtonLocator)).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(dropdownHistoryMenuLocator));
		driver.findElement(By.cssSelector(selectorString)).click();
	}
}
