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

package com.emc.metalnx.integration.test.fileproperties;

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
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.integration.test.utils.UserUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

@Deprecated
@Ignore
public class FilePropertiesSearchTest {
	private static final Logger logger = LoggerFactory.getLogger(FilePropertiesSearchTest.class);
	private static WebDriver driver = null;
	private static WebDriverWait wait = null;

	private By submitPropertiesSearchLocator = By.id("submitPropertiesSearch");
	private By addSearchParamLocator = By.id("addPropertiesSearchRow");

	public static final String[] SEARCH_FILES = { "1SeleniumTestMetadataSearch.png" };

	private String userHomeColl = String.format("/%s/%s/%s", UiTestUtilities.IRODS_ZONE, "home", UiTestUtilities.RODS_USERNAME);
	private String filePath = String.format("%s/%s", userHomeColl, SEARCH_FILES[0]);

	private enum AttributeOptions {
		FILE_NAME, FILE_PATH, RESC_NAME, OWNER_NAME, CREATION_DATE, MODIFICATION_DATE, SIZE, REPLICA_NUMBER, CHECKSUM, COMMENT
	}

	private enum OperatorOptions {
		EQUAL, NOT_EQUAL, LIKE, NOT_LIKE, LESS_THAN, BIGGER_THAN
	}

	@BeforeClass
	public static void setUpBeforeClass() throws DataGridException {
		// UITest.setUpBeforeClass();
		driver = UiTestUtilities.getDriver();
		wait = new WebDriverWait(driver, 10);
		FileUtils.uploadToHomeDirAsAdmin(SEARCH_FILES);
	}

	@Before
	public void setUp() {
		UiTestUtilities.login();

	}

	@After
	public void tearDown() {
		UiTestUtilities.logout();
	}

	@AfterClass
	public static void tearDownAfterClass() throws DataGridException {
		FileUtils.forceRemoveFilesFromHomeAsAdmin(SEARCH_FILES);

		if (driver != null) {
			driver.quit();
			driver = null;
			UiTestUtilities.setDriver(null);
		}
	}

	/**
	 * Method to test if an empty search will generate an empty table
	 */
	@Test
	public void emptySearch() {
		logger.info("Testing file properties search");

		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		// hitting search without adding value in search
		submitAndCheckEmptyTable();

	}

	/**
	 * Search for files that have one file property that matches the search criteria
	 *
	 * @throws DataGridException
	 */
	@Test
	public void searchEqualsOneCriteriaWithResults() throws DataGridException {
		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		fillSearchInFisrtField(AttributeOptions.FILE_PATH.toString(), OperatorOptions.EQUAL.toString(), filePath);

		submitAndCheckNotEmptyTable();
		checkIfUploadedFileExist();
	}

	/**
	 * Search by one file property and expects that no results will be found
	 */
	@Test
	public void searchEqualsOneCriteriaWithoutResults() {
		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		fillSearchInFisrtField(AttributeOptions.OWNER_NAME.toString(), OperatorOptions.EQUAL.toString(),
				"FilePropertyInexistentUser" + System.currentTimeMillis());

		// submiting form and assert result
		submitAndCheckEmptyTable();
	}

	/**
	 * Search using Not Equals
	 */
	@Test
	public void searchNotEqualsOneCriteriaWithResults() {
		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		fillSearchInFisrtField(AttributeOptions.FILE_PATH.toString(), OperatorOptions.NOT_EQUAL.toString(),
				userHomeColl);

		submitAndCheckNotEmptyTable();
	}

	/**
	 * Search using Contains with results
	 *
	 * @throws DataGridException
	 */
	@Test
	public void searchContainsOneCriteriaWithResults() throws DataGridException {
		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		fillSearchInFisrtField(AttributeOptions.FILE_PATH.toString(), OperatorOptions.LIKE.toString(), filePath);

		submitAndCheckNotEmptyTable();
		checkIfUploadedFileExist();
	}

	/**
	 * Search using Contains with no results
	 */
	@Test
	public void searchContainsOneCriteriaWithoutResults() {
		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		fillSearchInFisrtField(AttributeOptions.FILE_PATH.toString(), OperatorOptions.LIKE.toString(),
				"/FilePropertyInexistentObj" + System.currentTimeMillis());

		// submiting form and assert result
		submitAndCheckEmptyTable();
	}

	/**
	 * Search using Not Contains with results
	 */
	@Test
	public void searchNotContainsOneCriteriaWithResults() {
		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		fillSearchInFisrtField(AttributeOptions.FILE_PATH.toString(), OperatorOptions.NOT_LIKE.toString(),
				userHomeColl);

		submitAndCheckNotEmptyTable();
	}

	/**
	 * Search using Not Contains with no results
	 */
	@Test
	public void searchNotContainsOneCriteriaWithoutResults() {
		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		fillSearchInFisrtField(AttributeOptions.FILE_PATH.toString(), OperatorOptions.NOT_LIKE.toString(), "/");

		// submiting form and assert result
		submitAndCheckEmptyTable();
	}

