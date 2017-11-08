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

package com.emc.metalnx.integration.test.permissions;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.CollectionUtils;
import com.emc.metalnx.integration.test.utils.FileUtils;
import com.emc.metalnx.integration.test.utils.UserUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

/**
 * Selenium tests on the additional permission functionality when adding a new
 * user into Metalnx. This class tests adding read, write and ownership
 * permissions to files and collections for a brand new user.
 *
 */
@Deprecated
@Ignore
public class NewUserAdditionalPermissionTest {

	private String uname = "userAdditionalPermission" + System.currentTimeMillis();
	private String pwd = "webdriver";

	private static WebDriver driver = null;

	/*************************************
	 * TEST SET UP
	 *
	 * @throws DataGridException
	 *************************************/

	@BeforeClass
	public static void setUpBeforeClass() throws DataGridException {
		// UITest.setUpBeforeClass();
		driver = UiTestUtilities.getDriver();

		FileUtils.forceRemoveFilesFromDirAsAdmin("/" + UiTestUtilities.IRODS_ZONE, UiTestUtilities.TEST_FILE_NAMES);
		CollectionUtils.cleanUpCollectionsUnderZone(driver, UiTestUtilities.TEST_COLLECTION_NAMES);

		UiTestUtilities.login();
		for (String collName : UiTestUtilities.TEST_COLLECTION_NAMES) {
			CollectionUtils.createCollectionUnderZone(driver, collName, UiTestUtilities.IRODS_ZONE);
		}

		FileUtils.uploadToDirAsAdmin("/" + UiTestUtilities.IRODS_ZONE, UiTestUtilities.TEST_FILE_NAMES);
		UiTestUtilities.logout();
	}

	@Before
	public void setUp() throws Exception {
		UiTestUtilities.login();

	}

	/**
	 * After each test the user created for the test should be removed.
	 */
	@After
	public void tearDown() throws Exception {

		UiTestUtilities.logout();

		UserUtils.removeUser(uname, driver);
	}

	/**
	 * After all tests are done, the test must quit the driver. This will close
	 * every window associated with the current driver instance.
	 *
	 * @throws DataGridException
	 */

	@AfterClass
	public static void tearDownAfterClass() throws DataGridException {

		UiTestUtilities.login();
		FileUtils.forceRemoveFilesFromDirAsAdmin("/" + UiTestUtilities.IRODS_ZONE, UiTestUtilities.TEST_FILE_NAMES);
		CollectionUtils.cleanUpCollectionsUnderZone(driver, UiTestUtilities.TEST_COLLECTION_NAMES);
		UiTestUtilities.logout();

		if (driver != null) {
			driver.quit();
			driver = null;
			UiTestUtilities.setDriver(null);
		}
	}

	/*************************************
	 * RODS ADMIN TESTS
	 *************************************/

	/**
	 * Grant READ permission and a bookmark to a rods admin user on data objects,
	 * log in as such user and check if these data object show up in the bookmarks
	 * page.
	 */
	@Test
	public void testGrantReadPermissionAndBookmarkOnFilesToRodsAdminAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.READ_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_ADMIN_TYPE, UiTestUtilities.TEST_FILE_NAMES, driver);
	}

	/**
	 * Grant READ permission and a bookmark to a rods admin user on collections, log
	 * in as such user and check if these collections show up in the bookmarks page.
	 */
	@Test
	public void testGrantReadPermissionAndBookmarkOnCollsToRodsAdminAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.READ_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_ADMIN_TYPE, UiTestUtilities.TEST_COLLECTION_NAMES, driver);
	}

	/**
	 * Grant READ permission and a bookmark to a rods admin user on data objects,
	 * log in as such user and check if the data object show up in the bookmarks
	 * page.
	 */
	@Test
	public void testGrantWritePermissionAndBookmarkOnFilesToRodsAdminAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.WRITE_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_ADMIN_TYPE, UiTestUtilities.TEST_FILE_NAMES, driver);
	}

	/**
	 * Grant READ permission and a bookmark to a rods admin user on collections, log
	 * in as such user and check if these collections show up in the bookmarks page.
	 */
	@Test
	public void testGrantWritePermissionAndBookmarkOnCollsToRodsAdminAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.WRITE_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_ADMIN_TYPE, UiTestUtilities.TEST_COLLECTION_NAMES, driver);
	}

	/**
	 * Grant READ permission and a bookmark to a rods admin user on data objects,
	 * log in as such user and check if these data objects show up in the bookmarks
	 * page.
	 */
	@Test
	public void testGrantOwnPermissionAndBookmarkOnFilesToRodsAdminAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.OWN_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_ADMIN_TYPE, UiTestUtilities.TEST_FILE_NAMES, driver);
	}

	/**
	 * Grant WRITE permission and a bookmark to a rods admin user on collections,
	 * log in as such user and check if these collections show up in the bookmarks
	 * page.
	 */
	@Test
	public void testGrantOwnPermissionAndBookmarkOnCollsToRodsAdminAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.OWN_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_ADMIN_TYPE, UiTestUtilities.TEST_COLLECTION_NAMES, driver);
	}

	/*************************************
	 * RODS USER TESTS
	 *************************************/

	/**
	 * Grant READ permission and a bookmark to a rods user data objects, log in as
	 * such user and check if these data object shows up in the bookmarks page.
	 */
	@Test
	public void testGrantReadPermissionAndBookmarkOnFilesToRodsUserAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.READ_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_USER_TYPE, UiTestUtilities.TEST_FILE_NAMES, driver);
	}

	/**
	 * Grant READ permission and a bookmark to a rods user data objects, log in as
	 * such user and check if these data object shows up in the bookmarks page.
	 */
	@Test
	public void testGrantReadPermissionAndBookmarkOnCollsToRodsUserAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.READ_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_USER_TYPE, UiTestUtilities.TEST_COLLECTION_NAMES, driver);
	}

	/**
	 * Grant WRITE permission and a bookmark to a rods user on collections, log in
	 * as such user and check if these collections shows up in the bookmarks page.
	 */
	@Test
	public void testGrantWritePermissionAndBookmarkOnFilesToRodsUserAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.WRITE_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_USER_TYPE, UiTestUtilities.TEST_FILE_NAMES, driver);
	}

	/**
	 * Grant WRITE permission and a bookmark to a rods user on collections, log in
	 * as such user and check if these collections shows up in the bookmarks page.
	 */
	@Test
	public void testGrantWritePermissionAndBookmarkOnCollsToRodsUserAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.WRITE_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_USER_TYPE, UiTestUtilities.TEST_COLLECTION_NAMES, driver);
	}

	/**
	 * Grant OWN permission and a bookmark to a rods user data objects, log in as
	 * such user and check if the data object shows up in the bookmarks page.
	 */
	@Test
	public void testGrantOwnPermissionAndBookmarkOnFilesToRodsUserAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.OWN_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_USER_TYPE, UiTestUtilities.TEST_FILE_NAMES, driver);
	}

	/**
	 * Grant OWN permission and a bookmark to a rods user collections, log in as
	 * such user and check if these collections shows up in the bookmarks page.
	 */
	@Test
	public void testGrantOwnPermissionAndBookmarkOnCollsToRodsUserAndCheckIfBookmarksShowUp() {
		UserUtils.createUserGrantPermissionAndCheckBookmarks(UiTestUtilities.OWN_PERMISSION, uname, UiTestUtilities.IRODS_ZONE, pwd,
				UiTestUtilities.RODS_USER_TYPE, UiTestUtilities.TEST_COLLECTION_NAMES, driver);
	}
}
