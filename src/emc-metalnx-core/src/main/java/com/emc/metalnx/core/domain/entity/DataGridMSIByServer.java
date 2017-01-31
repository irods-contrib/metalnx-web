package com.emc.metalnx.core.domain.entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Has all types of MSIs installed on a host: metalnx, iRODS and others.
 */
public class DataGridMSIByServer {
    private String host;

    // Maps MSI name to True/False that indicates whether or not the MSI is installed
    private Map<String, Boolean> metalnxMSIs;

    // iRODS MSIs
    private Set<String> irodsMSIs;

    // Other MSIs
    private Set<String> otherMSIs;

    public DataGridMSIByServer(String host, Set<String> expectedMetalnxMSIs) {
        this.host = host;

        this.metalnxMSIs = new HashMap<>();
        this.irodsMSIs = new HashSet<>();
        this.otherMSIs = new HashSet<>();

        this.metalnxMSIs = new HashMap<>();
        if(expectedMetalnxMSIs != null) for(String msi: expectedMetalnxMSIs) this.metalnxMSIs.put(msi, false);
    }

    public Map<String, Boolean> getMetalnxMSIs() {
        return metalnxMSIs;
    }

    public void addToMsiMetalnx(String msi) {
        if(msi == null || msi.isEmpty()) return;

        // updating map
        this.metalnxMSIs.put(msi, true);
    }

    public Set<String> getIRODSMSIs() {
        return irodsMSIs;
    }

    public void addToMsiIRODS(String msi) {
        if(msi == null || msi.isEmpty()) return;
        this.irodsMSIs.add(msi);
    }

    public Set<String> getOtherMSIs() {
        return otherMSIs;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }


}