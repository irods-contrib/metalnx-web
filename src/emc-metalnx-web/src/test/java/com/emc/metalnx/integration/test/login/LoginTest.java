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

package com.emc.metalnx.integration.test.login;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.test.generic.UITest;

public class LoginTest {

    private static final Logger logger = LoggerFactory.getLogger(LoginTest.class);

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
     * Tests a valid username and password for login, and checks if it moves to the dashboard page
     *
     * @throws Exception
     */
    @Test
    public void testValidUsernameAndPasswordForLogin() throws Exception {
        logger.info("Testing valid username and password for login");
        UITest.login(UITest.RODS_USERNAME, UITest.RODS_PASSWORD);

        // check if after login, the user is redirected to the dashboard page
        assertEquals(UITest.DASHBOARD_URL, driver.getCurrentUrl());

        UITest.logout();
    }

    /**
     * Tests an invalid username and password for login and checks if an
     * error is shown
     *
     * @throws Exception
     */
    @Test
    public void testInvalidUsernameAndPasswordForLogin() throws Exception {
        logger.info("Testing invalid username and password for login");

        UITest.login("ThisIsAnInvalidUsername", "ThisIsAnInvalidPassword");

        WebElement errorMsg = driver.findElement(By.className("errorMsg"));

        // check if after entering invalid login credentials (username and password),
        // an error message is shown
        Assert.assertTrue(errorMsg.isDisplayed());
    }
}
