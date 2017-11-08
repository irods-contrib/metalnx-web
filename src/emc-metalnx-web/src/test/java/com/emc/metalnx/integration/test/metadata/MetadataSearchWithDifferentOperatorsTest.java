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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.fail;

@Deprecated
@Ignore
public class MetadataSearchWithDifferentOperatorsTest {
    private static final Logger logger = LoggerFactory.getLogger(MetadataSearchWithDifferentOperatorsTest.class);
    private static WebDriver driver = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UiTestUtilities.init();
        driver = UiTestUtilities.getDriver();
        FileUtils.forceRemoveFilesFromHomeAsAdmin(MetadataUtils.METADATA_SEARCH_FILES);
        FileUtils.uploadToHomeDirAsAdmin(MetadataUtils.METADATA_SEARCH_FILES);
        UiTestUtilities.login();

        driver.get(UiTestUtilities.COLLECTIONS_URL);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("breadcrumbOptionsMenu")));
        for (String file : MetadataUtils.METADATA_SEARCH_FILES) {
            try {
                new WebDriverWait(driver, 1).until(ExpectedConditions.elementToBeClickable(By.id("çççç")));
            }
            catch (Exception e) {

            }
            MetadataUtils.addMetadata(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_VALUE, "", file);
        }

        UiTestUtilities.logout();
    }

    @Before
    public void setUp() throws Exception {
        UiTestUtilities.login();
    }

    @After
    public void tearDown() throws Exception {
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
     * Method used for testing the equals operator for metadata search.
     * Search criteria: Selenium EQUALS Test
     * Number of results: 0
     * Results Expected: None
     * Expected Exceptions: {@link TimeoutException}
     */
    @Test(expected = TimeoutException.class)
    public void testSeleniumAttributeNotEqualsTestValue() {
        logger.info("Testing 'selenium' attribute not equals to 'test' value");

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);
        MetadataUtils.fillInMetadataSearchAttrVal(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_VALUE);
        MetadataUtils.fillInMetadataSearchOperator(driver, MetadataUtils.NOT_EQUAL);
        MetadataUtils.submitMetadataSearch(driver);
        MetadataUtils.waitForSearchResults(driver);

        fail("Metalnx should not find any results for this search (EQUALS).");
    }

    /**
     * Method used for testing the contains operator for metadata search.
     * Search criteria: Selenium CONTAINS Tes
     * Number of results: 3
     * Results Expected:
     * 1) 1SeleniumTestMetadataSearch.png
     * 2) 2SeleniumTestMetadataSearch.png
     * 3) 3SeleniumTestMetadataSearch.png
     */
    @Test
    public void testSeleniumAttributeContainsTeValue() {
        logger.info("Testing 'selenium' attribute contains 'Tes' value");

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);
        MetadataUtils.fillInMetadataSearchAttrVal(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_VALUE.subSequence(0, 2).toString());
        MetadataUtils.fillInMetadataSearchOperator(driver, MetadataUtils.LIKE);
        MetadataUtils.submitMetadataSearch(driver);
        MetadataUtils.waitForSearchResults(driver);

        List<WebElement> results = driver.findElements(MetadataUtils.METADATA_SEARCH_RESULTS);

        Assert.assertNotNull(results);

        logger.info("Check if the number of results found matches the number of files used for testing");
        Assert.assertEquals(MetadataUtils.TOTAL_FILES_METADATA_SEARCH, results.size());

        logger.info("Check if each file found by the search is the same as expected.");
        int index = 0;
        for (WebElement metadataSearchResult : results) {
            Assert.assertEquals(metadataSearchResult.getText().trim(), MetadataUtils.METADATA_SEARCH_FILES[index]);
            index++;
        }
    }

    /**
     * Method used for testing the NOT contains operator for metadata search.
     * Search criteria: Selenium NOT CONTAINS Tes
     * Number of results: 3
     * Results Expected:
     * 1) 1SeleniumTestMetadataSearch.png
     * 2) 2SeleniumTestMetadataSearch.png
     * 3) 3SeleniumTestMetadataSearch.png
     * Expected Exceptions: {@link TimeoutException}
     */
    @Test(expected = TimeoutException.class)
    public void testSeleniumAttributeNotContainsTeValue() {
        logger.info("Testing 'Selenium' attribute NOT contains 'Tes' value");

        driver.get(UiTestUtilities.METADATA_SEARCH_URL);
        MetadataUtils.fillInMetadataSearchAttrVal(driver, MetadataUtils.SELENIUM_ATTR, MetadataUtils.SELENIUM_VALUE.subSequence(0, 2).toString());
        MetadataUtils.fillInMetadataSearchOperator(driver, MetadataUtils.NOT_LIKE);
        MetadataUtils.submitMetadataSearch(driver);
        MetadataUtils.waitForSearchResults(driver);

        fail("Metalnx should not find any results for this search (NOT CONTAINS).");
    }
}
