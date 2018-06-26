package com.emc.metalnx.core.domain.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.irods.jargon.testutils.TestConfigurationException;

/**
 * Utilities to load testing properties from a properties file
 *
 * @author Mike Conway
 */
public class TestingMetalnxPropertiesHelper {

	/**
	 * Return the given property (by key) as an boolean
	 *
	 * @param testingProperties
	 *            {@link Properties}
	 * @param key
	 *            {@code String}
	 * @return {@code boolean} with the prop value
	 * @throws TestConfigurationException
	 *             {@link TestConfigurationException}
	 */
	public boolean getPropertyValueAsBoolean(final Properties testingProperties, final String key)
			throws TestConfigurationException {
		String propVal = (String) testingProperties.get(key);

		if (propVal == null || propVal.length() == 0) {
			throw new TestConfigurationException("missing or invalid value in test props");
		}

		boolean retVal = false;

		try {
			retVal = Boolean.parseBoolean(propVal);
		} catch (NumberFormatException nfe) {
			throw new TestConfigurationException("port is in valid format to convert to int:" + propVal, nfe);
		}

		return retVal;
	}

	/**
	 * Return the given property (by key) as an int
	 *
	 * @param testingProperties
	 *            {@link Properties}
	 * @param key
	 *            {@code String}
	 * @return {@code int} with the prop value
	 * @throws TestConfigurationException
	 *             {@link TestConfigurationException}
	 */
	public int getPropertyValueAsInt(final Properties testingProperties, final String key)
			throws TestConfigurationException {
		String propVal = (String) testingProperties.get(key);

		if (propVal == null || propVal.length() == 0) {
			throw new TestConfigurationException("missing or invalid value in test props");
		}

		int retVal = 0;

		try {
			retVal = Integer.parseInt(propVal);
		} catch (NumberFormatException nfe) {
			throw new TestConfigurationException("port is in valid format to convert to int:" + propVal, nfe);
		}

		return retVal;
	}

	/**
	 * Load the properties that control various tests from the testing.properties
	 * file on the code path
	 *
	 * @return {@link Properties} class with the test values
	 * @throws TestConfigurationException
	 *             {@link TestConfigurationException}
	 *
	 */
	public Properties getTestProperties() throws TestConfigurationException {
		ClassLoader loader = this.getClass().getClassLoader();
		InputStream in = loader.getResourceAsStream("test.metalnx.properties");
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

	/**
	 * Check if mail is to be tested
	 *
	 * @param testingProperties
	 *            {@link Properties}
	 * @return <code>boolean</code> with option
	 */
	public boolean isMailEnabled(final Properties testingProperties) {
		String val = (String) testingProperties.get(MetalnxTestUtils.MAIL_ENABLED);
		if (val == null) {
			return false;
		} else {
			return Boolean.parseBoolean(val);
		}
	}

}
