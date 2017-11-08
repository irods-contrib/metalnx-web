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

package com.emc.metalnx.integration.test.metadata;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.CollectionUtils;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.integration.test.utils.MetadataUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.fail;

@Deprecated
@Ignore
public class MetadataTest {
    private static final Logger logger = LoggerFactory.getLogger(MetadataTest.class);

    private static WebDriver driver = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UiTestUtilities.init();
        driver = UiTestUtilities.getDriver();
        FileUtils.forceRemoveFilesFromHomeAsAdmin(MetadataUtils.METADATA_SEARCH_FILES);
        FileUtils.uploadToHomeDirAsAdmin(MetadataUtils.METADATA_SEARCH_FILES);

        UiTestUtilities.login();
        WebDriverWait wait = new WebDriverWait(driver, 10);

        MetadataUtils.realWaitMethod(driver);
        driver.get(UiTestUtilities.COLLECTIONS_URL);
        wait.until(ExpectedConditions.elementToBeClickable(MetadataUtils.TAB_LINKS));
        wait.until(ExpectedConditions.elementToBeClickable(CollectionUtils.COLLS_TABLE));
        MetadataUtils.addMetadata(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_VALUE, "", MetadataUtils.METADATA_SEARCH_FILES[0]);

        MetadataUtils.realWaitMethod(driver);
        driver.get(UiTestUtilities.COLLECTIONS_URL);
        wait.until(ExpectedConditions.elementToBeClickable(MetadataUtils.TAB_LINKS));
        wait.until(ExpectedConditions.elementToBeClickable(CollectionUtils.COLLS_TABLE));
        MetadataUtils.addMetadata(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_VALUE, "", MetadataUtils.METADATA_SEARCH_FILES[1]);

        MetadataUtils.realWaitMethod(driver);
        driver.get(UiTestUtilities.COLLECTIONS_URL);
        wait.until(ExpectedConditions.elementToBeClickable(MetadataUtils.TAB_LINKS));
        wait.until(ExpectedConditions.elementToBeClickable(CollectionUtils.COLLS_TABLE));
        MetadataUtils.addMetadata(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_VALUE, "", MetadataUtils.METADATA_SEARCH_FILES[2]);

