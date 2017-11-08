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

package com.emc.metalnx.integration.test.datatables;

import com.emc.metalnx.test.generic.UiTestUtilities;
import junit.framework.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class PageSizeSelectorUtils {

    public static final String[] PAGE_SIZES = { "10", "25", "50", "100" };

    private static By collsPageSizeSelector = By.name("treeViewTable_length");
    private static By usersPageSizeSelector = By.name("usersListTable_length");
    private static By groupsPageSizeSelector = By.name("groupsListTable_length");
    private static By resourcesPageSizeSelector = By.name("resourcesListTable_length");

    private static Map<String, By> pageSizeSelectorMap;
    static {
        pageSizeSelectorMap = new HashMap<String, By>();
        pageSizeSelectorMap.put(UiTestUtilities.COLLECTIONS_URL, collsPageSizeSelector);
        pageSizeSelectorMap.put(UiTestUtilities.TRASH_URL, collsPageSizeSelector);
        pageSizeSelectorMap.put(UiTestUtilities.PUBLIC_URL, collsPageSizeSelector);
        pageSizeSelectorMap.put(UiTestUtilities.USERS_URL, usersPageSizeSelector);
        pageSizeSelectorMap.put(UiTestUtilities.GROUPS_URL, groupsPageSizeSelector);
        pageSizeSelectorMap.put(UiTestUtilities.RESOURCES_URL, resourcesPageSizeSelector);
    }

    /**
     * Auxiliar method that asserts if the page size selected matches the current page size
     * displayed after refreshing the page.
     * Table: Collections Management.
     *
     * @param driver
     * @param pageSize
     *            {@link String} representing the page size (10, 25, 50, 100)
     */
    public static void assertPageSize(WebDriver driver, String url, String pageSize) {
        driver.get(url);
        waitForTable(driver, url);
        selectPageSize(driver, url, pageSize);
        try {
            new WebDriverWait(driver, 1).until(ExpectedConditions.elementToBeClickable(By.id("çççç")));
        }
        catch (Exception e) {

        }
        waitForTable(driver, url);
        driver.get(url);
        waitForTable(driver, url);
        Assert.assertEquals(pageSize, findCurrentPageSize(driver, url));
    }

    /**
     * Selects a page size from the page size selector list.
     *
     * @param pageSize
     *            {@link String} representing the required page size
     */
    public static void selectPageSize(WebDriver driver, String url, String pageSize) {
        waitForPageSizeSelector(driver, url);
        new Select(driver.findElement(pageSizeSelectorMap.get(url))).selectByValue(pageSize);
        waitForPageSizeSelector(driver, url);
    }

    /**
     * Finds what is the current page size.
     *
     * @return {@link String} representing the current page size
     */
    public static String findCurrentPageSize(WebDriver driver, String url) {
        return new Select(driver.findElement(pageSizeSelectorMap.get(url))).getFirstSelectedOption().getText();
    }

    /**
     * Waits for the page size selector component to be loaded.
     *
     * @param driver
     * @param url
     */
    public static void waitForPageSizeSelector(WebDriver driver, String url) {
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(pageSizeSelectorMap.get(url)));
    }

    /**
     * Waits for the current page's table to load.
     *
     * @param driver
     * @param url
     */
    public static void waitForTable(WebDriver driver, String url) {
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("table tbody tr:first-child td")));
    }
}
