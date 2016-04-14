package com.emc.metalnx.integration.test.datatables;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.UserUtils;
import com.emc.metalnx.test.generic.UITest;

/**
 * Class that tests the application of a template with metadata fields on files.
 */
public class PageSizeSelectorRodsUserTest {

    private static WebDriver driver = null;
    private static String uname = "PageSizeUser" + System.currentTimeMillis();
    private static String pwd = "PageSize";

    /************************************* TEST SET UP *************************************/

    @BeforeClass
    public static void setUpBeforeClass() throws DataGridException {
        UITest.setUpBeforeClass();
        driver = UITest.getDriver();
        UserUtils.createUser(uname, pwd, UITest.RODS_USER_TYPE, driver);
    }

    @Before
    public void setUp() throws Exception {
        UITest.login(uname, pwd);
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
            UserUtils.removeUser(uname, driver);
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
     * Method that the page size selector component on the trash page.
     */
    @Test
    public void testPageSizeSelectorOnTrashPage() {
        for (String ps : PageSizeSelectorUtils.PAGE_SIZES) {
            PageSizeSelectorUtils.assertPageSize(driver, UITest.TRASH_URL, ps);
        }
    }

    /**
     * Method that the page size selector component on the trash page.
     */
    @Test
    public void testPageSizeSelectorOnPublicPage() {
        for (String ps : PageSizeSelectorUtils.PAGE_SIZES) {
            PageSizeSelectorUtils.assertPageSize(driver, UITest.PUBLIC_URL, ps);
        }
    }
}
