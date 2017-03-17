/*
 * Copyright (c) 2015-2017, Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emc.metalnx.core.domain.entity;

import com.emc.metalnx.core.domain.utils.DataGridCoreUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Has all types of MSIs installed on a host: metalnx, iRODS and others.
 */
public class DataGridMSIByServer {
    private String host;

    // Maps MSI name to True/False that indicates whether or not the MSI is installed
    private Map<String, Boolean> metalnxMSIs;
    private Map<String, Boolean> irodsMSIs;
    private Map<String, Boolean> otherMSIs;
    private List<String> msisInstalled;

    public DataGridMSIByServer(String host, List<String> expectedMetalnxMSIs, List<String> expectedIrodsMSIs,
                               List<String> expectedOtherMSIs) {
        this.host = host;
        this.metalnxMSIs = new HashMap<>();
        this.irodsMSIs = new HashMap<>();
        this.otherMSIs = new HashMap<>();

        DataGridCoreUtils.fillMSIMap(expectedMetalnxMSIs, metalnxMSIs);
        DataGridCoreUtils.fillMSIMap(expectedIrodsMSIs, irodsMSIs);
        DataGridCoreUtils.fillMSIMap(expectedOtherMSIs, otherMSIs);
    }

    public Map<String, Boolean> getMetalnxMSIs() {
        return metalnxMSIs;
    }

    public void addToMsiMetalnx(String msi) {
        if(msi == null || msi.isEmpty()) return;
        this.metalnxMSIs.put(msi, true);
    }

    public Map<String, Boolean> getIRODSMSIs() {
        return irodsMSIs;
    }

    public void addToMsiIRODS(String msi) {
        if(msi == null || msi.isEmpty()) return;
        this.irodsMSIs.put(msi, true);
    }

    public void addToMsiOther(String msi) {
        if(msi == null || msi.isEmpty()) return;
        this.otherMSIs.put(msi, true);
    }

    public Map<String, Boolean> getOtherMSIs() {
        return otherMSIs;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void addMicroservices(List<String> msis) {
        if(msis == null) return;

        this.msisInstalled = msis;

        // classifying MSIs by their type
        for(String msi: msisInstalled) {
            if(metalnxMSIs.keySet().contains(msi)) addToMsiMetalnx(msi);
            else if(irodsMSIs.keySet().contains(msi)) addToMsiIRODS(msi);
            else addToMsiOther(msi);
        }
    }

    public boolean isThereAnyMSI() {
        return this.msisInstalled != null && !this.msisInstalled.isEmpty();
    }
}