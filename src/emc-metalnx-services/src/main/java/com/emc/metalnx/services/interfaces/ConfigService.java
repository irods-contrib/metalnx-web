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

package com.emc.metalnx.services.interfaces;

import java.util.List;

/**
 * Service used to retrieve all configurable parameters from *.properties files.
 */
public interface ConfigService {
    /**
     * Finds the MSI API version supported by the current version of Metalnx.
     * @return string representing the version
     */
    String getMsiAPIVersionSupported();

    /**
     * Finds the list of all expected Metalnx microservices.
     * @return list of all Metalnx microservices.
     */
    List<String> getMlxMSIsExpected();

    /**
     * Finds the list of all expected iRODS 4.1.X microservices.
     * @return list of all iRODS 4.1.X microservices.
     */
    List<String> getIrods41MSIsExpected();

    /**
     * Finds the list of all expected irods 4.2.X microservices.
     * @return list of all irods 4.2.X microservices.
     */
    List<String> getIrods42MSIsExpected();

    /**
     * Finds the list of all third-party microservices.
     * @return list of all third-party microservices.
     */
    List<String> getOtherMSIsExpected();
}
