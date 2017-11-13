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
import junit.framework.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;


public class UserUtils {
    private static final Logger logger = LoggerFactory.getLogger(UserUtils.class);

    public static final By BOOKMARKS = By.cssSelector("#userBookmarksTable tbody tr");
    public static final By ZONE_FOLDER = By.name("/" + UiTestUtilities.IRODS_ZONE);
    public static final By USERNAME_INPUT = By.id("inputUsername");
    public static final By PWD_INPUT = By.id("inputPassword");
    public static final By PWD_CONF_INPUT = By.id("inputPasswordConfirmation");
    public static final By USER_TYPE_SELECT = By.id("selectUserType");
    public static final By SEARCH_INPUT = By.cssSelector("#usersListTable_filter input");

    private static final String userModifyStr = "modify/%s/%s/";

    /**
     * Checks if a user is in the group(s) given.
     *
     * @param driver
     * @param uname
     *            user name
     * @param group
     *            group name
     */
    public static void checkIfUserIsInGroup(WebDriver driver, String uname, String... group) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        UiTestUtilities.login();
        driver.get(UiTestUtilities.USERS_URL);
        UserUtils.searchUser(driver, uname);

        By MODIFY_USER_BTN = By.id(String.format(userModifyStr, uname, UiTestUtilities.IRODS_ZONE));
        wait.until(ExpectedConditions.elementToBeClickable(MODIFY_USER_BTN)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(UserUtils.ZONE_FOLDER));

        for (String grp : group) {
            assertNotNull(driver.findElement(By.id(grp)));
        }

