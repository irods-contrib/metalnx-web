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

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.*;
import com.emc.metalnx.core.domain.entity.enums.DataGridPermType;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.modelattribute.breadcrumb.DataGridBreadcrumb;
import com.emc.metalnx.modelattribute.collection.CollectionOrDataObjectForm;
import com.emc.metalnx.modelattribute.metadatatemplate.MetadataTemplateForm;
import com.emc.metalnx.services.interfaces.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@SessionAttributes({ "sourcePaths" })
@RequestMapping(value = "/collections")
public class CollectionController {

    @Autowired
    CollectionService cs;

    @Autowired
    ResourceService resourceService;

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    @Autowired
    GroupBookmarkService groupBookmarkService;

    @Autowired
    UserBookmarkService userBookmarkService;

    @Autowired
    TemplateService templateService;

    @Autowired
    MetadataService metadataService;

    @Autowired
    GroupBookmarkController groupBookmarkController;

    @Autowired
    PermissionsService permissionsService;

    @Autowired
    IRODSServices irodsServices;

    @Autowired
    FavoritesService favoritesService;

    @Autowired
    LoggedUserUtils loggedUserUtils;

    // parent path of the current directory in the tree view
    private String parentPath;

    // path to the current directory in the tree view
    private String currentPath;

    // number of pages for current path
    private int totalObjsForCurrentPath;

    // number of pages for current search
    private int totalObjsForCurrentSearch;

    // Auxiliary structure to manage download, upload, copy and move operations
    private List<String> sourcePaths;

    // Logged user home path
    private String homePath;

    // ui mode that will be shown when the rods user switches mode from admin to user and vice-versa
    public static final String UI_USER_MODE = "user";
    public static final String UI_ADMIN_MODE = "admin";
    private static String TRASH_PATH;
    private static String USER_TRASH_PATH;

    public static final int MAX_HISTORY_SIZE = 10;

    private boolean cameFromMetadataSearch;
    private boolean cameFromFilePropertiesSearch;
    private boolean cameFromBookmarks;

    private Stack<String> collectionHistoryBack;
    private Stack<String> collectionHistoryForward;

    private static final Logger logger = LoggerFactory.getLogger(CollectionController.class);

    @PostConstruct
    public void init() throws DataGridException {
        collectionHistoryBack = new Stack<String>();
        collectionHistoryForward = new Stack<String>();

        cameFromMetadataSearch = false;
        cameFromFilePropertiesSearch = false;
        cameFromBookmarks = false;

        homePath = cs.getHomeDirectyForCurrentUser();

        sourcePaths = new ArrayList<>();
        parentPath = "";
        currentPath = "";
        TRASH_PATH = String.format("/%s/trash", irodsServices.getCurrentUserZone());
        USER_TRASH_PATH = String.format("/%s/trash/home/%s", irodsServices.getCurrentUserZone(), irodsServices.getCurrentUser());
    }

    /**
     * Responds the collections/ request
     *
     * @param model
     * @return the collection management template
     * @throws DataGridException
     */
    @RequestMapping(value = "/")
    public String index(Model model, HttpServletRequest request, @RequestParam(value = "uploadNewTab", required = false) boolean uploadNewTab)
            throws DataGridConnectionRefusedException {
        try {
            sourcePaths.clear();

            if (!cs.isPathValid(currentPath)) {
                currentPath = homePath;
                parentPath = currentPath;
            }
            else if (cs.isDataObject(currentPath)) {
                parentPath = currentPath.substring(0, currentPath.lastIndexOf("/") + 1);
            }

            DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
            boolean overwriteFileOption = loggedUser.isForceFileOverwriting();
            String uiMode = (String) request.getSession().getAttribute("uiMode");

            if (uiMode == null || uiMode.isEmpty()) {
                if (loggedUser.isAdmin()) {
                    uiMode = UI_ADMIN_MODE;
                }
                else {
                    uiMode = UI_USER_MODE;
                }
            }

            if (uiMode.equals(UI_USER_MODE)) {
                model.addAttribute("homePath", homePath);
                model.addAttribute("publicPath", cs.getHomeDirectyForPublic());
            }
            if (uploadNewTab) {
                model.addAttribute("uploadNewTab", uploadNewTab);
            }

            model.addAttribute("cameFromFilePropertiesSearch", cameFromFilePropertiesSearch);
            model.addAttribute("cameFromMetadataSearch", cameFromMetadataSearch);
            model.addAttribute("cameFromBookmarks", cameFromBookmarks);
            model.addAttribute("uiMode", uiMode);
            model.addAttribute("currentPath", currentPath);
            model.addAttribute("parentPath", parentPath);
            model.addAttribute("resources", resourceService.findAll());
            model.addAttribute("overwriteFileOption", overwriteFileOption);

            cameFromMetadataSearch = false;
            cameFromFilePropertiesSearch = false;
            cameFromBookmarks = false;
        } catch (DataGridException e) {
            logger.error("Could not respond to request for collections: {}", e);
            model.addAttribute("unexpectedError", true);
        }

        return "collections/collectionManagement";
    }

