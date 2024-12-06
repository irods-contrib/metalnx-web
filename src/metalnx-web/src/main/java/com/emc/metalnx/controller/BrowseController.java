/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.extensions.dataprofiler.DataProfile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.util.StringUtils;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.IconObject;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.modelattribute.breadcrumb.DataGridBreadcrumb;
import com.emc.metalnx.modelattribute.collection.CollectionOrDataObjectForm;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.HeaderService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.MetadataService;
import com.emc.metalnx.services.interfaces.PermissionsService;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.RuleDeploymentService;
import com.emc.metalnx.services.interfaces.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Transitional controller factors out all sub functions of the
 * {@link CollectionController} so that this controller can respond to 'deep
 * linkable' paths, including from breadcrumbs in the info pages
 *
 * @author Mike Conway - NIEHS
 *
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@SessionAttributes({ "sourcePaths" })
@RequestMapping(value = "/browse")
public class BrowseController {

    @Autowired
    CollectionService cs;

    @Autowired
    ResourceService resourceService;

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    @Autowired
    MetadataService metadataService;

    @Autowired
    PermissionsService permissionsService;

    @Autowired
    IRODSServices irodsServices;

    @Autowired
    LoggedUserUtils loggedUserUtils;

    @Autowired
    RuleDeploymentService ruleDeploymentService;

    @Autowired
    HeaderService headerService;

    private static final Logger logger = LoggerFactory.getLogger(BrowseController.class);

    // ui mode that will be shown when the rods user switches mode from admin to
    // user and vice-versa
    public static final String UI_USER_MODE = "user";
    public static final String UI_ADMIN_MODE = "admin";

    public static final int MAX_HISTORY_SIZE = 10;

    private static final Map<SortingComparatorKey, Comparator<DataGridCollectionAndDataObject>>
        sortingComparators = new HashMap<>();

    static
    {
        sortingComparators.put(new SortingComparatorKey("1", "asc"),  Comparator.comparing(DataGridCollectionAndDataObject::getName));
        sortingComparators.put(new SortingComparatorKey("1", "desc"), Comparator.comparing(DataGridCollectionAndDataObject::getName).reversed());

        sortingComparators.put(new SortingComparatorKey("2", "asc"),  Comparator.comparing(DataGridCollectionAndDataObject::getModifiedAt));
        sortingComparators.put(new SortingComparatorKey("2", "desc"), Comparator.comparing(DataGridCollectionAndDataObject::getModifiedAt).reversed());

        sortingComparators.put(new SortingComparatorKey("3", "asc"),  Comparator.comparing(DataGridCollectionAndDataObject::getSize));
        sortingComparators.put(new SortingComparatorKey("3", "desc"), Comparator.comparing(DataGridCollectionAndDataObject::getSize).reversed());
    }
        
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

    private Stack<String> collectionHistoryBack;
    private Stack<String> collectionHistoryForward;

    // variable to save trash path for the logged user
    private String userTrashPath = "";
    // saves the trash under the zone
    private String zoneTrashPath = "";

    @PostConstruct
    public void init() throws DataGridException {
        collectionHistoryBack = new Stack<String>();
        collectionHistoryForward = new Stack<String>();

        sourcePaths = new ArrayList<>();
        parentPath = "";
        currentPath = "";
    }

