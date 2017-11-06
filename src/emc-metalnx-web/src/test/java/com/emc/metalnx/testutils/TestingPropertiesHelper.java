package com.emc.metalnx.testutils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestingPropertiesHelper {
	public static String IRODS_USER_KEY = "test.irods.user";
	public static String IRODS_PASSWORD_KEY = "test.irods.password";
	public static String IRODS_ADMIN_USER_KEY = "test.irods.admin";
	public static String IRODS_ADMIN_PASSWORD_KEY = "test.irods.admin.password";
	public static String CHROME_DRIVER = "selenium.test.chrome.driver";
	public static String CHROME_DRIVER_LOCATION = "selenium.test.chrome.driver.loaction";

	/**
	 * Load the properties that control various tests from the testing.properties
	 * file on the code path
	 *
	 * @return <code>Properties</code> class with the test values
	 * @throws TestConfigurationException
	 * 
	 */
	public Properties getTestProperties() throws TestConfigurationException {
		ClassLoader loader = this.getClass().getClassLoader();
		InputStream in = loader.getResourceAsStream("testing.properties");
		Properties properties = new Properties();

		try {
			properties.load(in);
		} catch (IOException ioe) {
			throw new TestConfigurationException("error loading test properties", ioe);
		} finally {
			try {
				in.close();
			} catch (Exception e) {
				// ignore
			}
		}
		return properties;
	}

}
