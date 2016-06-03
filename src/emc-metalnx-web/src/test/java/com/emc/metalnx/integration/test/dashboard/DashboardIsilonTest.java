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

package com.emc.metalnx.integration.test.dashboard;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.ResourceUtils;
import com.emc.metalnx.test.generic.UITest;

public class DashboardIsilonTest {

    private static WebDriver driver = null;
    private static WebDriverWait wait = null;

    private static final String RESOURCE_NAME = "fakeIsilonResc";
    private static final String RESOURCE_PATH = "/data/isilonVault1";
    private static final String ISILON_HOST = "192.168.11.2";
    private static final String ISILON_PORT = "8020";
    private static final String ISILON_USER = "root";

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        UITest.setUpBeforeClass();
        driver = UITest.getDriver();
    }

    @Before
    public void setUp() throws Exception {
        wait = new WebDriverWait(driver, 8);
        UITest.login();
        ResourceUtils.addIsilonResource(RESOURCE_NAME, null, UITest.IRODS_ZONE, RESOURCE_PATH, ISILON_HOST, ISILON_PORT, ISILON_USER, driver);
        UITest.logout();
    }

    @Test
    public void testIsilonRescOnDashboard() {
        UITest.login();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("dashboardServersList")));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.linkText("localhost")));
        UITest.logout();
    }

    @After
    public void tearDown() throws Exception {
        UITest.login();
        ResourceUtils.removeResource(RESOURCE_NAME, driver);
        UITest.logout();
    }

    @AfterClass
    public static void tearDownAfterClass() throws DataGridException {
        if (driver != null) {
            driver.quit();
            driver = null;
            UITest.setDriver(null);
        }
    }

}
