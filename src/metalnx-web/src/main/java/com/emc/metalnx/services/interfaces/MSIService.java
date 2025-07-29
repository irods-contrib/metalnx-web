/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.interfaces;

import java.util.List;

import com.emc.metalnx.core.domain.entity.DataGridMSIPkgInfo;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;

/**
 * Service for external software used by Metalnx
 */
public interface MSIService {

	/**
	 * Retrieves all MSIs packages installed in the grid.
	 * 
	 * @param host
	 *            hostname of the server to get the MSIs installed
	 * @return {@link DataGridServer} containing the list of MSIs classified by
	 *         type: metalnx, iRODS, other
	 * @throws DataGridException
	 *             if Metalnx cannot connect to the data grid
	 */
	DataGridServer getMSIsInstalled(String host) throws DataGridException;

	/**
	 * Retrieves information about the MSI package installed in the servers of the
	 * grid.
	 * 
	 * @return {@link DataGridMSIPkgInfo} containing information about the MSI
	 *         package of each server
	 * @throws DataGridException
	 *             if Metalnx cannot connect to the data grid
	 */
	DataGridMSIPkgInfo getMSIPkgInfo() throws DataGridException;

	/**
	 * Gets the MSI package version installed on all servers.
	 * 
	 * @return <server hostname, version> map
	 * @throws DataGridConnectionRefusedException
	 *             if Metalnx cannot connect to the grid
	 * @throws DataGridException
	 *             {@link DataGridException}
	 */
	List<DataGridServer> getMSIInfoForAllServers() throws DataGridException;

	/**
	 * Get the MSI package version for a specific server
	 * 
	 * @param server
	 *            server where the MSI package is installed
	 * @throws DataGridException
	 *             {@link DataGridException}
	 */
	void setMSIInfoForServer(DataGridServer server) throws DataGridException;

	/**
	 * Checks whether or not the MSI package version installed, if any, is
	 * compatible with this version of the Web App.
	 *
	 * @param resource
	 *            resource name
	 * @return True, if the MSI package version installed is compatible with this
	 *         version of the Web App. Otherwise, False.
	 * @throws DataGridException
	 *             {@link DataGridException}
	 */
	boolean isMSIAPICompatibleInResc(String resource) throws DataGridException;
}
