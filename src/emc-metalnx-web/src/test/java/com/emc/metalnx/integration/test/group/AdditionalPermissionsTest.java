/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.integration.test.group;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.CollectionUtils;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.integration.test.utils.UserUtils;
import com.emc.metalnx.test.generic.UITest;

public class AdditionalPermissionsTest extends UITest {
    private static final Logger logger = LoggerFactory.getLogger(AdditionalPermissionsTest.class);

    public static String uname = "useradditionalpermissionforgroup" + System.currentTimeMillis();
    public static String gname = "groupadditionalpermission" + System.currentTimeMillis();
    public static String pwd = "webdriver";

    private static WebDriver driver = null;

    /*************************************
     * TEST SET UP
     *
     * @throws DataGridException
     *************************************/

    @BeforeClass
    public static void setUpBeforeClass() {
        UITest.setUpBeforeClass();
        driver = getDriver();

        try {
            FileUtils.forceRemoveFilesFromDirAsAdmin("/" + UITest.IRODS_ZONE, UITest.TEST_FILE_NAMES);
            CollectionUtils.cleanUpCollectionsUnderZone(driver, UITest.TEST_COLLECTION_NAMES);

            UITest.login();
            for (String collName : UITest.TEST_COLLECTION_NAMES) {
                CollectionUtils.createCollectionUnderZone(driver, collName, UITest.IRODS_ZONE);
            }
            FileUtils.uploadToDirAsAdmin("/" + UITest.IRODS_ZONE, UITest.TEST_FILE_NAMES);
            UITest.logout();
        }
        catch (DataGridException e) {
            logger.error("Could not setup test.");
        }

    }

    @Before
    public void setUp() throws Exception {
        login();
    }

    /**
     * After each test the user created for the test should be removed.
     */
    @After
    public void tearDown() throws Exception {
        logout();

        UserUtils.removeUser(uname, driver);

        login();
        GroupUtils.removeGroup(gname, driver);

        logout();
    }

    /**
     * After all tests are done, the test must quit the driver. This will close every window
     * associated with the current driver instance.
     *
     * @throws DataGridException
     */

    @AfterClass
    public static void tearDownAfterClass() throws DataGridException {

        UITest.login();
        FileUtils.forceRemoveFilesFromDirAsAdmin("/" + UITest.IRODS_ZONE, UITest.TEST_FILE_NAMES);
        CollectionUtils.cleanUpCollectionsUnderZone(driver, UITest.TEST_COLLECTION_NAMES);
        UITest.logout();

        if (driver != null) {
            driver.quit();
            driver = null;
            UITest.setDriver(null);
        }
    }

    /************************************* RODS ADMIN TESTS *************************************/

