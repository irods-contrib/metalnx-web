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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.integration.test.utils.MetadataUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

@Deprecated
@Ignore
public class MetadataTest {

	private static final Logger logger = LoggerFactory.getLogger(MetadataTest.class);
	private static WebDriver driver = null;
	private static WebDriverWait wait = null;

	@BeforeClass
	public static void setUpBeforeClass() {
		// UITest.setUpBeforeClass();
		driver = UiTestUtilities.getDriver();
		wait = new WebDriverWait(driver, 10);
	}

	/**
	 * Logs in before each test.
	 */
	@Before
	public void setUp() {
		UiTestUtilities.login();
	}

	/**
	 * After each test the user is logged out.
	 */
	@After
	public void tearDown() {
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

	/*
	 * *****************************************************************************
	 * *************** ******************************* METADATA TAB
	 * *******************************************
	 * *****************************************************************************
	 * ***************
	 */

	/**
	 * Test method for adding a new metadata tag to a collection or file. It
	 * verifies if this new metadata was added to the list and if metadata count was
	 * incremented by one
	 */
	@Test
	public void testAddMetadata() {

		logger.info("Testing if adding metadata works");

		String attribute = "timestamp";
		String value = String.valueOf(System.currentTimeMillis());
		String unit = "ms";

		boolean foundRow = false;
		driver.get(UiTestUtilities.COLLECTIONS_URL);
		MetadataUtils.addMetadata(driver, attribute, value, unit);

		// checks if a success message is displayed
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("metadataSuccessAddedMsg")));
		// check if success message is displayed
		assertTrue(driver.findElement(By.id("metadataSuccessAddedMsg")).isDisplayed());

		// checks if row is in the list of metadatas
		filterTableAndWaitResult(value);
		List<WebElement> metadataChkboxs = driver.findElements(By.className("metadataCheckbox"));
		for (WebElement we : metadataChkboxs) {
			if (avuExistsInWebElement(we, attribute, value, unit)) {
				foundRow = true;
				break;
			}
		}

