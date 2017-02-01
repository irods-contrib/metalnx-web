package com.emc.metalnx.core.domain.entity;

import com.emc.metalnx.core.domain.utils.DataGridCoreUtils;

import java.util.List;

/**
 * Represents the status of the MSI package on the grid.
 */
public class DataGridMSIPkgInfo {
    private List<DataGridServer> servers;
    private boolean isThereAnyPkgMissing; // MSI pkg is not installed in one or more servers
    private boolean isThereAnyPkgNotSupported; // MSI Pkg is installed but out-of-date

    public DataGridMSIPkgInfo(List<DataGridServer> servers, String msiVersionSupported) {
        this.servers = servers;
        String versionSupported = DataGridCoreUtils.getAPIVersion(msiVersionSupported);

        for(DataGridServer server: servers) {
            String versionInstalled = DataGridCoreUtils.getAPIVersion(server.getMSIVersion());

            if(versionInstalled.isEmpty()) isThereAnyPkgMissing = true;
            else if(!versionInstalled.equalsIgnoreCase(versionSupported)) isThereAnyPkgNotSupported = true;
        }
    }

    public List<DataGridServer> getServers() {
        return servers;
    }

    public void setServers(List<DataGridServer> servers) {
        this.servers = servers;
    }

    public boolean isThereAnyPkgMissing() {
        return isThereAnyPkgMissing;
    }

    public boolean isThereAnyPkgNotSupported() {
        return isThereAnyPkgNotSupported;
    }
}