    @RequestMapping(value = "redirectFromMetadataToCollections/")
    @ResponseStatus(value = HttpStatus.OK)
    public void redirectFromMetadataToCollections(@RequestParam String path) {
        assignNewValuesToCurrentAndParentPath(path);
        cameFromMetadataSearch = true;
    }

    @RequestMapping(value = "redirectFromFavoritesToCollections/")
    @ResponseStatus(value = HttpStatus.OK)
    public void redirectFromFavoritesToCollections(@RequestParam String path) {
        assignNewValuesToCurrentAndParentPath(path);
    }

    @RequestMapping(value = "redirectFromGroupsBookmarksToCollections/")
    @ResponseStatus(value = HttpStatus.OK)
    public void redirectFromGroupsBookmarksToCollections(@RequestParam String path) {
        cameFromBookmarks = true;
        assignNewValuesToCurrentAndParentPath(path);
    }

    @RequestMapping(value = "redirectFromUserBookmarksToCollections/")
    @ResponseStatus(value = HttpStatus.OK)
    public void redirectFromUserBookmarksToCollections(@RequestParam String path) {
        cameFromBookmarks = true;
        assignNewValuesToCurrentAndParentPath(path);
    }

    @RequestMapping(value = "redirectFromFilePropertiesToCollections/")
    @ResponseStatus(value = HttpStatus.OK)
    public void redirectFromFilePropertiesToCollections(@RequestParam String path) {
        assignNewValuesToCurrentAndParentPath(path);
        cameFromFilePropertiesSearch = true;
    }

    /**
     * Get a list of resources in which an object doesn't have replicas
     *
     * @param model
     * @return list of resources in which an object can be replicated
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "getAvailableRescForPath/")
    public String getAvailableRescForPath(Model model) throws DataGridConnectionRefusedException {

        Map<DataGridCollectionAndDataObject, DataGridResource> replicasMap = null;
        List<DataGridResource> resources = resourceService.findFirstLevelResources();

        if (sourcePaths.size() == 0) {
            model.addAttribute("resources", resources);
        }
        else {
            for (String path : sourcePaths) {
                replicasMap = cs.listReplicasByResource(path);
                for (DataGridResource resc : replicasMap.values()) {
                    if (resources.contains(resc)) {
                        resources.remove(resc);
                    }
                }
                model.addAttribute("resources", resources);
            }
        }

        return "collections/collectionsResourcesForReplica";
    }

    /**
     * Switches an admin from the Rods_Admin UI to the Rods_User UI and vice-versa.
     *
     * @param model
     * @return redirects an admin user from to the new UI view mode (admin view or user view)
     */
    @RequestMapping(value = "/switchMode/")
    @ResponseStatus(value = HttpStatus.OK)
    public void switchMode(Model model, HttpServletRequest request, @RequestParam("currentMode") String currentMode,
            final RedirectAttributes redirectAttributes) {

        // if the admin is currently seeing the Admin UI, we need to switch it
        // over to the USER UI
        if (currentMode.equalsIgnoreCase(UI_ADMIN_MODE)) {
            request.getSession().setAttribute("uiMode", UI_USER_MODE);
        }
        // if the admin is currently seeing the User UI, we need to switch it
        // over to the ADMIN UI
        else if (currentMode.equalsIgnoreCase(UI_USER_MODE)) {
            request.getSession().setAttribute("uiMode", UI_ADMIN_MODE);
        }
    }

    /**
     * Responds the getSubdirectories request finding collections and data objects that exist
     * underneath a certain path
     *
     * @param model
     * @param path
     * @return treeView template that renders all nodes of certain path (parent)
     * @throws DataGridException
     */
    @RequestMapping(value = "/getSubDirectories/", method = RequestMethod.POST)
    public String getSubDirectories(Model model, @RequestParam("path") String path) throws DataGridException {

        // removes all ocurrences of "/" at the end of the path string
        while (path.endsWith("/") && !"/".equals(path)) {
            path = path.substring(0, path.lastIndexOf("/"));
        }

        logger.info("Get subdirectories of {}", path);
        DataGridCollectionAndDataObject obj = null;
        boolean isCollection = false;
        boolean isDataObj = false;

        try {
            obj = cs.findByName(path);
            isCollection = cs.isCollection(path);
            isDataObj = cs.isDataObject(path);
        }
        catch (DataGridException e) {
            logger.error("Path {} doesn't exist or user does not have access permission", path);
        }
        if (obj == null && !isCollection && !isDataObj) {
            model.addAttribute("invalidPath", path);
            path = currentPath;
        }
        else if (obj == null && (isCollection || isDataObj)) {
            model.addAttribute("pathPermissionDenied", path);
            path = currentPath;
        }

        // put old path in collection history stack
        if (!path.equals(currentPath)) {
            while (collectionHistoryBack.size() >= MAX_HISTORY_SIZE) {
                collectionHistoryBack.remove(0);
            }
            collectionHistoryBack.push(currentPath);
            if (!collectionHistoryForward.isEmpty()) {
                collectionHistoryForward.clear();
            }
        }

        return getCollBrowserView(model, path);
    }

