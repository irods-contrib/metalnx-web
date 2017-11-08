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

package com.emc.metalnx.integration.test.group;

import com.emc.metalnx.test.generic.UiTestUtilities;
import junit.framework.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Deprecated
public class GroupUtils {

    private static final By GROUP_FILTER_INPUT = By.cssSelector("#groupsListTable_filter input");
    public static final By GROUP_LIST_TABLE = By.cssSelector("#groupsListTable tbody");

    /**
     * Generic test for granting permissions to an existing group on a data object and check if
     * such data object shows up in the group bookmark list.
     *
     * @param gname
     *            group name
     * @param permType
     *            type of permission (read, write, or own) to grant a user
     * @param userName
     *            user name
     * @param pwd
     * @param testItems
     *            collections or data objects to be granted permission on
     */
    public static void grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(WebDriver driver, String gname, String permType, String userName,
            String pwd, String... testItems) {
        GroupUtils.modifyGroupWithPermission(gname, testItems, permType, driver);

        UiTestUtilities.logout();
        UiTestUtilities.login(userName, pwd);

        GroupUtils.checkPermissionBookmarks(gname, testItems, driver);

    }

    /**
     * Creates a new group
     *
     * @param groupName
     *            {@link String} the name of the group
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    public static void createGroup(String groupName, WebDriver driver) {
        driver.get(UiTestUtilities.ADD_GROUPS_URL);
        fillGroupGeneralInformation(groupName, driver);
        submitGroupForm(driver);
    }

    /**
     * Creates a new group containing the specified list of users
     *
     * @param groupName
     *            {@link String} the name of the group
     * @param users
     *            {@link String[]} the list of user names that must be included on the group
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    public static void createGroupWithUsers(String groupName, String[] users, WebDriver driver) {
        driver.get(UiTestUtilities.ADD_GROUPS_URL);
        fillGroupGeneralInformation(groupName, driver);
        assignUsersToGroup(users, driver);
        submitGroupForm(driver);
    }

    /**
     * Creates a new group with the specified permissions on items. Recursive permission WILL be applied.
     *
     * @param groupName
     *            {@link String} the name of the group
     * @param collsOrDataObjects
     *            {@link String[]} the list of items to be set permissions on (relative to
     *            /ZONE_NAME)
     * @param permission
     *            {@link String} the permission type
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    public static void createGroupWithRecursivePermission(WebDriver driver, String groupName, String permission, String... collsOrDataObjects) {
        driver.get(UiTestUtilities.ADD_GROUPS_URL);
        fillGroupGeneralInformation(groupName, driver);
        assignPermissionsOnCollectionsOrDataObjects(collsOrDataObjects, permission, driver, true);
        submitGroupForm(driver);
    }

    /**
     * Creates a new group with the specified permissions on items. *Recursive permission will NOT be applied.
     *
     * @param groupName
     *            {@link String} the name of the group
     * @param collsOrDataObjects
     *            {@link String[]} the list of items to be set permissions on (relative to
     *            /ZONE_NAME)
     * @param permission
     *            {@link String} the permission type
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    public static void createGroupWithPermissions(WebDriver driver, String groupName, String permission, String... collsOrDataObjects) {
        driver.get(UiTestUtilities.ADD_GROUPS_URL);
        fillGroupGeneralInformation(groupName, driver);
        assignPermissionsOnCollectionsOrDataObjects(collsOrDataObjects, permission, driver, false);
        submitGroupForm(driver);
    }

    /**
     * Creates a new group with the specified list of users to be included and items to be set
     * permissions to
     *
     * @param groupName
     *            {@link String} the name of the group
     * @param users
     *            {@link String[]} the list of user names that must be included on the group
     * @param collsOrDataObjects
     *            {@link String[]} the list of items to be set permissions on (relative to
     *            /ZONE_NAME)
     * @param permission
     *            {@link String} the permission type
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    public static void createGroupWithUsersAndPermissions(String groupName, String[] users, String[] collsOrDataObjects, String permission,
            WebDriver driver) {
        driver.get(UiTestUtilities.ADD_GROUPS_URL);
        fillGroupGeneralInformation(groupName, driver);
        assignUsersToGroup(users, driver);
        assignPermissionsOnCollectionsOrDataObjects(collsOrDataObjects, permission, driver, false);
        submitGroupForm(driver);
    }

    /**
     * Modifies a group settings permissions to the specified items
     *
     * @param groupName
     *            {@link String} the name of the group
     * @param collsOrDataObjects
     *            {@link String[]} the list of items to be set permissions on (relative to
     *            /ZONE_NAME)
     * @param permission
     *            {@link String} the permission type
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    public static void modifyGroupWithPermission(String groupName, String[] collsOrDataObjects, String permission, WebDriver driver) {
        driver.get(UiTestUtilities.getModifyGroupsPage(groupName));
        assignPermissionsOnCollectionsOrDataObjects(collsOrDataObjects, permission, driver, false);
        driver.findElement(By.id("submitGroupFormBtn")).click();
    }

    /**
     * Removes the specified group by its name
     *
     * @param groupName
     *            {@link String} the name of the group to be removed
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    public static void removeGroup(String groupName, WebDriver driver) {

        WebDriverWait wait = new WebDriverWait(driver, 5);
        By groupsTable = By.cssSelector("#groupsListTable tbody");
        By buttonToRemoveGroup = By.id("deleteBtn_" + groupName);
        By removalConfirmation = By.id("deleteGroupConfBtn");

        driver.get(UiTestUtilities.GROUPS_URL);
        Assert.assertEquals(driver.getCurrentUrl(), UiTestUtilities.GROUPS_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(groupsTable));
        wait.until(ExpectedConditions.visibilityOfElementLocated(GROUP_FILTER_INPUT)).sendKeys(groupName);

        wait.until(ExpectedConditions.elementToBeClickable(buttonToRemoveGroup));
        driver.findElement(buttonToRemoveGroup).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("removalModal")));
        wait.until(ExpectedConditions.elementToBeClickable(removalConfirmation));
        driver.findElement(removalConfirmation).click();
    }

    /**
     * Checks if the group bookmarks are available on the user interface
     *
     * @param groupName
     *            {@link String} the name of the group to be removed
     * @param testItems
     *            {@link String[]} the list of items to be set permissions on (relative to
     *            /ZONE_NAME)
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    public static void checkPermissionBookmarks(String groupName, String[] testItems, WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 15);

        driver.get(UiTestUtilities.GROUP_BOOKMARKS_URL);
        Assert.assertEquals(UiTestUtilities.GROUP_BOOKMARKS_URL, driver.getCurrentUrl());

        driver.findElement(By.cssSelector("#groupBookmarksTable_filter input")).sendKeys(groupName);
        try {
            new WebDriverWait(driver, 1).until(ExpectedConditions.elementToBeClickable(By.id("çççç")));
        }
        catch (Exception e) {

        }

        List<WebElement> bookmarks = driver.findElements(By.cssSelector("#groupBookmarksTable tbody tr td:first-child"));
        Assert.assertEquals(testItems.length, bookmarks.size());

        // Comparing bookmark links
        Set<String> bookmarksSet = new HashSet<String>();
        for (String path : testItems) {
            bookmarksSet.add(path);
        }

        for (int i = 0; i < bookmarks.size(); i++) {
            Assert.assertTrue(bookmarksSet.contains(bookmarks.get(i).getText()));
        }

        bookmarks.get(0).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#directoryPath .breadcrumb")));

        Assert.assertEquals(UiTestUtilities.COLLECTIONS_URL, driver.getCurrentUrl());
    }

    /* **************************************************************************************** */
    /* *********************************** PRIVATE METHODS ************************************ */
    /* **************************************************************************************** */

