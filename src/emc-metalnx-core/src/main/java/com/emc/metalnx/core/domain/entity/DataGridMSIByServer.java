package com.emc.metalnx.core.domain.entity;

import java.util.ArrayList;
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
    private List<String> otherMSIs;
    private List<String> msisInstalled;

    public DataGridMSIByServer(String host, List<String> expectedMetalnxMSIs, List<String> expectedIrodsMSIs) {
        this.host = host;
        this.metalnxMSIs = new HashMap<>();
        this.irodsMSIs = new HashMap<>();
        this.otherMSIs = new ArrayList<>();

        if(expectedMetalnxMSIs != null) for(String msi: expectedMetalnxMSIs) this.metalnxMSIs.put(msi, false);
        if(expectedIrodsMSIs != null) for(String msi: expectedIrodsMSIs) this.irodsMSIs.put(msi, false);
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
        this.otherMSIs.add(msi);
    }

    public List<String> getOtherMSIs() {
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