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

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.integration.test.utils.UserUtils;
import com.emc.metalnx.test.generic.UITest;

/**
 * Tests that check group permissions for files.
 */
public class ExistingGroupPermissionsOnFilesTest {

    private String rodsUserName = "rodsuseradditionalpermissionforgroup" + System.currentTimeMillis();
    private String rodsAdminName = "rodsadminadditionalpermissionforgroup" + System.currentTimeMillis();
    private String gname = "groupadditionalpermission" + System.currentTimeMillis();
    private String pwd = "webdriver";

    private static WebDriver driver = null;

    /************************************* TEST SET UP *************************************/

    @BeforeClass
    public static void setUpBeforeClass() throws DataGridException {
        UITest.setUpBeforeClass();
        driver = UITest.getDriver();
        FileUtils.forceRemoveFilesFromDirAsAdmin("/" + UITest.IRODS_ZONE, UITest.TEST_FILE_NAMES);
    }

    @Before
    public void setUp() throws Exception {
        FileUtils.uploadToDirAsAdmin("/" + UITest.IRODS_ZONE, UITest.TEST_FILE_NAMES);

        UserUtils.createUser(rodsUserName, pwd, "rodsuser", driver);
        UserUtils.createUser(rodsAdminName, pwd, "rodsadmin", driver);

        UITest.login();

        String[] users = { rodsAdminName, rodsUserName };
        GroupUtils.createGroupWithUsers(gname, users, driver);
    }

    /**
     * After each test the user created for the test should be removed.
     */
    @After
    public void tearDown() throws Exception {
        FileUtils.forceRemoveFilesFromDirAsAdmin("/" + UITest.IRODS_ZONE, UITest.TEST_FILE_NAMES);

        UITest.logout();
        UserUtils.removeUser(rodsUserName, driver);
        UserUtils.removeUser(rodsAdminName, driver);

        UITest.login();
        GroupUtils.removeGroup(gname, driver);
        UITest.logout();
    }

    /**
     * After all tests are done, the test must quit the driver. This will close every window
     * associated with the current driver instance.
     *
     * @throws DataGridException
     */

    @AfterClass
    public static void tearDownAfterClass() throws DataGridException {
        if (driver != null) {
            FileUtils.forceRemoveFilesFromDirAsAdmin("/" + UITest.IRODS_ZONE, UITest.TEST_FILE_NAMES);
            driver.quit();
            driver = null;
            UITest.setDriver(null);
        }
    }

    /**
     * Grant read permission on a data object for an existing group and check if its users
     * have access to the collection using bookmarks
     */
    @Test
    public void grantReadPermissionOnDataObject() {
        GroupUtils.grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(driver, gname, UITest.READ_PERMISSION, rodsUserName, pwd,
                UITest.TEST_FILE_NAMES);
    }

    /**
     * Grant write permission on a data object for an existing group and check if its users
     * have access to the collection using bookmarks
     */
    @Test
    public void grantWritePermissionOnDataObject() {
        GroupUtils.grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(driver, gname, UITest.WRITE_PERMISSION, rodsUserName, pwd,
                UITest.TEST_FILE_NAMES);
    }

    /**
     * Grant owner permission on a data object for an existing group and check if its users
     * have access to the collection using bookmarks
     */
    @Test
    public void grantOwnershipPermissionOnDataObject() {
        GroupUtils.grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(driver, gname, UITest.OWN_PERMISSION, rodsUserName, pwd,
                UITest.TEST_FILE_NAMES);
    }

    /**
     * Grant read permission on a data object for an existing group and check if its users
     * have access to the collection using bookmarks
     */
    @Test
    public void grantReadPermissionOnDataObjectForAdmin() {
        GroupUtils.grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(driver, gname, UITest.READ_PERMISSION, rodsAdminName, pwd,
                UITest.TEST_FILE_NAMES);
    }

    /**
     * Grant write permission on a data object for an existing group and check if its users
     * have access to the collection using bookmarks
     */
    @Test
    public void grantWritePermissionOnDataObjectForAdmin() {
        GroupUtils.grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(driver, gname, UITest.WRITE_PERMISSION, rodsAdminName, pwd,
                UITest.TEST_FILE_NAMES);
    }

    /**
     * Grant owner permission on a data object for an existing group and check if its users
     * have access to the collection using bookmarks
     */
    @Test
    public void grantOwnershipPermissionOnDataObjectForAdmin() {
        GroupUtils.grantPermissionAndBookmarkToGroupAndCheckIfBookmarkShowUp(driver, gname, UITest.OWN_PERMISSION, rodsAdminName, pwd,
                UITest.TEST_FILE_NAMES);
    }
}