    /**
     * Grant READ permission and a bookmark to a rods admin user on data objects, log in as such
     * user and check if these data object show up in the bookmarks page.
     */
    @Test
    public void testGrantReadPermissionAndBookmarkOnFilesToRodsAdminAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.READ_PERMISSION, UITest.RODS_ADMIN_TYPE, UITest.TEST_FILE_NAMES);
    }

    /**
     * Grant READ permission and a bookmark to a rods admin user on collections, log in as such
     * user and check if these collections show up in the bookmarks page.
     */
    @Test
    public void testGrantReadPermissionAndBookmarkOnCollsToRodsAdminAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.READ_PERMISSION, UITest.RODS_ADMIN_TYPE, UITest.TEST_COLLECTION_NAMES);
    }

    /**
     * Grant READ permission and a bookmark to a rods admin user on data objects, log in as such
     * user and check if the data object show up in the bookmarks page.
     */
    @Test
    public void testGrantWritePermissionAndBookmarkOnFilesToRodsAdminAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.WRITE_PERMISSION, UITest.RODS_ADMIN_TYPE, UITest.TEST_FILE_NAMES);
    }

    /**
     * Grant READ permission and a bookmark to a rods admin user on collections, log in as such
     * user and check if these collections show up in the bookmarks page.
     */
    @Test
    public void testGrantWritePermissionAndBookmarkOnCollsToRodsAdminAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.WRITE_PERMISSION, UITest.RODS_ADMIN_TYPE, UITest.TEST_COLLECTION_NAMES);
    }

    /**
     * Grant READ permission and a bookmark to a rods admin user on data objects, log in as such
     * user and check if these data objects show up in the bookmarks page.
     */
    @Test
    public void testGrantOwnPermissionAndBookmarkOnFilesToRodsAdminAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.OWN_PERMISSION, UITest.RODS_ADMIN_TYPE, UITest.TEST_FILE_NAMES);
    }

    /**
     * Grant WRITE permission and a bookmark to a rods admin user on collections, log in as such
     * user and check if these collections show up in the bookmarks page.
     */
    @Test
    public void testGrantOwnPermissionAndBookmarkOnCollsToRodsAdminAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.OWN_PERMISSION, UITest.RODS_ADMIN_TYPE, UITest.TEST_COLLECTION_NAMES);
    }

    /************************************* RODS USER TESTS *************************************/

    /**
     * Grant READ permission and a bookmark to a rods user data objects, log in as such user
     * and check if these data object shows up in the bookmarks page.
     */
    @Test
    public void testGrantReadPermissionAndBookmarkOnFilesToRodsUserAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.READ_PERMISSION, UITest.RODS_USER_TYPE, UITest.TEST_FILE_NAMES);
    }

    /**
     * Grant READ permission and a bookmark to a rods user data objects, log in as such user
     * and check if these data object shows up in the bookmarks page.
     */
    @Test
    public void testGrantReadPermissionAndBookmarkOnCollsToRodsUserAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.READ_PERMISSION, UITest.RODS_USER_TYPE, UITest.TEST_COLLECTION_NAMES);
    }

    /**
     * Grant WRITE permission and a bookmark to a rods user on collections, log in as such
     * user and check if these collections shows up in the bookmarks page.
     */
    @Test
    public void testGrantWritePermissionAndBookmarkOnFilesToRodsUserAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.WRITE_PERMISSION, UITest.RODS_USER_TYPE, UITest.TEST_FILE_NAMES);
    }

    /**
     * Grant WRITE permission and a bookmark to a rods user on collections, log in as such
     * user and check if these collections shows up in the bookmarks page.
     */
    @Test
    public void testGrantWritePermissionAndBookmarkOnCollsToRodsUserAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.WRITE_PERMISSION, UITest.RODS_USER_TYPE, UITest.TEST_COLLECTION_NAMES);
    }

    /**
     * Grant OWN permission and a bookmark to a rods user data objects, log in as such user
     * and check if the data object shows up in the bookmarks page.
     */
    @Test
    public void testGrantOwnPermissionAndBookmarkOnFilesToRodsUserAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.OWN_PERMISSION, UITest.RODS_USER_TYPE, UITest.TEST_FILE_NAMES);
    }

    /**
     * Grant OWN permission and a bookmark to a rods user collections, log in as such user
     * and check if these collections shows up in the bookmarks page.
     */
    @Test
    public void testGrantOwnPermissionAndBookmarkOnCollsToRodsUserAndCheckIfBookmarksShowUp() throws Exception {
        grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(UITest.OWN_PERMISSION, UITest.RODS_USER_TYPE, UITest.TEST_COLLECTION_NAMES);
    }

    /**
     * Generic test for granting permissions to an group on a data object and check if
     * such data object shows up in the group bookmark list.
     *
     * @param permType
     *            type of permission (read, write, or own) to grant a user
     * @param userType
     *            type of user (rods admin or rods user) who will be created to test granting
     *            permissions
     */
    public void grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(String permType, String userType, String[] testItems) throws Exception {
        logger.info("Test grant read permission and bookmark on a data object to a test user.");

        logout();
        UserUtils.createUser(uname, pwd, userType, driver);
        login();

        String[] users = { uname };
        GroupUtils.createGroupWithUsersAndPermissions(gname, users, testItems, permType, driver);

        logout();
        login(uname, pwd);

        GroupUtils.checkPermissionBookmarks(gname, testItems, driver);

    }
}