        MetadataUtils.realWaitMethod(driver);
        MetadataUtils.addMetadataToSpecificFiles(driver);
        UiTestUtilities.logout();
    }

    /**
     * Logs in before each test.
     */
    @Before
    public void setUp() throws Exception {
        UiTestUtilities.login();
    }

    /**
     * After each test the user is logged out.
     */
    @After
    public void tearDown() throws Exception {
        UiTestUtilities.logout();
    }

    /**
     * After all tests are done, the test must quit the driver. This will close every window
     * associated with the current driver instance.
     *
     * @throws DataGridException
     */
    @AfterClass
    public static void tearDownAfterClass() throws DataGridException {
        FileUtils.forceRemoveFilesFromHomeAsAdmin(MetadataUtils.METADATA_SEARCH_FILES);

        if (driver != null) {
            driver.quit();
            driver = null;
            UiTestUtilities.setDriver(null);
        }
    }

    /**
     * Test metadata search using "Selenium" as attribute and "Test" as value.
     * Number of results expected: 3. All files in {@code metadataSearchFiles} have this piece of
     * metadata.
     * Results expected: 1SeleniumTestMetadataSearch.png, 2SeleniumTestMetadataSearch.png, and
     * 3SeleniumTestMetadataSearch.png. The results must appear in this particular order.
     */
    @Test
    public void testSeleniumAsAttrAndTestAsValue() {
        logger.info("Testing Metadata search");

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);
        MetadataUtils.fillInMetadataSearchAttrVal(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_VALUE);
        MetadataUtils.submitMetadataSearch(driver);
        MetadataUtils.waitForSearchResults(driver);
        List<WebElement> results = driver.findElements(MetadataUtils.METADATA_SEARCH_RESULTS);
        Assert.assertNotNull(results);

        // check if the number of results found matches the number of files used for testing
        Assert.assertEquals(MetadataUtils.TOTAL_FILES_METADATA_SEARCH, results.size());

        // check if each file found is the file used for testing
        int index = 0;
        for (WebElement metadataSearchResult : results) {
            Assert.assertEquals(metadataSearchResult.getText().trim(), MetadataUtils.METADATA_SEARCH_FILES[index]);
            index++;
        }
    }

    /**
     * Test metadata search on the file 1SeleniumTestMetadataSearch.
     * Search parameters: SeleniumTest (attribute) and 1 (value)
     * Number of results expected: 1 (only this file should have this particular piece of metadata)
     * Results expected: 1SeleniumTestMetadataSearch.png
     */
    @Test
    public void testSeleniumTestAsAttrAnd1AsValue() {
        logger.info("Testing Metadata search using Selenium Test as the attribute and 1 as the value");
        driver.get(UiTestUtilities.METADATA_SEARCH_URL);

        MetadataUtils.fillInMetadataSearchAttrVal(driver, MetadataUtils.SELENIUM_TEST_ATTR, MetadataUtils.SELENIUM_TEST_VAL1);
        MetadataUtils.submitMetadataSearch(driver);
        MetadataUtils.waitForSearchResults(driver);
        List<WebElement> results = driver.findElements(MetadataUtils.METADATA_SEARCH_RESULTS);
        Assert.assertNotNull(results);

        // check if the number of results found matches the number of files used for testing
        Assert.assertEquals(1, results.size());

        // check if each file found is the file used for testing
        Assert.assertEquals(results.get(0).getText().trim(), MetadataUtils.METADATA_SEARCH_FILES[0]);
    }

    /**
     * Test metadata search on the file 2SeleniumTestMetadataSearch.
     * Search parameters: SeleniumTest (attribute) and 2 (value)
     * Number of results expected: 1 (only this file should have this particular piece of metadata)
     * Results expected: 2SeleniumTestMetadataSearch.png
     */
    @Test
    public void testSeleniumTestAsAttrAnd2AsValue() {
        logger.info("Testing Metadata search using Selenium Test as the attribute and 2 as the value");

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);
        MetadataUtils.fillInMetadataSearchAttrVal(driver, MetadataUtils.SELENIUM_TEST_ATTR, MetadataUtils.SELENIUM_TEST_VAL2);
        MetadataUtils.submitMetadataSearch(driver);
        MetadataUtils.waitForSearchResults(driver);
        List<WebElement> results = driver.findElements(MetadataUtils.METADATA_SEARCH_RESULTS);
        Assert.assertNotNull(results);
        Assert.assertEquals(1, results.size());

        // check if each file found is the file used for testing
        Assert.assertEquals(results.get(0).getText().trim(), MetadataUtils.METADATA_SEARCH_FILES[1]);
    }

    /**
     * Test metadata search on the file 3SeleniumTestMetadataSearch.
     * Search parameters: SeleniumTest (attribute) and 3 (value)
     * Number of results expected: 1 (only this file should have this particular piece of metadata)
     * Results expected: 3SeleniumTestMetadataSearch.png
     */
    @Test
    public void testSeleniumTestAsAttrAnd3AsValue() {
        logger.info("Testing Metadata search using Selenium Test as the attribute and 3 as the value");
        driver.get(UiTestUtilities.METADATA_SEARCH_URL);

        MetadataUtils.fillInMetadataSearchAttrVal(driver, MetadataUtils.SELENIUM_TEST_ATTR, MetadataUtils.SELENIUM_TEST_VAL3);
        MetadataUtils.submitMetadataSearch(driver);
        MetadataUtils.waitForSearchResults(driver);
        List<WebElement> results = driver.findElements(MetadataUtils.METADATA_SEARCH_RESULTS);
        Assert.assertNotNull(results);

        // check if the number of results found matches the number of files used for testing
        Assert.assertEquals(1, results.size());

        // check if each file found is the file used for testing
        Assert.assertEquals(results.get(0).getText().trim(), MetadataUtils.METADATA_SEARCH_FILES[2]);
    }

    /**
     * Test metadata search on the file 1SeleniumTestMetadataSearch and 2SeleniumTestMetadataSearch.
     * Search parameters: SeleniumTest (attribute) and 12 (value)
     * Number of results expected: 2 (both 1SeleniumTestMetadataSearch and
     * 2SeleniumTestMetadataSearch files should have this particular piece of metadata)
     * Results expected: 1SeleniumTestMetadataSearch.png and 2SeleniumTestMetadataSearch.png in this
     * order.
     */
    @Test
    public void testSeleniumTestAsAttrAnd12AsValue() {
        logger.info("Testing Metadata search using Selenium Test as the attribute and 12 as the value");

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);

        MetadataUtils.fillInMetadataSearchAttrVal(driver, MetadataUtils.SELENIUM_TEST_ATTR, MetadataUtils.SELENIUM_TEST_VAL12);
        MetadataUtils.submitMetadataSearch(driver);
        MetadataUtils.waitForSearchResults(driver);
        List<WebElement> results = driver.findElements(MetadataUtils.METADATA_SEARCH_RESULTS);
        Assert.assertNotNull(results);

        // check if the number of results matches the number of files used for testing
        Assert.assertEquals(2, results.size());

        // check if each file found is the file used for testing
        int index = 0;
        for (WebElement metadataSearchResult : results) {
            Assert.assertEquals(metadataSearchResult.getText().trim(), MetadataUtils.METADATA_SEARCH_FILES[index]);
            index++;
        }
    }

    /**
     * Test looking for a piece of metadata that does not exist.
     * Search parameters: Random string (attribute) and random string (value)
     * Number of results expected: 0
     * Results expected: None
     * Expected Exceptions: {@link TimeoutException}
     */
    @Test(expected = TimeoutException.class)
    public void testSearchUsingNonExistentMetadataTags() {
        logger.info("Testing Metadata search using non-existent metadata tags");
        driver.get(UiTestUtilities.METADATA_SEARCH_URL);

        String attr = RandomStringUtils.randomAlphanumeric(4096);
        String val = RandomStringUtils.randomAlphanumeric(4096);
        MetadataUtils.fillInMetadataSearchAttrVal(driver, attr, val);
        MetadataUtils.submitMetadataSearch(driver);

        // Expecting a Timeout exception to happen here since Metalnx is supposed to return
        // nothing. Then, the metadata results table will not be displayed.
        MetadataUtils.waitForSearchResults(driver);

        fail("Non-existing metadata is actually returning something from the search.");
    }

    /**
     * Test searching for metadata without giving any search criteria. In other words, testing a
     * search without giving any attribute or value as the search criteria.
     * Search parameters: None
     * Number of results expected: 0
     * Results expected: None
     * Expected Exceptions: {@link TimeoutException}
     */
    @Test(expected = TimeoutException.class)
    public void testSearchWithoutSearchCriteria() {
        logger.info("Testing Metadata search using non-existent metadata tags");
        driver.get(UiTestUtilities.METADATA_SEARCH_URL);

        MetadataUtils.submitMetadataSearch(driver);

        // Expecting a Timeout exception to happen here since Metalnx is supposed to return
        // nothing. Then, the metadata results table will not be displayed.
        MetadataUtils.waitForSearchResults(driver);

        fail("Blank metadata search is actually returning something.");
    }

    /**
     * Test the number of matches shown for each file used in the search.
     * Search parameters:
     * 1) Selenium Test
     * 2) SeleniumTest 1
     * 3) SeleniumTest 12
     * Number of results expected: 3
     * Results expected:
     * 1) 1SeleniumTestMetadataSearch.png (Number of matches: 3)
     * 2) 2SeleniumTestMetadataSearch.png (Number of matches: 2)
     * 3) 3SeleniumTestMetadataSearch.png (Number of matches: 1)
     */
    @Test
    public void testNumberOfMatchesDisplayedForEachFileAfterSearching() {
        logger.info("Testing the number of matches displayed for each file after doing a search.");
        WebElement inputAttr = null;
        WebElement inputValue = null;
        WebElement addMetadataSearchRowBtn = null;
        WebElement metadataSearchMatchColumn = null;
        int[] expectedMatchingCounts = { 3, 2, 1 };

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);

        logger.info("Entering search criteria.");
        // adding multiples search criteria to the search
        inputAttr = driver.findElement(By.id("metadataAttr0"));
        inputAttr.sendKeys(MetadataUtils.SELENIUM_ATTR);

        inputValue = driver.findElement(By.id("metadataValue0"));
        inputValue.sendKeys(MetadataUtils.SELENIUM_VALUE);

        addMetadataSearchRowBtn = driver.findElement(By.id("addMetadataSearchRow"));
        addMetadataSearchRowBtn.click();

        inputAttr = driver.findElement(By.id("metadataAttr1"));
        inputAttr.sendKeys(MetadataUtils.SELENIUM_TEST_ATTR);

        inputValue = driver.findElement(By.id("metadataValue1"));
        inputValue.sendKeys(MetadataUtils.SELENIUM_TEST_VAL1);

        addMetadataSearchRowBtn.click();

        inputAttr = driver.findElement(By.id("metadataAttr2"));
        inputAttr.sendKeys(MetadataUtils.SELENIUM_TEST_ATTR);

        inputValue = driver.findElement(By.id("metadataValue2"));
        inputValue.sendKeys(MetadataUtils.SELENIUM_TEST_VAL12);

        MetadataUtils.submitMetadataSearch(driver);
        MetadataUtils.waitForSearchResults(driver);
        List<WebElement> results = driver.findElements(MetadataUtils.METADATA_SEARCH_RESULTS);

        List<WebElement> metadataSearchResultsMatchesCount = driver.findElements(By.cssSelector("#treeViewTable tbody tr td:last-child"));

        logger.info("Checking if any result was found", new Date());
        Assert.assertNotNull(results);

        // check if the number of results found matches the number of files used for testing
        Assert.assertEquals(MetadataUtils.TOTAL_FILES_METADATA_SEARCH, results.size());

        logger.info("Checking if the number of matches of each file is the same as expected.");
        int index = 0;
        int matchingCount = 0;
        for (WebElement r : results) {
            Assert.assertEquals(r.getText().trim(), MetadataUtils.METADATA_SEARCH_FILES[index]);

            metadataSearchMatchColumn = metadataSearchResultsMatchesCount.get(index);
            matchingCount = metadataSearchMatchColumn.findElements(By.tagName("i")).size();
            Assert.assertEquals(expectedMatchingCounts[index], matchingCount);
            index++;
        }
    }

    /**
     * Adds number maximum of parameters for the metadata search, which is five, and assures that
     * add button is hidden when the maximum is reached
     */
    @Test
    public void testAddMaxNumOfSearchParams() {
        MetadataUtils.addMaxNumberOfSearchParams(driver);
        List<WebElement> metadataSearchParameters = driver.findElements(By.className("metadataSearchRow"));

        Assert.assertEquals(metadataSearchParameters.size(), 5);
    }

    /**
     * Adds number maximum of parameters for the metadata search, which is five, and assures that
     * add button is hidden when the maximum is reached
     */
    @Test
    public void testRmvMaxNumOfSearchParams() {
        MetadataUtils.addMaxNumberOfSearchParams(driver);
        List<WebElement> metadataSearchParameters = driver.findElements(By.className("metadataSearchRow"));

        Assert.assertEquals(metadataSearchParameters.size(), 5);

        WebElement rmSearchParam = driver.findElement(By.className("rmMetadataSearchRow"));
        rmSearchParam.click();

        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addMetadataSearchRow")));

    }
}