    /**
     * Goes back in collection historic stack
     *
     * @param model
     * @param steps
     * @return treeView template that renders all nodes of certain path (parent)
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/goBackHistory/", method = RequestMethod.POST)
    public String goBackHistory(Model model, @RequestParam("steps") int steps) throws DataGridException, JargonException {
        if (collectionHistoryBack.size() < steps || steps < 1) {
            model.addAttribute("invalidStepsBackwards", steps);
            logger.info("It is not possible to go back {} steps, current stack size is {}", steps, collectionHistoryBack.size());
            return getCollBrowserView(model, currentPath);
        }

        logger.info("Going back {} steps in collection history", steps);

        // pop paths from collectionHistoryBack and push them to collectionHistoryForward
        while (collectionHistoryForward.size() >= MAX_HISTORY_SIZE) {
            collectionHistoryForward.remove(0);
        }
        collectionHistoryForward.push(currentPath);
        for (int i = 0; i < steps - 1; i++) {
            String elementHistory = collectionHistoryBack.pop();
            while (collectionHistoryForward.size() >= MAX_HISTORY_SIZE) {
                collectionHistoryForward.remove(0);
            }
            collectionHistoryForward.push(elementHistory);
        }

        return getCollBrowserView(model, collectionHistoryBack.pop());
    }

    /**
     * Goes forward in collection historic stack
     *
     * @param model
     * @param steps
     * @return treeView template that renders all nodes of certain path (parent)
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/goForwardHistory/", method = RequestMethod.POST)
    public String goForwardHistory(Model model, @RequestParam("steps") int steps) throws DataGridException, JargonException {
        if (collectionHistoryForward.size() < steps || steps < 1) {
            model.addAttribute("invalidStepsForward", steps);
            return getCollBrowserView(model, currentPath);
        }

        logger.info("Going {} steps forward in collection history", steps);

        // pop paths from collectionHistoryBack and push them to collectionHistoryForward
        while (collectionHistoryBack.size() >= MAX_HISTORY_SIZE) {
            collectionHistoryBack.remove(0);
        }
        collectionHistoryBack.push(currentPath);
        for (int i = 0; i < steps - 1; i++) {
            String elementHistory = collectionHistoryForward.pop();
            while (collectionHistoryBack.size() >= MAX_HISTORY_SIZE) {
                collectionHistoryBack.remove(0);
            }
            collectionHistoryBack.push(elementHistory);
        }

        return getCollBrowserView(model, collectionHistoryForward.pop());
    }

    /**
     * Responds the getSubdirectories request finding collections and data objects that exist
     * underneath a certain path
     *
     * @param model
     * @param path
     * @return treeView template that renders all nodes of certain path (parent)
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/getSubDirectoriesOldTree/")
    public String getSubDirectoriesOldTree(Model model, @RequestParam("path") String path) throws DataGridConnectionRefusedException {

        if (path.isEmpty()) {
            path = "/";
        }
        else {
            if (path.endsWith("/") && path.compareTo("/") != 0) {
                path = path.substring(0, path.length() - 1);
            }
        }

        // The line below was modified so that only collection would be retrieved
        model.addAttribute("dataGridCollectionAndDataObjectList", cs.getSubCollectionsUnderPath(path));

        return "collections/oldTreeView :: oldTreeView";
    }

    /**
     * Gets checksum, total number of replicas and where each replica lives in the data grid for a
     * specific data object
     *
     * @param model
     * @param path
     *            path to the data object to get checksum and replica information
     * @return the template that shows the data object information
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/info/", method = RequestMethod.POST)
    public String getFileInfo(Model model, @RequestParam("path") String path) throws DataGridConnectionRefusedException {

        DataGridCollectionAndDataObject dataGridObj = null;
        Map<DataGridCollectionAndDataObject, DataGridResource> replicasMap = null;

        try {
            dataGridObj = cs.findByName(path);

            if (dataGridObj != null && !dataGridObj.isCollection()) {
                replicasMap = cs.listReplicasByResource(path);
                dataGridObj.setChecksum(cs.getChecksum(path));
                dataGridObj.setNumberOfReplicas(cs.getTotalNumberOfReplsForDataObject(path));
                dataGridObj.setReplicaNumber(String.valueOf(cs.getReplicationNumber(path)));
                permissionsService.resolveMostPermissiveAccessForUser(dataGridObj, loggedUserUtils.getLoggedDataGridUser());
            }

        }
        catch (DataGridConnectionRefusedException e) {
            logger.error("Could not connect to the data grid", e);
            throw e;
        }
        catch (DataGridException e) {
            logger.error("Could not get file info for {}", path, e);
        }

        model.addAttribute("collectionAndDataObject", dataGridObj);
        model.addAttribute("currentCollection", dataGridObj);
        model.addAttribute("replicasMap", replicasMap);

        return "collections/collectionInfo";
    }

    /**
     * Finds all collections and files existing under a certain path for a given group name.
     *
     * @param model
     * @param path
     *            start point to get collections and files
     * @param groupName
     *            group that all collections and files permissions will be listed
     * @return
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/getDirectoriesAndFilesForGroupForm")
    public String getDirectoriesAndFilesForGroupForm(Model model, @RequestParam("path") String path, @RequestParam("groupName") String groupName,
            @RequestParam("retrievePermissions") boolean retrievePermissions) throws DataGridConnectionRefusedException {
        if (path == null || path == "") {
            path = "/";
        }

        List<DataGridCollectionAndDataObject> list = null;
        list = cs.getSubCollectionsAndDataObjetsUnderPath(path);

        Set<String> readPermissions = null;
        Set<String> writePermissions = null;
        Set<String> ownershipPermissions = null;
        Set<String> inheritPermissions = null;

        if (retrievePermissions) {
            readPermissions = cs.listReadPermissionsForPathAndGroup(path, groupName);
            writePermissions = cs.listWritePermissionsForPathAndGroup(path, groupName);
            ownershipPermissions = cs.listOwnershipForPathAndGroup(path, groupName);
            inheritPermissions = cs.listInheritanceForPath(path);
        }
        else {
            readPermissions = new HashSet<String>();
            writePermissions = new HashSet<String>();
            ownershipPermissions = new HashSet<String>();
            inheritPermissions = new HashSet<String>();
        }

        List<String> groupBookmarks = new ArrayList<String>();
        if (groupName.length() > 0) {
            DataGridGroup group = groupService.findByGroupname(groupName).get(0);
            groupBookmarks = groupBookmarkService.findBookmarksForGroupAsString(group);
        }

        model.addAttribute("dataGridCollectionAndDataObjectList", list);
        model.addAttribute("currentPath", path);
        model.addAttribute("readPermissions", readPermissions);
        model.addAttribute("writePermissions", writePermissions);
        model.addAttribute("ownershipPermissions", ownershipPermissions);
        model.addAttribute("inheritPermissions", inheritPermissions);
        model.addAttribute("addBookmark", groupBookmarkController.getAddBookmark());
        model.addAttribute("removeBookmark", groupBookmarkController.getRemoveBookmark());
        model.addAttribute("groupBookmarks", groupBookmarks);

        return "collections/treeViewForGroupForm :: treeView";
    }

    /**
     * Finds all collections existing under a certain path.
     *
     * @param model
     * @param path
     *            start point to get collections and files
     * @param username
     *            user who all collections and files permissions will be listed
     * @return the template that will render the tree
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/getDirectoriesAndFilesForUser")
    public String getDirectoriesAndFilesForUser(Model model, @RequestParam("path") String path, @RequestParam("username") String username,
            @RequestParam("retrievePermissions") boolean retrievePermissions) throws DataGridConnectionRefusedException {
        List<DataGridCollectionAndDataObject> list = new ArrayList<DataGridCollectionAndDataObject>();
        Set<String> readPermissions = new HashSet<String>();
        Set<String> writePermissions = new HashSet<String>();
        Set<String> ownershipPermissions = new HashSet<String>();
        Set<String> inheritPermissions = new HashSet<String>();
        List<String> userBookmarks = new ArrayList<String>();

        // If a string is null, empty or contains only white spaces, StringUtils
        // returns true
        boolean isPathEmpty = StringUtils.isEmptyOrWhitespace(path);
        boolean isUsernameEmpty = StringUtils.isEmptyOrWhitespace(username);

        if (!isPathEmpty) {
            // When adding a user (there is no username), we still need to be
            // able to walk through the iRODS tree
            list = cs.getSubCollectionsAndDataObjetsUnderPath(path);

            if (!isUsernameEmpty) {
                if (retrievePermissions) {
                    readPermissions = cs.listReadPermissionsForPathAndUser(path, username);
                    writePermissions = cs.listWritePermissionsForPathAndUser(path, username);
                    ownershipPermissions = cs.listOwnershipForPathAndUser(path, username);
                    inheritPermissions = cs.listInheritanceForPath(path);
                }

                List<DataGridUser> users = userService.findByUsername(username);
                if (users != null && !users.isEmpty()) {
                    userBookmarks = userBookmarkService.findBookmarksForUserAsString(users.get(0));
                }
            }
        }

        model.addAttribute("dataGridCollectionAndDataObjectList", list);
        model.addAttribute("currentPath", path);
        model.addAttribute("readPermissions", readPermissions);
        model.addAttribute("writePermissions", writePermissions);
        model.addAttribute("ownershipPermissions", ownershipPermissions);
        model.addAttribute("inheritPermissions", inheritPermissions);
        model.addAttribute("addBookmark", new ArrayList<String>());
        model.addAttribute("removeBookmark", new ArrayList<String>());
        model.addAttribute("userBookmarks", userBookmarks);

        return "collections/treeViewForUserForm :: treeView";
    }

    /**
     * Looks for collections or data objects that match the parameter string
     *
     * @param model
     * @param name
     *            collection name that will be searched in the data grid
     * @return the template that renders all collections and data objects matching the parameter
     *         string
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/find/{name}")
    public String listCollectionsAndDataObjects(Model model, @PathVariable String name) throws DataGridConnectionRefusedException {
        logger.info("Finding collections or data objects that match " + name);

        // Find collections and data objects
        List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = cs.searchCollectionAndDataObjectsByName(name + "%");
        model.addAttribute("dataGridCollectionAndDataObjects", dataGridCollectionAndDataObjects);
        return "collections/collectionsBrowser :: treeView";
    }

    /**
     * Responds the collection/add/ request and displays the add collection form
     *
     * @param model
     * @return add collection template
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "add/")
    public String showAddCollection(Model model) throws DataGridConnectionRefusedException {
        CollectionOrDataObjectForm collectionForm = new CollectionOrDataObjectForm();
        String permission = cs.getPermissionsForPath(currentPath);
        boolean inheritance = cs.getInheritanceOptionForCollection(currentPath);
        if (inheritance) {
            collectionForm.setInheritOption(true);
        }
        else {
            collectionForm.setInheritOption(false);
        }
        model.addAttribute("inheritanceDisabled", !"own".equals(permission) && inheritance);
        model.addAttribute("collection", collectionForm);
        model.addAttribute("requestMapping", "/collections/add/action/");
        model.addAttribute("currentPath", currentPath);
        model.addAttribute("parentPath", parentPath);

        return "collections/addCollectionForm";
    }

    /**
     * Performs the action of actually creating a collection in iRODS
     *
     * @param model
     * @param collection
     * @return if the creation of collection was successful, it returns the collection management
     *         template, and returns the add collection template,
     *         otherwise.
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "add/action/", method = RequestMethod.POST)
    public String addCollection(Model model, @ModelAttribute CollectionOrDataObjectForm collection, final RedirectAttributes redirectAttributes)
            throws DataGridConnectionRefusedException {
        DataGridCollectionAndDataObject newCollection = new DataGridCollectionAndDataObject(currentPath + '/' + collection.getCollectionName(),
                collection.getCollectionName(), currentPath, true);

        newCollection.setParentPath(currentPath);
        newCollection.setCreatedAt(new Date());
        newCollection.setModifiedAt(newCollection.getCreatedAt());
        newCollection.setInheritanceOption(collection.getInheritOption());

        boolean creationSucessful;
        try {
            creationSucessful = cs.createCollection(newCollection);

            if (creationSucessful) {
                redirectAttributes.addFlashAttribute("collectionAddedSuccessfully", collection.getCollectionName());
            }
        }
        catch (DataGridConnectionRefusedException e) {
            throw e;
        }
        catch (DataGridException e) {
            logger.error("Could not create collection/data object (lack of permission): ", e.getMessage());
            redirectAttributes.addFlashAttribute("missingPermissionError", true);
        }

        return "redirect:/collections/";
    }

    /**
     * Performs the action of modifying a collection
     *
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "modify/action", method = RequestMethod.POST)
    public String modifyAction(@ModelAttribute CollectionOrDataObjectForm collectionForm, Model model, RedirectAttributes redirectAttributes)
            throws DataGridConnectionRefusedException, DataGridException {
        boolean modificationSuccessful = false;
        if (sourcePaths.size() != 1) {
            throw new DataGridException("Cannot rename more than one element at a time.");
        }

        String targetPath = sourcePaths.get(0);
        String path = targetPath.substring(0, targetPath.lastIndexOf("/"));
        String newPath = String.format("%s/%s", path, collectionForm.getCollectionName());

        logger.info("Modify action for " + targetPath + "/" + newPath);
        modificationSuccessful = cs.modifyCollectionAndDataObject(targetPath, newPath, collectionForm.getInheritOption());
        
        if (modificationSuccessful) {
            logger.debug("Collection/Data Object {} modified to {}", targetPath, newPath);
            
        	userBookmarkService.updateBookmark(targetPath, newPath);
        	groupBookmarkService.updateBookmark(targetPath, newPath);
        	
            redirectAttributes.addFlashAttribute("collectionModifiedSuccessfully", collectionForm.getCollectionName());
        }

        return "redirect:/collections/";
    }

    @RequestMapping(value = "applyTemplatesToCollections/", method = RequestMethod.POST)
    public String applyTemplatesToCollections(Model model, RedirectAttributes redirectAttributes, @ModelAttribute MetadataTemplateForm templateForm)
            throws DataGridConnectionRefusedException {
        boolean templatesAppliedSuccessfully = applyTemplatesToPath(templateForm, sourcePaths);
        sourcePaths.clear();
        redirectAttributes.addFlashAttribute("templatesAppliedSuccessfully", templatesAppliedSuccessfully);
        return "redirect:/collections/";
    }

    public boolean applyTemplatesToPath(MetadataTemplateForm templateForm, List<String> paths) throws DataGridConnectionRefusedException {
        boolean allMetadataAdded = true;
        List<String> attributes = templateForm.getAvuAttributes();
        List<String> values = templateForm.getAvuValues();
        List<String> units = templateForm.getAvuUnits();

        if (attributes == null || values == null || units == null) {
            return false;
        }

        for (int i = 0; i < attributes.size(); i++) {
            String attr = attributes.isEmpty() ? "" : attributes.get(i);
            String val = values.isEmpty() ? "" : values.get(i);
            String unit = units.isEmpty() ? "" : units.get(i);
            for (String path : paths) {
                boolean isMetadadaAdded = metadataService.addMetadataToPath(path, attr, val, unit);
                if (!isMetadadaAdded) {
                    allMetadataAdded = false;
                }
            }
        }

        return allMetadataAdded;
    }

    /*
     * ****************************************************************************
     * ************************ USER COLLECTION CONTROLLER ************************
     * ****************************************************************************
     */

