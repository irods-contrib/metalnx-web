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

package com.emc.metalnx.integration.test.upload;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.integration.test.utils.UserUtils;
import com.emc.metalnx.test.generic.UiTestUtilities;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

@Deprecated
@Ignore
public class UploadTests {

	private static final Logger logger = LoggerFactory.getLogger(UploadTests.class);
	private static WebDriver driver = null;
	private static WebDriverWait wait = null;

	private static final String username = "admin_user_" + System.currentTimeMillis();
	private static final String password = "pwd_" + +System.currentTimeMillis();

	@BeforeClass
	public static void setUpBeforeClass() {
		logger.info("Before class: Creating admin user {}", username);
		// UITest.setUpBeforeClass();
		driver = UiTestUtilities.getDriver();
		wait = new WebDriverWait(driver, 30);

		UserUtils.createUser(username, password, "rodsadmin", driver);
	}

	/**
	 * After all tests are done, the test must quit the driver. This will close
	 * every window associated with the current driver instance.
	 */

	@AfterClass
	public static void tearDownAfterClass() {
		logger.info("After class: Removing admin user {}", username);
		UserUtils.removeUser(username, driver);

		if (driver != null) {
			driver.quit();
			driver = null;
			UiTestUtilities.setDriver(null);
		}
	}

	@Test
	@Ignore
	public void test1() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
		UiTestUtilities.login(username, password);

		driver.get(UiTestUtilities.COLLECTIONS_URL);
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable tbody tr td")));
		wait.until(ExpectedConditions.elementToBeClickable(By.id("uploadIcon"))).click();
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#browseButton")));

		JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
		jsExecutor.executeScript("document.getElementById('inputFiles').style.display='block';");

		By fileInput = By.cssSelector("#inputFiles");
		wait.until(ExpectedConditions.visibilityOfElementLocated(fileInput))
				.sendKeys("C:\\Users\\ipenoguh.TPA-ELD\\Documents\\docs\\apostila_conteudo_cursojulho2013.pdf");
		jsExecutor.executeScript("document.getElementById('inputFiles').style.display='none';");

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#filesList p")));
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#uploadButton"))).click();

		String base = driver.getWindowHandle();
		Set<String> set = driver.getWindowHandles();
		set.remove(base);

		driver.switchTo().window((String) set.toArray()[0]);

		driver.close();
		driver.switchTo().window(base);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable tbody tr td")));

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable_filter input")))
				.sendKeys("apostila_conteudo_cursojulho2013.pdf");
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable tbody tr td")));
	}

}
