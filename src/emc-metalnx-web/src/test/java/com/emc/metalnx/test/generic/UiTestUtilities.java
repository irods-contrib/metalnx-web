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

package com.emc.metalnx.test.generic;

import java.util.Properties;

import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.css.sac.CSSParseException;

import com.emc.metalnx.utils.EmcMetalnxVersion;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;

public class UiTestUtilities {

	private static final Logger logger = LoggerFactory.getLogger(UiTestUtilities.class);
	private static boolean isDevEnv = EmcMetalnxVersion.BUILD_NUMBER.equals("DEV");

	// to read properties from testing.properties file
	public static Properties testingProperties = new Properties();

	// Metalnx URL Connection parts
	public static final String http = "http://";
	// public static final String HOST = isDevEnv ? "localhost" :
	// "metalnx.localdomain";
	public static final String HOST = "localhost";
	public static final String PORT = "8080";
	public static final String URL_PREFIX = http + HOST + ":" + PORT;

	// Data Grid Info
	public static String RODS_USERNAME = TestingPropertiesHelper.IRODS_USER_KEY;
	public static String RODS_PASSWORD = TestingPropertiesHelper.IRODS_PASSWORD_KEY;
	public static String IRODS_ZONE = isDevEnv ? "tempZone" : "testZone";
	public static String IRODS_HOST = isDevEnv ? "icat.localdomain" : "icat.prod.localdomain";
	public static String DEFAULT_RESC = "demoResc";
	public static int IRODS_PORT = 1247;

	// Metalnx pages
	public static String LOGIN_URL = URL_PREFIX + "/emc-metalnx-web/login/";
	public static String LOGINERROR_URL = URL_PREFIX + "/emc-metalnx-web/login/exception/";
	public static String DASHBOARD_URL = URL_PREFIX + "/emc-metalnx-web/dashboard/";
	public static String RULES_URL = URL_PREFIX + "/emc-metalnx-web/rules/";
	public static String PREFERENCES_URL = URL_PREFIX + "/emc-metalnx-web/preferences/";
	public static String USERS_URL = URL_PREFIX + "/emc-metalnx-web/users/";
	public static String ADD_USERS_URL = URL_PREFIX + "/emc-metalnx-web/users/add/";
	public static String PROFILES_URL = URL_PREFIX + "/emc-metalnx-web/users/profile/";
	public static String ADD_PROFILES_URL = URL_PREFIX + "/emc-metalnx-web/users/profile/create/";
	public static String GROUPS_URL = URL_PREFIX + "/emc-metalnx-web/groups/";
	public static String ADD_GROUPS_URL = URL_PREFIX + "/emc-metalnx-web/groups/add/";
	public static String MODIFY_GROUPS_URL = URL_PREFIX + "/emc-metalnx-web/groups/modify/";
	public static String RESOURCES_URL = URL_PREFIX + "/emc-metalnx-web/resources/";
	public static String ADD_RESOURCES_URL = URL_PREFIX + "/emc-metalnx-web/resources/add/";
	public static String TEMPLATES_URL = URL_PREFIX + "/emc-metalnx-web/templates/";
	public static String ADD_TEMPLATES_URL = URL_PREFIX + "/emc-metalnx-web/templates/add/";
	public static String METADATA_SEARCH_URL = URL_PREFIX + "/emc-metalnx-web/metadata/";
	public static String COLLECTIONS_URL = URL_PREFIX + "/emc-metalnx-web/collections/";
	public static String TRASH_URL = URL_PREFIX + "/emc-metalnx-web/collections/trash/";
	public static String PUBLIC_URL = URL_PREFIX + "/emc-metalnx-web/collections/public/";
	public static String USER_BOOKMARKS_URL = URL_PREFIX + "/emc-metalnx-web/userBookmarks/";
	public static String GROUP_BOOKMARKS_URL = URL_PREFIX + "/emc-metalnx-web/groupBookmarks/groups/";
	public static String FAVORITES_URL = URL_PREFIX + "/emc-metalnx-web/favorites/";
	public static String TICKETS_URL = URL_PREFIX + "/emc-metalnx-web/tickets/";
	