    /**
     * Responds the collections/home request
     *
     * @param model
     * @return the collection management template
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/home/")
    public String homeCollection(Model model) throws DataGridConnectionRefusedException, DataGridException {
        // cleaning session variables
        sourcePaths.clear();
        currentPath = cs.getHomeDirectyForCurrentUser();
        parentPath = currentPath;
        return "redirect:/collections/";
    }

    /**
     * Responds the collections/public request
     *
     * @param model
     * @return the collection management template
     */
    @RequestMapping(value = "/public/")
    public String publicCollection(Model model) throws DataGridConnectionRefusedException, DataGridException {
        // cleaning session variables
        sourcePaths.clear();

        currentPath = cs.getHomeDirectyForPublic();
        parentPath = currentPath;

        model.addAttribute("publicPath", currentPath);
        model.addAttribute("currentPath", currentPath);
        model.addAttribute("parentPath", parentPath);
        model.addAttribute("homePath", cs.getHomeDirectyForCurrentUser());
        model.addAttribute("resources", resourceService.findAll());

        return "collections/collectionManagement";
    }

    /**
     * Responds the collections/trash request
     *
     * @param model
     * @return the collection management template
     * @throws DataGridException
     */
    @RequestMapping(value = "/trash/")
    public String trashCollection(Model model) throws DataGridException {
        // cleaning session variables
        sourcePaths.clear();

        currentPath = USER_TRASH_PATH;
        parentPath = currentPath;

        model.addAttribute("currentPath", currentPath);
        model.addAttribute("parentPath", parentPath);
        model.addAttribute("publicPath", cs.getHomeDirectyForPublic());
        model.addAttribute("homePath", cs.getHomeDirectyForCurrentUser());
        model.addAttribute("resources", resourceService.findAll());

        return "collections/collectionManagement";
    }