    /**
     * Get a list of resources in which an object doesn't have replicas
     *
     * @param model
     * @return list of resources in which an object can be replicated
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "getAvailableRescForPath/")
    public String getAvailableRescForPath(final Model model, @RequestParam("isUpload") final boolean isUpload)
            throws DataGridConnectionRefusedException {

        logger.info("getAvailableRescForPath()");

        Map<DataGridCollectionAndDataObject, DataGridResource> replicasMap = null;
        List<DataGridResource> resources = resourceService.findFirstLevelResources();

        if (!isUpload) {
            for (String path : sourcePaths) {
                replicasMap = cs.listReplicasByResource(path);
                for (DataGridResource resc : replicasMap.values()) {
                    if (resources.contains(resc)) {
                        resources.remove(resc);
                    }
                }
            }
        }
        model.addAttribute("resources", resources);
        return "collections/collectionsResourcesForReplica";
    }

    /**
     * Switches an admin from the Rods_Admin UI to the Rods_User UI and vice-versa.
     *
     * @param model
     * @return redirects an admin user from to the new UI view mode (admin view or
     *         user view)
     */
    @RequestMapping(value = "/switchMode/")
    @ResponseStatus(value = HttpStatus.OK)
    public void switchMode(final Model model, final HttpServletRequest request,
            @RequestParam("currentMode") final String currentMode, final RedirectAttributes redirectAttributes) {

        logger.info("switchMode()");

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
     * Responds the getSubdirectories request finding collections and data objects
     * that exist underneath a certain path
     *
     * @param model
     * @param path  path to find all subdirectories and objects
     * @return treeView template that renders all nodes of certain path (parent)
     * @throws DataGridException if Metalnx cannot find collections and objects
     *                           inside the path
     * @throws JargonException
     */
    @RequestMapping(value = "/getSubDirectories/", method = RequestMethod.POST)
    public String getSubDirectories(final Model model, @RequestParam("path") String path)
            throws DataGridException, JargonException {

        logger.info("getSubDirectories()");
        logger.info("model:{}", model);
        logger.info("path:{}", path);

        logger.info("Get subdirectories of {}", path);

        try {
            return getCollBrowserView(model, path);
        } catch (Exception e) {
            logger.error("exception getting coll browser view", e);
            throw e;
        }
    }

    /**
     * Responds the getSubdirectories request finding collections and data objects
     * that exist underneath a certain path
     *
     * @param model
     * @param path
     * @return treeView template that renders all nodes of certain path (parent)
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/getSubDirectoriesOldTree/")
    public String getSubDirectoriesOldTree(final Model model, @RequestParam("path") String path)
            throws DataGridConnectionRefusedException {

        if (path.isEmpty()) {
            path = "/";
        } else {
            if (path.endsWith("/") && path.compareTo("/") != 0) {
                path = path.substring(0, path.length() - 1);
            }
        }

        // The line below was modified so that only collection would be retrieved
        model.addAttribute("dataGridCollectionAndDataObjectList", cs.getSubCollectionsUnderPath(path));

        return "collections/oldTreeView :: oldTreeView";
    }

    /**
     * Gets checksum, total number of replicas and where each replica lives in the
     * data grid for a specific data object
     *
     * @param model
     * @param path  path to the data object to get checksum and replica information
     * @return the template that shows the data object information
     * @throws DataGridException
     * @throws FileNotFoundException
     */
    @RequestMapping(value = "/info/", method = RequestMethod.POST)
    public String getFileInfo(final Model model, final String path) throws DataGridException, FileNotFoundException {

        logger.info("CollectionController getInfoFile() starts :: " + path);
        DataGridCollectionAndDataObject dataGridObj = null;
        Map<DataGridCollectionAndDataObject, DataGridResource> replicasMap = null;

        try {

            dataGridObj = cs.findByName(path);

            if (dataGridObj != null && !dataGridObj.isCollection()) {
                replicasMap = cs.listReplicasByResource(path);
                dataGridObj.setChecksum(cs.getChecksum(path));
                dataGridObj.setNumberOfReplicas(cs.getTotalNumberOfReplsForDataObject(path));
                dataGridObj.setReplicaNumber(String.valueOf(cs.getReplicationNumber(path)));
                dataGridObj.setMostPermissiveAccessForCurrentUser(permissionsService.resolveMostPermissiveAccessForUser(
                        dataGridObj.getPath(), loggedUserUtils.getLoggedDataGridUser().getUsername()));

            }

        } catch (DataGridConnectionRefusedException e) {
            logger.error("Could not connect to the data grid", e);
            throw e;
        } catch (DataGridException e) {
            logger.error("Could not get file info for {}", path, e);
            throw e;
        } catch (FileNotFoundException e) {
            logger.error("file does not exist for:{}", path, e);
            throw e;
        }

        model.addAttribute("collectionAndDataObject", dataGridObj);
        model.addAttribute("currentCollection", dataGridObj);
        model.addAttribute("replicasMap", replicasMap);
        model.addAttribute("infoFlag", true);

        logger.info("CollectionController getInfoFile() ends !!");
        return "collections/info :: infoView";
        // return "collections/info";
    }

    /**
     * Finds all collections and files existing under a certain path for a given
     * group name.
     *
     * @param model
     * @param path      start point to get collections and files
     * @param groupName group that all collections and files permissions will be
     *                  listed
     * @return
     * @throws DataGridConnectionRefusedException
     * @throws JargonException
     * @throws FileNotFoundException
     */
    @RequestMapping(value = "/getDirectoriesAndFilesForGroupForm")
    public String getDirectoriesAndFilesForGroupForm(final Model model, @RequestParam("path") String path,
            @RequestParam("groupName") final String groupName,
            @RequestParam("retrievePermissions") final boolean retrievePermissions)
            throws DataGridConnectionRefusedException, FileNotFoundException, JargonException {
        if (path == null || path == "") {
            path = MiscIRODSUtils.buildPathZoneAndHome(irodsServices.getCurrentUserZone());
        }

        List<DataGridCollectionAndDataObject> list = null;
        list = cs.getSubCollectionsAndDataObjectsUnderPath(path);

        Set<String> readPermissions = null;
        Set<String> writePermissions = null;
        Set<String> ownershipPermissions = null;
        Set<String> inheritPermissions = null;

        if (retrievePermissions) {
            readPermissions = cs.listReadPermissionsForPathAndGroup(path, groupName);
            writePermissions = cs.listWritePermissionsForPathAndGroup(path, groupName);
            ownershipPermissions = cs.listOwnershipForPathAndGroup(path, groupName);
            try {
                inheritPermissions = cs.listInheritanceForPath(path);
            } catch (DataGridException dnf) {
                // may not find based on permissions..it's ok
                inheritPermissions = new HashSet<String>();
            }
        } else {
            readPermissions = new HashSet<String>();
            writePermissions = new HashSet<String>();
            ownershipPermissions = new HashSet<String>();
            inheritPermissions = new HashSet<String>();
        }

        model.addAttribute("dataGridCollectionAndDataObjectList", list);
        model.addAttribute("currentPath", path);
        model.addAttribute("encodedCurrentPath", URLEncoder.encode(currentPath));

        model.addAttribute("readPermissions", readPermissions);
        model.addAttribute("writePermissions", writePermissions);
        model.addAttribute("ownershipPermissions", ownershipPermissions);
        model.addAttribute("inheritPermissions", inheritPermissions);
        return "collections/treeViewForGroupForm :: treeView";
    }

    /**
     * Finds all collections existing under a certain path.
     *
     * @param model
     * @param path     start point to get collections and files
     * @param username user who all collections and files permissions will be listed
     * @return the template that will render the tree
     * @throws DataGridConnectionRefusedException
     * @throws JargonException
     * @throws FileNotFoundException
     */
    @RequestMapping(value = "/getDirectoriesAndFilesForUser")
    public String getDirectoriesAndFilesForUser(final Model model, @RequestParam("path") final String path,
            @RequestParam("username") final String username,
            @RequestParam("retrievePermissions") final boolean retrievePermissions)
            throws DataGridConnectionRefusedException, FileNotFoundException, JargonException {

        logger.info("getDirectoriesAndFilesForUser()");

        if (model == null) {
            throw new IllegalArgumentException("null model");
        }

        if (path == null) {
            throw new IllegalArgumentException("null path");
        }

        if (username == null) {
            throw new IllegalArgumentException("null username");
        }

        logger.info("model:{}", model);
        logger.info("path:{}", path);
        logger.info("username:{}", username);
        logger.info("retrievePermissions:{}", retrievePermissions);

        List<DataGridCollectionAndDataObject> list = new ArrayList<DataGridCollectionAndDataObject>();
        Set<String> readPermissions = new HashSet<String>();
        Set<String> writePermissions = new HashSet<String>();
        Set<String> ownershipPermissions = new HashSet<String>();
        Set<String> inheritPermissions = new HashSet<String>();

        // If a string is null, empty or contains only white spaces, StringUtils
        // returns true
        boolean isPathEmpty = StringUtils.isEmptyOrWhitespace(path);
        boolean isUsernameEmpty = StringUtils.isEmptyOrWhitespace(username);

        if (!isPathEmpty) {
            logger.info("path not empty");
            // When adding a user (there is no username), we still need to be
            // able to walk through the iRODS tree
            list = cs.getSubCollectionsAndDataObjectsUnderPath(path);

            if (!isUsernameEmpty) {
                if (retrievePermissions) {
                    readPermissions = cs.listReadPermissionsForPathAndUser(path, username);
                    writePermissions = cs.listWritePermissionsForPathAndUser(path, username);
                    ownershipPermissions = cs.listOwnershipForPathAndUser(path, username);
                    try {
                        inheritPermissions = cs.listInheritanceForPath(path);
                    } catch (DataGridException dnf) {
                        // may not find based on permissions..it's ok
                    }
                }
            }
        }

        model.addAttribute("dataGridCollectionAndDataObjectList", list);
        model.addAttribute("currentPath", path);
        model.addAttribute("encodedCurrentPath", URLEncoder.encode(currentPath));

        model.addAttribute("readPermissions", readPermissions);
        model.addAttribute("writePermissions", writePermissions);
        model.addAttribute("ownershipPermissions", ownershipPermissions);
        model.addAttribute("inheritPermissions", inheritPermissions);
        logger.info("model:{}", model);

        logger.info("done with processing:{}", model);
        return "collections/treeViewForUserForm :: treeView";
    }

    /**
     * Looks for collections or data objects that match the parameter string
     *
     * @param model
     * @param name  collection name that will be searched in the data grid
     * @return the template that renders all collections and data objects matching
     *         the parameter string
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "/find/{name}")
    public String listCollectionsAndDataObjects(final Model model, @PathVariable final String name)
            throws DataGridConnectionRefusedException {
        logger.info("Finding collections or data objects that match " + name);

        // Find collections and data objects
        List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = cs
                .searchCollectionAndDataObjectsByName(name + "%");
        model.addAttribute("dataGridCollectionAndDataObjects", dataGridCollectionAndDataObjects);
        return "collections/collectionsBrowser :: treeView";
    }

    /**
     * Performs the action of actually creating a collection in iRODS
     *
     * @param model
     * @param collection
     * @return if the creation of collection was successful, it returns the
     *         collection management template, and returns the add collection
     *         template, otherwise.
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "add/action", method = RequestMethod.POST)
    public String addCollection(final Model model, @ModelAttribute final CollectionOrDataObjectForm collection,
            final RedirectAttributes redirectAttributes) throws DataGridConnectionRefusedException {

        logger.info("addCollection()");
        logger.info("collection:{}", collection);
        logger.info("redirectAttributes:{}", redirectAttributes);

        DataGridCollectionAndDataObject newCollection = new DataGridCollectionAndDataObject(
                currentPath + '/' + collection.getCollectionName(), collection.getCollectionName(), currentPath, true);

        logger.info("newCollection:{}", newCollection);

        newCollection.setParentPath(currentPath);
        newCollection.setCreatedAt(new Date());
        newCollection.setModifiedAt(newCollection.getCreatedAt());
        newCollection.setInheritanceOption(collection.getInheritOption());

        boolean creationSucessful;
        try {
            creationSucessful = cs.createCollection(newCollection, true);
            logger.info("creationSuccessful?:{}", creationSucessful);

            if (creationSucessful) {
                redirectAttributes.addFlashAttribute("collectionAddedSuccessfully", collection.getCollectionName());
            }
        } catch (DataGridConnectionRefusedException e) {
            throw e;
        } catch (DataGridException e) {
            logger.error("Could not create collection/data object (lack of permission): ", e.getMessage());
            redirectAttributes.addFlashAttribute("missingPermissionError", true);
        }

        return "redirect:/collections?path=" + URLEncoder.encode(currentPath);
    }

    /**
     * Performs the action of modifying a collection
     *
     * @throws DataGridConnectionRefusedException
     */
    @RequestMapping(value = "modify/action", method = RequestMethod.POST)
    public String modifyAction(@ModelAttribute final CollectionOrDataObjectForm collForm,
            final RedirectAttributes redirectAttributes) throws DataGridException {
        logger.info("modify/action starts...");
        String previousPath = collForm.getPath();
        String parentPath = previousPath.substring(0, previousPath.lastIndexOf("/"));
        String newPath = String.format("%s/%s", parentPath, collForm.getCollectionName());
        logger.info("previousPath: " + previousPath);
        logger.info("parentPath: " + parentPath);
        logger.info("newPath: " + newPath);
        logger.info("Path values used to modify/action previousPath: {} to newPath: {}", previousPath, newPath);

        boolean modificationSuccessful = cs.modifyCollectionAndDataObject(previousPath, newPath,
                collForm.getInheritOption());

        if (modificationSuccessful) {
            logger.debug("Collection/Data Object {} modified to {}", previousPath, newPath);
            redirectAttributes.addFlashAttribute("collectionModifiedSuccessfully", collForm.getCollectionName());
        }

        String template = "redirect:/collections" + parentPath;
        logger.info("Returning after renaming :: " + template);

        return "redirect:/collections?path=" + URLEncoder.encode(parentPath);
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
    @RequestMapping(value = "/home")
    public String homeCollection(final Model model) throws DataGridException {
        // cleaning session variables
        logger.info("homeCollection()");
        sourcePaths.clear();

        DataGridUser user = loggedUserUtils.getLoggedDataGridUser();

        currentPath = "anonymous".equals(user.getUsername())
            ? cs.getHomeDirectyForPublic()
            : cs.getHomeDirectyForCurrentUser();

        parentPath = currentPath;

        return "redirect:/collections?path=" + URLEncoder.encode(currentPath);
    }

    @RequestMapping(value = "/getBreadCrumbForObject/")
    public String getBreadCrumbForObject(final Model model, @RequestParam("path") String path)
            throws DataGridException {
        logger.info("getBreadCrumbForObject()");

        if (path.isEmpty()) {
            path = currentPath;
        } else {
            if (path.endsWith("/") && path.compareTo("/") != 0) {
                path = path.substring(0, path.length() - 1);
            }

            currentPath = URLDecoder.decode(path);

        }

        logger.info("path:{}", path);

        setBreadcrumbToModel(model, path);
        return "collections/collectionsBreadCrumb";
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
    // FIXME: urlencode? - mcc
    @ResponseBody
    @RequestMapping(value = "isValidCollectionName/{newObjectName}/", method = RequestMethod.GET, produces = {
            "text/plain" })
    public String isValidCollectionName(@PathVariable final String newObjectName) throws DataGridException {
        String rc = "true";
        String newPath = String.format("%s/%s", currentPath, newObjectName);

        try {
            cs.findByName(newPath);
            rc = "false";
        } catch (FileNotFoundException e) {
            logger.debug("Path {} does not exist. Executing modification", newPath, e);
        } catch (DataGridException e) {
            logger.error("unexpected exception validating path:{}", newPath, e);
            throw e;
        }
        return rc;
    }

    /*
     * *************************************************************************
     * ******************************** UTILS **********************************
     * *************************************************************************
     */
    
    private static class SortingComparatorKey
    {
        final String columnIndex;
        final String direction;
        final int hashCode;
        
        public SortingComparatorKey(String _columnIndex, String _direction)
        {
            if (null == _columnIndex || _columnIndex.isEmpty()) {
                throw new IllegalArgumentException("column index is null or empty");
            }

            if (null == _direction || _direction.isEmpty()) {
                throw new IllegalArgumentException("direction is null or empty");
            }

            columnIndex = _columnIndex;
            direction = _direction;
            hashCode = Objects.hash(columnIndex, direction);
        }
        
        @Override
        public int hashCode()
        {
            return hashCode;
        }
        
        @Override
        public boolean equals(Object _other)
        {
            if (this == _other) return true;
            if (null == _other) return false;
            if (this.getClass() != _other.getClass()) return false;
            
            SortingComparatorKey that = (SortingComparatorKey) _other;
            
            return columnIndex.equals(that.columnIndex) && direction.equals(that.direction);
        }
    }
    
    private static void sort(List<DataGridCollectionAndDataObject> _objects,
                             String _columnToSortBy,
                             String _sortDirection)
    {
        logger.info("sort()");
        logger.info("_columnToSortBy: {}", _columnToSortBy);
        logger.info("_sortDirection: {}", _sortDirection);

        if (_columnToSortBy == null || _columnToSortBy.isEmpty() || _sortDirection == null || _sortDirection.isEmpty()) {
            logger.info("Not enough information to sort collections and data objects.");
            return;
        }

        SortingComparatorKey key = new SortingComparatorKey(_columnToSortBy, _sortDirection);
        Comparator<DataGridCollectionAndDataObject> comp = sortingComparators.get(key);

        if (null == comp) {
            logger.info("No comparator matched sorting criteria.");
            return;
        }

        _objects.sort(comp);
    }

    /**
     * Finds all collections and data objects existing under a certain path
     *
     * @param request contains all parameters in a map, we can use it to get all
     *                parameters passed in request
     * @return json with collections and data objects
     * @throws DataGridConnectionRefusedException
     * @throws JargonException
     */
    @RequestMapping(value = "getPaginatedJSONObjs/")
    @ResponseBody
    public String getPaginatedJSONObjs(final HttpServletRequest request) throws DataGridException
    {
        logger.info("getPaginatedJSONObjs()");

        List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects;

        int draw = Integer.parseInt(request.getParameter("draw"));
        int start = Integer.parseInt(request.getParameter("start"));
        int length = Integer.parseInt(request.getParameter("length"));
        boolean deployRule = request.getParameter("rulesdeployment") != null;

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
            logger.info("using path of:{}", currentPath);
            logger.debug("deployRule:{}", deployRule);
            String path = currentPath;

            if (deployRule) {
                logger.debug("getting rule cache path");
                path = ruleDeploymentService.getRuleCachePath();
                currentPath = path;
                logger.info("rule cache path:{}", path);

                if (!ruleDeploymentService.ruleCacheExists()) {
                    logger.warn("no rule cache in place, create zone/.ruleCache directory");
                    ruleDeploymentService.createRuleCache();
                }
            }

            Math.floor(start / length);
            logger.info("getting subcollections under path:{}", path);
            dataGridCollectionAndDataObjects = cs.getSubCollectionsAndDataObjectsUnderPath(path); // TODO: temporary add paging service

            logger.debug("dataGridCollectionAndDataObjects:{}", dataGridCollectionAndDataObjects);
            
            // Sort based on the column selected by the user.
            sort(dataGridCollectionAndDataObjects,
                 request.getParameter("order[0][column]"),
                 request.getParameter("order[0][dir]"));

            /*
             * cs.getSubCollectionsAndDataObjectsUnderPathThatMatchSearchTextPaginated(
             * path, searchString, startPage.intValue(), length, orderColumn, orderDir,
             * pageContext);
             */
            totalObjsForCurrentSearch = pageContext.getTotalNumberOfItems();
            totalObjsForCurrentPath = pageContext.getTotalNumberOfItems();

            jsonResponse.put("recordsTotal", String.valueOf(totalObjsForCurrentPath));
            jsonResponse.put("recordsFiltered", String.valueOf(totalObjsForCurrentSearch));
            jsonResponse.put("data", dataGridCollectionAndDataObjects);
        }
        catch (DataGridConnectionRefusedException e) {
            logger.error("connection refused", e);
            throw e;
        }
        catch (Exception e) {
            logger.error("Could not get collections/data objs under path {}: {}", currentPath, e.getMessage());
            throw new DataGridException("exception getting paginated objects", e);
        }

        try {
            jsonString = mapper.writeValueAsString(jsonResponse);
        }
        catch (JsonProcessingException e) {
            logger.error("Could not parse hashmap in collections to json: {}", e.getMessage());
            throw new DataGridException("exception in json parsing", e);
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
     *
     * @param path path to be removed
     */
    public void removePathFromHistory(final String path) {
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
     * Creates the breadcrumb based on a given path.
     *
     * @param model Model attribute to set variables to be used in the view
     * @param path  path that will be displayed in the breadcrumb
     * @throws DataGridException
     */
    private void setBreadcrumbToModel(final Model model, final String path) throws DataGridException {
        DataGridCollectionAndDataObject obj;
        try {
            obj = cs.findByName(path);
        } catch (FileNotFoundException e) {
            obj = new DataGridCollectionAndDataObject();
            obj.setPath(path);
            obj.setCollection(false);
            obj.setParentPath(path.substring(0, path.lastIndexOf("/") + 1));
            obj.setName(path.substring(path.lastIndexOf("/") + 1, path.length()));
            logger.error("Could not find DataGridCollectionAndDataObject by path: {}", e.getMessage());
        } catch (DataGridException e) {
            logger.error("unable to find path for breadcrumb", e);
            throw e;
        }

        setBreadcrumbToModel(model, obj);
    }

    /**
     * Creates the breadcrumb based on a given path.
     *
     * @param model Model attribute to set variables to be used in the view
     * @param obj   {@code DataGridCollectionAndDataObject} object
     */
    private void setBreadcrumbToModel(final Model model, final DataGridCollectionAndDataObject obj) {
        logger.info("setBreadcrumbToModel()");

        if (model == null) {
            throw new IllegalArgumentException("null model");
        }

        if (obj == null) {
            throw new IllegalArgumentException("null obj");
        }

        logger.info("model:{}", model);
        logger.info("obj:{}", obj);

        model.addAttribute("collectionAndDataObject", obj);
        logger.info("path for breadcrumb:{}", obj.getPath());
        DataGridBreadcrumb breadcrumb = new DataGridBreadcrumb(obj.getPath());
        logger.info("breadcrumb is:{}", breadcrumb);
        model.addAttribute("breadcrumb", breadcrumb);
        model.addAttribute("homeCollectionName", irodsServices.getCurrentUser());
    }

    /**
     * Finds all collections and data objects existing under a certain path
     *
     * @param model
     * @param path  path to get all directories and data objects from
     * @return collections browser template that renders all items of certain path
     *         (parent)
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the
     *                                            grid.
     */
    private String getCollBrowserView(final Model model, String path) throws JargonException, DataGridException {
        logger.info("getCollBrowserView()");

        logger.info("model:{}", model);
        logger.info("path:{}", path);

        logger.info("find collection by name:{}", path);
        DataGridCollectionAndDataObject dataGridObj = null;
        try {
            dataGridObj = cs.findByName(path);
        } catch (FileNotFoundException fnf) {
            logger.warn("file not found for:{}", path);
            // I don't have a path so use the user home
            logger.info("no path, so using user home directory");
            model.addAttribute("invalidPath", path); // TODO: refactor into something more elegant - mcc
            IRODSAccount irodsAccount = irodsServices.getCollectionAO().getIRODSAccount();
            path = MiscIRODSUtils.buildIRODSUserHomeForAccountUsingDefaultScheme(irodsAccount);
        }

        if (path.endsWith("/") && path.compareTo("/") != 0) {
            path = path.substring(0, path.length() - 1);
        }

        currentPath = path;
        logger.info("currentPath:{}", currentPath);

        DataGridUser user = loggedUserUtils.getLoggedDataGridUser();

        if (zoneTrashPath == null || zoneTrashPath.isEmpty()) {
            zoneTrashPath = String.format("/%s/trash", irodsServices.getCurrentUserZone());
        }

        // TODO: do I really need these permission path checks? I can let iRODS worry
        // about permissions - mcc

        CollectionOrDataObjectForm collectionForm = new CollectionOrDataObjectForm();
        String permissionType = "none";

        if (dataGridObj.isProxy()) {
            logger.info("this is a proxy, so fake out the options");
            collectionForm.setInheritOption(false);
            permissionType = "read";
        } else {
            logger.info("this is not a proxy, so gather permission info");

            permissionType = cs.getPermissionsForPath(path);
            collectionForm.setInheritOption(cs.getInheritanceOptionForCollection(currentPath));
            dataGridObj.setMostPermissiveAccessForCurrentUser(
                    permissionsService.resolveMostPermissiveAccessForUser(dataGridObj.getPath(), user.getUsername()));
        }

        logger.debug("permission options are set");

        boolean isPermissionOwn = "own".equals(permissionType);
        boolean isTrash = path.contains(zoneTrashPath) && (isPermissionOwn || user.isAdmin());
        boolean inheritanceDisabled = !isPermissionOwn && collectionForm.getInheritOption();

        model.addAttribute("collectionAndDataObject", dataGridObj);
        model.addAttribute("isTrash", isTrash);
        model.addAttribute("permissionType", permissionType);
        model.addAttribute("currentPath", currentPath);
        model.addAttribute("encodedCurrentPath", URLEncoder.encode(currentPath));

        model.addAttribute("isCurrentPathCollection", cs.isCollection(path));
        model.addAttribute("user", user);
        model.addAttribute("trashColl", cs.getTrashForPath(currentPath));
        model.addAttribute("collection", collectionForm);
        model.addAttribute("inheritanceDisabled", inheritanceDisabled);
        model.addAttribute("requestMapping", "/browse/add/action/");
        model.addAttribute("parentPath", parentPath);

        setBreadcrumbToModel(model, dataGridObj);
        logger.info("forwarding to collections/collectionsBrowser");
        return "collections/collectionsBrowser";

    }

    @RequestMapping(value = "/summary", method = RequestMethod.POST)
    public String getSummary(final Model model, @RequestParam("path") final String path)
            throws DataGridException, UnsupportedEncodingException {
        logger.info("BrowseController getSummary() starts :: " + path);

        IconObject icon = null;
        String mimeType = "";

        try {
            @SuppressWarnings("rawtypes")
            DataProfile dataProfile = cs.getCollectionDataProfile(URLDecoder.decode(path, "UTF-8"));
            logger.info("DataProfiler is :: " + dataProfile);

            if (dataProfile != null && dataProfile.isFile()) {
                mimeType = dataProfile.getDataType().getMimeType();
            }
            icon = cs.getIcon(mimeType);

            model.addAttribute("icon", icon);
            model.addAttribute("dataProfile", dataProfile);

            logger.info("getSummary() ends !!");
            return "collections/summarySidenav :: SummarySidenavView";
        } catch (FileNotFoundException e) {
            logger.error("#########################");
            logger.error("collection does not exist.");
            e.printStackTrace();
            logger.error("#########################");
            return "httpErrors/noAccess";
        }
    }

}
