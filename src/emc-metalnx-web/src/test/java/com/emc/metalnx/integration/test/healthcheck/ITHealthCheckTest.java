package com.emc.metalnx.integration.test.healthcheck;

import java.util.Properties;

import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.integration.test.utils.HealthCheckUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;

import junit.framework.Assert;


public class ITHealthCheckTest {
	private static final Logger logger = LoggerFactory.getLogger(ITHealthCheckTest.class);
	private static WebDriver driver = null;
	private static WebDriverWait wait = null;

	private static Properties testingProperties = new Properties();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "ITHealthCheckTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		org.irods.jargon.testutils.TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
		UiTestUtilities.init();
		driver = UiTestUtilities.getDriver();
		wait = new WebDriverWait(driver, 5);
		UiTestUtilities.login();
	}

	@Before
	public void setUp() {
		//UiTestUtilities.login();

	}

	@After
	public void tearDown() {
		//UiTestUtilities.logout();

	}

	@AfterClass
	public static void tearDownAfterClass() {
		if (driver != null) {
			driver.quit();
			driver = null;
			UiTestUtilities.setDriver(null);
			irodsFileSystem.closeAndEatExceptions();
			UiTestUtilities.logout();
		}
	}
	
	/**
	 * Test fetches the web page url and compares the page header
	 */
	@Test
	public void testGotoDashboard() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.DASHBOARD_URL);
		logger.info("Expected Header: " + UiTestUtilities.DASHBOARD_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.DASHBOARD_HDR, pageHeader);
	}
	
	@Test
	public void testGotoResources() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.RESOURCES_URL);
		logger.info("Expected Header: " + UiTestUtilities.RESOURCES_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.RESOURCES_HDR, pageHeader);
	}
	
	@Test
	public void testGotoRules() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.RULES_URL);
		logger.info("Expected Header: " + UiTestUtilities.RULES_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.RULES_HDR, pageHeader);
	}
	
	@Test
	public void testGotoUsers() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.USERS_URL);
		logger.info("Expected Header: " + UiTestUtilities.USERS_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.USERS_HDR, pageHeader);
	}
	
	@Test
	public void testGotoGroups() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.GROUPS_URL);
		logger.info("Expected Header: " + UiTestUtilities.GROUPS_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.GROUPS_HDR, pageHeader);
	}
	
	@Test
	public void testGotoProfile() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.PROFILES_URL);
		logger.info("Expected Header: " + UiTestUtilities.PROFILES_HDR+ " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.PROFILES_HDR, pageHeader);
	}
	
	@Test
	public void testGotoCollections() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.COLLECTIONS_URL);
		logger.info("Expected Header: " + UiTestUtilities.COLLECTIONS_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.COLLECTIONS_HDR, pageHeader);
	}
	
	@Test
	public void testGotoSearch() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.METADATA_SEARCH_URL);
		logger.info("Expected Header: " + UiTestUtilities.SEARCH_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.SEARCH_HDR, pageHeader);
	}
	
	@Test
	public void testGotoTemplates() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.TEMPLATES_URL);
		logger.info("Expected Header: " + UiTestUtilities.TEMPLATES_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.TEMPLATES_HDR, pageHeader);
	}
	
	@Test
	public void testGotoFavorites() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.USER_BOOKMARKS_URL);
		logger.info("Expected Header: " + UiTestUtilities.SHARED_LINKS_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.SHARED_LINKS_HDR, pageHeader);
	}
	
	@Test
	public void testGotoTickets() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.TICKETS_URL);
		logger.info("Expected Header: " + UiTestUtilities.TICKETS_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.TICKETS_HDR, pageHeader);
	}
	
	@Test
	public void testGotoPublic() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.PUBLIC_URL);
		logger.info("Expected Header: " + UiTestUtilities.PUBLIC_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.PUBLIC_HDR, pageHeader);
	}
	
	@Ignore
	@Test
	public void testGotoTrash() {
		String pageHeader = HealthCheckUtils.getPageHeader(driver, wait, UiTestUtilities.TRASH_URL);
		logger.info("Expected Header: " + UiTestUtilities.TRASH_HDR + " Actual Header: " + pageHeader );
		Assert.assertEquals(UiTestUtilities.TRASH_HDR, pageHeader);
	}
	
	

}