    @RequestMapping(value = "/getBreadCrumbForObject/")
    public String getBreadCrumbForObject(Model model, @RequestParam("path") String path) throws DataGridConnectionRefusedException {
        if (path.isEmpty()) {
            path = currentPath;
        }
        else {
            if (path.endsWith("/") && path.compareTo("/") != 0) {
                path = path.substring(0, path.length() - 1);
            }
            if (!currentPath.equals(path) && (collectionHistoryBack.isEmpty() || !currentPath.equals(collectionHistoryBack.peek()))) {
                while (collectionHistoryBack.size() >= MAX_HISTORY_SIZE) {
                    collectionHistoryBack.remove(0);
                }
                collectionHistoryBack.push(currentPath);
                if (!collectionHistoryForward.isEmpty()) {
                    collectionHistoryForward.clear();
                }
            }
            currentPath = path;
        }

        setBreadcrumbToModel(model, path);
        return "collections/collectionsBreadCrumb";
    }

    /*
     * *************************************************************************
     * ************************** HANDLING SESSION *****************************
     * *************************************************************************
     */

    /**
     * Method that adds a path to the list of paths to perform download, copy, move, or other types
     * of operations.
     *
     * @param path
     * @return the permission the user has on the path given as the parameter
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/addToSourcePaths/", method = RequestMethod.POST, produces = { "text/plain" })
    @ResponseBody
    synchronized public String addToSourcePaths(@RequestParam("path") String path) throws DataGridConnectionRefusedException {
        if (path != null && !path.isEmpty() && !sourcePaths.contains(path)) {
            sourcePaths.add(path);
        }
        return getMostRestrictivePermission();
    }

    @RequestMapping(value = "/removeFromSourcePaths/", method = RequestMethod.POST, produces = { "text/plain" })
    @ResponseBody
    synchronized public String removeFromSourcePaths(@RequestParam("path") String path) throws DataGridConnectionRefusedException {
        if (path != null && !path.isEmpty() && sourcePaths.contains(path)) {
            sourcePaths.remove(path);
        }
        return getMostRestrictivePermission();
    }

    /**
     * Method that adds a path to the list of paths to perform download, copy, move, or other types
     * of operations.
     *
     * @param paths
     * @return the permission the user has on the path given as the parameter
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/addArrayToSourcePaths/", method = RequestMethod.POST, produces = { "text/plain" })
    @ResponseBody
    synchronized public String addToSourcePaths(@RequestParam("paths[]") String[] paths) throws DataGridConnectionRefusedException {
        if (paths != null && paths.length > 0) {
            for (String path : paths) {
                if (!sourcePaths.contains(path)) {
                    sourcePaths.add(path);
                }
            }
        }

        return getMostRestrictivePermission();
    }

    @RequestMapping(value = "/removeArrayFromSourcePaths/", method = RequestMethod.POST, produces = { "text/plain" })
    @ResponseBody
    synchronized public String removeFromSourcePaths(@RequestParam("paths[]") String[] paths) throws DataGridConnectionRefusedException {
        if (paths != null && paths.length > 0) {
            sourcePaths.removeAll(Arrays.asList(paths));
        }
        return getMostRestrictivePermission();
    }

    /*
     * *****************************************************************************
     * ******************************** VALIDATION *********************************
     * *****************************************************************************
     */

