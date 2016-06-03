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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridResourceType;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.entity.enums.DataGridResourceTypeEnum;
import com.emc.metalnx.core.domain.entity.enums.DataGridServerType;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.modelattribute.enums.URLMap;
import com.emc.metalnx.modelattribute.resource.ResourceForm;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.MachineInfoService;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.ServerService;
import com.emc.metalnx.services.interfaces.ZoneService;

@Controller
@RequestMapping(value = "/resources")
@Scope(WebApplicationContext.SCOPE_SESSION)
@SessionAttributes({ "sourcePaths" })
public class ResourceController {

    @Autowired
    ResourceService resourceService;

    @Autowired
    ZoneService zoneService;

    @Autowired
    ServerService serverService;

    @Autowired
    MachineInfoService machineInfoService;

    @Autowired
    IRODSServices irodsServices;

    @Value("${irods.host}")
    private String irodsHost;

    @Value("${irods.zoneName}")
    private String zoneName;

    private List<DataGridResource> dataGridResources = null;

    private static final String ISILON_CONTEXT_STRING = "isi_host=%s;isi_port=%s;isi_user=%s";

    private String treeImagePath = "../../images/Data-Network-48.png";
    private String treeImagePathForDashboard = "../images/Data-Network-48.png";
    private String zoneImagePath = "../../images/zone-48.png";
    private String zoneImagePathForDashBoard = "../images/zone-48.png";

    private static final Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @RequestMapping(value = "/")
    public String listResources(Model model) throws DataGridConnectionRefusedException {
        dataGridResources = resourceService.findAll();

        model.addAttribute("resources", dataGridResources);
        model.addAttribute("resultSize", dataGridResources.size());
        model.addAttribute("foundResources", dataGridResources.size() > 0);

        return "resources/resourceManagement";
    }

