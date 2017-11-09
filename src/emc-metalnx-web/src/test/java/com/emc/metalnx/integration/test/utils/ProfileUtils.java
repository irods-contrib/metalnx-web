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
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;


public class ProfileUtils {
    public static void accessAddNewProfileForm(WebDriver driver) {
        driver.get(UiTestUtilities.PROFILES_URL);
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("addProfileButton")));

        driver.findElement(By.id("addProfileButton")).click();
    }

    public static void fillProfileForm(String name, String description, List<String> groupsNames, WebDriver driver) {
        driver.findElement(By.id("inputProfileName")).sendKeys(name);
        driver.findElement(By.id("inputDescription")).sendKeys(description);

        if (groupsNames != null) {
            for (String groupName : groupsNames) {
                driver.findElement(By.cssSelector("#groupsListTable_filter input")).clear();
                driver.findElement(By.cssSelector("#groupsListTable_filter input")).sendKeys(groupName);
                driver.findElement(By.id("chk_" + groupName)).click();
            }
        }
    }

    public static void submitProfileForm(WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("submitProfileFormBtn")));

        driver.findElement(By.id("submitProfileFormBtn")).click();
    }

    public static void addUserProfile(String name, String description, List<String> groupsNames, WebDriver driver) {
        driver.get(UiTestUtilities.ADD_PROFILES_URL);
        WebDriverWait wait = new WebDriverWait(driver, 8);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("inputProfileName")));

        fillProfileForm(name, description, groupsNames, driver);
        submitProfileForm(driver);
    }

    public static boolean isProfileInList(String name, WebDriver driver) {
        if (!driver.getCurrentUrl().equals(UiTestUtilities.PROFILES_URL)) {
            driver.get(UiTestUtilities.PROFILES_URL);

        }
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("userProfilesListTable")));

        List<WebElement> profilesNamesElements = driver.findElements(By.className("col-profile-name"));

        for (WebElement e : profilesNamesElements) {
            if (e.getText().equals(name)) {
                return true;
            }
        }

        return false;
    }

    public static void clickOnRemoveIcon(String name, WebDriver driver) {
        if (!driver.getCurrentUrl().equals(UiTestUtilities.PROFILES_URL)) {
            driver.get(UiTestUtilities.PROFILES_URL);

        }
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("userProfilesListTable")));

        driver.findElement(By.id("btn_removal_" + name)).click();

        wait.until(ExpectedConditions.elementToBeClickable(By.id("btnConfProfileRemoval_Yes")));
    }

    public static void removeProfile(String name, WebDriver driver) {
        if (isProfileInList(name, driver)) {
            clickOnRemoveIcon(name, driver);
            driver.findElement(By.id("btnConfProfileRemoval_Yes")).click();
        }
    }

    public static boolean errorMessageIsDisplayed(String errorMessageElementId, WebDriver driver) {
        WebDriverWait wait = new WebDriverWait(driver, 8);
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id(errorMessageElementId)));

        WebElement invalidMsg = driver.findElement(By.id(errorMessageElementId));
        return invalidMsg.isDisplayed();
    }
}