    /**
     * Validates a collection name in iRODS
     *
     * @return True, if the collection name can be used. False, otherwise.
     * @throws DataGridConnectionRefusedException
     */
    @ResponseBody
    @RequestMapping(value = "isValidCollectionName/{newObjectName}/", method = RequestMethod.GET, produces = { "text/plain" })
    public String isValidCollectionName(@PathVariable String newObjectName) throws DataGridConnectionRefusedException, DataGridException {
        String rc = "true";
        String newPath = String.format("%s/%s", currentPath, newObjectName);

        try {
            cs.findByName(newPath);
            rc = "false";
        }
        catch (DataGridException e) {
            logger.debug("Path {} does not exist. Executing modification", newPath, e);
        }
        return rc;
    }

    /*
     * *************************************************************************
     * ******************************** UTILS **********************************
     * *************************************************************************
     */

    /**
     * Method that returns the most restrictive permission existing in the list of paths to operate
     * on.
     *
     * @return string containing the most restrictive permission ("none", "read", "write", or "own")
     * @throws DataGridConnectionRefusedException
     */
    synchronized private String getMostRestrictivePermission() throws DataGridConnectionRefusedException {
        DataGridPermType mostRestrictivePermission = DataGridPermType.NONE;
        String currPermission = "";
        List<String> srcPathsPermissions = new ArrayList<>();

        try {
            for (String path : sourcePaths) {
                currPermission = cs.getPermissionsForPath(path);

                if (!srcPathsPermissions.contains(currPermission)) {
                    srcPathsPermissions.add(currPermission);
                    if ("none".equalsIgnoreCase(currPermission)) {
                        break;
                    }
                }
            }

            if (srcPathsPermissions.contains("none")) {
                mostRestrictivePermission = DataGridPermType.NONE;
            }
            else if (srcPathsPermissions.contains("read")) {
                mostRestrictivePermission = DataGridPermType.READ;
            }
            else if (srcPathsPermissions.contains("write")) {
                mostRestrictivePermission = DataGridPermType.WRITE;
            }
            else if(srcPathsPermissions.contains("own")){
                mostRestrictivePermission = DataGridPermType.OWN;
            }
        }
        catch (DataGridConnectionRefusedException e) {
            logger.error("Could not connect to the data grid.");
            throw e;
        }
        catch (Exception e) {
            logger.error("Could not get the most restrictive permission. Setting it to 'none'. {}", e.getMessage());
        }

        boolean isAdmin = loggedUserUtils.getLoggedDataGridUser().isAdmin();
        boolean isPermNone = mostRestrictivePermission.equals(DataGridPermType.NONE);

        if (isPermNone && isAdmin) mostRestrictivePermission = DataGridPermType.IRODS_ADMIN;

        return mostRestrictivePermission.toString().toLowerCase();
    }