    /**
     * Lists all resources that match the parameter given
     *
     * @param model
     * @return
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/find/{resourceName}/")
    public String findResourcesByName(Model model, @PathVariable String resourceName) throws DataGridConnectionRefusedException {

        DataGridResource dataGridResource = resourceService.find(resourceName);
        model.addAttribute("resources", dataGridResource);
        model.addAttribute("resourceName", resourceName);

        return "resources/resourceList :: resourceList";
    }

    /**
     * Lists all resources
     *
     * @param model
     * @return
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/findAll/")
    public String findAllResources(Model model) throws DataGridConnectionRefusedException {
        if (dataGridResources == null || dataGridResources.isEmpty()) {
            dataGridResources = resourceService.findAll();
        }

        model.addAttribute("resources", dataGridResources);

        return "resources/resourceList :: resourceList";
    }

    /**
     * Shows the add resource form
     *
     * @param model
     * @return the template that renders the add resource from
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/add/")
    public String showAddResourceFormWithoutParentSet(Model model) throws DataGridConnectionRefusedException {

        addResourceForm(model);
        String parent = irodsServices.getCurrentUserZone();
        model.addAttribute("parent", parent);
        model.addAttribute("previousPage", "list");

        return "resources/resourceForm";
    }

    /**
     * Shows the add resource form with parent set
     *
     * @param model
     * @return the template that renders the add resource from
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/add/{parent}")
    public String showAddResourceFormWithParentSet(Model model, @PathVariable String parent) throws DataGridConnectionRefusedException {

        addResourceForm(model);
        model.addAttribute("parent", parent);
        model.addAttribute("previousPage", "map");

        return "resources/resourceForm";
    }

    private void addResourceForm(Model model) throws DataGridConnectionRefusedException {
        List<DataGridResource> dataGridResources = resourceService.findAll();

        List<DataGridResourceType> resourceTypes = resourceService.listResourceTypes();
        List<DataGridResourceType> coordinatingResources = getCoordinatingResources(resourceTypes);
        List<DataGridResourceType> storageResources = getStorageResources(resourceTypes);

        model.addAttribute("resourceForm", new ResourceForm());
        model.addAttribute("requestMapping", URLMap.URL_ADD_RESOURCE_ACTION);
        model.addAttribute("resources", dataGridResources);
        model.addAttribute("childrenList", new ArrayList<String>());
        model.addAttribute("coordinatingResources", coordinatingResources);
        model.addAttribute("storageResources", storageResources);
        model.addAttribute("zones", zoneService.findAll());
    }

    /**
     * Add a resource to the data grid
     *
     * @param model
     * @return the template that renders the add resource from
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/add/action/")
    public String addResource(@ModelAttribute ResourceForm resourceForm, HttpServletRequest httpServletRequest, RedirectAttributes redirectAttributes)
            throws DataGridConnectionRefusedException {

        String[] resourceType = (String[]) httpServletRequest.getParameterMap().get("resourceType");
        String[] resourceParent = (String[]) httpServletRequest.getParameterMap().get("resourceParent");
        String[] previousPage = (String[]) httpServletRequest.getParameterMap().get("previousPage");

        String type = resourceType[0];
        String parent = null;
        if (resourceParent != null) {
            parent = resourceParent[0];
            resourceForm.setParent(parent);
        }

        resourceForm.setType(type);

        DataGridResource newDataGridResource = getDataGridResource(resourceForm);

        boolean resourceCreatedSuccessfully = resourceService.createResource(newDataGridResource);

        // a parent resource can be any other existing resource, but it can also be set as
        // the zone, which means a resource has no parent.
        boolean isParentSetAndDifferentFromZone = false;

        if (parent != null && parent.compareTo(irodsServices.getCurrentUserZone()) != 0) {
            isParentSetAndDifferentFromZone = true;
        }

        // if a resource A was created in the data grid, now we are adding this resource A as a
        // child of another resource B
        if (resourceCreatedSuccessfully) {
            String child = newDataGridResource.getName();

            if (isParentSetAndDifferentFromZone) {
                resourceService.addChildToResource(parent, child);
            }
        }

        redirectAttributes.addFlashAttribute("resourceAddedSuccessfully", resourceForm.getName());

        if (previousPage[0].contains("map")) {
            return "redirect:/resources/map/";
        }

        return "redirect:/resources/";
    }

    /**
     * Shows the modify resource form
     *
     * @param model
     * @return the template that renders the add resource from
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/modify/{resourceName}/")
    public String showModifyResourceForm(Model model, @PathVariable String resourceName) throws DataGridConnectionRefusedException {
        if (dataGridResources == null || dataGridResources.isEmpty()) {
            dataGridResources = resourceService.findAll();
        }

        DataGridResource dataGridResourceToModify = resourceService.find(resourceName);
        List<String> childrenList = resourceService.getImmediateChildren(resourceName);

        List<DataGridResourceType> coordinatingResources = getCoordinatingResources(resourceService.listResourceTypes());

        List<DataGridResourceType> storageResources = getStorageResources(resourceService.listResourceTypes());

        for (DataGridResourceType dataGridResourceType : resourceService.listResourceTypes()) {
            if (dataGridResourceType.getDataGridType().equals(DataGridResourceTypeEnum.IRODS_COORDINATING)) {
                coordinatingResources.add(dataGridResourceType);
            }
            else if (dataGridResourceType.getDataGridType().equals(DataGridResourceTypeEnum.IRODS_STORAGE)) {
                storageResources.add(dataGridResourceType);
            }
        }

        ResourceForm resourceForm = new ResourceForm(dataGridResourceToModify);

        // prevent the same resource being modified from showing in the parent resource list

        model.addAttribute("resourceForm", resourceForm);
        model.addAttribute("requestMapping", URLMap.URL_MODIFY_RESOURCE_ACTION);
        model.addAttribute("resources", dataGridResources);
        model.addAttribute("childrenList", childrenList);
        model.addAttribute("coordinatingResources", coordinatingResources);
        model.addAttribute("storageResources", storageResources);

        return "resources/resourceForm";
    }

    /**
     * modify a resource from the data grid
     *
     * @param model
     * @return the template that renders the add resource from
     */
    @RequestMapping(value = "/modify/action/")
    public String modifyResource(@ModelAttribute ResourceForm resourceForm, HttpServletRequest httpServletRequest,
            RedirectAttributes redirectAttributes) {

        resourceService.updateResource(resourceForm.getName(), null, null);

        redirectAttributes.addFlashAttribute("resourceModifiedSuccessfully", resourceForm.getName());

        return "redirect:/resources/";
    }

    /**
     * Deletes a resource from the data grid
     *
     * @return
     */
    @RequestMapping(value = "/delete/{resourceName}/")
    public String deleteResource(@PathVariable String resourceName, RedirectAttributes redirectAttributes) {

        if (resourceService.deleteResource(resourceName)) {
            redirectAttributes.addFlashAttribute("resourceDeletedSuccessfully", resourceName);
        }
        else {
            redirectAttributes.addFlashAttribute("resourceNotDeletedSuccessfully", resourceName);
        }

        return "redirect:/resources/";
    }