	/**
	 * Search using 3 different criteria
	 */
	@Test
	public void searchMultipleCriteriasWithResults() {
		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		// setting first criteria
		fillSearchInFisrtField(AttributeOptions.OWNER_NAME.toString(), OperatorOptions.EQUAL.toString(), "rods");

		// adding a new criteria search field
		driver.findElement(addSearchParamLocator).click();
		wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".propertiesSearchRow:nth-of-type(2)")));

		// setting second criteria
		fillSearchInNthField(AttributeOptions.FILE_PATH.toString(), OperatorOptions.LIKE.toString(), "rods", 2);

		// adding a new criteria search field
		driver.findElement(addSearchParamLocator).click();
		wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".propertiesSearchRow:nth-of-type(3)")));

		// setting third criteria
		fillSearchInNthField(AttributeOptions.SIZE.toString(), OperatorOptions.BIGGER_THAN.toString(), "0", 3);

		// submiting form and assert result
		submitAndCheckNotEmptyTable();
	}

	/**
	 * Search using opposite operators: Equals and Not Equals
	 */
	@Test
	public void searchEqualsAndNotEquals() {
		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		// setting first criteria
		fillSearchInFisrtField(AttributeOptions.OWNER_NAME.toString(), OperatorOptions.EQUAL.toString(), "rods");

		// adding a new criteria search field
		driver.findElement(addSearchParamLocator).click();
		wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".propertiesSearchRow:nth-of-type(2)")));

		// setting second criteria
		fillSearchInNthField(AttributeOptions.OWNER_NAME.toString(), OperatorOptions.NOT_EQUAL.toString(), "rods", 2);

		// submiting form and assert result
		submitAndCheckEmptyTable();
	}

	/**
	 * Search using opposite operators: Contains and Not Contains
	 */
	@Test
	public void searchContainsAndNotContains() {
		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		// setting first criteria
		fillSearchInFisrtField(AttributeOptions.OWNER_NAME.toString(), OperatorOptions.LIKE.toString(), "rods");

		// adding a new criteria search field
		driver.findElement(addSearchParamLocator).click();
		wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".propertiesSearchRow:nth-of-type(2)")));

		// setting second criteria
		fillSearchInNthField(AttributeOptions.OWNER_NAME.toString(), OperatorOptions.NOT_LIKE.toString(), "rods", 2);

		// submiting form and assert result
		submitAndCheckEmptyTable();
	}

	/**
	 * Creates a new user to test if file properties search can find objects that
	 * rodsadmin user has no permission on
	 */
	@Test
	public void searchForObjsWithoutPermission() {
		// creating user for testing purposes
		String uname = "filePropertiesSearch" + System.currentTimeMillis();
		String pwd = "webdriver";
		driver.get(UiTestUtilities.ADD_USERS_URL);
		UserUtils.fillInUserGeneralInformation(uname, pwd, UiTestUtilities.RODS_USER_TYPE, driver);
		UserUtils.submitUserForm(driver);

		// getting to the file properties search tab
		getFilePropertiesSearchTab();

		fillSearchInFisrtField(AttributeOptions.OWNER_NAME.toString(), OperatorOptions.EQUAL.toString(), uname);

		// submiting form and assert result
		driver.findElement(submitPropertiesSearchLocator).click();

		// if there is at least one <a> tag, then it means there are objects which the
		// current user has access to
		wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("#treeViewTable tbody tr")));
		Assert.assertTrue(
				driver.findElements(By.cssSelector("#treeViewTable tbody > tr > td:first-child > span")).size() != 0);

		// deleting test user
		UiTestUtilities.logout();
		UserUtils.removeUser(uname, driver);
	}

	/**
	 * Method to get to file properties search tab
	 */
	private void getFilePropertiesSearchTab() {
		driver.get(UiTestUtilities.METADATA_SEARCH_URL);
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[href=\"#properties-search\"]"))).click();
		wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("properties-search")));
	}

	/**
	 * Submits search and checks if there is no results (empty table)
	 */
	private void submitAndCheckEmptyTable() {
		driver.findElement(submitPropertiesSearchLocator).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable .dataTables_empty")));
		Assert.assertTrue(driver.findElement(By.cssSelector("#treeViewTable .dataTables_empty")).isDisplayed());
	}

	/**
	 * Submits search and checks if there is at least one result from this search
	 */
	private void submitAndCheckNotEmptyTable() {
		// click submit
		driver.findElement(submitPropertiesSearchLocator).click();
		// if there is at least one <a> tag, then it means there are objects which the
		// curretn user has access to
		wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector("#treeViewTable tbody tr")));
		Assert.assertTrue(driver.findElements(By.cssSelector("#treeViewTable tbody a")).size() != 0);
	}

	/**
	 * Compares path column to check if value from the table is the same as the one
	 * expected to be
	 */
	private void checkIfUploadedFileExist() {
		Assert.assertEquals(driver.findElements(By.cssSelector("#treeViewTable tbody a")).size(), 1);
		Assert.assertEquals(filePath,
				driver.findElement(By.cssSelector("#treeViewTable tbody tr td:nth-child(2)")).getText());
	}

	/**
	 * This method is used to fill the first line in file properties search
	 *
	 * @param attribute
	 *            can be get from AttributeOptions enum
	 * @param operator
	 *            can be get from OperatorOptions enum
	 * @param value
	 */
	private void fillSearchInFisrtField(String attribute, String operator, String value) {
		fillSearchInNthField(attribute, operator, value, 1);
	}

	/**
	 * This method is used to fill the nth line in file properties search
	 *
	 * @param attribute
	 *            can be get from AttributeOptions enum
	 * @param operator
	 *            can be get from OperatorOptions enum
	 * @param value
	 * @param nth
	 *            represents the nth search parameter in which the options above are
	 *            to be placed and it starts at 1
	 */
	private void fillSearchInNthField(String attribute, String operator, String value, Integer nth) {
		new Select(driver.findElement(
				By.cssSelector(".propertiesSearchRow:nth-of-type(" + nth.toString() + ") .propertiesAttr")))
						.selectByValue(attribute);
		new Select(driver.findElement(
				By.cssSelector(".propertiesSearchRow:nth-of-type(" + nth.toString() + ") .propertiesOper")))
						.selectByValue(operator);
		driver.findElement(By.cssSelector(".propertiesSearchRow:nth-of-type(" + nth.toString() + ") .propertiesVal"))
				.sendKeys(value);
	}
}
