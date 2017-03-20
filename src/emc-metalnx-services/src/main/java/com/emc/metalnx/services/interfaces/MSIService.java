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

package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.DataGridMSIPkgInfo;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;

import java.util.List;

/**
 * Service for external software used by Metalnx
 */
public interface MSIService {

    /**
     * Retrieves all MSIs packages installed in the grid.
     * @param host hostname of the server to get the MSIs installed
     * @return {@link DataGridServer} containing the list of MSIs classified by type: metalnx, iRODS, other
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    DataGridServer getMSIsInstalled(String host) throws DataGridConnectionRefusedException;

    /**
     * Retrieves information about the MSI package installed in the servers of the grid.
     * @return {@link DataGridMSIPkgInfo} containing information about the MSI package of each server
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    DataGridMSIPkgInfo getMSIPkgInfo() throws DataGridConnectionRefusedException;

    /**
     * Gets the MSI package version installed on all servers.
     * @return <server hostname, version> map
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the grid
     */
    List<DataGridServer> getMSIInfoForAllServers() throws DataGridConnectionRefusedException;

    /**
     * Get the MSI package version for a specific server
     * @param server server where the MSI package is installed
     * @throws DataGridRuleException if cannot execute rule that gets the MSI package version
     * @throws DataGridConnectionRefusedException if cannot connect to the grid
     */
    void setMSIInfoForServer(DataGridServer server) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Checks whether or not the MSI package version installed, if any, is compatible with this version of the Web App.
     *
     * @param resource resource name
     * @return True, if the MSI package version installed is compatible with this version of the Web App. Otherwise, False.
     */
    boolean isMSIAPICompatibleInResc(String resource) throws DataGridConnectionRefusedException;
}
