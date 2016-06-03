/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.services.interfaces;

import java.util.HashMap;
import java.util.List;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

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
