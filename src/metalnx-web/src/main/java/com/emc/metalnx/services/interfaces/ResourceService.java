 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridResourceType;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

import java.util.List;

public interface ResourceService {

    /**
     * Deletes all children resources from a specific resource.
     * @param dgRescToRemove resource to remove the children from
     * @throws DataGridConnectionRefusedException if Metalnx cannot communicate with the grid.
     */
    void deleteChildrenFromResource(DataGridResource dgRescToRemove) throws DataGridConnectionRefusedException;

    /**
     * Get all resources existing in the data grid
     *
     * @return List of resources
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridResource> findAll() throws DataGridConnectionRefusedException;

    /**
     * Returns all the first-level resources for direct access.
     *
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridResource> findFirstLevelResources() throws DataGridConnectionRefusedException;

    /**
     * Look for a specific resource existing in the data grid by matching the name parameter
     *
     * @return The resource found or null if no resource matches the name
     * @throws DataGridConnectionRefusedException
     */
    public DataGridResource find(String resourceName) throws DataGridConnectionRefusedException;

    /**
     * List all resource types available in the data grid
     *
     * @return list with all resource types
     */
    public List<DataGridResourceType> listResourceTypes();

    /**
     * Get immediate children of a given resource
     *
     * @return list with resources names
     * @throws DataGridConnectionRefusedException
     */
    public List<String> getImmediateChildren(String resourceName) throws DataGridConnectionRefusedException;

    /**
     * Create a new resource in the data grid
     *
     * @throws DataGridConnectionRefusedException
     */
    public boolean createResource(DataGridResource newDataGridResource) throws DataGridConnectionRefusedException;

    /**
     * Delete a resource from the data grid
     *
     * @param resourceName
     * @return True, if the resource given was deleted. False, otherwise.
     */
    public boolean deleteResource(String resourceName);

    /**
     * Update resource children
     *
     * @param resourceName
     * @param childrenToBeAdded
     * @param childrenToBeRemoved
     * @return True, if the resource was updated successfully. False, otherwise.
     */
    public boolean updateResource(String resourceName, List<String> childrenToBeAdded, List<String> childrenToBeRemoved);

    /**
     * Add child to a given resource
     *
     * @param child
     * @return True, if a child was added to a resource. False, otherwise
     */
    public boolean addChildToResource(String resourceName, String child);

    /**
     * Get all resource servers existing in the data grid sorted alphabetically
     *
     * @param resources
     *            list of all resources existing in the grid
     * @return list of all servers
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridServer> getAllResourceServers(List<DataGridResource> resources) throws DataGridConnectionRefusedException;

    /**
     * Get all Isilon servers existing in the data grid sorted alphabetically
     *
     * @param resources
     *            list of all resources existing in the grid
     * @return list of all Isilon servers
     */
    public List<DataGridServer> getAllIsilonServers(List<DataGridResource> resources);

    /**
     * Get all resources from a particular server.
     *
     * @param serverName
     *            name of the server to find resources
     * @return List<String>
     *         list that contains all paths where resources are mounted on
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridResource> getResourcesOfAServer(String serverName) throws DataGridConnectionRefusedException;

    /**
     * Get all resources from a particular server. This method requires a resource cache to speed up the process by
     * avoiding a call to the grid.
     *
     * @param serverName
     *            name of the server to find resources
     * @param dataGridResources
     *            list of all current resources in the grid
     * @return List<String>
     *         list that contains all paths where resources are mounted on
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridResource> getResourcesOfAServer(String serverName, List<DataGridResource> dataGridResources)
            throws DataGridConnectionRefusedException;
}
