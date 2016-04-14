package com.emc.metalnx.integration.test.templates;

import junit.framework.Assert;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.integration.test.utils.TemplateUtils;
import com.emc.metalnx.test.generic.UITest;

/**
 * Test adding a long attribute field to a template.
 *
 */
public class LongTemplateAVUFieldsTest {
    private static WebDriver driver = null;
    private static String templateName = null;

    /************************************* TEST SET UP *************************************/

    @BeforeClass
    public static void setUpBeforeClass() throws DataGridException {
        UITest.setUpBeforeClass();
        driver = UITest.getDriver();

        try {
            UITest.login();
            driver.get(UITest.TEMPLATES_URL);
            TemplateUtils.removeAllTemplates(driver);
        }
        catch (Exception e) {}
        finally {
            UITest.logout();
        }
    }

    @Before
    public void setUp() throws Exception {
        templateName = RandomStringUtils.randomAlphanumeric(60) + System.currentTimeMillis();
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
        UITest.login();
        driver.get(UITest.TEMPLATES_URL);
        TemplateUtils.removeAllTemplates(driver);
        UITest.logout();

        if (driver != null) {
            driver.quit();
            driver = null;
            UITest.setDriver(null);
        }
    }

    @Test
    public void testSystemTemplateAVUsWith100Characters() {
        String[] attrs = { RandomStringUtils.randomAlphanumeric(100) };
        String[] values = { RandomStringUtils.randomAlphanumeric(100) };
        String[] units = { RandomStringUtils.randomAlphanumeric(100) };

        TemplateUtils.createTemplateWithFields(driver, templateName, TemplateUtils.SYSTEM_TEMPLATE_TYPE, attrs, values, units);
        TemplateUtils.assertTemplateSuccessfulCreation(driver, templateName);

        TemplateUtils.searchByTemplateName(driver, templateName);
        Assert.assertNotNull(driver.findElement(By.cssSelector("#templatesListTable tbody tr td[title='" + templateName + "']")));
    }

    @Test
    public void testPrivateTemplateAVUsWith100Characters() {
        String[] attrs = { RandomStringUtils.randomAlphanumeric(100) };
        String[] values = { RandomStringUtils.randomAlphanumeric(100) };
        String[] units = { RandomStringUtils.randomAlphanumeric(100) };

        TemplateUtils.createTemplateWithFields(driver, templateName, TemplateUtils.PRIVATE_TEMPLATE_TYPE, attrs, values, units);
        TemplateUtils.assertTemplateSuccessfulCreation(driver, templateName);

        TemplateUtils.searchByTemplateName(driver, templateName);
        Assert.assertNotNull(driver.findElement(By.cssSelector("#templatesListTable tbody tr td[title='" + templateName + "']")));
    }
}
