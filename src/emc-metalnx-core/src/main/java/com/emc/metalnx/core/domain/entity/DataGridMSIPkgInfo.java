package com.emc.metalnx.core.domain.entity;

import com.emc.metalnx.core.domain.utils.DataGridCoreUtils;

import java.util.List;

/**
 * Represents the status of the MSI package on the grid.
 */
public class DataGridMSIPkgInfo {
    private List<DataGridServer> servers;
    private String msiVersionSupported;

    public enum msiVersionGridStatus { OK, NOT_INSTALLED, NOT_SUPPORTED, NOT_INSTALLED_NOT_SUPPORTED }

    public DataGridMSIPkgInfo(List<DataGridServer> servers, String msiVersionSupported) {
        this.servers = servers;
        this.msiVersionSupported = msiVersionSupported;
    }

    public msiVersionGridStatus msiVersionGridStatus() {
        String versionSupported = DataGridCoreUtils.getAPIVersion(msiVersionSupported);
        boolean notInstalled = false;
        boolean notSupported = false;

        for(DataGridServer server: servers) {
            String versionInstalled = DataGridCoreUtils.getAPIVersion(server.getMSIVersion());

            if(versionInstalled.isEmpty()) notInstalled = true;
            else if(!versionInstalled.equalsIgnoreCase(versionSupported)) notSupported = true;
        }

        msiVersionGridStatus status = msiVersionGridStatus.OK;

        if(notInstalled && notSupported){
            status = msiVersionGridStatus.NOT_INSTALLED_NOT_SUPPORTED;
        }
        else if(notInstalled) {
            status = msiVersionGridStatus.NOT_INSTALLED;
        }
        else if(notSupported) {
            status = msiVersionGridStatus.NOT_SUPPORTED;
        }

        return status;
    }

    public List<DataGridServer> getServers() {
        return servers;
    }

    public void setServers(List<DataGridServer> servers) {
        this.servers = servers;
    }

    public String getMsiVersionSupported() {
        return msiVersionSupported;
    }

    public void setMsiVersionSupported(String msiVersionSupported) {
        this.msiVersionSupported = msiVersionSupported;
    }
}
