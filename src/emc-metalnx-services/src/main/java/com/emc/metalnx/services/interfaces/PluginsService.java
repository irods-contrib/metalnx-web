package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;

import java.util.List;

/**
 * Service for external software used by Metalnx
 */
public interface PluginsService {

    /**
     * Gets the MSI package version installed on all servers.
     * @return <server hostname, version> map
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the grid
     */
    List<DataGridServer> getMSIVersionForAllServers() throws DataGridConnectionRefusedException;

    /**
     * Get the MSI package version for a specific server
     * @param server server where the MSI package is installed
     * @return version of the MSI package installed
     * @throws DataGridRuleException if cannot execute rule that gets the MSI package version
     * @throws DataGridConnectionRefusedException if cannot connect to the grid
     */
    void setMSIVersionForServer(DataGridServer server) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Checks whether or not the MSI package version installed, if any, is compatible with this version of the Web App.
     *
     * @param resource resource name
     * @return True, if the MSI package version installed is compatible with this version of the Web App. Otherwise, False.
     */
    boolean isMSIAPICompatibleInResc(String resource);
}
