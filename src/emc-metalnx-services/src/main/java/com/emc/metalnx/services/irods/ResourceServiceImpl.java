/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridResourceType;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.entity.enums.DataGridResourceTypeEnum;
import com.emc.metalnx.core.domain.entity.enums.DataGridServerType;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.MachineInfoService;
import com.emc.metalnx.services.interfaces.ResourceService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.ResourceAO;
import org.irods.jargon.core.pub.ZoneAO;
import org.irods.jargon.core.pub.domain.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.UnknownHostException;
import java.util.*;

@Service("resourceService")
@Transactional
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private IRODSServices irodsServices;

    @Autowired
    private MachineInfoService machineInfoService;

    private static final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);

    @Override
    public List<DataGridResource> findAll() throws DataGridConnectionRefusedException {
        logger.info("Find all resources in the grid");

        List<DataGridResource> dataGridResources = new ArrayList<DataGridResource>();
        ResourceAO resourceAO = irodsServices.getResourceAO();

        try {
            for (Resource irodsResource : resourceAO.findAll()) {
                DataGridResource newDataGridResource = getDataGridResource(irodsResource);
                dataGridResources.add(newDataGridResource);
            }
        }
        catch (JargonException e) {
            logger.error("Could not find all resources: ", e);
        }

        // sorting this list alphabetically
        Collections.sort(dataGridResources);
        return dataGridResources;
    }

    @Override
    public List<DataGridResource> findFirstLevelResources() throws DataGridConnectionRefusedException {
        logger.info("Find all first level resources in the grid");

        List<DataGridResource> firstLevelResources = new ArrayList<>();
        List<DataGridResource> allResources = findAll();

        for (DataGridResource resc : allResources) {
            if (resc.isFirstLevelResc()) {
                firstLevelResources.add(resc);
            }
        }

        return firstLevelResources;
    }

    @Override
    public DataGridResource find(String resourceName) throws DataGridConnectionRefusedException {
        logger.info("Find specific resource by name");

        if (resourceName == null || resourceName.isEmpty()) {
            return null;
        }

        DataGridResource resc = null;

        try {
            ResourceAO resourceAO = irodsServices.getResourceAO();
            Resource irodsResource = resourceAO.findByName(resourceName);

            List<DataGridResource> resources = findAll();

            for (DataGridResource r : resources) {
                if (resourceName.equals(r.getName())) {
                    irodsResource.setParentName(r.getParent());
                    break;
                }
            }

            resc = getDataGridResource(irodsResource);
        }
        catch (JargonException e) {
            logger.error("Could not find a resource named " + resourceName);
        }

        return resc;
    }

    private DataGridResource getDataGridResource(Resource irodsResource) {
        long irodsResourceId = Long.valueOf(irodsResource.getId());
        String irodsResourceName = irodsResource.getName();
        String irodsResourceZone = irodsResource.getZone().getZoneName();
        String irodsResourceType = irodsResource.getType();
        String irodsResourcePath = irodsResource.getVaultPath();
        long irodsResourceFreeSpace = irodsResource.getFreeSpace();
        Date irodsResourceFreeSpaceTimeStamp = irodsResource.getFreeSpaceTime();
        List<String> irodsResourceChildren = irodsResource.getImmediateChildren();
        String irodsResourceParent = irodsResource.getParentName();
        String irodsResourceStatus = irodsResource.getStatus();
        String irodsResourceHost = irodsResource.getLocation();
        Date irodsResourceCreateTime = irodsResource.getCreateTime();
        Date irodsResourceModifyTime = irodsResource.getModifyTime();
        String irodsResourceInfo = irodsResource.getInfo();
        String irodsContextString = irodsResource.getContextString();
        int irodsResourceTotalRecords = irodsResource.getTotalRecords();

        if (irodsResourceParent == null || irodsResourceParent.isEmpty()) {
            irodsResourceParent = irodsServices.getCurrentUserZone();
        }

        if (irodsResourceType == null || irodsResourceType.isEmpty()) {
            irodsResourceType = irodsContextString;
        }

        DataGridResource newDataGridResource = new DataGridResource(irodsResourceId, irodsResourceName, irodsResourceZone, irodsResourceType,
                irodsResourcePath, irodsResourceFreeSpace, irodsResourceFreeSpaceTimeStamp, irodsResourceChildren, irodsResourceParent,
                irodsResourceStatus, irodsResourceHost, irodsResourceCreateTime, irodsResourceModifyTime, irodsResourceInfo,
                irodsResourceTotalRecords, irodsContextString);

        return newDataGridResource;

    }

    @Override
    public List<DataGridResourceType> listResourceTypes() {

        List<DataGridResourceType> dataGridResourceTypes = new ArrayList<DataGridResourceType>();
        dataGridResourceTypes.add(new DataGridResourceType("Replication", "replication", DataGridResourceTypeEnum.IRODS_COORDINATING));
        dataGridResourceTypes.add(new DataGridResourceType("Round Robin", "roundrobin", DataGridResourceTypeEnum.IRODS_COORDINATING));
        dataGridResourceTypes.add(new DataGridResourceType("Load Balanced", "load_balanced", DataGridResourceTypeEnum.IRODS_COORDINATING));
        dataGridResourceTypes.add(new DataGridResourceType("Compound", "compound", DataGridResourceTypeEnum.IRODS_COORDINATING));
        dataGridResourceTypes.add(new DataGridResourceType("Random", "random", DataGridResourceTypeEnum.IRODS_COORDINATING));
        dataGridResourceTypes.add(new DataGridResourceType("Passthru", "passthru", DataGridResourceTypeEnum.IRODS_COORDINATING));

        dataGridResourceTypes.add(new DataGridResourceType("Unix File System", "unixfilesystem", DataGridResourceTypeEnum.IRODS_STORAGE));
        dataGridResourceTypes.add(new DataGridResourceType("Universal Mass Storage", "univmss", DataGridResourceTypeEnum.IRODS_STORAGE));
        dataGridResourceTypes.add(new DataGridResourceType("MSO", "mso", DataGridResourceTypeEnum.IRODS_STORAGE));
        dataGridResourceTypes.add(new DataGridResourceType("MSSOFile", "mssofile", DataGridResourceTypeEnum.IRODS_STORAGE));
        dataGridResourceTypes.add(new DataGridResourceType("Mockarchive", "mockarchive", DataGridResourceTypeEnum.IRODS_STORAGE));
        dataGridResourceTypes.add(new DataGridResourceType("Non-blocking", "nonblocking", DataGridResourceTypeEnum.IRODS_STORAGE));
        dataGridResourceTypes.add(new DataGridResourceType("Deferred", "deferred", DataGridResourceTypeEnum.IRODS_STORAGE));
        dataGridResourceTypes.add(new DataGridResourceType("Struct file", "structfile", DataGridResourceTypeEnum.IRODS_STORAGE));
        dataGridResourceTypes.add(new DataGridResourceType("EMC Isilon", "isilon", DataGridResourceTypeEnum.IRODS_STORAGE));
        dataGridResourceTypes.add(new DataGridResourceType("EMC ECS", "ecs", DataGridResourceTypeEnum.IRODS_STORAGE));
        dataGridResourceTypes.add(new DataGridResourceType("WOS", "wos", DataGridResourceTypeEnum.IRODS_STORAGE));

        Collections.sort(dataGridResourceTypes);
        return dataGridResourceTypes;
    }

    @Override
    public List<String> getImmediateChildren(String resourceName) throws DataGridConnectionRefusedException {

        try {
            ResourceAO resourceAO = irodsServices.getResourceAO();

            Resource irodsResource = resourceAO.findByName(resourceName);

            return irodsResource.getImmediateChildren();
        }
        catch (JargonException e) {
            logger.error("Could not get immediate children of resource " + resourceName + ": ", e);
        }

        return new ArrayList<String>();
    }

    @Override
    public boolean createResource(DataGridResource newDataGridResource) throws DataGridConnectionRefusedException {

        try {
            ResourceAO resourceAO = irodsServices.getResourceAO();
            ZoneAO zoneAO = irodsServices.getZoneAO();

            // mapping data grid resource to iRODS Resource
            Resource irodsResource = new Resource();
            irodsResource.setName(newDataGridResource.getName());
            irodsResource.setType(newDataGridResource.getType());
            irodsResource.setZone(zoneAO.getZoneByName(newDataGridResource.getZone()));
            irodsResource.setCreateTime(newDataGridResource.getCreateTime());
            irodsResource.setModifyTime(newDataGridResource.getModifyTime());
            irodsResource.setStatus(newDataGridResource.getStatus());
            irodsResource.setInfo(newDataGridResource.getInfo());
            irodsResource.setParentName(newDataGridResource.getParent());
            irodsResource.setVaultPath(newDataGridResource.getPath());
            irodsResource.setLocation(newDataGridResource.getHost());

            // context string is not always set
            if (newDataGridResource.getContextString() != null) {
                irodsResource.setContextString(newDataGridResource.getContextString());
            }

            // adding the new resource to iRODS
            resourceAO.addResource(irodsResource);

            return true;

        }
        catch (JargonException e) {
            logger.error("Could not create resource: ", e);
        }

        return false;
    }

    @Override
    public boolean deleteResource(String resourceName) {
        if (resourceName.isEmpty()) {
            logger.error("Could not delete resource: name cannot be empty.");
            return false;
        }

        boolean isResourceDeleted = true;

        try {
            DataGridResource dgRescToRemove = find(resourceName);

            if (dgRescToRemove == null) {
                logger.error("Could not delete resource: resource {} not found.", resourceName);
                return false;
            }

            ResourceAO resourceAO = irodsServices.getResourceAO();

            // if resource has a parent, it is necessary remove the relationship between that resource and its parent
            if (!dgRescToRemove.isFirstLevelResc()) {
                resourceAO.removeChildFromResource(dgRescToRemove.getParent(), resourceName);
            }

            deleteChildrenFromResource(dgRescToRemove);

            resourceAO.deleteResource(resourceName);
        }
        catch (Exception e) {
            logger.error("Could not delete resource " + resourceName + ": ", e);
            isResourceDeleted = false;
        }

        return isResourceDeleted;
    }

    public void deleteChildrenFromResource(DataGridResource dgRescToRemove) throws DataGridConnectionRefusedException {
        ResourceAO resourceAO = irodsServices.getResourceAO();

        try {
            List<String> childrenResources = dgRescToRemove.getChildren();
            if (childrenResources != null && !childrenResources.isEmpty()) {
                for (String childResource : childrenResources) {
                    resourceAO.removeChildFromResource(dgRescToRemove.getName(), childResource);
                }
            }
        }
        catch (JargonException e) {
            logger.error("Could not delete children from resource {}.", dgRescToRemove.getName());
        }
    }

    @Override
    public boolean updateResource(String resourceName, List<String> childrenToBeAdded, List<String> childrenToBeRemoved) {

        return false;
    }

    @Override
    public boolean addChildToResource(String resourceName, String child) {
        try {
            ResourceAO resourceAO = irodsServices.getResourceAO();

            // adding new children to the resource
            resourceAO.addChildToResource(resourceName, child, "");

            return true;
        }
        catch (Exception e) {
            logger.error("Could not add children to the " + resourceName + " resource: ", e);
        }

        return false;
    }

    @Override
    public List<DataGridServer> getAllResourceServers(List<DataGridResource> resources) throws DataGridConnectionRefusedException {

        logger.info("Getting all resource servers");

        List<DataGridServer> servers = new ArrayList<>();
        boolean isResourceWithEmptyHost = false;

        for (DataGridResource resource : resources) {

            logger.debug("Listing resource information: {}", resource);

            if (resource.getContextString().contains("isi_host")) {
                continue;
            }

            else if (!resource.getHost().isEmpty()) {

                DataGridServer server = new DataGridServer();

                try {
                    server.setHostname(resource.getHost());
                    server.setIp(machineInfoService.getAddress(resource.getHost()));
                    server.setResources(getResourcesOfAServer(server.getHostname(), resources));
                }
                catch (UnknownHostException e) {
                    logger.error("Could not retrieve IP address for [{}]", resource.getHost());
                    isResourceWithEmptyHost = true;
                }
                catch (DataGridConnectionRefusedException e) {
                    logger.error("Could not get all resources of the server: ", resource.getHost());
                    server.setResources(null);
                }

                if (!isResourceWithEmptyHost && !servers.contains(server)) {
                    servers.add(server);
                }
            }

            isResourceWithEmptyHost = false;
        }

        Collections.sort(servers);

        return servers;
    }

    @Override
    public List<DataGridServer> getAllIsilonServers(List<DataGridResource> resources) {
        logger.info("Getting all isilon servers");

        List<DataGridServer> isilonServers = new ArrayList<>();

        for (DataGridResource resource : resources) {
            logger.debug("Listing resource information: {}", resource);

            if (resource.getContextString().contains("isi_host")) {

                DataGridServer isilonServer = new DataGridServer();

                String isilonHostIp = getIsilonProperties(resource.getContextString()).get("isi_host");

                if (isilonHostIp == null) {
                    isilonHostIp = new String("");
                }

                isilonServer.setHostname(isilonHostIp);
                isilonServer.setIp(isilonHostIp);
                isilonServer.setType(DataGridServerType.ISILON);

                if (!isilonServers.contains(isilonServer)) {
                    isilonServers.add(isilonServer);
                }
            }
        }

        Collections.sort(isilonServers);

        logger.info("Isilon servers done");

        return isilonServers;
    }

    @Override
    public List<DataGridResource> getResourcesOfAServer(String serverName) throws DataGridConnectionRefusedException {
        logger.info("Get resources of a specific server (NOT the using cache)");
        return getResourcesOfAServer(serverName, null);
    }

    @Override
    public List<DataGridResource> getResourcesOfAServer(String serverName, List<DataGridResource> dataGridResources)
            throws DataGridConnectionRefusedException {
        logger.info("Get resources of a specific server (using cache)");

        if (dataGridResources == null || dataGridResources.isEmpty()) {
            logger.info("No cache provided. Calling the grid.");
            dataGridResources = findAll();
        }

        List<DataGridResource> dataGridResourcesOfAServer = new ArrayList<DataGridResource>();

        for (DataGridResource dataGridResource : dataGridResources) {
            if (dataGridResource.getHost().compareTo(serverName) == 0) {
                dataGridResourcesOfAServer.add(dataGridResource);
            }
        }

        return dataGridResourcesOfAServer;
    }

    private HashMap<String, String> getIsilonProperties(String contextString) {
        HashMap<String, String> properties = new HashMap<String, String>();

        String parts[] = contextString.split(";");
        for (String property : parts) {
            String propertyParts[] = property.split("=");
            if (propertyParts.length == 2) {
                properties.put(propertyParts[0], propertyParts[1]);
            }
            else {
                properties.put(propertyParts[0], new String(""));
            }
        }

        return properties;
    }

}
