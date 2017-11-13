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

package com.emc.metalnx.integration.test.utils;

import com.emc.metalnx.test.generic.UiTestUtilities;
import org.junit.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;


public class TemplateUtils {
    private static final Logger logger = LoggerFactory.getLogger(TemplateUtils.class);

    public static final int TOTAL_METADATA_FIELDS = 5;

    public static final String TEMPLATE_TEST_NAME = "templateTest";
    public static final String TEMPLATE_TEST_DESC = "template description test";
    public static final String TEMPLATE_USE_INFO = "template use info";

    public static final String PRIVATE_TEMPLATE_TYPE = "private";
    public static final String SYSTEM_TEMPLATE_TYPE = "system";

    public static final String attribute = "attrTest";
    public static final String value = "valueTest";
    public static final String unit = "unitTest";
    public static final String[] TEST_FILES = { "applyTemplateTest.png" };
    public static final String[] TEST_COLLS = { "applyTemplateTestColl" + System.currentTimeMillis() };

    public static final By TEMPLATES_LIST_TABLE = By.id("templatesListTable");
    public static final By LIST_TEMPLATE_FIELDS_BTN = By.id("listTemplateFieldsBtn");
    public static final By SUBMIT_TEMPLATE_BTN = By.id("submitTemplateBtn");

