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
import junit.framework.Assert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

import static org.junit.Assert.assertTrue;


public class ResourceUtils {
    public static final String RESC_COMPOUND = "compound";
    public static final String RESC_LOAD_BAL = "load_balanced";
    public static final String RESC_PASSTHRU = "passthru";
    public static final String RESC_RANDOM = "random";
    public static final String RESC_REPL = "replication";
    public static final String RESC_ROUND_ROBIN = "roundrobin";
    public static final String RESC_DEFERRED = "deferred";
    public static final String RESC_ECS = "ecs";
    public static final String RESC_ISILON = "isilon";
    public static final String RESC_MOCK = "mockarchive";
    public static final String RESC_MSO = "mso";
    public static final String RESC_MSO_FILE = "mssofile";
    public static final String RESC_NON_BLOCKING = "nonblocking";
    public static final String RESC_STRUCT_FILE = "structfile";
    public static final String RESC_UNIVMSS = "univmss";
    public static final String RESC_UNIX_FILE_SYSTEM = "unixfilesystem";
    public static final String RESC_WOS = "wos";

    public static final String RESOURCE_PATH = "/var/lib/irods/iRODS/Vault";

    private static final String[] COORDINATING_RESOURCES = { RESC_COMPOUND, RESC_LOAD_BAL, RESC_PASSTHRU, RESC_RANDOM, RESC_REPL, RESC_ROUND_ROBIN };

    public static final By RESC_LIST_TABLE = By.cssSelector("#resourcesListTable tbody");

    private static boolean isCoordinatingResource(String resourceType) {
        for (int i = 0; i < COORDINATING_RESOURCES.length; i++) {
            if (COORDINATING_RESOURCES[i].equals(resourceType)) {
                return true;
            }
        }
        return false;
    }

