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

package com.emc.metalnx.services.machine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StorageServiceImpl implements StorageService {
    @Autowired
    ResourceService resourceService;

    // block size
    private int blockSize = 1024;

    private static final Logger logger = LoggerFactory.getLogger(StorageServiceImpl.class);

    @Override
    public long totalUsedStorageOfGrid(List<DataGridServer> servers) {
        logger.info("Get total used storage of the grid");

        long totalUsedStorage = 0;

        for (DataGridServer dataGridServer : servers) {
            totalUsedStorage += dataGridServer.getTotalStorageUsed();
        }

        return totalUsedStorage;
    }

    @Override
    public long totalAvailableStorageOfGrid(List<DataGridServer> servers) {
        logger.info("Get total available storage of the grid");

        long totalAvailableStorage = 0;

        for (DataGridServer dataGridServer : servers) {
            totalAvailableStorage += dataGridServer.getTotalStorageAvailable();
        }

        return totalAvailableStorage;
    }

    @Override
    public long totalUsedStorageOfAServer(String hostname, String diskInfoJson, List<DataGridResource> currentServerResources)
            throws DataGridConnectionRefusedException {
        logger.info("Get total used storage of a specific server {}", hostname);

        long totalUsed = 0;

        if (hostname == null || diskInfoJson == null || hostname.isEmpty() || diskInfoJson.isEmpty()) {
            return totalUsed;
        }

        HashMap<String, String> usedMap = null;
        List<DataGridResource> dataGridResources = null;

        try {
            dataGridResources = resourceService.getResourcesOfAServer(hostname, currentServerResources);
        }
        catch (DataGridConnectionRefusedException e) {
            logger.error("Could not get resources from server ", hostname);

            // if it is no possible to retrieve the resources of a server due to a connect
            // exception,
            // we will use all known resources of this server stored in cache
            if (currentServerResources != null) {
                dataGridResources = currentServerResources;
            }
            else {
                dataGridResources = new ArrayList<DataGridResource>();
            }
        }

        try {
            usedMap = createMapPartitionAndAmountUsed(diskInfoJson);

            for (String mountedOn : usedMap.keySet()) {
                boolean foundResc = false;
                for (DataGridResource dataGridResource : dataGridResources) {
                    String resourcePath = dataGridResource.getPath();
                    if (resourcePath.startsWith(mountedOn)) {
                        totalUsed += Long.parseLong(usedMap.get(mountedOn));
                        foundResc = true;
                        dataGridResources.remove(dataGridResource);
                        break;
                    }
                }
                if (foundResc) {
                    continue;
                }
            }

        }
        catch (JsonProcessingException e) {
            logger.info("Could not parse total used storage information for: ", hostname);
        }
        catch (IOException e) {
            logger.info("Could not parse total used storage information for: ", hostname);
        }
        catch (NumberFormatException e) {
            logger.info("Could not format String:{} ", e.getMessage());
        }

        return totalUsed * blockSize;
    }

    @Override
    public long totalAvailableStorageOfAServer(String hostname, String diskInfoJson, List<DataGridResource> currentServerResources)
            throws DataGridConnectionRefusedException {
        logger.info("Get total available storage of a specific server {}", hostname);

        long totalAvailable = 0;

        if (hostname == null || diskInfoJson == null || hostname.isEmpty() || diskInfoJson.isEmpty()) {
            return totalAvailable;
        }

        HashMap<String, String> availableMap = null;
        List<DataGridResource> dataGridResources = null;

        try {
            dataGridResources = resourceService.getResourcesOfAServer(hostname, currentServerResources);
        }
        catch (DataGridConnectionRefusedException e) {
            logger.error("Could not get resources from server ", hostname);

            // if it is no possible to retrieve the resources of a server due to a connect
            // exception,
            // we will use all known resources of this server stored in cache
            if (currentServerResources != null) {
                dataGridResources = currentServerResources;
            }
            else {
                dataGridResources = new ArrayList<DataGridResource>();
            }
        }

        try {
            availableMap = createMapPartitionAndAmountAvailable(diskInfoJson);

            for (String mountedOn : availableMap.keySet()) {
                boolean foundResc = false;
                for (DataGridResource dataGridResource : dataGridResources) {
                    String resourcePath = dataGridResource.getPath();
                    if (resourcePath.startsWith(mountedOn)) {
                        totalAvailable += Long.parseLong(availableMap.get(mountedOn));
                        foundResc = true;
                        dataGridResources.remove(dataGridResource);
                        break;
                    }
                }
                if (foundResc) {
                    continue;
                }
            }

        }
        catch (JsonProcessingException e) {
            logger.info("Could not parse total available storage information for: ", hostname);
        }
        catch (IOException e) {
            logger.info("Could not parse total available storage information for: ", hostname);
        }
        catch (NumberFormatException e) {
            logger.info("Could not format String:{} ", e.getMessage());
        }

        return totalAvailable * blockSize;
    }

    /**
     * Creates a Hash mapping where a partition is mounted and the amount of used storage
     *
     * @param partitionsLocation
     *            JSON with machine disk status
     * @return Hash Map
     * @throws IOException
     * @throws JsonProcessingException
     */
    private HashMap<String, String> createMapPartitionAndAmountUsed(String partitionsLocation) throws JsonProcessingException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(partitionsLocation);

        HashMap<String, String> partNameMountedMap = new HashMap<String, String>();

        Iterator<String> fieldNames = json.fieldNames();
        while (fieldNames.hasNext()) {
            JsonNode jo = json.get(fieldNames.next());
            String mounted_on = jo.get("mounted_on").textValue();
            String used = jo.get("used").textValue();
            partNameMountedMap.put(mounted_on, used);
        }

        return partNameMountedMap;
    }

    /**
     * Creates a Hash mapping where a partition is mounted and the amount of available storage
     *
     * @param partitionsLocation
     *            JSON with machine disk status
     * @return
     * @throws IOException
     * @throws JsonProcessingException
     */
    private HashMap<String, String> createMapPartitionAndAmountAvailable(String partitionsLocation) throws JsonProcessingException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(partitionsLocation);

        HashMap<String, String> partNameMountedMap = new HashMap<String, String>();

        Iterator<String> fieldNames = json.fieldNames();
        while (fieldNames.hasNext()) {
            JsonNode jo = json.get(fieldNames.next());
            String mounted_on = jo.get("mounted_on").textValue();
            String available = jo.get("available").textValue();
            partNameMountedMap.put(mounted_on, available);
        }

        return partNameMountedMap;
    }
}
