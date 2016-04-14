package com.emc.metalnx.integration.test.templates;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.integration.test.utils.TemplateUtils;
import com.emc.metalnx.test.generic.UITest;

public class TemplateTest {

    private static final Logger logger = LoggerFactory.getLogger(TemplateTest.class);

    private static WebDriver driver = null;
    private String templateName = null;

    /************************************* TEST SET UP *************************************/

    @BeforeClass
    public static void setUpBeforeClass() {
        UITest.setUpBeforeClass();
        driver = UITest.getDriver();
    }

    @Before
    public void setUp() throws Exception {
        templateName = TemplateUtils.TEMPLATE_TEST_NAME + System.currentTimeMillis();
        UITest.login();
    }

    @After
    public void tearDown() throws Exception {
        TemplateUtils.removeTemplate(templateName, driver);
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
     * Test method for adding a brand new private template with no metadata fields.
     * It verifies if after adding a template, the user is redirected to the template mgmt. page and
     * if this template was created successfully;
     */
    @Test
    public void testAddPrivateTemplateWithNoMetadataFields() {
        logger.info("Testing add a new private template with no metadata fields");
        TemplateUtils.createTemplateWithNoFields(driver, templateName, TemplateUtils.PRIVATE_TEMPLATE_TYPE);
        TemplateUtils.assertTemplateSuccessfulCreation(driver, templateName);
    }

    /**
     * Test method for adding a brand new public template with no metadata fields.
     * It verifies if after adding a template, the user is redirected to the template mgmt. page and
     * if this template was created successfully;
     */
    @Test
    public void testAddSystemTemplateWithNoMetadataFields() {
        logger.info("Testing add a new public template with no metadata fields");
        TemplateUtils.createTemplateWithNoFields(driver, templateName, TemplateUtils.SYSTEM_TEMPLATE_TYPE);
        TemplateUtils.assertTemplateSuccessfulCreation(driver, templateName);
    }

    /**
     * Test method for adding a brand new private template with metadata fields.
     * It verifies if after adding a template, the user is redirected to the template mgmt. page and
     * if this template was created successfully;
     */
    @Test
    public void testAddPrivateTemplateWithMetadataFields() {
        logger.info("Testing add a new private template with metadata fields");
        TemplateUtils.createTemplateWithFields(driver, templateName, TemplateUtils.PRIVATE_TEMPLATE_TYPE);
    }

    /**
     * Test method for adding a brand new public template with metadata fields.
     * It verifies if after adding a template, the user is redirected to the template mgmt. page and
     * if this template was created successfully;
     */
    @Test
    public void testAddSystemTemplateWithMetadataFields() {
        logger.info("Testing add a new public template with metadata fields");
        TemplateUtils.createTemplateWithFields(driver, templateName, TemplateUtils.SYSTEM_TEMPLATE_TYPE);
    }
}
