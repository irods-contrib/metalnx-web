 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.core.domain.entity;

import java.util.List;

import com.emc.metalnx.core.domain.utils.DataGridCoreUtils;

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

		for (DataGridServer server : servers) {
			String versionInstalled = DataGridCoreUtils.getAPIVersion(server.getMSIVersion());

			if (versionInstalled.isEmpty())
				isThereAnyPkgMissing = true;
			else if (!versionInstalled.equalsIgnoreCase(versionSupported))
				isThereAnyPkgNotSupported = true;
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

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridMSIPkgInfo [");
		if (servers != null) {
			builder.append("servers=").append(servers.subList(0, Math.min(servers.size(), maxLen))).append(", ");
		}
		builder.append("isThereAnyPkgMissing=").append(isThereAnyPkgMissing).append(", isThereAnyPkgNotSupported=")
				.append(isThereAnyPkgNotSupported).append("]");
		return builder.toString();
	}
}