    /**
     * Searches for a template by its name.
     *
     * @param driver
     * @param template
     *            name of the template name to be found
     */
    public static void searchByTemplateName(WebDriver driver, String template) {
        driver.get(UiTestUtilities.TEMPLATES_URL);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("templatesListTable_filter")));
        driver.findElement(By.cssSelector("#templatesListTable_filter input[type='search']")).click();
        driver.findElement(By.cssSelector("#templatesListTable_filter input[type='search']")).clear();
        driver.findElement(By.cssSelector("#templatesListTable_filter input[type='search']")).sendKeys(template);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#templateListAsync tbody")));
    }

    /**
     * Removes all existing templates from Metalnx.
     *
     * @param driver
     */
    public static void removeAllTemplates(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 10);

        By delTemplatesCb = By.cssSelector("#templatesListTable input[name='selectAllCheckboxes']");
        By remoteTemplatesBtn = By.id("removeTemplatesBtn");
        By remoteTemplatesConfBtn = By.cssSelector("#removalModal .btn-primary");

        driver.get(UiTestUtilities.TEMPLATES_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(delTemplatesCb));
        driver.findElement(delTemplatesCb).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(remoteTemplatesBtn));
        driver.findElement(remoteTemplatesBtn).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(remoteTemplatesConfBtn));
        driver.findElement(remoteTemplatesConfBtn).click();
    }

    /**
     * Creates a template in Metalnx.
     *
     * @param template
     *            name of the template being created
     * @param desc
     *            template description
     * @param desc
     *            template usage information
     * @param templateType
     *            type of the template (System or Private)
     * @param driver
     */
    public static void createTemplateWithNoFields(String template, String desc, String useInfo, String templateType, WebDriver driver) {
        logger.info("Creating a template without any metadata fields");
        driver.get(UiTestUtilities.ADD_TEMPLATES_URL);
        fillInTemplateInformation(template, desc, useInfo, templateType, driver);
        submitTemplateForm(driver);
    }

    public static void createTemplateWithNoFields(WebDriver driver, String template, String templateType) {
        createTemplateWithNoFields(template, TEMPLATE_TEST_DESC, TEMPLATE_USE_INFO, templateType, driver);
    }

    /**
     * Method used for adding a brand new system or private template with metadata fields.
     * It verifies if after adding a template, the user is redirected to the template mgmt. page and
     * if this template was created successfully;
     */
    public static void createTemplateWithFields(WebDriver driver, String template, String templateType) {
        driver.get(UiTestUtilities.ADD_TEMPLATES_URL);
        addFieldsToTemplate(driver);
        fillInTemplateInformation(template, TEMPLATE_TEST_DESC, TEMPLATE_USE_INFO, templateType, driver);
        TemplateUtils.submitTemplateForm(driver);
        TemplateUtils.assertTemplateSuccessfulCreation(driver, template);
    }

    /**
     * Method used for adding a brand new system or private template with the given metadata fields.
     * It verifies if after adding a template, the user is redirected to the template mgmt. page and
     * if this template was created successfully;
     */
    public static void createTemplateWithFields(WebDriver driver, String template, String templateType, String[] attrs, String[] values,
            String[] units) {
        driver.get(UiTestUtilities.ADD_TEMPLATES_URL);
        addFieldsToTemplate(driver, attrs, values, units);
        fillInTemplateInformation(template, TEMPLATE_TEST_DESC, TEMPLATE_USE_INFO, templateType, driver);
        TemplateUtils.submitTemplateForm(driver);
        TemplateUtils.assertTemplateSuccessfulCreation(driver, template);
    }

    /**
     * Removes the template created for testing purposes.
     *
     * @param template
     *            name of the template that will be removed
     * @throws Exception
     */
    public static void removeTemplate(String templateName, WebDriver driver) throws Exception {
        logger.info("Removing template {}", templateName);

        WebDriverWait wait = new WebDriverWait(driver, 15);

        searchByTemplateName(driver, templateName);

        wait.until(ExpectedConditions.elementToBeClickable(By.id("templatesListTable")));
        String cssSelector = "#" + templateName + " input[name=selectedTemplates]";
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(cssSelector)));
        driver.findElement(By.cssSelector(cssSelector)).click();
        driver.findElement(By.id("removeTemplatesBtn")).click();

        // wait for the template removal confirmation modal to show up
        wait.until(ExpectedConditions.elementToBeClickable(By.id("removeTemplatesConfBtn")));
        driver.findElement(By.id("removeTemplatesConfBtn")).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".alert-success")));
    }

    /**
     * Method used for filling in template information when creating a template.
     *
     * @param templateAccessType
     *            template type being added (system or private)
     */
    public static void fillInTemplateInformation(String templateName, String templateDescription, String templateUseInfo, String templateAccessType,
            WebDriver driver) {
        driver.findElement(By.id("templateName")).click();
        driver.findElement(By.id("templateName")).clear();
        driver.findElement(By.id("templateName")).sendKeys(templateName);
        driver.findElement(By.id("templateDescription")).click();
        driver.findElement(By.id("templateDescription")).clear();
        driver.findElement(By.id("templateDescription")).sendKeys(templateDescription);
        new Select(driver.findElement(By.id("templateAccessType"))).selectByValue(templateAccessType);
    }

    /**
     * Checks if after adding a Template, if the system returns to the template mgmt. screen and a
     * feedback message is displayed.
     *
     * @param driver
     */
    public static void assertTemplateSuccessfulCreation(WebDriver driver, String template) {
        new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(By.id("templatesListTable")));
        Assert.assertEquals(UiTestUtilities.TEMPLATES_URL, driver.getCurrentUrl());
        WebElement divAlertSucess = driver.findElement(By.className("alert-success"));
        assertTrue(divAlertSucess.isDisplayed());
        assertTrue(divAlertSucess.getText().contains(template));
    }

    /**
     * Submits the template form.
     *
     * @param driver
     */
    public static void submitTemplateForm(WebDriver driver) {
        driver.findElement(By.id("submitTemplateFormBtn")).click();
    }

    /**
     * Checks if a list of AVUs were added to a file.
     *
     * @param driver
     * @param items
     *            files or collections to check metadata
     * @param attributes
     *            list of attributes to check
     * @param values
     *            list of values to check
     * @param units
     *            list of units to check
     */
    public static void checkIfMetadataWasAdded(WebDriver driver, boolean isCollection, List<String> attributes, List<String> values,
            List<String> units, String... items) {

        WebDriverWait wait = new WebDriverWait(driver, 10);
        driver.get(UiTestUtilities.COLLECTIONS_URL);

        CollectionUtils.waitForItemToLoad(driver, items[items.length - 1]);
        for (String item : items) {
            driver.findElement(By.cssSelector("#treeViewTable_filter input")).clear();
            driver.findElement(By.cssSelector("#treeViewTable_filter input")).sendKeys(item);
            driver.findElement(CollectionUtils.getFileLocatorUnderRodsHome(item)).click();

            MetadataUtils.openMetadataTab(driver);

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#metadaTable tbody tr:nth-child(5)")));
            List<String> attributesFromMlx = convertWebElementListToString(driver.findElements(MetadataUtils.METADATA_ATTR_COL));
            List<String> valuesFromMlx = convertWebElementListToString(driver.findElements(MetadataUtils.METADATA_VAL_COL));
            List<String> unitsFromMlx = convertWebElementListToString(driver.findElements(MetadataUtils.METADATA_UNIT_COL));

            for (int i = 0; i < TemplateUtils.TOTAL_METADATA_FIELDS; i++) {
                Assert.assertTrue(attributesFromMlx.contains(attributes.get(i)));
                Assert.assertTrue(valuesFromMlx.contains(values.get(i)));
                Assert.assertTrue(unitsFromMlx.contains(units.get(i)));
            }
        }
    }

    /**
     * Auxiliar method that finds a given template to be applied against a file.
     *
     * @param files
     *            files' names the template will be applied
     * @param template
     *            template name
     */
    public static void findTemplateToApply(WebDriver driver, String template, String... files) {

        WebDriverWait wait = new WebDriverWait(driver, 10);

        By waitCondition = By.cssSelector("#treeViewTable tbody tr td a span");
        wait.until(ExpectedConditions.visibilityOfElementLocated(waitCondition));

        for (String f : files) {
            driver.findElement(By.id(f)).click();

            CollectionUtils.waitForSelectActionBtnToBeEnabled(driver);
            driver.findElement(CollectionUtils.SELECT_ACTION_BTN).click();

            CollectionUtils.waitForActionsDropdownToBeShown(driver);
            driver.findElement(CollectionUtils.APPLY_TEMPLATE_BTN).click();

            waitForTemplatesToBeLoadedUnderCollsPage(driver);
            driver.findElement(By.id(template)).click();

            wait.until(ExpectedConditions.elementToBeClickable(TemplateUtils.LIST_TEMPLATE_FIELDS_BTN));
            driver.findElement(TemplateUtils.LIST_TEMPLATE_FIELDS_BTN).click();
        }
    }

    /**
     * Converts a list of Web elements to a set of strings.
     *
     * @param webElements
     *            web elements to be converted
     * @return {@link List} with the text present in all elements
     */
    public static List<String> convertWebElementListToString(List<WebElement> webElements) {
        List<String> elements = new ArrayList<String>();

        if (webElements != null) {
            for (WebElement e : webElements) {
                String text = e.getText();
                if (text != null && !text.isEmpty()) {
                    elements.add(text);
                }
            }
        }

        return elements;
    }

    /**
     * Submits a template to be applied.
     */
    public static void submitApplyTemplateForm(WebDriver driver) {
        By submitTemplateBtn = By.id("submitTemplateBtn");
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(submitTemplateBtn));
        driver.findElement(submitTemplateBtn).click();
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.cssSelector("#treeViewTable tbody tr td")));
    }

    /**
     * Checks if a success message is displayed after applying a template.
     *
     * @param driver
     */
    public static void isSuccessMessageShown(WebDriver driver) {
        waitForAlertToBeShown(driver);
        Assert.assertNotNull(driver.findElement(By.className("alert-success")));
    }

    /**
     * Waits for the templates table to be loaded under the collections page.
     *
     * @param driver
     */
    public static void waitForTemplatesToBeLoadedUnderCollsPage(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.visibilityOfElementLocated(TemplateUtils.TEMPLATES_LIST_TABLE));
    }

    /**
     * Waits for the templates table to be loaded under the collections page.
     *
     * @param driver
     */
    public static void waitForTemplatesToBeLoaded(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.visibilityOfElementLocated(TemplateUtils.TEMPLATES_LIST_TABLE));
    }

    /**
     * Waits for an alert box to be shown after an apply template operation.
     *
     * @param driver
     */
    public static void waitForAlertToBeShown(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#applyTemplateSuccess")));
    }

    /**
     * Adds the given AVUs to a template.
     */
    public static void addFieldsToTemplate(WebDriver driver, String[] attrs, String[] values, String[] units) {
        WebDriverWait wait = new WebDriverWait(driver, 15);

        By inputAttr = By.id("attribute");
        By inputValue = By.id("value");
        By inputUnit = By.id("unit");
        By addAttributeBtn = By.id("addAttributeBtn");

        // adding metadata fields to the template
        for (int i = 0; i < attrs.length; i++) {
            wait.until(ExpectedConditions.elementToBeClickable(addAttributeBtn));
            driver.findElement(addAttributeBtn).click();

            wait.until(ExpectedConditions.elementToBeClickable(inputAttr));
            driver.findElement(inputAttr).click();
            driver.findElement(inputAttr).clear();
            driver.findElement(inputAttr).sendKeys(attrs[i] + i);

            driver.findElement(inputValue).click();
            driver.findElement(inputValue).clear();
            driver.findElement(inputValue).sendKeys(values[i] + i);

            driver.findElement(inputUnit).click();
            driver.findElement(inputUnit).clear();
            driver.findElement(inputUnit).sendKeys(units[i] + i);

            driver.findElement(By.id("addFieldBtn")).click();

            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("newTemplateAVU")));

            driver.get(driver.getCurrentUrl());
            wait.until(ExpectedConditions.elementToBeClickable(addAttributeBtn));
        }
    }

    /**
     * Adds several metadata fields to a template.
     */
    public static void addFieldsToTemplate(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 15);

        By inputAttr = By.id("attribute");
        By inputValue = By.id("value");
        By inputUnit = By.id("unit");
        By addAttributeBtn = By.id("addAttributeBtn");

        // adding metadata fields to the template
        for (int i = 0; i < TOTAL_METADATA_FIELDS; i++) {
            wait.until(ExpectedConditions.elementToBeClickable(addAttributeBtn));
            driver.findElement(addAttributeBtn).click();

            wait.until(ExpectedConditions.elementToBeClickable(inputAttr));
            driver.findElement(inputAttr).click();
            driver.findElement(inputAttr).clear();
            driver.findElement(inputAttr).sendKeys(attribute + i);

            driver.findElement(inputValue).click();
            driver.findElement(inputValue).clear();
            driver.findElement(inputValue).sendKeys(value + i);

            driver.findElement(inputUnit).click();
            driver.findElement(inputUnit).clear();
            driver.findElement(inputUnit).sendKeys(unit + i);

            driver.findElement(By.id("addFieldBtn")).click();

            wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("newTemplateAVU")));

            driver.get(driver.getCurrentUrl());
            wait.until(ExpectedConditions.elementToBeClickable(By.id("addAttributeBtn")));
        }
    }
}
