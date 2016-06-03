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

package com.emc.metalnx.integration.test.permissions;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.CollectionUtils;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.integration.test.utils.UserUtils;
import com.emc.metalnx.test.generic.UITest;

/**
 * Selenium tests on the additional permission functionality when modifying an existing Metalnx
 * user. This class tests adding read, write and ownership permissions to files and collections for
 * an existing user.
 *
 */
public class ExistingUserAdditionalPermissionTest {

    private String uname = "existingUserAdditionalPermission" + System.currentTimeMillis();
    private String pwd = "webdriver";

    private static WebDriver driver = null;

    /************************************** TEST SET UP **************************************/

    @BeforeClass
    public static void setUpBeforeClass() throws DataGridException {
        UITest.setUpBeforeClass();
        driver = UITest.getDriver();

        FileUtils.forceRemoveFilesFromDirAsAdmin("/" + UITest.IRODS_ZONE, UITest.TEST_FILE_NAMES);
        CollectionUtils.cleanUpCollectionsUnderZone(driver, UITest.TEST_COLLECTION_NAMES);

        UITest.login();
        for (String collName : UITest.TEST_COLLECTION_NAMES) {
            CollectionUtils.createCollectionUnderZone(driver, collName, UITest.IRODS_ZONE);
        }

        FileUtils.uploadToDirAsAdmin("/" + UITest.IRODS_ZONE, UITest.TEST_FILE_NAMES);
        UITest.logout();
    }

    @Before
    public void setUp() throws Exception {
        UserUtils.createUser(uname, pwd, UITest.RODS_ADMIN_TYPE, driver);
        UITest.login();
    }

    /**
     * After each test the user created for the test should be removed.
     */
    @After
    public void tearDown() throws Exception {
        UITest.logout();
        UserUtils.removeUser(uname, driver);
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
    public void testGrantReadPermissionAndBookmarkOnFilesToRodsAdminAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.READ_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_FILE_NAMES, driver);
    }

    /**
     * Grant READ permission and a bookmark to a rods admin user on collections, log in as such
     * user and check if these collections show up in the bookmarks page.
     */
    @Test
    public void testGrantReadPermissionAndBookmarkOnCollsToRodsAdminAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.READ_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_COLLECTION_NAMES,
                driver);
    }

    /**
     * Grant READ permission and a bookmark to a rods admin user on data objects, log in as such
     * user and check if the data object show up in the bookmarks page.
     */
    @Test
    public void testGrantWritePermissionAndBookmarkOnFilesToRodsAdminAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.WRITE_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_FILE_NAMES, driver);
    }

    /**
     * Grant READ permission and a bookmark to a rods admin user on collections, log in as such
     * user and check if these collections show up in the bookmarks page.
     */
    @Test
    public void testGrantWritePermissionAndBookmarkOnCollsToRodsAdminAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.WRITE_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_COLLECTION_NAMES,
                driver);
    }

    /**
     * Grant READ permission and a bookmark to a rods admin user on data objects, log in as such
     * user and check if these data objects show up in the bookmarks page.
     */
    @Test
    public void testGrantOwnPermissionAndBookmarkOnFilesToRodsAdminAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.OWN_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_FILE_NAMES, driver);
    }

    /**
     * Grant WRITE permission and a bookmark to a rods admin user on collections, log in as such
     * user and check if these collections show up in the bookmarks page.
     */
    @Test
    public void testGrantOwnPermissionAndBookmarkOnCollsToRodsAdminAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.OWN_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_COLLECTION_NAMES,
                driver);
    }

    /************************************* RODS USER TESTS *************************************/

    /**
     * Grant READ permission and a bookmark to a rods user data objects, log in as such user
     * and check if these data object shows up in the bookmarks page.
     */
    @Test
    public void testGrantReadPermissionAndBookmarkOnFilesToRodsUserAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.READ_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_FILE_NAMES, driver);
    }

    /**
     * Grant READ permission and a bookmark to a rods user data objects, log in as such user
     * and check if these data object shows up in the bookmarks page.
     */
    @Test
    public void testGrantReadPermissionAndBookmarkOnCollsToRodsUserAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.READ_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_COLLECTION_NAMES,
                driver);
    }

    /**
     * Grant WRITE permission and a bookmark to a rods user on collections, log in as such
     * user and check if these collections shows up in the bookmarks page.
     */
    @Test
    public void testGrantWritePermissionAndBookmarkOnFilesToRodsUserAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.WRITE_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_FILE_NAMES, driver);
    }

    /**
     * Grant WRITE permission and a bookmark to a rods user on collections, log in as such
     * user and check if these collections shows up in the bookmarks page.
     */
    @Test
    public void testGrantWritePermissionAndBookmarkOnCollsToRodsUserAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.WRITE_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_COLLECTION_NAMES,
                driver);
    }

    /**
     * Grant OWN permission and a bookmark to a rods user data objects, log in as such user
     * and check if the data object shows up in the bookmarks page.
     */
    @Test
    public void testGrantOwnPermissionAndBookmarkOnFilesToRodsUserAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.OWN_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_FILE_NAMES, driver);
    }

    /**
     * Grant OWN permission and a bookmark to a rods user collections, log in as such user
     * and check if these collections shows up in the bookmarks page.
     */
    @Test
    public void testGrantOwnPermissionAndBookmarkOnCollsToRodsUserAndCheckIfBookmarksShowUp() {
        UserUtils.modifyUserGrantPermissionAndCheckBookmarks(UITest.OWN_PERMISSION, uname, UITest.IRODS_ZONE, pwd, UITest.TEST_COLLECTION_NAMES,
                driver);
    }
}