		assertTrue(foundRow);
	}

	/**
	 * Test method for modifying a metadata tag from a collection or file. It
	 * verifies if this new metadata was modified
	 */
	@Test
	public void testModMetadata() {

		logger.info("Testing if modifying metadata works");

		// adding a new metadata in case there is no metadata attached in the
		// colletcion/file
		String attributeTS = "timestamp";
		String valueTS = String.valueOf(System.currentTimeMillis());
		String unitTS = "ms";
		driver.get(UiTestUtilities.COLLECTIONS_URL);
		MetadataUtils.addMetadata(driver, attributeTS, valueTS, unitTS);

		// MetadataUtils.openMetadataTab(driver);

		// find row to modify it
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".modal-backdrop.fade.in")));
		wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("#metadaTable tbody tr:first-child button.enableEditAVUActionColumn"))).click();
		// driver.findElement(By.cssSelector("#metadaTable tbody tr:first-child
		// button.enableEditAVUActionColumn")).click();

		// inputs of the form
		wait.until(ExpectedConditions
				.elementToBeClickable(By.cssSelector("#metadaTable tbody tr:first-child .saveEditAVUActionColumn")));
		driver.findElement(By.cssSelector("#metadaTable tbody tr:first-child .newAttrMetadataTable")).sendKeys("_mod");

		// sending form
		driver.findElement(By.cssSelector("#metadaTable tbody tr:first-child .saveEditAVUActionColumn")).click();

		// checks if a success message is displayed
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("metadataSuccessModMsg")));
		// check if success message is displayed
		assertTrue(driver.findElement(By.id("metadataSuccessModMsg")).isDisplayed());

	}

	/**
	 * Test method for deleting a metadata tag from a collection or file. It
	 * verifies if this new metadata was deleted from the list
	 */
	@Test
	public void testDelMetadata() {

		logger.info("Testing if removing metadata works");

		// adding a new metadata in case there is no metadata attached in the
		// colletcion/file
		String attributeTS = "timestamp";
		String valueTS = String.valueOf(System.currentTimeMillis());
		String unitTS = "ms";
		boolean foundRow = false;
		driver.get(UiTestUtilities.COLLECTIONS_URL);
		MetadataUtils.addMetadata(driver, attributeTS, valueTS, unitTS);

		MetadataUtils.openMetadataTab(driver);

		// find row to delete it
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector("#metadaTable tbody tr:first-child .metadataCheckbox")));
		WebElement chkbx = driver.findElement(By.cssSelector("#metadaTable tbody tr:first-child .metadataCheckbox"));

		String attr = chkbx.getAttribute("data-attr");
		String val = chkbx.getAttribute("data-val");
		String unit = chkbx.getAttribute("data-unit");
		chkbx.click();

		// delete the metadata
		driver.findElement(By.cssSelector("#metadaTable tbody tr:first-child .deleteAVUActionColumn")).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("deleteMetadataModal")));
		driver.findElement(By.cssSelector("#deleteMetadataModal .btn-primary")).click();

		// checking if element was indeed removed
		filterTableAndWaitResult(val);
		List<WebElement> metadataChkboxs = driver.findElements(By.className("metadataCheckbox"));
		for (WebElement we : metadataChkboxs) {
			if (avuExistsInWebElement(we, attr, val, unit)) {
				foundRow = true;
				break;
			}
		}

		assertFalse(foundRow);
	}

	/**
	 * Test method to try creating an existing metadata tag from a collection or
	 * file. It verifies if this metadata was not duplicated
	 */
	@Test
	public void testSaveExistingMetadata() {

		logger.info("Testing if saving an existing metadata fails");

		// adding a new metadata in case there is no metadata attached in the
		// colletcion/file
		String attributeTS = "timestamp";
		String valueTS = String.valueOf(System.currentTimeMillis());
		String unitTS = "ms";
		int foundRow = 0;
		driver.get(UiTestUtilities.COLLECTIONS_URL);
		MetadataUtils.addMetadata(driver, attributeTS, valueTS, unitTS);

		// MetadataUtils.openMetadataTab(driver);

		// find first row to copy its values
		wait.until(ExpectedConditions.presenceOfElementLocated(By.id("metadaTable")));
		WebElement chkbx = driver.findElement(By.cssSelector("#metadaTable tbody tr:first-child .metadataCheckbox"));

		String attr = chkbx.getAttribute("data-attr");
		String val = chkbx.getAttribute("data-val");
		String unit = chkbx.getAttribute("data-unit");

		// openning addMetadata modal
		MetadataUtils.openAddMetadataModalAndSendForm(driver, attr, val, unit);

		// checks if an error message is displayed
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector(".browse-collection-files-content .alert-danger")));
		WebElement divAlertError = driver.findElement(By.cssSelector(".browse-collection-files-content .alert-danger"));
		// check if success message is displayed
		assertTrue(divAlertError.isDisplayed());

		// checking if element was not created indeed
		filterTableAndWaitResult(val);
		List<WebElement> metadataChkboxs = driver.findElements(By.className("metadataCheckbox"));
		for (WebElement we : metadataChkboxs) {
			if (avuExistsInWebElement(we, attr, val, unit)) {
				foundRow++;
			}
		}

		// If foundRow is bigger than 1, it means there is a metadata duplicated.
		// If it is 0, then for some reason the metadata was removed
		assertEquals(1, foundRow);
	}

	/**
	 * Test method to try creating an empty metadata tag in a collection or file. It
	 * verifies if this metadata was not created
	 */
	@Test
	public void testSaveEmptyMetadata() {

		logger.info("Testing if saving an empty metadata fails");

		boolean foundRow = false;

		driver.get(UiTestUtilities.COLLECTIONS_URL);
		MetadataUtils.openMetadataTab(driver);

		String attr = "";
		String val = "";
		String unit = "";

		// openning addMetadata modal
		MetadataUtils.openAddMetadataModalAndSendForm(driver, attr, val, unit);

		// checks if an error message is displayed
		wait.until(ExpectedConditions
				.visibilityOfElementLocated(By.cssSelector(".browse-collection-files-content .alert-danger")));
		WebElement divAlertError = driver.findElement(By.cssSelector(".browse-collection-files-content .alert-danger"));
		// check if success message is displayed
		assertTrue(divAlertError.isDisplayed());

		// checking if element was not created removed
		filterTableAndWaitResult(val);
		List<WebElement> metadataChkboxs = driver.findElements(By.className("metadataCheckbox"));
		for (WebElement we : metadataChkboxs) {
			if (avuExistsInWebElement(we, attr, val, unit)) {
				foundRow = true;
			}
		}

		assertFalse(foundRow);
	}

	/**
	 * Test method to try creating a large metadata tag in a collection or file. It
	 * verifies if this metadata was created without problems Limit for metadata
	 * value is 64 characters
	 */
	@Test
	public void testSaveLargeMetadata() {

		logger.info("Testing if removing metadata works");

		boolean foundRow = false;

		driver.get(UiTestUtilities.COLLECTIONS_URL);
		MetadataUtils.openMetadataTab(driver);

		// generating a big string
		char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
		StringBuilder sb = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i < 63; i++) {
			char c = chars[random.nextInt(chars.length)];
			sb.append(c);
		}
		String attr = sb.toString();
		String val = sb.toString();
		String unit = sb.toString();

		// openning addMetadata modal
		MetadataUtils.openAddMetadataModalAndSendForm(driver, attr, val, unit);

		// checks if a success message is displayed
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("metadataSuccessAddedMsg")));
		WebElement divAlertSuccess = driver.findElement(By.id("metadataSuccessAddedMsg"));
		// check if success message is displayed
		assertTrue(divAlertSuccess.isDisplayed());

		// checking if element was created removed
		filterTableAndWaitResult(val);
		List<WebElement> metadataChkboxs = driver.findElements(By.className("metadataCheckbox"));
		for (WebElement we : metadataChkboxs) {
			if (avuExistsInWebElement(we, attr, val, unit)) {
				foundRow = true;
			}
		}

		assertTrue(foundRow);
	}

	/**
	 * Test method for deleting all metadata from a collection or file. It verifies
	 * if all metadata from a collection/file were deleted
	 */
	@Test
	@Ignore
	public void testDelAllMetadata() {

		logger.info("Testing if adding metadata works");

		String attributeTS = "timestamp";
		String valueTS = String.valueOf(System.currentTimeMillis());
		String unitTS = "ms";
		driver.get(UiTestUtilities.COLLECTIONS_URL);
		MetadataUtils.addMetadata(driver, attributeTS, valueTS, unitTS);

		// opens metadata tab
		MetadataUtils.openMetadataTab(driver);

		// find select all metadata checkbox
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("checkAllMetadata")));
		WebElement chkbx = driver.findElement(By.id("checkAllMetadata"));
		chkbx.click();

		// delete the metadata
		driver.findElement(By.id("delMetadataBtn")).click();
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#deleteMetadataModal .btn-primary")));
		driver.findElement(By.cssSelector("#deleteMetadataModal .btn-primary")).click();

		// checks if row is in the list of metadatas
		List<WebElement> metadataChkboxs = driver.findElements(By.className("metadataCheckbox"));

		assertTrue(metadataChkboxs.isEmpty());
	}

	/**
	 * Method to test if a certain WebElement contains the avu tuple passed as
	 * parameters
	 */
	private boolean avuExistsInWebElement(WebElement we, String attr, String val, String unit) {
		if (attr.equals(we.getAttribute("data-attr")) && val.equals(we.getAttribute("data-val"))
				&& unit.equals(we.getAttribute("data-unit"))) {
			return true;
		}
		return false;
	}

	/**
	 * Search for a value in metadatatable and waits until the table is drawn again
	 */
	private void filterTableAndWaitResult(String searchString) {
		wait.until(ExpectedConditions
				.elementToBeClickable(By.cssSelector("#metadaTable_length select[name='metadaTable_length']")));
		Select selectPageSize = new Select(
				driver.findElement(By.cssSelector("#metadaTable_length select[name='metadaTable_length']")));
		selectPageSize.selectByValue("100");
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#metadaTable_filter input")))
				.sendKeys(searchString);
		wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.cssSelector("#metadaTable tbody tr")));
	}

}
