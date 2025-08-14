 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.Base64Utils;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class EncodedPropertiesConfigurer extends PropertyPlaceholderConfigurer {

    private static final String PROPERTIES_TO_BE_DECODED = "encoded.properties";
    private static final String DEFAULT_ENCODED_FIELDS = "db.password,irods.admin.password";
    private static final String PWD_SALT = "!M3t4Lnx@1234";

    private static final Logger logger = LogManager.getLogger(EncodedPropertiesConfigurer.class);

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactory, Properties props) throws BeansException {

        for (String prop : propertiesToBeDecoded(props)) {
            logger.debug("Decoding property [{}]", prop);
            String currentValue = props.getProperty(prop);
            props.setProperty(prop, decodePassword(currentValue));
        }

        super.processProperties(beanFactory, props);
    }

    /**
     * Decodes an encoded password on properties placeholders
     *
     * @param currentValue the current password value
     * @return {@link String} decoded password
     */
    private String decodePassword(String currentValue) {
        logger.debug("Decoding value [{}]", currentValue);
        
        String pwd = "";
        Integer key = 0;

        try {
			key = getKey();
			
            byte[] encodedBytes = Base64Utils.decodeFromString(currentValue);
			for(byte b: encodedBytes) {
			    pwd += (char) ((b ^ key) & 0xFF);
			}
			
		} catch (UnknownHostException e) {
			logger.error("Could not get machine's hostname at start up.");
		} catch (NoSuchAlgorithmException e) {
			logger.error("Could not get a MD5 algorithm at start up.");
		} catch (UnsupportedEncodingException e) {
			logger.error("Encoding not supported (US_ASCII).");
		}

        logger.debug("Decoded value [{}]", pwd);
        return pwd;
    }

	private Integer getKey() throws UnknownHostException,
			NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");

        String hostname = Inet4Address.getLocalHost().getHostName();

        // Using only FQDN
        if (hostname.contains(".")) {
            hostname = hostname.substring(0, hostname.indexOf("."));
        }

        String s = PWD_SALT + hostname;
		
		Integer key = 0;
		byte[] digestedBytes = md5.digest(s.getBytes());
		for (byte b : digestedBytes) {
			key += b & 0xFF;
		}
		
		return key;
	}

    /**
     * Returns the name of the encoded properties on the configuration
     * files
     *
     * @param props the properties hashtable
     * @return {@link java.lang.reflect.Array} of {@link String}
     */
    private List<String> propertiesToBeDecoded(Properties props) {
        logger.debug("Looking for properties to be decoded");
        String propertiesToBeDecodedRaw = props.getProperty(PROPERTIES_TO_BE_DECODED, DEFAULT_ENCODED_FIELDS);

        logger.debug("The following properties will be decoded: {}", propertiesToBeDecodedRaw);
        List<String> properties = new ArrayList<>(Arrays.asList(propertiesToBeDecodedRaw.split(",")));

        // Filtering out all the invalid property names
        Set<String> propertyNames = props.stringPropertyNames();
        ListIterator<String> lit = properties.listIterator();

        while (lit.hasNext()) {
            String currentPropName = (String) lit.next();
            if (!propertyNames.contains(currentPropName)) {
                logger.warn("Found invalid property [{}].", currentPropName);
                lit.remove();
            }
        }

        return properties;
    }

}
