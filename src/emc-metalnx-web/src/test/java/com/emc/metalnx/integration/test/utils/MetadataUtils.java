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

package com.emc.metalnx.integration.test.utils;

import com.emc.metalnx.test.generic.UiTestUtilities;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Contains all common operations and attributes for metadata search testing.
 *
 */

public class MetadataUtils {
    public static final String SELENIUM_ATTR = "Selenium";
    public static final String SELENIUM_VALUE = "Test";
    public static final String SELENIUM_UNIT = "Mlx";
    public static final String SELENIUM_TEST_ATTR = "SeleniumTest";
    public static final String SELENIUM_TEST_VAL1 = "1";
    public static final String SELENIUM_TEST_VAL2 = "2";
    public static final String SELENIUM_TEST_VAL3 = "3";
    public static final String SELENIUM_TEST_VAL12 = "12";

    public static final String[] METADATA_SEARCH_FILES = { "1SeleniumTestMetadataSearch.png", "2SeleniumTestMetadataSearch.png",
            "3SeleniumTestMetadataSearch.png" };

    public static final int TOTAL_FILES_METADATA_SEARCH = 3;

    public static final By TAB_LINKS = By.id("breadcrumbOptionsMenu");
    public static final By BROWSE_TAB = By.cssSelector("#breadcrumbOptions ul.dropdown-menu li:first-child a");
    public static final By METADATA_TAB = By.cssSelector("#breadcrumbOptions ul.dropdown-menu li:nth-child(2) a");
    public static final By METADATA_SEARCH_RESULTS = By.cssSelector("#treeViewTable tbody a");
    public static final By SUBMIT_SEARCH_BTN = By.id("submitMetadataSearch");
    public static final By SEARCH_OPERATOR = By.name("operator");
    public static final String EQUAL = "EQUAL";
    public static final String NOT_EQUAL = "NOT_EQUAL";
    public static final String LIKE = "LIKE";
    public static final String NOT_LIKE = "NOT_LIKE";

    // metadata table columns
    public static final By METADATA_ATTR_COL = By.cssSelector("#metadaTable tbody tr td:nth-child(2)");
    public static final By METADATA_VAL_COL = By.cssSelector("#metadaTable tbody tr td:nth-child(3)");
    public static final By METADATA_UNIT_COL = By.cssSelector("#metadaTable tbody tr td:nth-child(4)");

    private static final Logger logger = LoggerFactory.getLogger(MetadataUtils.class);

