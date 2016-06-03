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

package com.emc.metalnx.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.auth.UserTokenDetails;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.ServerService;
import com.emc.metalnx.services.interfaces.StorageService;
import com.emc.metalnx.services.interfaces.TemplateService;
import com.emc.metalnx.services.interfaces.UserProfileService;
import com.emc.metalnx.services.interfaces.UserService;

@Controller
@RequestMapping(value = "/dashboard")
@Scope(WebApplicationContext.SCOPE_SESSION)
public class DashboardController {

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    @Autowired
    CollectionService collectionService;

    @Autowired
    ResourceService resourceService;

    @Autowired
    StorageService storageService;

    @Autowired
    ServerService serverService;

    @Autowired
    UserProfileService profileService;

    @Autowired
    TemplateService templateService;

    // list of all resource servers of the grid
    private List<DataGridServer> servers;

    // list of servers that are used as NFS for some resources
    private List<DataGridServer> nonResourceServers;

    // list of isilon servers
    private List<DataGridServer> isilonServers;

    // list of all resources existing in the grid
    private List<DataGridResource> resources;

    private HashMap<String, DataGridServer> serverMap;

    // ui mode that will be shown when the rods user switches mode from admin to user and vice-versa
    public static final String UI_USER_MODE = "user";
    public static final String UI_ADMIN_MODE = "admin";

    // check if the iCAT Server is responding
    public boolean isServerResponding;

    private int totalNumberOfUsers;
    private int totalNumberOfGroups;
    private int totalNumberOfFiles;
    private int totalNumberOfProfiles;
    private int totalNumberOfTemplates;

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(Model model, HttpServletRequest request) {
        try {
            resources = resourceService.findAll();

            isServerResponding = true;

            // Checking if user is Admin or normal user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth instanceof UsernamePasswordAuthenticationToken) {
                UserTokenDetails userDetails = (UserTokenDetails) auth.getDetails();

                // If user is not admin, redirect him to the user UI
                if (!userDetails.getUser().isAdmin()) {
                    request.getSession().setAttribute("uiMode", UI_USER_MODE);
                    return "redirect:/collections/";
                }
            }

            request.getSession().setAttribute("uiMode", UI_ADMIN_MODE);
            totalNumberOfUsers = userService.countAll();
            totalNumberOfGroups = groupService.countAll();
            totalNumberOfFiles = collectionService.countAll();
            totalNumberOfProfiles = profileService.countAll();
            totalNumberOfTemplates = templateService.countAll();

        }
        catch (DataGridConnectionRefusedException e) {
            logger.info("Could not connect to the server. ", e.getMessage());
            isServerResponding = false;
        }

        model.addAttribute("totalNumberOfUsers", totalNumberOfUsers);
        model.addAttribute("totalNumberOfGroups", totalNumberOfGroups);
        model.addAttribute("totalNumberOfFiles", totalNumberOfFiles);
        model.addAttribute("totalNumberOfProfiles", totalNumberOfProfiles);
        model.addAttribute("totalNumberOfTemplates", totalNumberOfTemplates);