        UiTestUtilities.logout();
    }

    /**
     * As given Admin, this method created a profile, and includes the given group to this profile.
     *
     * @param driver
     * @param adminUser
     *            admin username to log in Metalnx
     * @param adminPwd
     *            admin password to log in Metalnx
     * @param profile
     *            profile name that will be created
     * @param group
     *            name of the group that will be inserted into the profile
     */
    public static void createProfileAndIncludeGroupToThisProfileAsAdmin(WebDriver driver, String adminUser, String adminPwd, String profile,
            String group) {
        List<String> groupNames = new ArrayList<String>();
        groupNames.add(group);

        UiTestUtilities.login(adminUser, adminPwd);
        driver.get(UiTestUtilities.ADD_PROFILES_URL);
        ProfileUtils.addUserProfile(profile, "profileTestScenarioDesc", groupNames, driver);
        new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.id("userProfilesListTable")));
        assertTrue(driver.findElement(By.className("alert-success")).isDisplayed());
        UiTestUtilities.logout();
    }

    /**
     * Fills in user information (username, password, and confirmation password) and save this user.
     *
     * @param uname
     *            username
     * @param pwd
     *            password
     * @param driver
     *            driver already logged in the application
     */
    public static void createUser(String uname, String pwd, String userType, WebDriver driver) {
        UiTestUtilities.login();
        driver.get(UiTestUtilities.ADD_USERS_URL);
        fillInUserGeneralInformation(uname, pwd, userType, driver);
        submitUserForm(driver);
        UiTestUtilities.logout();
    }

    /**
     * As an admin, opens the modify user form to apply a profile to the given user.
     *
     * @param driver
     * @param adminUname
     *            administrator username to log in
     * @param adminPwd
     *            administrator password to log in
     * @param uname
     *            user who will be applied the given profile
     * @param rodsUserType
     *            user type
     * @param profile
     *            profile name that will be applied on the rods user
     */
    public static void modifyUserAsAdminAndApplyProfile(WebDriver driver, String adminUname, String adminPwd, String uname, String zone,
            String rodsUserType, String profile) {
        UiTestUtilities.login(adminUname, adminPwd);
        modifyUser(uname, zone, driver);
        new WebDriverWait(driver, 15).until(ExpectedConditions.elementToBeClickable(By.id("selectProfile")));
        new Select(driver.findElement(By.id("selectProfile"))).selectByVisibleText(profile);
        submitUserForm(driver);
        UiTestUtilities.logout();
    }

    /**
     * Fills in user information (username, password, and confirmation password) and save this user using a given admin account.
     *
     * @param adminUname
     *            admin username used to create the user account
     * @param adminPwd
     *            admin passwrod
     * @param uname
     *            name of the user
     * @param pwd
     *            password
     * @param userType
     * @param driver
     */
    public static void createUserAsAdminAndApplyProfile(WebDriver driver, String adminUname, String adminPwd, String uname, String pwd,
            String userType, String profile) {

        UiTestUtilities.login(adminUname, adminPwd);
        driver.get(UiTestUtilities.ADD_USERS_URL);
        fillInUserGeneralInformation(uname, pwd, userType, driver);
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("showGroupsListBtn"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(By.id("selectProfile")));
        new Select(driver.findElement(By.id("selectProfile"))).selectByVisibleText(profile);
        submitUserForm(driver);
        UiTestUtilities.logout();
    }

    /**
     * Removes a user from the application based on his username.
     *
     * @param uname
     *            username
     * @param driver
     *            driver already logged in the application
     */
    public static void removeUser(String uname, WebDriver driver) {

        WebDriverWait wait = new WebDriverWait(driver, 15);

        UiTestUtilities.login();
        driver.get(UiTestUtilities.USERS_URL);

        By removeButton = By.id("btn_remove_" + uname);
        By removeConfirmationButton = By.id("btnConfUserRemoval_Yes");

        searchUser(driver, uname);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(removeButton));

            driver.findElement(removeButton).click();
            wait.until(ExpectedConditions.elementToBeClickable(removeConfirmationButton));
            driver.findElement(removeConfirmationButton).click();
        }
        catch (Exception e) {
            logger.error("Could not remove user [{}]", uname);
        }
        UiTestUtilities.logout();
    }

    /**
     * Goes to the modify user page based on the given username and zone.
     *
     * @param uname
     *            username to be modified
     * @param zone
     *            zone where the user is
     * @param driver
     *            web driver already logged in the application
     */
    public static void modifyUser(String uname, String zone, WebDriver driver) {
        driver.get(UiTestUtilities.USERS_URL);

        WebDriverWait wait = new WebDriverWait(driver, 15);

        By modifyButton = By.id(String.format(userModifyStr, uname, zone));

        searchUser(driver, uname);
        wait.until(ExpectedConditions.elementToBeClickable(modifyButton));

        // find the user to modify

        driver.findElement(modifyButton).click();

        // checking if Mlx goes to the user modify form
        String modifyUserURL = UiTestUtilities.USERS_URL + String.format(userModifyStr, uname, zone);
        Assert.assertEquals(modifyUserURL, driver.getCurrentUrl());
    }

    /**
     * Generic test for granting permissions to an user on a data object or collection and check if
     * such data object shows up in the user bookmark list.
     *
     * @param permType
     *            type of permission that will be granted
     * @param uname
     *            name of the user the permission will be granted
     * @param zone
     *            zone name where the user is
     * @param pwd
     *            user password
     * @param runTestForCollection
     *            permissions
     * @param driver
     */
    public static void createUserGrantPermissionAndCheckBookmarks(String permType, String uname, String zone, String pwd, String userType,
            String[] testItems, WebDriver driver) {
        logger.info("Test grant read permission and bookmark on a data object to a test user.");

        try {
            driver.get(UiTestUtilities.ADD_USERS_URL);
            fillInUserGeneralInformation(uname, pwd, userType, driver);
            grantPermissionToUserAndCheckBookmarks(permType, uname, zone, pwd, testItems, driver);
        }
        catch (Exception e) {
            logger.error("Could not run test properly: {}", e.getMessage());
            fail("Fail to grant read permission and bookmarks to user 'Test': " + e.getMessage());
        }
    }

    /**
     * Generic test for granting permissions to an user on a data object or collection and check if
     * such data object shows up in the user bookmark list.
     *
     * @param permType
     *            type of permission (read, write, or own) to grant a user
     */
    public static void modifyUserGrantPermissionAndCheckBookmarks(String permType, String uname, String zone, String pwd, String[] testItems,
            WebDriver driver) {
        logger.info("Test grant read permission and bookmark on a data object to an existing user.");
        UserUtils.modifyUser(uname, zone, driver);
        grantPermissionToUserAndCheckBookmarks(permType, uname, zone, pwd, testItems, driver);
    }

    /**
     * Checks if for a given user, all items (collections or data objects) are marked as bookmarks.
     *
     * @param uname
     *            username
     * @param pwd
     *            user password
     * @param testItems
     *            list of collections or data objects
     * @param driver
     */
    public static void checkIfBookmarksExist(String uname, String pwd, String[] testItems, WebDriver driver) {

        // logout from Metalnx and login as the test user
        UiTestUtilities.logout();
        UiTestUtilities.login(uname, pwd);

        driver.get(UiTestUtilities.USER_BOOKMARKS_URL);
        Assert.assertEquals(UiTestUtilities.USER_BOOKMARKS_URL, driver.getCurrentUrl());

        new WebDriverWait(driver, 15).until(ExpectedConditions.visibilityOfAllElementsLocatedBy(BOOKMARKS));
        List<WebElement> bookmarks = driver.findElements(BOOKMARKS);
        Assert.assertTrue(testItems.length <= bookmarks.size());

        logger.info("Checking if bookmarks show up in order");
        Arrays.sort(testItems);
        for (int i = 0; i < testItems.length; i++) {
            Assert.assertTrue(bookmarks.get(i).getText().contains(testItems[i]));
        }

        UiTestUtilities.logout();
        UiTestUtilities.login();
    }

    /**
     * Grants permission on a set of items (collection or data objects).
     *
     * @param permType
     *            type of permission to be granted (read, write, own or none)
     * @param testItems
     *            array of items (collections or data objects) that the permission will be applied
     * @param driver
     *            driver already logged in the application
     */
    private static void grantPermissionOnItems(String permType, String[] testItems, WebDriver driver) {

        WebDriverWait wait = new WebDriverWait(driver, 15);

        // Setting Read, write, or ownership permission on the test files or test collection
        for (String item : testItems) {

            By permissionValue = By.id("permission_" + item);
            By fileItem = By.cssSelector(String.format("input[name='/%s/%s']", UiTestUtilities.IRODS_ZONE, item));

            wait.until(ExpectedConditions.elementToBeClickable(By.id("permission_" + item)));
            new Select(driver.findElement(permissionValue)).selectByValue(permType);
            String recursiveSelector = String.format("#recursiveQuestion_%s .btn-recursive-no", item);
            try {
                new WebDriverWait(driver, 5).until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(recursiveSelector))).click();
            }
            catch (Exception e) {}
            wait.until(ExpectedConditions.elementToBeClickable(fileItem));
            wait.until(ExpectedConditions.elementSelectionStateToBe(fileItem, true));
        }
    }

    /**
     * Fills in user information (username, password, and confirmation password) but does not save
     * the user. The user form remains open for future edition.
     *
     * @param uname
     *            username
     * @param pwd
     *            password
     * @param driver
     *            driver already logged in the application
     */
    public static void fillInUserGeneralInformation(String uname, String pwd, String userType, WebDriver driver) {

        WebDriverWait wait = new WebDriverWait(driver, 15);

        wait.until(ExpectedConditions.visibilityOfElementLocated(UserUtils.USERNAME_INPUT));
        wait.until(ExpectedConditions.visibilityOfElementLocated(UserUtils.ZONE_FOLDER));

        driver.findElement(USERNAME_INPUT).sendKeys(uname);
        driver.findElement(PWD_INPUT).sendKeys(pwd);
        driver.findElement(PWD_CONF_INPUT).sendKeys(pwd);
        new Select(driver.findElement(USER_TYPE_SELECT)).selectByValue(userType);
    }

    /**
     * Grants permission to user on a set of items.
     *
     * @param permType
     *            permission type to be granted to user
     * @param uname
     *            username to give permissions
     * @param zone
     *            zone name where the user is
     * @param pwd
     *            user password
     * @param testItems
     *            array of items (collections or data objects) that will be given permission
     * @param driver
     *            web driver already logged in the application
     */
    private static void grantPermissionToUserAndCheckBookmarks(String permType, String uname, String zone, String pwd, String[] testItems,
            WebDriver driver) {

        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.elementToBeClickable(By.name("/" + zone)));

        // navigating to where the test items are located (/zoneName)
        driver.findElement(By.name("/" + zone)).click();

        grantPermissionOnItems(permType, testItems, driver);
        submitUserForm(driver);
        searchUser(driver, uname);
        checkIfBookmarksExist(uname, pwd, testItems, driver);
    }

    /**
     * Method that submits a user form and checks whether or not after that the application shows
     * the list of users page.
     *
     * @param driver
     *            web driver already in the user form
     */
    public static void submitUserForm(WebDriver driver) {
        driver.findElement(By.id("submitUserFormBtn")).click();
        Assert.assertEquals(UiTestUtilities.USERS_URL, driver.getCurrentUrl());
    }

    /**
     * Method that fills in the personal information section of the user form.
     *
     * @param driver
     * @param fname
     *            user's first name
     * @param lname
     *            user's last name
     * @param email
     *            user's email
     */
    public static void fillInPersonalInfo(WebDriver driver, String fname, String lname, String email) {
        new WebDriverWait(driver, 15).until(ExpectedConditions.elementToBeClickable(By.id("inputFirstName")));

        driver.findElement(By.id("inputFirstName")).sendKeys(fname);
        driver.findElement(By.id("inputLastName")).sendKeys(lname);
        driver.findElement(By.id("inputEmail")).sendKeys(email);
    }

    public static void searchUser(WebDriver driver, String username) {
        WebDriverWait wait = new WebDriverWait(driver, 5);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("çççç")));
        }
        catch (Exception e) {}
        wait.until(ExpectedConditions.visibilityOfElementLocated(SEARCH_INPUT));
        driver.findElement(SEARCH_INPUT).sendKeys(username);
    }
}
