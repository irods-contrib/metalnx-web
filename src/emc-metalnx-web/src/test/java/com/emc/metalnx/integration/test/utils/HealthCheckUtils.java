package com.emc.metalnx.integration.test.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.emc.metalnx.test.generic.UiTestUtilities;

public class HealthCheckUtils {
	
	public static String getPageHeader(WebDriver driver, WebDriverWait wait, String url) {
		driver.get(url);
		wait.until(ExpectedConditions.visibilityOfElementLocated(UiTestUtilities.dashboardHdrLocator));
		return driver.findElement(UiTestUtilities.dashboardHdrLocator).getText();
	}
}