        return "dashboard/dashboard";
    }

    @RequestMapping(value = "/servers/", method = RequestMethod.GET)
    public String getAllServers(Model model) throws DataGridConnectionRefusedException {
        logger.info("Get all servers from the grid");

        try {
            if (resources == null || resources.isEmpty()) {
                resources = resourceService.findAll();
            }

            isServerResponding = true;
        }
        catch (DataGridConnectionRefusedException e) {
            logger.info("Could not connect to the server.", e);

            isServerResponding = false;

            // if there is no cache available for displaying the servers, we will redirect
            // the user to the iCAT-not-responding page
            if (resources == null || resources.isEmpty()) {
                throw new DataGridConnectionRefusedException();
            }
        }
        finally {
            logger.info("Listing servers from the cache.");
            servers = serverService.getAllServers(resources, serverMap);
        }

        model.addAttribute("servers", servers);
        model.addAttribute("isRMDRunningOnAllServers", isRMDRunningOnAllServers());

        return "dashboard/servers :: serversList";
    }

    @RequestMapping(value = "/nonResourceServers/", method = RequestMethod.GET)
    public String getNonResourceServers(Model model) throws DataGridConnectionRefusedException {

        logger.info("Listing non resource servers from the cache.");
        nonResourceServers = serverService.getAllNonResourceServers(servers, serverMap, resources);
        model.addAttribute("nonResourceServers", nonResourceServers);

        return "dashboard/nonResourceServers :: nonResourceServers";
    }

    @RequestMapping(value = "/isilonServers/", method = RequestMethod.GET)
    public String getIsilonServers(Model model) throws DataGridConnectionRefusedException {
        boolean isAnyIsilonWithNotWellFormedContextString = false;

        logger.info("Listing isilon servers from the cache.");

        isilonServers = serverService.getAllIsilonServers(resources);

        // check if there is any isilon whose context string is not well formed
        isAnyIsilonWithNotWellFormedContextString = isAnyIsilonServerWithBadFormedContextString();

        model.addAttribute("isilonServers", isilonServers);
        model.addAttribute("isAnyIsilonWithNotWellFormedContextString", isAnyIsilonWithNotWellFormedContextString);

        return "dashboard/isilonServers :: isilonServersList";
    }

    @RequestMapping(value = "/systemHealth/", method = RequestMethod.GET)
    public String systemHealth(Model model) {
        String systemStatus = getSystemHealthStatus();
        model.addAttribute("systemStatus", systemStatus);

        return "dashboard/systemHealth :: systemHealth";
    }

    @RequestMapping(value = "/detail/{hostname}/", method = RequestMethod.GET)
    public String getDetailsForHostname(@PathVariable String hostname, Model model) throws DataGridConnectionRefusedException {

        DataGridServer server = serverMap.get(hostname);

        if (server != null) {
            if (isServerResponding) {
                server.setResources(resourceService.getResourcesOfAServer(hostname, resources));
            }
            // if the iCAT server is not respoding, we'll get the resources of a server from the cache
            else {
                server.setResources(serverMap.get(hostname).getResources());
            }
        }

        model.addAttribute("server", server);
        model.addAttribute("isServerResponding", isServerResponding);

        return "dashboard/details/index";
    }

    @RequestMapping(value = "/storageServer/", method = RequestMethod.POST, produces = { "text/plain" })
    @ResponseBody
    public String totalStorageByServer(Model model, @RequestParam("hostname") String hostname) {
        DataGridServer serverObj = serverMap.get(hostname);

        long totalUsedStorage = serverObj.getTotalStorageUsed();
        long totalAvailableStorage = serverObj.getTotalStorageAvailable();
        long totalStorage = totalUsedStorage + totalAvailableStorage;
        float usagePercentage = (float) totalUsedStorage / totalStorage * 100;

        return FileUtils.byteCountToDisplaySize(totalUsedStorage) + "/" + FileUtils.byteCountToDisplaySize(totalAvailableStorage) + "/"
                + FileUtils.byteCountToDisplaySize(totalStorage) + "/" + String.valueOf(usagePercentage);
    }

    @RequestMapping(value = "/storageGrid/", method = RequestMethod.GET, produces = { "text/plain" })
    @ResponseBody
    public String totalStorageGrid(Model model) {

        long totalUsedStorage = storageService.totalUsedStorageOfGrid(servers);
        long totalAvailableStorage = storageService.totalAvailableStorageOfGrid(servers);
        long totalStorage = totalUsedStorage + totalAvailableStorage;

        float usagePercentage = 0;

        if (totalStorage != 0) {
            usagePercentage = (float) totalUsedStorage / totalStorage * 100;
        }

        return FileUtils.byteCountToDisplaySize(totalUsedStorage) + "/" + FileUtils.byteCountToDisplaySize(totalAvailableStorage) + "/"
                + FileUtils.byteCountToDisplaySize(totalStorage) + "/" + String.valueOf(usagePercentage) + "/" + isRMDRunningOnAllServers();
    }

    @RequestMapping(value = "/resourceDetails/{resourceName}/", method = RequestMethod.GET)
    public String getDetailsForResource(@PathVariable String resourceName, Model model) throws DataGridConnectionRefusedException {

        DataGridResource resource = resourceService.find(resourceName);

        if (resource != null) {
            model.addAttribute("resource", resource);
        }
        else {
            model.addAttribute("resourceNotFound", resourceName);
        }

        return "dashboard/details/resourceInfo";
    }

    /*
     * ********************************************************************************************
     * ********************************* PRIVATE METHODS **************************************
     * ********************************************************************************************
     */

    /**
     * Checks if there is any isilon server that does not have a well formed context string.
     * If there is, this server is removed from the list of servers and will not be shown in the
     * view's server list.
     *
     * @return True, is there is at least one Isilon server that does not have a well formed string.
     *         False, otherwise.
     */
    private boolean isAnyIsilonServerWithBadFormedContextString() {
        if (isilonServers == null) {
            return false;
        }

        boolean isIsilonListWithAnyBadFormedContextString = false;
        Iterator<DataGridServer> isiServersIterator = isilonServers.iterator();
        while (isiServersIterator.hasNext()) {
            DataGridServer dataGridServer = isiServersIterator.next();

            if (dataGridServer.getHostname().isEmpty() || dataGridServer.getIp().isEmpty()) {
                isIsilonListWithAnyBadFormedContextString = true;
                isiServersIterator.remove();
            }
        }

        return isIsilonListWithAnyBadFormedContextString;
    }

    /**
     * Gets the system health status
     *
     * @return the status (normal, error or warning)
     */
    private String getSystemHealthStatus() {
        String status = "normal";
        for (DataGridServer server : servers) {
            serverMap.put(server.getHostname(), server);
            if (server.getMachineStatus().compareTo("error") == 0) {
                status = "error";
                break;
            }
            else if (server.getMachineStatus().compareTo("warning") == 0 && status.compareTo("normal") == 0) {
                status = "warning";
            }
        }
        return status;
    }

    /**
     * Checks if the RMD package is running on all data grid servers
     *
     * @return true, if the package is running on all servers. False, otherwise.
     */
    private boolean isRMDRunningOnAllServers() {
        boolean isRunning = true;

        serverMap = new HashMap<String, DataGridServer>();
        for (DataGridServer server : servers) {
            serverMap.put(server.getHostname(), server);

            if (!server.isRmdPackageRunning()) {
                isRunning = false;
            }
        }

        return isRunning;
    }
}