	public static String LOGOUT_URL = URL_PREFIX + "/emc-metalnx-web/logout/";
	public static String RESOURCES_MAP_URL = URL_PREFIX + "/emc-metalnx-web/resources/map/";
	public static String RESOURCES_SERVERS_URL = URL_PREFIX + "/emc-metalnx-web/resources/servers/";
	public static String HTTP_ERROR_500_URL = URL_PREFIX + "/emc-metalnx-web/httpError/500/";
	public static String MY_GROUPS_PAGE = URL_PREFIX + "/emc-metalnx-web/groupBookmarks/groups/";
	
	
	// metalnx page header locator
	public static By dashboardHdrLocator = By.cssSelector(".page-header.pull-left");
	
	// metalnx page header text
	public static String DASHBOARD_HDR = "Dashboard";
	public static String RESOURCES_HDR = "Resources";
	public static String RULES_HDR = "Rules";
	public static String USERS_HDR = "Users";
	public static String GROUPS_HDR = "Groups";
	public static String PROFILES_HDR = "Profiles";
	public static String COLLECTIONS_HDR = "Collections";
	public static String SEARCH_HDR = "Search";
	public static String TEMPLATES_HDR = "Templates";
	public static String SHARED_LINKS_HDR = "Shared";
	public static String FAVORITES_HDR = "Favorites";
	public static String TICKETS_HDR = "Tickets";
	public static String PUBLIC_HDR = "Collections";
	public static String TRASH_HDR = "Collections";
	
	// permission types used in the tests
	public static final String READ_PERMISSION = "read";
	public static final String WRITE_PERMISSION = "write";
	public static final String OWN_PERMISSION = "own";
	public static final String NONE_PERMISSION = "none";

	// user types used in the tests
	public static final String RODS_ADMIN_TYPE = "rodsadmin";
	public static final String RODS_USER_TYPE = "rodsuser";

	private static WebDriver driver = null;

	// driver used for testing
	public static final String FIREFOX = "FIREFOX";
	public static final String CHROME = "CHROME";

	public static String[] TEST_FILE_NAMES = { "1SeleniumTestUserAdditionalPermission.png" };

	public static String[] TEST_COLLECTION_NAMES = { "SeleniumTestAdditionalPermCol" };

	public static final String CHROME_DRIVER = "selenium.test.chrome.driver";
	public static final String CHROME_DRIVER_LOCATION = "selenium.test.chrome.driver.loaction";

	public static void init() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();

