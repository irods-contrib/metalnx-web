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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.test.generic.UiTestUtilities;


public class CollectionUtils {
    private static final Logger logger = LoggerFactory.getLogger(CollectionUtils.class);
    private static By publicSidebarBtnCssSelectorAdmin = By.cssSelector("#side-menu li a[href='/emc-metalnx-web/collections/public/']");
    private static By publicSidebarBtnCssSelectorUser = By.cssSelector("#side-menu li a[href='/emc-metalnx-web/collections/public/']");
    private static By directoryPathId = By.id("directoryPath");
    private static By inputUsernameLoginId = By.id("inputUsernameLogin");
    private static By homeLinkText = By.linkText("home");
    private static By publicCollSelectorA = By.cssSelector(String.format("a[title=\"/%s/home/public\"]", UiTestUtilities.IRODS_ZONE));
    private static By publicCollSelectorSpan = By.cssSelector(String.format("span[title=\"/%s/home/public\"]", UiTestUtilities.IRODS_ZONE));
    private static By breadcrumbLocator = By.className("breadcrumb");
    private static By navigationInputLocator = By.id("navigationInput");

    public static final By EMPTY_TRASH_CONF_BTN = By.id("emptyTrashConfBtn");
    public static final By EMPTY_TRASH_BTN = By.id("emptyTrashBtn");
    public static final By EMPTY_TRASH_MODAL = By.id("emptyTrashModal");
    public static final By COLLS_TABLE = By.id("treeViewTable");
    public static final By DELETE_MODAL = By.id("deleteModal");

    public static final By SELECT_ACTION_BTN = By.cssSelector("#actions button");
    public static final By BROWSE_TAB = By.cssSelector("button[onclick='goBackHistory(1);']");
    public static final By METADATA_TAB = By.id("metadataTab");
    public static final By APPLY_TEMPLATE_BTN = By.id("applyTemplatesBtn");
    public static String RODS_COLL_PATH = String.format("/%s/home/%s", UiTestUtilities.IRODS_ZONE, UiTestUtilities.RODS_USERNAME);