    /**
     * Fills group general information on the group form page
     *
     * @param groupName
     *            {@link String} the name of the group to be modified
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    private static void fillGroupGeneralInformation(String groupName, WebDriver driver) {
        driver.findElement(By.id("inputGroupname")).sendKeys(groupName);
    }

    /**
     * Assigns users to the group whose form is opened
     *
     * @param users
     *            {@link String[]} the list of user names that must be included on the group
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    private static void assignUsersToGroup(String[] users, WebDriver driver) {
        driver.findElement(By.id("showUsersListBtn")).click();
        for (String user : users) {
            driver.findElement(By.cssSelector("#usersListTable_filter input")).click();
            driver.findElement(By.cssSelector("#usersListTable_filter input")).clear();
            driver.findElement(By.cssSelector("#usersListTable_filter input")).sendKeys(user);
            driver.findElement(By.cssSelector("#" + user + " input[type='checkbox']")).click();
        }
        driver.findElement(By.id("hideUsersListBtn")).click();
    }

    /**
     * Fills in the permission items to be set on the group
     *
     * @param collsOrDataObjects
     *            {@link String[]} the list of items to be set permissions on (relative to
     *            /ZONE_NAME)
     * @param permission
     *            {@link String} the permission type
     * @param applyPermRecursively
     *            {@link boolean} true, if the permission should be applied to subcollections and files. False, otherwise.
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    private static void assignPermissionsOnCollectionsOrDataObjects(String[] collsOrDataObjects, String permission, WebDriver driver,
            boolean applyPermRecursively) {
        WebDriverWait wait = new WebDriverWait(driver, 5);
        wait.until(ExpectedConditions.elementToBeClickable(By.name(UiTestUtilities.IRODS_ZONE)));

        driver.findElement(By.name(UiTestUtilities.IRODS_ZONE)).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("permission_" + collsOrDataObjects[0])));

        for (String item : collsOrDataObjects) {
            new Select(driver.findElement(By.id("permission_" + item))).selectByValue(permission);
            answerRecursiveQuestionGroupForm(driver, item, applyPermRecursively);
            By permissionSelector = By.cssSelector(String.format("input[name='/%s/%s']", UiTestUtilities.IRODS_ZONE, item));
            wait.until(ExpectedConditions.elementSelectionStateToBe(permissionSelector, true));
        }
    }

    /**
     * Answers the question if the user wants to apply recursive permission on a collection.
     *
     * @param driver
     * @param collection
     *            collection name to apply permission recursively
     * @param applyPermRecursively
     *            True, if the chosen permission should be applied recursively. False, otherwise.
     */
    private static void answerRecursiveQuestionGroupForm(WebDriver driver, String collection, boolean applyPermRecursively) {
        // Eating the exception is needed in case a Data Object (no question will be asked) is given instead of a colletion.
        // From Selenium, there is no easy way to check if an item is a collection or data object.
        try {
            String btnRecursive = applyPermRecursively ? " .btn-recursive-yes" : " .btn-recursive-no";
            String recursiveSelector = String.format("#recursiveQuestion_%s %s", collection, btnRecursive);
            new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(recursiveSelector))).click();
        }
        catch (Exception e) {}
    }

    /**
     * Submits the group form with the created/modified information
     *
     * @param driver
     *            {@link WebDriver} the Web Driver instance
     */
    private static void submitGroupForm(WebDriver driver) {
        driver.findElement(By.id("submitGroupFormBtn")).click();
        Assert.assertEquals(UiTestUtilities.GROUPS_URL, driver.getCurrentUrl());
    }

}