    /**
     * Goes back to the collections list from the metadata tab.
     *
     * @param driver
     */
    public static void goBackToBrowseTab(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.elementToBeClickable(CollectionUtils.BROWSE_TAB));
        WebElement backBtn = driver.findElement(CollectionUtils.BROWSE_TAB);
        new Actions(driver).moveToElement(backBtn).perform();
        backBtn.click();
    }

    /**
     * Add an AVU tag to the given file.
     *
     * @param driver
     * @param attribute
     *            attribute to be added to the file
     * @param value
     *            value to be added to the file
     * @param unit
     *            unit to be added to the file
     * @param file
     *            name of the file that will have the AVU
     */
    public static void addMetadata(WebDriver driver, String attribute, String value, String unit, String file) {
        driver.get(UiTestUtilities.COLLECTIONS_URL);

        WebDriverWait wait = new WebDriverWait(driver, 10);
        String locator = String.format("#treeViewTable a[name='/%s/home/%s/%s']", UiTestUtilities.IRODS_ZONE, UiTestUtilities.RODS_USERNAME, file);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("treeViewTable")));
        driver.findElement(By.cssSelector("#treeViewTable_filter input")).sendKeys(file);
        // driver.manage().timeouts().implicitlyWait(10L, TimeUnit.SECONDS);
        realWaitMethod(driver);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(locator)));
        // wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable tbody tr:first-child")));
        driver.findElement(By.cssSelector(locator)).click();

        openMetadataTab(driver);

        // opening addMetadata modal
        openAddMetadataModalAndSendForm(driver, attribute, value, unit);
        goBackToBrowseTab(driver);
    }

    /**
     * Method that removes all metadata tags added by a template to a given file or collection.
     *
     * @param item
     *            file or collection a template added metadata previously
     */
    public static void removeAllMetadata(WebDriver driver, String item) {
        WebDriverWait wait = new WebDriverWait(driver, 10);

        By metadataBulkDel = By.cssSelector("#metadaTable #checkAllMetadata");
        By delMetadataBtn = By.id("delMetadataBtn");
        By deleteMetadataModal = By.id("deleteMetadataModal");
        By metadataDelConf = By.cssSelector("#deleteMetadataModal .btn-primary");

        driver.get(UiTestUtilities.COLLECTIONS_URL);

        CollectionUtils.waitForItemToLoad(driver, item);
        driver.findElement(CollectionUtils.getFileLocatorUnderRodsHome(item)).click();

        wait.until(ExpectedConditions.textToBePresentInElementLocated(CollectionUtils.BROWSE_TAB, "Back"));
        driver.findElement(CollectionUtils.METADATA_TAB).click();

        CollectionUtils.waitForMetadataToBeLoaded(driver);
        driver.findElement(metadataBulkDel).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(delMetadataBtn));
        driver.findElement(delMetadataBtn).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(deleteMetadataModal));
        wait.until(ExpectedConditions.elementToBeClickable(metadataDelConf));
        driver.findElement(metadataDelConf).click();
    }

    public static void waitForSearchResults(WebDriver driver) {
        logger.info("Waiting for metadata search results {}...");
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(MetadataUtils.METADATA_SEARCH_RESULTS));
    }

    public static void addMetadata(WebDriver driver, String attribute, String value, String unit) {

        openMetadataTab(driver);

        // openning addMetadata modal
        openAddMetadataModalAndSendForm(driver, attribute, value, unit);
    }

    /**
     * Method that opens metadata tab in the user management page always brings to the
     * collection's metadata page.
     */
    public static void openMetadataTab(WebDriver driver) {
        logger.info("Testing if metadata tab button works");

        WebDriverWait wait = new WebDriverWait(driver, 10);

        realWaitMethod(driver);
        wait.until(ExpectedConditions.elementToBeClickable(TAB_LINKS));
        driver.findElement(TAB_LINKS).click();
        wait.until(ExpectedConditions.elementToBeClickable(METADATA_TAB));
        driver.findElement(METADATA_TAB).click();

        By idMetadataCollection = By.id("metadataActions");

        WebElement metadataDiv = wait.until(ExpectedConditions.presenceOfElementLocated(idMetadataCollection));

        assertTrue(metadataDiv.isDisplayed());
    }

    public static void openAddMetadataModalAndSendForm(WebDriver driver, String attribute, String value, String unit) {

        By idAddMetadataBtn = By.id("addMetadataBtn");
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.elementToBeClickable(idAddMetadataBtn));
        driver.findElement(idAddMetadataBtn).click();

        // inputs of the form
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#metadataModal .modal-dialog")));
        WebElement inputAttr = driver.findElement(By.id("newAttrModal"));
        WebElement inputVal = driver.findElement(By.id("newValModal"));
        WebElement inputUnit = driver.findElement(By.id("newUnitModal"));

        inputAttr.click();
        inputAttr.clear();
        inputAttr.sendKeys(attribute);

        inputVal.click();
        inputVal.clear();
        inputVal.sendKeys(value);

        inputUnit.click();
        inputUnit.clear();
        inputUnit.sendKeys(unit);

        // sending form
        driver.findElement(By.id("saveMetadata")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("newAttrModal")));
    }

    /**
     * This method exists so that metadata search test can be successful.
     * It will insert certain metadata tags in three specific files in rods collection
     */
    public static void addMetadataToSpecificFiles(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 15);
        driver.get(UiTestUtilities.COLLECTIONS_URL);
        wait.until(ExpectedConditions.elementToBeClickable(TAB_LINKS));

        for (int i = 0; i < METADATA_SEARCH_FILES.length; i++) {
            MetadataUtils.realWaitMethod(driver);
            driver.findElement(By.cssSelector("#treeViewTable_filter input")).sendKeys(METADATA_SEARCH_FILES[i]);
            MetadataUtils.realWaitMethod(driver);
            WebElement file = driver.findElement(By.cssSelector("#treeViewTable tr:first-child td:nth-child(2) a"));
            if (i == 0) {
                file.click();
                addMetadata(driver, SELENIUM_TEST_ATTR, SELENIUM_TEST_VAL1, "");
                addMetadata(driver, SELENIUM_TEST_ATTR, SELENIUM_TEST_VAL12, "");
            }
            else if (i == 1) {
                file.click();
                addMetadata(driver, SELENIUM_TEST_ATTR, SELENIUM_TEST_VAL2, "");
                addMetadata(driver, SELENIUM_TEST_ATTR, SELENIUM_TEST_VAL12, "");
            }
            else if (i == 2) {
                file.click();
                addMetadata(driver, SELENIUM_TEST_ATTR, SELENIUM_TEST_VAL3, "");
            }

            goBackToBrowseTab(driver);
        }
    }

    /**
     * Method that asserts the order of results shown after a metadata search is done.
     *
     * @param driver
     */
    public static void assertMetadataSearchResultsOrder(WebDriver driver) {
        List<WebElement> metadataSearchResults = driver.findElements(By.cssSelector("#treeViewTableBody a"));

        int index = 0;
        for (WebElement metadataSearchResult : metadataSearchResults) {
            Assert.assertEquals(metadataSearchResult.getText().trim(), MetadataUtils.METADATA_SEARCH_FILES[index]);
            index++;
        }
    }

    /**
     * Method that submits a metadata search form to apply the search against the data grid.
     *
     * @param driver
     */
    public static void submitMetadataSearch(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(MetadataUtils.SUBMIT_SEARCH_BTN));
        driver.findElement(MetadataUtils.SUBMIT_SEARCH_BTN).click();
    }

    /**
     * Fills search criteria for a metadata search.
     *
     * @param driver
     * @param attr
     *            attribute to be filled in the attribute field
     * @param val
     *            value to be filled in the value field
     * @param unit
     *            unit to be filled in the unit field
     */
    public static void fillInMetadataSearchAttrValAndUnit(WebDriver driver, String attr, String val, String unit) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.name("attribute")));
        wait.until(ExpectedConditions.elementToBeClickable(By.name("value")));
        wait.until(ExpectedConditions.elementToBeClickable(By.name("unit")));

        driver.findElement(By.name("attribute")).sendKeys(attr);
        driver.findElement(By.name("value")).sendKeys(val);
        driver.findElement(By.name("unit")).sendKeys(unit);
    }

    /**
     * Fills search criteria for a metadata search.
     *
     * @param driver
     * @param attr
     *            attribute to be filled in the attribute field
     * @param val
     *            value to be filled in the value field
     */
    public static void fillInMetadataSearchAttrVal(WebDriver driver, String attr, String val) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.name("attribute")));
        wait.until(ExpectedConditions.elementToBeClickable(By.name("value")));

        driver.findElement(By.name("attribute")).sendKeys(attr);
        driver.findElement(By.name("value")).sendKeys(val);
    }

    /**
     * Fills search criteria for a metadata search.
     *
     * @param driver
     * @param attr
     *            attribute to be filled in the attribute field
     * @param unit
     *            unit to be filled in the unit field
     */
    public static void fillInMetadataSearchAttrUnit(WebDriver driver, String attr, String unit) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.name("attribute")));
        wait.until(ExpectedConditions.elementToBeClickable(By.name("unit")));

        driver.findElement(By.name("attribute")).sendKeys(attr);
        driver.findElement(By.name("unit")).sendKeys(unit);
    }

    /**
     * Fills search criteria for a metadata search.
     *
     * @param driver
     * @param unit
     *            unit to be filled in the unit field
     */
    public static void fillInMetadataSearchUnit(WebDriver driver, String unit) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.name("unit")));

        driver.findElement(By.name("unit")).sendKeys(unit);
    }

    /**
     * Fills search criteria for a metadata search.
     *
     * @param driver
     * @param operator
     *            operator to be applied to the search (EQUAL, NOT_EQUAL, LIKE, NOT_LIKE)
     */
    public static void fillInMetadataSearchOperator(WebDriver driver, String operator) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(SEARCH_OPERATOR));

        new Select(driver.findElement(SEARCH_OPERATOR)).selectByValue(operator);
    }

    /**
     * Keeps adding search parameters field until it reaches a maximum
     *
     * @param driver
     */
    public static void addMaxNumberOfSearchParams(WebDriver driver) {
        driver.get(UiTestUtilities.METADATA_SEARCH_URL);

        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("addMetadataSearchRow")));
        WebElement addMetadataSearchRow = driver.findElement(By.id("addMetadataSearchRow"));

        for (int i = 0; i < 4; i++) {
            addMetadataSearchRow.click();
        }

        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.className("metadataSearchRow")));

    }

    public static void realWaitMethod(WebDriver driver) {
        try {
            new WebDriverWait(driver, 1).until(ExpectedConditions.elementToBeClickable(By.id("çççç")));
        }
        catch (Exception e) {

        }
    }
}
