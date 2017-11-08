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
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.integration.test.utils.MetadataUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;
import org.junit.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

@Deprecated
@Ignore
public class MetadataSearchUnitsTest {
    private static final Logger logger = LoggerFactory.getLogger(MetadataSearchUnitsTest.class);

    private static WebDriver driver = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UiTestUtilities.init();
        driver = UiTestUtilities.getDriver();
        FileUtils.forceRemoveFilesFromHomeAsAdmin(MetadataUtils.METADATA_SEARCH_FILES);
        FileUtils.uploadToHomeDirAsAdmin(MetadataUtils.METADATA_SEARCH_FILES);

        UiTestUtilities.login();
        for (String file : MetadataUtils.METADATA_SEARCH_FILES) {
            MetadataUtils.addMetadata(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_VALUE, MetadataUtils.SELENIUM_UNIT, file);
            try {
                new WebDriverWait(driver, 1).until(ExpectedConditions.elementToBeClickable(By.id("çççç")));
            }
            catch (Exception e) {

            }
        }
        UiTestUtilities.logout();
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
        FileUtils.forceRemoveFilesFromHomeAsAdmin(MetadataUtils.METADATA_SEARCH_FILES);

        if (driver != null) {
            driver.quit();
            driver = null;
            UiTestUtilities.setDriver(null);
        }
    }

    /**
     * Test metadata search using "Selenium" as attribute, "Test" as value, and "Mlx" as unit.
     * Results expected: 1SeleniumTestMetadataSearch.png, 2SeleniumTestMetadataSearch.png, and
     * 3SeleniumTestMetadataSearch.png. Results must appear in this particular order.
     */
    @Test
    public void testSeleniumAsAttrTestAsValueAndMlxAsUnit() {
        logger.info("Testing Metadata search");

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);
        MetadataUtils.fillInMetadataSearchAttrValAndUnit(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_VALUE,
                MetadataUtils.SELENIUM_UNIT);
        MetadataUtils.submitMetadataSearch(driver);
        assertMetadataSearchResults();
    }

    /**
     * Test metadata search using "Selenium" as attribute and "Mlx" as unit.
     * Results expected: 1SeleniumTestMetadataSearch.png, 2SeleniumTestMetadataSearch.png, and
     * 3SeleniumTestMetadataSearch.png. Results must appear in this particular order.
     */
    @Test
    public void testMetadataSearchAttrAndUnit() {
        logger.info("Testing Metadata search");

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);
        MetadataUtils.fillInMetadataSearchAttrUnit(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_UNIT);
        MetadataUtils.submitMetadataSearch(driver);
        assertMetadataSearchResults();
    }

    /**
     * Test metadata search only using "Mlx" as unit.
     * Results expected: 1SeleniumTestMetadataSearch.png, 2SeleniumTestMetadataSearch.png, and
     * 3SeleniumTestMetadataSearch.png. Results must appear in this particular order.
     */
    @Test
    public void testMetadataSearchUnit() {
        logger.info("Testing Metadata search");

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);
        MetadataUtils.fillInMetadataSearchUnit(driver, MetadataUtils.SELENIUM_UNIT);
        MetadataUtils.submitMetadataSearch(driver);
        assertMetadataSearchResults();
    }

    /**
     * Test the number of matches shown for each file used in the search.
     * Search parameters:
     * 1) Selenium Test Mlx
     * 2) Selenium Test
     * 3) Selenium Mlx
     * Number of results expected: 3
     * Results expected:
     * 1) 1SeleniumTestMetadataSearch.png (Number of matches: 3)
     * 2) 2SeleniumTestMetadataSearch.png (Number of matches: 3)
     * 3) 3SeleniumTestMetadataSearch.png (Number of matches: 3)
     */
    @Test
    public void testNumberOfMatchesDisplayedForEachFileAfterSearching() {
        logger.info("Testing the number of matches displayed for each file after doing a search.");

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);

        logger.info("Entering search criteria.");

        // Search for AVU
        driver.findElement(By.id("metadataAttr0")).sendKeys(MetadataUtils.SELENIUM_ATTR);
        driver.findElement(By.id("metadataValue0")).sendKeys(MetadataUtils.SELENIUM_VALUE);
        driver.findElement(By.id("metadataUnit0")).sendKeys(MetadataUtils.SELENIUM_UNIT);

        driver.findElement(By.id("addMetadataSearchRow")).click();

        // Search for AV
        driver.findElement(By.id("metadataAttr1")).sendKeys(MetadataUtils.SELENIUM_ATTR);
        driver.findElement(By.id("metadataValue1")).sendKeys(MetadataUtils.SELENIUM_VALUE);

        driver.findElement(By.id("addMetadataSearchRow")).click();

        // Search for AU
        driver.findElement(By.id("metadataAttr2")).sendKeys(MetadataUtils.SELENIUM_ATTR);
        driver.findElement(By.id("metadataUnit2")).sendKeys(MetadataUtils.SELENIUM_UNIT);

        MetadataUtils.submitMetadataSearch(driver);

        assertMetadataSearchResults();

        List<WebElement> numberOfMatchesForAllItems = driver.findElements(By.cssSelector("#treeViewTableBody tr td:last-child"));

        logger.info("Checking if the number of matches of each file is the same as expected.");
        int numOfMatches = 0;
        for (WebElement numOfMatchesForItem : numberOfMatchesForAllItems) {
            numOfMatches = numOfMatchesForItem.findElements(By.tagName("i")).size();
            Assert.assertEquals(3, numOfMatches);
        }
    }

    @Test
    public void testClickingOnSearchResultAndComingBackToMetadataSearch() {
        logger.info("Test for clicking on a search result item and come back to the previous search results.");

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);

        MetadataUtils.fillInMetadataSearchAttrUnit(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_UNIT);
        MetadataUtils.submitMetadataSearch(driver);

        MetadataUtils.waitForSearchResults(driver);

        String itemNameSelector = String.format("/%s/home/%s/%s", UiTestUtilities.IRODS_ZONE, UiTestUtilities.RODS_USERNAME, MetadataUtils.METADATA_SEARCH_FILES[0]);
        driver.findElement(By.name(itemNameSelector)).click();

        new WebDriverWait(driver, 10).until(
                ExpectedConditions.visibilityOfElementLocated(By
                        .cssSelector("#backToMetadataSearch a[href='/emc-metalnx-web/metadata/?backFromCollections=true']"))).click();

        // after coming back from the colls page, the same results as before clicking on a metadata
        // search result item should be shown
        assertMetadataSearchResults();
    }

    /**
     * Private method that checks is any result is got from the query. Also, checks if the number of
     * results shown is the same as expected. Finally, it checks if the results are shown in the
     * correct order.
     */
    private void assertMetadataSearchResults() {
        logger.info("Waiting for metadata search results {}...", new Date());

        MetadataUtils.waitForSearchResults(driver);

        List<WebElement> metadataSearchResults = driver.findElements(By.cssSelector("#treeViewTable tbody a"));

        logger.info("Checking if any result was found", new Date());
        Assert.assertNotNull(metadataSearchResults);

        // check if the number of results found matches the number of files used for testing
        Assert.assertEquals(MetadataUtils.TOTAL_FILES_METADATA_SEARCH, metadataSearchResults.size());

        MetadataUtils.assertMetadataSearchResultsOrder(driver);
    }
}