		// driver used for testing
		driver = getDriver();
	}

	/**
	 * Logs the rods user into the UI.
	 */
	public static void login() {
		login(UiTestUtilities.testingProperties.getProperty(TestingPropertiesHelper.IRODS_USER_KEY), UiTestUtilities.testingProperties.getProperty(TestingPropertiesHelper.IRODS_PASSWORD_KEY));
	}

	/**
	 * Logs in the application using the username and password given as parameters.
	 *
	 * @param uname
	 *            username
	 */
	public static void login(String uname, String pwd) {
		logger.info("Logging into Metalnx using " + uname + " and " + pwd);

		if (driver == null) {
			Assert.fail("No driver found.");
		}

		driver.get(LOGIN_URL);

		new WebDriverWait(driver, 15).until(ExpectedConditions.visibilityOfElementLocated(By.id("inputUsernameLogin")));
		
		Assert.assertEquals(LOGIN_URL, driver.getCurrentUrl());

		WebElement inputUsername = driver.findElement(By.id("inputUsernameLogin"));
		WebElement inputPassword = driver.findElement(By.id("inputPasswordLogin"));
		WebElement inputSubmit = driver.findElement(By.id("loginBtn"));

		inputUsername.sendKeys(uname);
		inputPassword.sendKeys(pwd);
		inputSubmit.click();
	}

	public static void logout() {

		logger.info("Logging out of Metalnx");
		if (driver == null) {
			return;
		}

		getDriver().get(LOGOUT_URL);
		//getDriver().get(LOGIN_URL);
		//new WebDriverWait(driver, 15).until(ExpectedConditions.visibilityOfElementLocated(By.id("inputUsernameLogin")));
		
	}

	public static WebDriver getDriver() {
		System.setProperty(testingProperties.getProperty(CHROME_DRIVER),
				testingProperties.getProperty(CHROME_DRIVER_LOCATION));
		// return driver == null ? new FirefoxDriver() : driver;
		return driver == null ? new ChromeDriver() : driver;
	}

	/**
	 * @param driver
	 *            the driver to set
	 */
	public static void setDriver(WebDriver driver) {
		UiTestUtilities.driver = driver;
	}

	// class that removes the amount of logs displayed in the console by validating
	// CSS
	protected class SilentHtmlUnitDriver extends HtmlUnitDriver {
		SilentHtmlUnitDriver() {
			super(BrowserVersion.FIREFOX_24);
			setJavascriptEnabled(true);
			getWebClient().setCssErrorHandler(new SilentCssErrorHandler());
		}

		public void warning(CSSParseException e) {
			// no warning logs
		}

		public void error(CSSParseException e) {
			// no error logs
		}
	}

	public static String getModifyGroupsPage(String gname) {
		return String.format("%s%s/%s/", MODIFY_GROUPS_URL, gname, IRODS_ZONE);
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return HOST;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return PORT;
	}

	/**
	 * @return the loginPage
	 */
	public String getLoginPage() {
		return LOGIN_URL;
	}

	/**
	 * @return the dashboardPage
	 */
	public String getDashboardPage() {
		return DASHBOARD_URL;
	}

	/**
	 * @return the preferencesPage
	 */
	public String getPreferencesPage() {
		return PREFERENCES_URL;
	}

	/**
	 * @return the usersPage
	 */
	public String getUsersPage() {
		return USERS_URL;
	}

	/**
	 * @return the addUsersPage
	 */
	public String getAddUsersPage() {
		return ADD_USERS_URL;
	}

	/**
	 * @return the addProfilesPage
	 */
	public String getAddProfilesPage() {
		return ADD_PROFILES_URL;
	}

	/**
	 * @return the profilesPage
	 */
	public String getProfilesPage() {
		return PROFILES_URL;
	}

	/**
	 * @return the groupsPage
	 */
	public String getGroupsPage() {
		return GROUPS_URL;
	}

	/**
	 * @return the addGroupsPage
	 */
	public String getAddGroupsPage() {
		return ADD_GROUPS_URL;
	}

	/**
	 * @return the resourcesPage
	 */
	public String getResourcesPage() {
		return RESOURCES_URL;
	}

	/**
	 * @return the addResourcesPage
	 */
	public String getAddResourcesPage() {
		return ADD_RESOURCES_URL;
	}

	/**
	 * @return the irodsHost
	 */
	public String getIrodsHost() {
		return IRODS_HOST;
	}

	/**
	 * @return the templatesPage
	 */
	public String getTemplatesPage() {
		return TEMPLATES_URL;
	}

	/**
	 * @return the addTemplatesPage
	 */
	public String getAddTemplatesPage() {
		return ADD_TEMPLATES_URL;
	}

	/**
	 * @return the metadataSearchPage
	 */
	public String getMetadataSearchPage() {
		return METADATA_SEARCH_URL;
	}

	/**
	 * @return the collectionsPage
	 */
	public String getCollectionsPage() {
		return COLLECTIONS_URL;
	}

	/**
	 * @return the userBookmarks
	 */
	public String getUserBookmarks() {
		return USER_BOOKMARKS_URL;
	}

	/**
	 * @return the logout
	 */
	public String getLogout() {
		return LOGOUT_URL;
	}

	/**
	 * @return the zone
	 */
	public String getZone() {
		return IRODS_ZONE;
	}
}
