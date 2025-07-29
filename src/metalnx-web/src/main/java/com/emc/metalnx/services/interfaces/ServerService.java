 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

import java.util.HashMap;
import java.util.List;

public interface ServerService {

    /**
     * Gets all (resource) servers from the grid.
     *
     * @param resources
     *            list of all resources existing in the grid
     * @param serverMapInCache
     *            host name, DataGridServer object map stored in cache
     * @return list of DataGridServer
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridServer> getAllServers(List<DataGridResource> resources, HashMap<String, DataGridServer> serverMapInCache)
            throws DataGridConnectionRefusedException;

    /**
     * Gets all (resource) servers from the grid.
     *
     * @param resources
     *            list of all resources existing in the grid
     * @return list of DataGridServer
     */
    public List<DataGridServer> getAllIsilonServers(List<DataGridResource> resources);

    /**
     * Gets all non-resource servers from the grid.
     *
     * @param servers
     *            list of servers existing in the grid
     * @param serverMapInCache
     *            host name, DataGridServer object map stored in cache
     * @return list of DataGridServer
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridServer> getAllNonResourceServers(List<DataGridServer> servers, HashMap<String, DataGridServer> serverMapInCache)
            throws DataGridConnectionRefusedException;

    /**
     * Gets all non-resource servers from the grid using a resource cache. This cache is used to avoid
     * a call to the grid.
     *
     * @param servers
     *            list of servers existing in the grid
     * @param serverMapInCache
     *            host name, DataGridServer object map stored in cache
     * @param dataGridResources
     *            list of all current resources existing in the grid
     * @return list of servers
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridServer> getAllNonResourceServers(List<DataGridServer> servers, HashMap<String, DataGridServer> serverMapInCache,
            List<DataGridResource> dataGridResources) throws DataGridConnectionRefusedException;

}