    /**
     * Finds all collections and data objects existing under a certain path
     *
     * @param draw
     * @param start
     *            index of the first element of the page that needs to be shown
     * @param length
     *            page size
     * @param searchString
     *            [value]
     *            string value when searching for a collection/file name
     * @return json with collections and data objects
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "getPaginatedJSONObjs/")
    @ResponseBody
    public String getPaginatedJSONObjs(@RequestParam("draw") int draw, @RequestParam("start") int start, @RequestParam("length") int length,
            @RequestParam("search[value]") String searchString) throws DataGridConnectionRefusedException {
        List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = new ArrayList<DataGridCollectionAndDataObject>();

        // Pagination context to get the sequence number for the listed items
        DataGridPageContext pageContext = new DataGridPageContext();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonResponse = new HashMap<String, Object>();
        jsonResponse.put("draw", String.valueOf(draw));
        jsonResponse.put("recordsTotal", String.valueOf(1));
        jsonResponse.put("recordsFiltered", String.valueOf(0));
        jsonResponse.put("data", new ArrayList<String>());
        String jsonString = "";

        try {
            Double startPage = Math.floor(start / length) + 1;
            dataGridCollectionAndDataObjects = cs.getSubCollectionsAndDataObjetsUnderPathThatMatchSearchTextPaginated(currentPath,
                    searchString, startPage.intValue(), length, pageContext);
            totalObjsForCurrentSearch = pageContext.getTotalNumberOfItems();
            totalObjsForCurrentPath = pageContext.getTotalNumberOfItems();

            jsonResponse.put("recordsTotal", String.valueOf(totalObjsForCurrentPath));
            jsonResponse.put("recordsFiltered", String.valueOf(totalObjsForCurrentSearch));
            jsonResponse.put("data", dataGridCollectionAndDataObjects);
        }
        catch (DataGridConnectionRefusedException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Could not get collections/data objs under path {}: {}", currentPath, e.getMessage());
        }

        try {
            jsonString = mapper.writeValueAsString(jsonResponse);
        }
        catch (JsonProcessingException e) {
            logger.error("Could not parse hashmap in collections to json: {}", e.getMessage());
        }

        return jsonString;
    }

    /**
     * @return the sourcePaths
     */
    public List<String> getSourcePaths() {
        return sourcePaths;
    }

