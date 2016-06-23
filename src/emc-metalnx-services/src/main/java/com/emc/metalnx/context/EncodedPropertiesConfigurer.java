/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.Base64Utils;

import java.util.*;

public class EncodedPropertiesConfigurer extends PropertyPlaceholderConfigurer {

    private static final String PROPERTIES_TO_BE_DECODED = "encoded.properties";
    private static final String DEFAULT_ENCODED_FIELDS = "db.password,jobs.irods.password";

    private static final Logger logger = LoggerFactory.getLogger(EncodedPropertiesConfigurer.class);

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
        return new String(Base64Utils.decodeFromString(currentValue));
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
        ListIterator lit = properties.listIterator();

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
