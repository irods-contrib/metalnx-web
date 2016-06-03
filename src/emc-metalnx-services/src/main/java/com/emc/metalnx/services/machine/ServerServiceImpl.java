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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.MachineInfoService;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.ServerService;
import com.emc.metalnx.services.interfaces.StorageService;
import com.emc.metalnx.services.machine.enums.ServerRequestInfoType;
import com.emc.metalnx.services.machine.util.DataGridServerStatusComparator;
import com.emc.metalnx.services.machine.util.ServerInformationRetrievalThread;
import com.emc.metalnx.services.machine.util.ServerUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ServerServiceImpl implements ServerService {

    @Autowired
    ResourceService resourceService;

    @Autowired
    StorageService storageService;

    @Autowired
    MachineInfoService machineInfoService;

    @Value("${rmd.connection.timeout}")
    private String rmdConnectionTimeout;

    @Value("${rmd.connection.port}")
    private String rmdConnectionPort;

    @Value("${reverse.dns.lookup}")
    private String reverseDnsLookup;

    // Cache attributes
    private Hashtable<String, String> serverInfoCache = new Hashtable<String, String>();
    private Hashtable<String, Long> cacheTimeToLive = new Hashtable<String, Long>();

    private List<ServerInformationRetrievalThread> threadList = new ArrayList<ServerInformationRetrievalThread>();

    private static final Logger logger = LoggerFactory.getLogger(ServerServiceImpl.class);

    @Override
    public List<DataGridServer> getAllServers(List<DataGridResource> resources, HashMap<String, DataGridServer> serverMapInCache)
            throws DataGridConnectionRefusedException {
        logger.info("Getting the list of all servers of the Grid");

        List<DataGridServer> servers = resourceService.getAllResourceServers(resources);

        initServerThreads(servers);

        for (DataGridServer server : servers) {
            String serverHostName = server.getHostname();

            List<DataGridResource> serverResources = null;

            // Getting general status of the server

            String serverInfo = getServerInfo(serverHostName);
            ObjectMapper mapper = new ObjectMapper();
            String machineStatus = null;
            String diskInfo = null;
            String rmdInfo = null;
            try {
                JsonNode json = mapper.readTree(serverInfo);
                machineStatus = json.get("serverstatus").toString();
                diskInfo = json.get("disk").toString();
                rmdInfo = json.get("version").toString();

                serverResources = serverMapInCache.get(serverHostName).getResources();
            }
            catch (JsonProcessingException e) {
                logger.error("Could not parse server information", e);
            }
            catch (IOException e) {
                logger.error("Could not parse server information", e);
            }
            catch (NullPointerException e) {
                logger.debug("Hostname not found in cache, need to resolve it.");
            }

            ServerUtil.populateDataGridServerStatus(machineStatus, server);
            ServerUtil.setDataGridServerRMDInfo(rmdInfo, server);

            // Retrieving storage info
            long totalStorageAvailable = storageService.totalAvailableStorageOfAServer(serverHostName, diskInfo, serverResources);

            long totalStorageUsed = storageService.totalUsedStorageOfAServer(serverHostName, diskInfo, serverResources);

            long totalStorage = totalStorageAvailable + totalStorageUsed;

            server.setTotalStorage(totalStorage);
            server.setTotalStorageAvailable(totalStorageAvailable);
            server.setTotalStorageUsed(totalStorageUsed);
        }

        Collections.sort(servers, new DataGridServerStatusComparator());

        return servers;
    }

    @Override
    public List<DataGridServer> getAllIsilonServers(List<DataGridResource> resources) {
        logger.info("Getting the list of all isilon servers of the Grid");
        List<DataGridServer> isilonServers = resourceService.getAllIsilonServers(resources);

        Collections.sort(isilonServers, new DataGridServerStatusComparator());

        return isilonServers == null || isilonServers.isEmpty() ? null : isilonServers;
    }

    @Override
    public List<DataGridServer> getAllNonResourceServers(List<DataGridServer> servers, HashMap<String, DataGridServer> serverMapInCache)
            throws DataGridConnectionRefusedException {
        logger.info("Getting the list of all non-resource servers from the Grid");
        return getAllNonResourceServers(servers, serverMapInCache, null);
    }

    @Override
    public List<DataGridServer> getAllNonResourceServers(List<DataGridServer> servers, HashMap<String, DataGridServer> serverMapInCache,
            List<DataGridResource> dataGridResources) throws DataGridConnectionRefusedException {
        logger.info("Getting the list of all non-resource servers from the Grid using Resource cache");

        List<DataGridServer> nonResourceServers = new ArrayList<DataGridServer>();

        for (DataGridServer server : servers) {

            if (server.getHostname().compareToIgnoreCase("localhost") == 0) {
                continue;
            }

            ObjectMapper mapper = new ObjectMapper();
            String mounts = null;
            try {
                String serverInfo = getServerInfo(server.getHostname());
                JsonNode json = mapper.readTree(serverInfo);
                mounts = json.get("mounts").toString();
            }
            catch (JsonProcessingException e) {
                logger.error("Could not parse server information", e.getMessage());
            }
            catch (IOException e) {
                logger.error("Could not parse server information", e.getMessage());
            }

            if (mounts != null && !mounts.isEmpty()) {
                HashMap<String, String> hashMap = ServerUtil.getNFSMountMap(mounts);
                List<DataGridResource> resources = null;

                try {
                    logger.info("Getting resources of a server {}", server.getHostname());
                    resources = resourceService.getResourcesOfAServer(server.getHostname(), dataGridResources);
                }
                catch (DataGridConnectionRefusedException e) {
                    // if it is not possible to retrieve all resources existing in a server due to
                    // a connect exception, we will need to use the resources stored in cache, if
                    // any
                    if (serverMapInCache != null) {
                        resources = serverMapInCache.get(server.getHostname()).getResources();
                    }
                    else {
                        resources = new ArrayList<DataGridResource>();
                    }
                }

                for (DataGridResource resource : resources) {
                    if (hashMap.containsKey(resource.getPath())) {
                        String host = hashMap.get(resource.getPath());
                        DataGridServer newServer = new DataGridServer();
                        try {
                            host = reverseDnsLookup.compareTo("true") == 0 ? machineInfoService.getHostName(host) : host;
                            newServer.setHostname(host);
                            newServer.setIp(machineInfoService.getAddress(host));
                        }
                        catch (UnknownHostException e) {
                            logger.error("Could not resolve IP address for " + host);
                        }

                        if (!nonResourceServers.contains(newServer)) {
                            nonResourceServers.add(newServer);
                        }
                    }
                }
            }

        }

        return nonResourceServers.isEmpty() ? null : nonResourceServers;
    }

    /********************************************************************************************/
    /* CACHE METHODS */
    /********************************************************************************************/

    private void initServerThreads(List<DataGridServer> servers) {
        for (DataGridServer server : servers) {

            // Instantiating thread
            ServerInformationRetrievalThread t = new ServerInformationRetrievalThread(server.getHostname(), rmdConnectionPort,
                    ServerRequestInfoType.ALL, Integer.parseInt(rmdConnectionTimeout));

            // Starting thread execution
            t.start();

            // Keeping track of the thread instance
            synchronized (threadList) {
                threadList.add(t);
            }
        }

        synchronized (threadList) {
            for (ServerInformationRetrievalThread t : threadList) {
                try {
                    t.join();
                    String results = t.getResult();

                    if (results == null) {
                        results = "";
                    }

                    serverInfoCache.put(t.getServerHost(), results);
                    cacheTimeToLive.put(t.getServerHost(), System.currentTimeMillis() + 5 * 1000);

                }
                catch (InterruptedException e) {
                    logger.error("Could not get server information on [{}]", t.getServerHost(), e);
                }
            }
        }

        synchronized (threadList) {
            threadList.clear();
        }
    }

    private String getServerInfo(String hostname) {
        logger.debug("Getting Server Info for [{}]", hostname);
        long currentTime = System.currentTimeMillis();

        if (serverInfoCache.containsKey(hostname) && currentTime < cacheTimeToLive.get(hostname)) {
            logger.debug("Cache hit for [{}]", hostname);
            return serverInfoCache.get(hostname);
        }
        else if (currentTime > cacheTimeToLive.get(hostname)) {

            // Invalidating entries
            serverInfoCache.remove(hostname);
            cacheTimeToLive.remove(hostname);
        }

        logger.debug("Cache miss for [{}]", hostname);
        logger.info("Getting all metrics from [{}]", hostname);

        ServerInformationRetrievalThread serverInfoThread = new ServerInformationRetrievalThread(hostname, rmdConnectionPort,
                ServerRequestInfoType.ALL, Integer.parseInt(rmdConnectionTimeout));
        serverInfoThread.start();
        try {
            serverInfoThread.join();
        }
        catch (InterruptedException e) {
            logger.error("Could not get server information on [{}]", serverInfoThread.getServerHost(), e);
        }

        String results = serverInfoThread.getResult();

        if (results != null) {
            serverInfoCache.put(hostname, results);

            // Settings time to live for entry
            cacheTimeToLive.put(hostname, System.currentTimeMillis() + 5 * 1000);
        }

        return results;
    }

    /********************************************************************************************/
    /* END CACHE METHODS */
    /********************************************************************************************/

}
