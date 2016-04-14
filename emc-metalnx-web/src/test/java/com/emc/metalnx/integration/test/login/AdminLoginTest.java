package com.emc.metalnx.integration.test.login;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.test.generic.UITest;

public class AdminLoginTest {

    private static final Logger logger = LoggerFactory.getLogger(AdminLoginTest.class);

    private static WebDriver driver = null;

    /************************************* TEST SET UP *************************************/

    @BeforeClass
    public static void setUpBeforeClass() {
        UITest.setUpBeforeClass();
        driver = UITest.getDriver();
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

    /**
     * Test for admin log in.
     *
     * @throws Exception
     */
    @Test
    public void testLoginAsAdmin() throws Exception {
        logger.info("Test logging in as a admin user (rods, irods@1234)");
        UITest.login("rods", "irods@1234");
        Assert.assertEquals(UITest.DASHBOARD_URL, driver.getCurrentUrl());
    }

}