    /**
     * Method that waits for the select action button to be enabled for clicking.
     *
     * @param driver
     */
    public static void waitForSelectActionBtnToBeEnabled(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.elementToBeClickable(CollectionUtils.SELECT_ACTION_BTN));
    }

    /**
     * Method that waits for the select action options to be enabled for clicking.
     *
     * @param driver
     */
    public static void waitForActionsDropdownToBeShown(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.elementToBeClickable(CollectionUtils.APPLY_TEMPLATE_BTN));
    }

    /**
     * Get the locator of a file or collection under the rods home folder.
     *
     * @param item
     *            file or collection to be found
     * @return
     */
    public static By getFileLocatorUnderRodsHome(String item) {
        return getFileLocator(item, UiTestUtilities.IRODS_ZONE, UiTestUtilities.RODS_USERNAME);
    }

    /**
     * Get the locator of a file or collection.
     *
     * @param item
     *            file or collection to be found
     * @return
     */
    public static By getFileLocator(String item, String zone, String user) {
        String collPattern = String.format("/%s/home/%s/%s", UiTestUtilities.IRODS_ZONE, UiTestUtilities.RODS_USERNAME, item);
        return By.name(collPattern);
    }

    /**
     * Navigate through metalnx to get to the zone folder using a rodsadmin or rodsuser type.
     * Depending on the type, the navigation changes because the menu options are different
     *
     * @param driver
     *            WebDriver of the current browser
     * @param userType
     *            type of the user currently being used in the test
     * @param uname
     *            username being used in the current test
     * @param pwd
     *            password of the user being used in the test
     */

    public static void goToZoneCollection(WebDriver driver, String userType, String uname, String pwd) {
        logger.info("Beggining navigation to the public collection...");
        if (UiTestUtilities.RODS_ADMIN_TYPE.equals(userType)) {
            goToPublicCollectionByAdmin(driver, uname, pwd);
        }
        else {
            goToPublicCollectionByUser(driver, uname, pwd);
        }
    }

    /**
     * Navigate through metalnx to get to the public page using a rodsadmin or rodsuser type.
     * Depending on the type, the navigation changes because the menu options are different
     *
     * @param driver
     *            WebDriver of the current browser
     * @param userType
     *            type of the user currently being used in the test
     * @param uname
     *            username being used in the current test
     * @param pwd
     *            password of the user being used in the test
     */

    public static void goToPublicCollection(WebDriver driver, String userType, String uname, String pwd) {
        logger.info("Beggining navigation to the public collection...");
        if (UiTestUtilities.RODS_ADMIN_TYPE.equals(userType)) {
            goToPublicCollectionByAdmin(driver, uname, pwd);
        }
        else {
            goToPublicCollectionByUser(driver, uname, pwd);
        }
    }

    /**
     * Waits for the metadata table of a file or collection to be shown.
     *
     * @param driver
     */
    public static void waitForMetadataToBeLoaded(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#metadaTable tbody tr td")));
    }

    /**
     * Method that waits for an item to be shown in the collections page.
     *
     * @param item
     *            file or collection name
     */
    public static void waitForItemToLoad(WebDriver driver, String item) {
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.id(item)));
    }

    /**
     * Creates a collection through Metalnx.
     *
     * @param driver
     * @param collName
     *            collection name to be created
     */
    public static void createCollection(WebDriver driver, String collName) {

        WebDriverWait wait = new WebDriverWait(driver, 10);
        By collFormBtn = By.id("showCollectionFormBtn");
        By collNameLoc = By.id("inputCollectionName");

        driver.get(UiTestUtilities.COLLECTIONS_URL);

        wait.until(ExpectedConditions.elementToBeClickable(collFormBtn)).click();

        // Line below had to be added so that the driver also waits for the modal to be fully shown
        // and only then it will try to access inputCollectioName
        wait.until(ExpectedConditions.elementToBeClickable(collNameLoc)).click();
        driver.findElement(collNameLoc).clear();
        driver.findElement(collNameLoc).sendKeys(collName);
        driver.findElement(By.id("submitCollectionFormBtn")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("addAndModifyModal")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable tbody tr td")));
    }

    /**
     * Creates a collection through Metalnx.
     *
     * @param driver
     * @param collName
     *            collection name to be created
     */
    public static void createCollectionUnderZone(WebDriver driver, String collName, String zone) {
        WebDriverWait wait = new WebDriverWait(driver, 15);
        driver.get(UiTestUtilities.COLLECTIONS_URL);

        wait.until(ExpectedConditions.elementToBeClickable(By.linkText(zone))).click();
        wait.until(ExpectedConditions.elementToBeClickable(CollectionUtils.COLLS_TABLE));
        driver.findElement(By.id("showCollectionFormBtn")).click();

        // Line below had to be added so that the driver also waits for the modal to be fully shown
        // and only then it will try to access inputCollectioName
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.id("inputCollectionName")));
        driver.findElement(By.id("inputCollectionName")).click();
        driver.findElement(By.id("inputCollectionName")).clear();
        driver.findElement(By.id("inputCollectionName")).sendKeys(collName);
        driver.findElement(By.id("submitCollectionFormBtn")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("addAndModifyModal")));
    }

    /**
     * Goes to the given user's home folder using the breadcrumb.
     *
     * @param driver
     * @param user
     *            user's name
     */
    public static void goToUserHome(WebDriver driver, String user) {

        WebDriverWait wait = new WebDriverWait(driver, 10);

        driver.get(UiTestUtilities.COLLECTIONS_URL);
        String collLink = String.format("a[title='/%s/home/%s']", UiTestUtilities.IRODS_ZONE, user);

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(collLink))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable tbody tr td")));
    }

    /**
     * Removes a collection from Metalnx.
     *
     * @param driver
     * @param collName
     *            collection name to be removed
     */
    public static void removeColl(WebDriver driver, String collName) {
        driver.get(UiTestUtilities.COLLECTIONS_URL);
        removeItem(driver, collName);
    }

    /**
     * Removes a collection from Metalnx.
     *
     * @param driver
     * @param collName
     *            collection name to be removed
     */
    public static void removeCollUnderZone(WebDriver driver, String collName, String zone) {
        driver.get(UiTestUtilities.COLLECTIONS_URL);
        new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(By.linkText(zone))).click();
        removeItem(driver, collName);
    }

    /**
     * Removes a collection or data object from the data grid.
     *
     * @param driver
     * @param item
     *            name of the collection or the data object that will be removed
     */
    public static void removeItem(WebDriver driver, String item) {
        WebDriverWait wait = new WebDriverWait(driver, 10);
        waitForItemToLoad(driver, item);

        wait.until(ExpectedConditions.elementToBeClickable(By.id(item))).click();

        waitForSelectActionBtnToBeEnabled(driver);
        driver.findElement(CollectionUtils.SELECT_ACTION_BTN).click();

        waitForActionsDropdownToBeShown(driver);
        driver.findElement(By.id("deleteBtn")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(DELETE_MODAL));
        driver.findElement(By.cssSelector("#deleteModal .modal-footer .btn-primary")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(DELETE_MODAL));

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable tbody tr td")));
    }

    /**
     * Removes a list of collections or data objects from the data grid.
     *
     * @param driver
     * @param item
     *            name of the collection or the data object that will be removed
     */
    public static void removeItems(WebDriver driver, String... items) {
        for (String item : items) {
            removeItem(driver, item);
        }
    }

    /**
     * Clicks on the empty trash button available in the trash can.
     *
     * @param driver
     *            web driver of the current browser
     */
    public static void clickOnEmptyTrash(WebDriver driver) {
        logger.info("Clicking on the empty trash button");
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.elementToBeClickable(EMPTY_TRASH_BTN));
        driver.findElement(EMPTY_TRASH_BTN).click();
    }

    /**
     * Confirms empty trash operation.
     *
     * @param driver
     *            web driver of the current browser
     */
    public static void confirmEmptyTrash(WebDriver driver) {
        logger.info("Confirm empty trash operation");
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(EMPTY_TRASH_MODAL));
        driver.findElement(EMPTY_TRASH_CONF_BTN).click();
    }

    /**
     * This method writes a string on editable breadcrumb
     *
     * @param driver
     * @param wait
     * @param pathToBeWritten
     */
    public static void writeOnEditableBreadCrumb(WebDriver driver, WebDriverWait wait, String pathToBeWritten) {
        driver.get(UiTestUtilities.COLLECTIONS_URL);
        // clicking on breadcrumb
        wait.until(ExpectedConditions.elementToBeClickable(breadcrumbLocator));
        driver.findElement(breadcrumbLocator).click();

        // waiting for the breacrumb to turn into an input text
        wait.until(ExpectedConditions.visibilityOfElementLocated(navigationInputLocator));
        driver.findElement(navigationInputLocator).sendKeys(pathToBeWritten);
        driver.findElement(navigationInputLocator).sendKeys(Keys.ENTER);

    }

    /**
     * Removes collections from the data grid.
     *
     * @param driver
     * @param collections
     *            list of collections to be removed from the grid.
     */
    public static void cleanUpCollectionsUnderZone(WebDriver driver, String... collections) {
        try {
            UiTestUtilities.login();
            for (String collName : collections) {
                CollectionUtils.removeCollUnderZone(driver, collName, UiTestUtilities.IRODS_ZONE);
            }
        }
        catch (Exception e) {}
        finally {
            UiTestUtilities.logout();
        }
    }

    /**
     * Navigate through metalnx to get to the public page using a rodsadmin type
     *
     * @param driver
     *            WebDriver of the current browser
     * @param uname
     *            username being used in the current test
     * @param pwd
     *            password of the user being used in the test
     */
    private static void goToPublicCollectionByAdmin(WebDriver driver, String uname, String pwd) {
        logger.info("Navigating to public collection by admin interface...");
        WebDriverWait wait = new WebDriverWait(driver, 15);
        wait.until(ExpectedConditions.presenceOfElementLocated(inputUsernameLoginId));
        if (uname == null) {
            UiTestUtilities.login();
        }
        else {
            UiTestUtilities.login(uname, pwd);
        }
        driver.get(UiTestUtilities.COLLECTIONS_URL);

        // clicking on collections link on the left menu bar
        wait.until(ExpectedConditions.elementToBeClickable(publicSidebarBtnCssSelectorAdmin)).click();

        // going to home collection through breadcrumb
        wait.until(ExpectedConditions.visibilityOfElementLocated(directoryPathId)).findElement(homeLinkText).click();

        // clicking on public
        By searchInputField = By.cssSelector("#treeViewTable_filter input");
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInputField)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(searchInputField)).clear();
        for (char letter : "public".toCharArray()) {
            wait.until(ExpectedConditions.visibilityOfElementLocated(searchInputField)).sendKeys(Character.toString(letter));
        }

        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(CollectionUtils.COLLS_TABLE));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#treeViewTable tbody tr[role='row']")));
        wait.until(ExpectedConditions.elementToBeClickable(publicCollSelectorA)).click();

        // checking if we did in fact got inside the public collection
        assertTrue(wait.until(ExpectedConditions.visibilityOfElementLocated(publicCollSelectorSpan)).getText().contains("public"));
    }

    /**
     * Navigate through metalnx to get to the public page using a rodsuser type
     *
     * @param driver
     *            WebDriver of the current browser
     * @param uname
     *            username being used in the current test
     * @param pwd
     *            password of the user being used in the test
     */

    private static void goToPublicCollectionByUser(WebDriver driver, String uname, String pwd) {
        logger.info("Navigating to public collection by user interface...");
        WebDriverWait wait = new WebDriverWait(driver, 15);

        UiTestUtilities.logout();
        UiTestUtilities.login(uname, pwd);

        driver.get(UiTestUtilities.COLLECTIONS_URL);

        // "publicSidebarBtnCssSelector" is being used both by admin and user only because this
        // button is in the same position in both interfaces, but it is possible that eventually its
        // position will differ
        wait.until(ExpectedConditions.elementToBeClickable(publicSidebarBtnCssSelectorUser)).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(directoryPathId));
        WebElement breacrumb = driver.findElement(directoryPathId);
        assertTrue(breacrumb.getText().contains("public"));
    }

    /**
     * Gets into a given collection.
     *
     * @param driver
     * @param collection
     *            collection name to get in
     */
    public static void goToCollection(WebDriver driver, String collection) {
        By directoryPathLiSpan = By.cssSelector("#directoryPath .breadcrumb > li > span");
        By locator = By.cssSelector(String.format("#treeViewTable tbody tr:first-child td a", collection));
        WebDriverWait wait = new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable_filter input"))).sendKeys(collection);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("çççç")));
        }
        catch (Exception e) {}
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        wait.until(ExpectedConditions.textToBePresentInElementLocated(directoryPathLiSpan, collection));
    }

    /**
     * Changes the permission of a group on a collection or data object to the given permission
     *
     * @param group
     *            group that will get permission changed
     * @param permission
     *            new permission to be set
     */
    public static void changePermissionOfGroup(WebDriver driver, String group, String permission, String... items) {
        driver.get(UiTestUtilities.COLLECTIONS_URL);

        for (String collection : items) {
            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText(UiTestUtilities.IRODS_ZONE))).click();

            CollectionUtils.goToCollection(driver, collection);

            By breadcrumbOptions = By.id("breadcrumbOptions");
            By permissions = By.id("permissions");
            By groupPermissionTab = By.cssSelector("a[href='#groupPermissions']");
            By groupPermissionsTable = By.id("groupPermissionsTable");
            By groupPermissionSelector = By.id("groupOptionForPermission_" + group);
            By applyRecursionMsg = By.className("confirm-msg-table");
            By applyRecursionMsgYes = By.className("confirm-msg-table-yes");

            wait.until(ExpectedConditions.elementToBeClickable(breadcrumbOptions));
            driver.findElement(breadcrumbOptions).click();

            wait.until(ExpectedConditions.elementToBeClickable(permissions));
            driver.findElement(permissions).click();

            wait.until(ExpectedConditions.elementToBeClickable(groupPermissionTab));
            driver.findElement(groupPermissionTab).click();

            wait.until(ExpectedConditions.presenceOfElementLocated(groupPermissionsTable));
            wait.until(ExpectedConditions.presenceOfElementLocated(groupPermissionSelector));
            new Select(driver.findElement(groupPermissionSelector)).selectByVisibleText(permission.toUpperCase());

            wait.until(ExpectedConditions.presenceOfElementLocated(applyRecursionMsg));
            wait.until(ExpectedConditions.elementToBeClickable(applyRecursionMsgYes));
            driver.findElement(applyRecursionMsgYes).click();
        }
    }

    /**
     * Replicates a file to a resource.
     * @param driver
     * @param file
     * 			file path that will be replicated
     * @param resource
     * 			resource where the file will be replicated
     */
	public static void replicateFile(WebDriver driver, String resource, String file) {
		WebDriverWait wait = new WebDriverWait(driver, 10);
		By fileCheckBox = By.id(file);
		By selectActionBtn = By.cssSelector("#actions .btn-group button.btn-default");
		By replicateBtn = By.id("replicateBtn");
		By resourceSelectBox = By.id("selectResourceForReplication");
		By resourceOption = By.cssSelector("option[value=" + resource + "]");
		By replicateModalBtn = By.id("replicateButton");
		By replicateModal = By.id("replicateModal");
		
		driver.get(UiTestUtilities.COLLECTIONS_URL);
		wait.until(ExpectedConditions.elementToBeClickable(fileCheckBox));
		driver.findElement(fileCheckBox).click();
		wait.until(ExpectedConditions.elementToBeClickable(selectActionBtn));
		driver.findElement(selectActionBtn).click();
		wait.until(ExpectedConditions.elementToBeClickable(replicateBtn));
		driver.findElement(replicateBtn).click();
		wait.until(ExpectedConditions.visibilityOfElementLocated(resourceSelectBox));
		driver.findElement(resourceSelectBox).click();
		wait.until(ExpectedConditions.elementToBeClickable(resourceOption));
		driver.findElement(resourceOption).click();
		wait.until(ExpectedConditions.elementToBeClickable(replicateModalBtn));
		driver.findElement(replicateModalBtn).click();
		driver.findElement(replicateModalBtn).click();
		wait.until(ExpectedConditions.invisibilityOfElementLocated(replicateModal));
		wait.until(ExpectedConditions.elementToBeClickable(fileCheckBox));
	}

	/**
	 * Trim the replicas of a file down to one.
	 * @param driver
	 * @param string
	 */
	public static void deleteFileReplicas(WebDriver driver, String irodsFileAbsolutePath) {
		WebDriverWait wait = new WebDriverWait(driver, 10);
		By file = By.cssSelector("a[name=\"" + irodsFileAbsolutePath +"\"]");
		By deleteReplicaBtn = By.cssSelector("#deleteReplicaModal button.btn-primary");
		
		driver.get(UiTestUtilities.COLLECTIONS_URL);
		wait.until(ExpectedConditions.elementToBeClickable(file));
		driver.findElement(file).click();
		
		waitForReplicasTable(driver);		
		
		List<WebElement> replicaBtns = driver.findElements(By.cssSelector("#replicaAndChecksumInfo table tbody tr td button"));
		replicaBtns.remove(0);
		for (WebElement btn : replicaBtns) {
			btn.click();
			wait.until(ExpectedConditions.elementToBeClickable(deleteReplicaBtn));
			driver.findElement(deleteReplicaBtn).click();
			waitForSuccessMessage(driver);
		}
	}

	public static void waitForSuccessMessage(WebDriver driver) {
		new WebDriverWait(driver, 10).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.alert-success")));
	}

	public static void waitForReplicasTable(WebDriver driver) {
		By replicasTable = By.id("replicaAndChecksumInfo");
		new WebDriverWait(driver, 10).until(ExpectedConditions.visibilityOfElementLocated(replicasTable));
	}

	public static void goToHomeCollection(WebDriver driver) {
		By userHomeBtn = By.id("breadcrumbHome");
		new WebDriverWait(driver, 10).until(ExpectedConditions.elementToBeClickable(userHomeBtn));
        driver.findElement(userHomeBtn).click();
	}
}