    /**
     * List resources by server
     *
     * @return
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/servers/")
    public String getServers(Model model) throws DataGridConnectionRefusedException {
        logger.info("Getting the list of servers");

        if (dataGridResources == null || dataGridResources.isEmpty()) {
            dataGridResources = resourceService.findAll();
        }

        List<DataGridServer> dataGridServers = serverService.getAllServers(dataGridResources, null);

        // if no data grid servers were found
        if (dataGridServers == null) {
            dataGridServers = new ArrayList<DataGridServer>();
        }

        model.addAttribute("servers", dataGridServers);
        model.addAttribute("resultSize", dataGridServers.size());
        model.addAttribute("foundServers", dataGridServers.size() > 0);

        return "resources/resourceServers";
    }

    /**
     * Show the resource map
     *
     * @return
     * @throws JSONException
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/map/")
    public String getResourceTreeStructure(Model model) throws JSONException, DataGridConnectionRefusedException {
        getResourcesMap(model, false);
        return "resources/resourceMap";
    }

    /**
     * Show the resource map in dashboard
     *
     * @return
     * @throws JSONException
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/mapForDashboard/")
    public String getResourceTreeStructureFordashboard(Model model) throws JSONException, DataGridConnectionRefusedException {
        getResourcesMap(model, true);
        return "resources/resourceMap :: resourceMapPanel";
    }

    /**
     * Gets all information of a resource
     *
     * @return
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/viewInfo/{resourceName}", method = RequestMethod.GET)
    @ResponseBody
    public String viewResourceInfo(@PathVariable String resourceName) throws DataGridConnectionRefusedException {
        logger.info("creating JSON for resource info");

        JSONObject jsonObject = null;
        DataGridResource dataGridResource = null;

        try {

            jsonObject = new JSONObject();

            if (resourceName.compareTo(zoneName) == 0) {
                jsonObject.put("zoneName", zoneName);
            }

            else {
                dataGridResource = resourceService.find(resourceName);

                if (dataGridResource != null) {
                    jsonObject.put("name", dataGridResource.getName());
                    // TODO - resource type is being returned in contextString variable and not in type
                    jsonObject.put("type", dataGridResource.getType());
                    jsonObject.put("zone", dataGridResource.getZone());
                    jsonObject.put("host", dataGridResource.getHost());
                    jsonObject.put("path", dataGridResource.getPath());
                    jsonObject.put("parent", dataGridResource.getParent());
                    jsonObject.put("info", dataGridResource.getInfo());
                    jsonObject.put("status", dataGridResource.getStatus());
                    jsonObject.put("ip", machineInfoService.getAddress(dataGridResource.getHost()));
                }
            }

        }
        catch (JSONException e) {
            logger.error("Could not create JSON for resource info: ", e);
        }
        catch (UnknownHostException e) {
            logger.error("Could not find the IP address of " + dataGridResource.getHost());
        }

        return jsonObject.toString();
    }

    /*
     * **************************************************************************************
     * ****************************** VALIDATION METHODS ************************************
     * **************************************************************************************
     */

    /**
     * Validates a resource name in iRODS
     *
     * @param resourceName
     *            which is the resource name to be validated
     * @return true, if the username can be used. False, otherwise.
     * @throws DataGridConnectionRefusedException
     */
    @ResponseBody
    @RequestMapping(value = "isValidResourceName/{resourceName}/", method = RequestMethod.GET)
    public String isValidResourceName(@PathVariable String resourceName) throws DataGridConnectionRefusedException {

        String rc = "false";

        if (resourceName.compareTo("") != 0) {
            // if no resources are found with this name, it means this resource name can be used
            DataGridResource dataGridResource = resourceService.find(resourceName);
            rc = dataGridResource == null ? "true" : "false";

            if (resourceName.contains("%20") || resourceName.contains(" ")) {
                rc = "false";
            }
        }

        return rc;
    }

    /*
     * ***********************************************************************************
     * ****************************** PRIVATE METHODS ************************************
     * ***********************************************************************************
     */

    /**
     * Add a resource and its children to a JSON Tree
     *
     * @param parent
     * @param jsonParent
     * @param jsonChildren
     * @param dataGridResourcesMap
     */
    private void addParentToJSON(DataGridResource parent, JSONObject jsonParent, JSONArray jsonChildren,
            Map<String, DataGridResource> dataGridResourcesMap, boolean isDashboard) {

        try {

            if (parent != null) {
                JSONObject element = new JSONObject();
                element.put("name", parent.getName());
                if (isDashboard) {
                    element.put("icon", treeImagePathForDashboard);
                }
                else {
                    element.put("icon", treeImagePath);
                }

                List<String> childrenResources = parent.getChildren();

                if (childrenResources != null && !childrenResources.isEmpty()) {
                    JSONArray childrenOfElement = new JSONArray();

                    for (String childResource : childrenResources) {
                        DataGridResource child = dataGridResourcesMap.get(childResource);
                        addParentToJSON(child, element, childrenOfElement, dataGridResourcesMap, isDashboard);
                    }
                }

                jsonChildren.put(element);
                jsonParent.put("children", jsonChildren);
            }

        }
        catch (JSONException e) {
            logger.error("Could not create JSON Tree: ", e);
        }

        return;
    }

