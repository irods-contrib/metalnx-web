package com.emc.metalnx.integration.test.datatables;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.test.generic.UITest;

/**
 * Class that tests the application of a template with metadata fields on files.
 */
public class PageSizeSelectorTest {

    private static WebDriver driver = null;

    /************************************* TEST SET UP *************************************/

    @BeforeClass
    public static void setUpBeforeClass() throws DataGridException {
        UITest.setUpBeforeClass();
        driver = UITest.getDriver();
    }

    @Before
    public void setUp() throws Exception {
        UITest.login();
    }

    @After
    public void tearDown() throws Exception {
        UITest.logout();
    }

    /**
     * After all tests are done, the test must quit the driver. This will close every window
     * associated with the current driver instance.
     */

    @AfterClass
    public static void tearDownAfterClass() {
        if (driver != null) {
            driver.quit();
            driver = null;
            UITest.setDriver(null);
        }
    }

    /************************************* TESTS *************************************/

    /**
     * Method that the page size selector component on the collections page.
     */
    @Test
    public void testPageSizeSelectorOnCollsPage() {
        for (String ps : PageSizeSelectorUtils.PAGE_SIZES) {
            PageSizeSelectorUtils.assertPageSize(driver, UITest.COLLECTIONS_URL, ps);
        }
    }

    /**
     * Method that the page size selector component on the users page.
     */
    @Test
    public void testPageSizeSelectorOnUsersPage() {
        for (String ps : PageSizeSelectorUtils.PAGE_SIZES) {
            PageSizeSelectorUtils.assertPageSize(driver, UITest.USERS_URL, ps);
        }
    }

    /**
     * Method that the page size selector component on the groups page.
     */
    @Test
    public void testPageSizeSelectorOnGroupsPage() {
        for (String ps : PageSizeSelectorUtils.PAGE_SIZES) {
            PageSizeSelectorUtils.assertPageSize(driver, UITest.GROUPS_URL, ps);
        }
    }

    /**
     * Method that the page size selector component on the resources page.
     */
    @Test
    public void testPageSizeSelectorOnResourcesPage() {
        for (String ps : PageSizeSelectorUtils.PAGE_SIZES) {
            PageSizeSelectorUtils.assertPageSize(driver, UITest.RESOURCES_URL, ps);
        }
    }
}