    public static void accessAddResourceFormFrom(String url, String parent, WebDriver driver) {
        driver.get(url);
        WebDriverWait wait = new WebDriverWait(driver, 15);

        if (url.equals(UiTestUtilities.RESOURCES_MAP_URL)) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("resourceMapPanel")));

            rightClickOnNodeInMap(parent, driver);
            driver.findElement(By.id("addChildMenuItem")).click();

        }
        else {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("addResourceButton")));
            driver.findElement(By.id("addResourceButton")).click();
        }
    }

    public static void rightClickOnNodeInMap(String resourceName, WebDriver driver) {
        List<WebElement> nodes = driver.findElements(By.className("node"));
        WebElement parentNode = null;
        for (WebElement node : nodes) {
            String nodeName = node.findElement(By.tagName("text")).getText();
            if (nodeName.equals(resourceName)) {
                parentNode = node;
                break;
            }
        }
        if (parentNode != null) {
            Actions action = new Actions(driver);
            action.contextClick(parentNode).build().perform();

            WebDriverWait wait = new WebDriverWait(driver, 8);
            wait.until(ExpectedConditions.elementToBeClickable(By.id("addChildMenuItem")));
        }
    }

    public static void fillResourceForm(String resourceName, String resourceType, String parent, String zone, String host, String path,
            WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submitResourceFormBtn")));

        if (parent != null) {
            Assert.assertEquals(UiTestUtilities.ADD_RESOURCES_URL + parent + "/", driver.getCurrentUrl());
        }
        else {
            Assert.assertEquals(UiTestUtilities.ADD_RESOURCES_URL, driver.getCurrentUrl());
        }

        driver.findElement(By.id("inputResourceName")).sendKeys(resourceName);

        new Select(driver.findElement(By.id("selectResourceType"))).selectByValue(resourceType);

        if (isCoordinatingResource(resourceType)) {
            if (parent != null) {
                new Select(driver.findElement(By.id("selectResourceParent"))).selectByValue(parent);
            }

            if (zone != null) {
                new Select(driver.findElement(By.id("selectZone"))).selectByValue(zone);
            }
        }
        else {
            // It is a storage resource
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inputResourceHost")));

            driver.findElement(By.id("inputResourceHost")).sendKeys(host);
            driver.findElement(By.id("inputResourcePath")).sendKeys(path);
        }
    }

    public static void fillResourceFormForIsilon(String resourceName, String parent, String zone, String path, String isiHost, String isiPort,
            String isiUser, WebDriver driver) {

        By isiHostInput = By.id("inputIsilonResourceHost");
        By isiPortInput = By.id("inputIsilonResourcePort");
        By isiUserInput = By.id("inputIsilonResourceUser");

        fillResourceForm(resourceName, ResourceUtils.RESC_ISILON, parent, zone, "", path, driver);

        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.visibilityOfElementLocated(isiHostInput));
        wait.until(ExpectedConditions.visibilityOfElementLocated(isiPortInput));
        wait.until(ExpectedConditions.visibilityOfElementLocated(isiUserInput));

        driver.findElement(isiHostInput).sendKeys(isiHost);
        driver.findElement(isiPortInput).sendKeys(isiPort);
        driver.findElement(isiUserInput).sendKeys(isiUser);
    }

    public static void fillAndCancelResourceForm(String resourceName, boolean confirmCancel, WebDriver driver) {
        fillResourceForm(resourceName, ResourceUtils.RESC_COMPOUND, null, null, null, null, driver);

        driver.findElement(By.id("cancelResourceFormBtn")).click();

        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("cancelModal")));

        WebElement modalFooter = driver.findElement(By.className("modal-footer"));
        if (confirmCancel) {
            WebElement yesLink = modalFooter.findElement(By.linkText("Yes"));
            wait.until(ExpectedConditions.elementToBeClickable(yesLink));
            yesLink.click();
        }
        else {
            WebElement noButton = modalFooter.findElement(By.tagName("button"));
            wait.until(ExpectedConditions.elementToBeClickable(noButton));
            noButton.click();
        }
    }

    public static void fillAndSubmitResourceForm(String resourceName, String resourceType, String parent, String zone, String host, String path,
            WebDriver driver) {
        fillResourceForm(resourceName, resourceType, parent, zone, host, path, driver);
        driver.findElement(By.id("submitResourceFormBtn")).click();
    }

    public static void fillAndSubmitIsilonResourceForm(String resourceName, String parent, String zone, String path, String isiHost, String isiPort,
            String isiUser, WebDriver driver) {
        fillResourceFormForIsilon(resourceName, parent, zone, path, isiHost, isiPort, isiUser, driver);
        driver.findElement(By.id("submitResourceFormBtn")).click();
    }

    public static void addResource(String from, String resourceName, String resourceType, String parent, String zone, String host, String path,
            WebDriver driver) {
        accessAddResourceFormFrom(from, parent, driver);

        fillAndSubmitResourceForm(resourceName, resourceType, parent, zone, host, path, driver);

        if (!from.equals(UiTestUtilities.RESOURCES_MAP_URL)) {
            // checks if a success message is displayed and if the resource was successfully added
            WebElement divAlertSucess = driver.findElement(By.className("alert-success"));
            assertTrue(divAlertSucess.isDisplayed());
            assertTrue(divAlertSucess.getText().contains(resourceName));
        }
    }

    public static void addIsilonResource(String resourceName, String parent, String zone, String path, String isiHost, String isiPort,
            String isiUser, WebDriver driver) {

        // By submitFormBtn = By.id("submitResourceFormBtn");
        // WebDriverWait wait = new WebDriverWait(driver, 8);

        accessAddResourceFormFrom(UiTestUtilities.RESOURCES_URL, parent, driver);
        fillAndSubmitIsilonResourceForm(resourceName, parent, zone, path, isiHost, isiPort, isiUser, driver);

        // wait.until(ExpectedConditions.elementToBeClickable(submitFormBtn));
        // driver.findElement(submitFormBtn).click();
        new WebDriverWait(driver, 8).until(ExpectedConditions.visibilityOfElementLocated(By.id("resourcesListTable")));
    }

    public static void removeResource(String resourceName, WebDriver driver) throws Exception {
        if (ResourceUtils.isInResourcesList(UiTestUtilities.RESOURCES_URL, resourceName, null, driver)) {
            clickOnResouceRemoveIcon(resourceName, driver);
            driver.findElement(By.id("btnConfRescRemoval_Yes")).click();
        }
    }

    public static void clickOnResouceRemoveIcon(String resourceName, WebDriver driver) {

        driver.get(UiTestUtilities.RESOURCES_URL);
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btn_removal_" + resourceName)));
        driver.findElement(By.id("btn_removal_" + resourceName)).click();

        wait = new WebDriverWait(driver, 5);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnConfRescRemoval_Yes")));
    }

    public static int searchResource(String searchBy, int expectedResults, WebDriver driver) {

        WebDriverWait wait = new WebDriverWait(driver, 15);
        By resourcesFilter = By.cssSelector("#resourcesListTable_filter input");
        By resourcesTable = By.cssSelector("#resourcesListTable tbody tr td");

        driver.get(UiTestUtilities.RESOURCES_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(resourcesTable));
        Assert.assertEquals(UiTestUtilities.RESOURCES_URL, driver.getCurrentUrl());

        for (char c : searchBy.toCharArray()) {
            char letters[] = { c };
            wait.until(ExpectedConditions.visibilityOfElementLocated(resourcesFilter));
            driver.findElement(resourcesFilter).sendKeys(new String(letters));
            wait.until(ExpectedConditions.visibilityOfElementLocated(resourcesTable));
        }

        return getResourcesCount(driver);
    }

    public static int getResourcesCount(WebDriver driver) {
        By isThereATable = By.cssSelector("#resourcesListTable tbody tr td");
        By rescRowSelector = By.cssSelector("tbody tr[role='row']");
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.visibilityOfElementLocated(isThereATable));
        return driver.findElements(rescRowSelector).size();
    }

    public static boolean isInResourcesList(String url, String resourceName, String host, WebDriver driver) {

        By resourceTable = By.cssSelector("#resourcesListTable tbody");

        if (!driver.getCurrentUrl().equals(url)) {
            driver.get(url);
        }

        WebDriverWait wait = new WebDriverWait(driver, 8);
        if (url.equals(UiTestUtilities.RESOURCES_URL)) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(resourceTable));

            WebElement resourcesListTable = driver.findElement(resourceTable);
            List<WebElement> elements = resourcesListTable.findElements(By.className("col-resource-name"));
            for (WebElement e : elements) {
                if (e.getText().equals(resourceName)) {
                    return true;
                }
            }
        }
        else if (url.equals(UiTestUtilities.RESOURCES_MAP_URL)) {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("resourceMapPanel")));

            List<WebElement> nodes = driver.findElements(By.className("node"));
            for (WebElement node : nodes) {
                if (node.findElement(By.tagName("text")).getText().equals(resourceName)) {
                    return true;
                }
            }
        }
        else if (url.equals(UiTestUtilities.RESOURCES_SERVERS_URL)) {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.id("resourcesByServerList")));

            List<WebElement> elements = driver.findElements(By.id("hostname"));
            for (WebElement e : elements) {
                if (e.getText().equals(host)) {
                    e.click();
                    By resourceInfo = By.id("info_" + resourceName);
                    wait.until(ExpectedConditions.visibilityOfElementLocated(resourceInfo));
                    return driver.findElement(resourceInfo).findElement(By.cssSelector("#info_" + resourceName + " td:first-child span")).getText()
                            .equals(resourceName);
                }
            }
        }

        return false;
    }

    public static void assertResourceInfo(String resourceName, String resourceType, String zone, String host, String path, String parent,
            WebDriver driver) {
        assertTrue(driver.findElement(By.id("name")).getText().equals(resourceName));
        assertTrue(driver.findElement(By.id("type")).getText().equals(resourceType));
        assertTrue(driver.findElement(By.id("zone")).getText().equals(zone));
        if (!isCoordinatingResource(resourceType)) {
            System.out.println(driver.findElement(By.id("host")).getText() + ", " + host);
            assertTrue(driver.findElement(By.id("host")).getText().equals(host));
            assertTrue(driver.findElement(By.id("path")).getText().equals(path));
        }
        assertTrue(driver.findElement(By.id("parent")).getText().equals(parent));
    }

    public static boolean errorMessageIsDisplayed(String errorMessageElementId, WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(errorMessageElementId)));

        WebElement invalidMsg = driver.findElement(By.id(errorMessageElementId));
        return invalidMsg.isDisplayed();
    }
}