    /**
     * @return the currentPath
     */
    public String getCurrentPath() {
        return currentPath;
    }

    public String getParentPath() {
        return parentPath;
    }
    
    /**
     * Removes a path from the user's navigation history
     * @param path
     * 			path to be removed
     */
    public void removePathFromHistory(String path) {
    	if (path == null || path.isEmpty()) {
    		return;
    	}
    	
    	collectionHistoryBack.remove(path);
    	collectionHistoryForward.remove(path);
    }

    /*
     * **************************************************************************
     * **************************** PRIVATE METHODS *****************************
     * **************************************************************************
     */

    /**
     * Sets the current path and parent path based on a given path.
     *
     * @param path
     *            new path to update current path and parent path
     */
    private void assignNewValuesToCurrentAndParentPath(String path) {
        if (path == null || path.isEmpty()) {
            return;
        }

        currentPath = path;
        parentPath = currentPath.substring(0, currentPath.lastIndexOf("/") + 1);
    }

    /**
     * Creates the breadcrumb based on a given path.
     *
     * @param model
     *            Model attribute to set variables to be used in the view
     * @param path
     *            path that will be displayed in the breadcrumb
     */
    private void setBreadcrumbToModel(Model model, String path) {

        DataGridCollectionAndDataObject obj = new DataGridCollectionAndDataObject();
        try {
            obj = cs.findByName(path);
        }
        catch (DataGridException e) {
            obj.setPath(path);
            obj.setCollection(false);
            obj.setParentPath(path.substring(0, path.lastIndexOf("/") + 1));
            obj.setName(path.substring(path.lastIndexOf("/") + 1, path.length()));
            logger.error("Could not find DataGridCollectionAndDataObject by path: {}", e.getMessage());
        }

        ArrayList<String> listHistoryBack = new ArrayList<String>(collectionHistoryBack);
        Collections.reverse(listHistoryBack);

        model.addAttribute("collectionPastHistory", listHistoryBack);
        model.addAttribute("collectionPastHistoryEmpty", collectionHistoryBack.isEmpty());
        model.addAttribute("collectionForwardHistory", collectionHistoryForward);
        model.addAttribute("collectionForwardHistoryEmpty", collectionHistoryForward.isEmpty());
        model.addAttribute("collectionForwardHistory", collectionHistoryForward);
        model.addAttribute("collectionAndDataObject", obj);
        model.addAttribute("breadcrumb", new DataGridBreadcrumb(obj.getPath()));
        model.addAttribute("starredPath", favoritesService.isPathFavoriteForUser(loggedUserUtils.getLoggedDataGridUser(), path));
        model.addAttribute("homeCollectionName", irodsServices.getCurrentUser());
    }

    /**
     * Finds all collections and data objects existing under a certain path
     *
     * @param model
     * @param path
     *            path to get all directories from
     * @return collections browser template that renders all items of certain path (parent)
     * @throws DataGridConnectionRefusedException
     */
    private String getCollBrowserView(Model model, String path) throws DataGridException {
        String permissionType = "none";
        boolean isCurrentPathCollection = false;
        boolean isTrash = false;

        permissionType = cs.getPermissionsForPath(path);
        isCurrentPathCollection = cs.isCollection(path);

        if (path.isEmpty()) {
            path = currentPath;
        }
        else {
            if (path.endsWith("/") && path.compareTo("/") != 0) {
                path = path.substring(0, path.length() - 1);
            }
            currentPath = path;
        }

        setBreadcrumbToModel(model, path);

        DataGridCollectionAndDataObject dataGridObj = new DataGridCollectionAndDataObject();
        try {
            dataGridObj = cs.findByName(path);
            if (dataGridObj != null && !dataGridObj.isCollection()) {
                dataGridObj.setChecksum(cs.getChecksum(path));
                dataGridObj.setNumberOfReplicas(cs.getTotalNumberOfReplsForDataObject(path));
                dataGridObj.setReplicaNumber(String.valueOf(cs.getReplicationNumber(path)));
            }
        }
        catch (DataGridException e) {
            dataGridObj.setPath(path);
            dataGridObj.setCollection(false);
            dataGridObj.setParentPath(path.substring(0, path.lastIndexOf("/") + 1));
            dataGridObj.setName(path.substring(path.lastIndexOf("/") + 1, path.length()));
            logger.error("Could not get file info for {}", path, e);
        }

        DataGridUser user = loggedUserUtils.getLoggedDataGridUser();
        permissionsService.resolveMostPermissiveAccessForUser(dataGridObj, user);
        isTrash = path.contains(TRASH_PATH) && ("own".equals(permissionType) || user.isAdmin());

        model.addAttribute("collectionAndDataObject", dataGridObj);
        model.addAttribute("permissionType", permissionType);
        model.addAttribute("currentPath", currentPath);
        model.addAttribute("isCurrentPathCollection", isCurrentPathCollection);
        model.addAttribute("user", user);
        model.addAttribute("isTrash", isTrash);
        model.addAttribute("trashColl", cs.getTrashForPath(currentPath));

        return "collections/collectionsBrowser";
    }
}