    /**
     * Builds a hash map(string, resource object) based on a list of resources
     *
     * @param dataGridResources
     * @return
     */
    private Map<String, DataGridResource> buildDataGridResourcesMap(List<DataGridResource> dataGridResources) {

        Map<String, DataGridResource> dataGridResourcesMap = new HashMap<String, DataGridResource>();

        for (DataGridResource dataGridResource : dataGridResources) {
            dataGridResourcesMap.put(dataGridResource.getName(), dataGridResource);
        }

        return dataGridResourcesMap;

    }

    /**
     * Creates a DataGridResource from a ResourceForm
     *
     * @param resourceForm
     * @return DataGridResource object
     */
    private DataGridResource getDataGridResource(ResourceForm resourceForm) {
        Date currentDate = new Date();

        DataGridResource newDataGridResource = new DataGridResource();
        newDataGridResource.setName(resourceForm.getName());
        newDataGridResource.setType(resourceForm.getType());
        newDataGridResource.setZone(resourceForm.getZone());
        newDataGridResource.setCreateTime(currentDate);
        newDataGridResource.setModifyTime(currentDate);
        newDataGridResource.setFreeSpaceDate(currentDate);
        newDataGridResource.setPath(resourceForm.getPath());
        newDataGridResource.setInfo(resourceForm.getInfo());
        newDataGridResource.setStatus(resourceForm.getStatus());
        newDataGridResource.setParent(resourceForm.getParent());
        newDataGridResource.setHost(resourceForm.getHost());
        newDataGridResource.setComment(resourceForm.getComment());
        newDataGridResource.setChildren(resourceForm.getChildren());

        if (resourceForm.getType().compareToIgnoreCase(DataGridServerType.ISILON.name()) == 0) {

            // Builds Isilon context string containing the HDFS credentials to access file system
            String context = String.format(ISILON_CONTEXT_STRING, resourceForm.getIsiHost(), resourceForm.getIsiPort(), resourceForm.getIsiUser());
            newDataGridResource.setContextString(context);
        }

        return newDataGridResource;
    }

    /**
     * Gets from a list of {@link DataGridResourceType} all resources types whose classified as
     * coordinating resources.
     *
     * @param dataGridResourceTypes
     *            list of all resource types available in the data grid
     * @return list of all coordinating resource types available
     */
    private List<DataGridResourceType> getCoordinatingResources(List<DataGridResourceType> dataGridResourceTypes) {
        List<DataGridResourceType> coordinatingResources = new ArrayList<DataGridResourceType>();

        for (DataGridResourceType dataGridResourceType : resourceService.listResourceTypes()) {
            if (dataGridResourceType.getDataGridType().equals(DataGridResourceTypeEnum.IRODS_COORDINATING)) {
                coordinatingResources.add(dataGridResourceType);
            }
        }

        return coordinatingResources;
    }

    /**
     *
     * Gets from a list of {@link DataGridResourceType} all resources type whose classified as
     * storage resources.
     *
     * @param dataGridResourceTypes
     *            list of all resource types available in the data grid
     * @return list of all storage resource types available
     */
    private List<DataGridResourceType> getStorageResources(List<DataGridResourceType> dataGridResourceTypes) {
        List<DataGridResourceType> storageResources = new ArrayList<DataGridResourceType>();

        for (DataGridResourceType dataGridResourceType : resourceService.listResourceTypes()) {
            if (dataGridResourceType.getDataGridType().equals(DataGridResourceTypeEnum.IRODS_STORAGE)) {
                storageResources.add(dataGridResourceType);
            }
        }

        return storageResources;
    }

    private void getResourcesMap(Model model, boolean isDashboard) throws JSONException, DataGridConnectionRefusedException {
        dataGridResources = resourceService.findAll();

        Map<String, DataGridResource> dataGridResourcesMap = buildDataGridResourcesMap(dataGridResources);

        // JSON object that will have all data of resources to be displayed as a tree
        logger.debug("Building JSON Tree");
        JSONArray treeData = new JSONArray();

        JSONObject root = new JSONObject();
        root.put("name", zoneName);
        if (isDashboard) {
            root.put("icon", zoneImagePathForDashBoard);
        }
        else {
            root.put("icon", zoneImagePath);
        }

        JSONArray childrenOfRoot = new JSONArray();

        for (DataGridResource dataGridResource : dataGridResources) {
            if (dataGridResource.getParent().equals(zoneName)) {
                addParentToJSON(dataGridResource, root, childrenOfRoot, dataGridResourcesMap, isDashboard);
            }
        }

        treeData.put(root);
        logger.debug("JSON Tree finished");

        model.addAttribute("treeData", treeData.toString());
    }
}
