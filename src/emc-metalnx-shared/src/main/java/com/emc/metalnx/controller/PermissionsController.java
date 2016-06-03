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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridFilePermission;
import com.emc.metalnx.core.domain.entity.DataGridGroup;
import com.emc.metalnx.core.domain.entity.DataGridGroupBookmark;
import com.emc.metalnx.core.domain.entity.DataGridGroupPermission;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.DataGridUserBookmark;
import com.emc.metalnx.core.domain.entity.DataGridUserPermission;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.GroupBookmarkService;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.PermissionsService;
import com.emc.metalnx.services.interfaces.UserBookmarkService;
import com.emc.metalnx.services.interfaces.UserService;

@Controller
@SessionAttributes({ "currentPath", "groupsToAdd", "usersToAdd" })
@RequestMapping(value = "/permissions")
public class PermissionsController {

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    @Autowired
    GroupBookmarkService groupBookmarkService;

    @Autowired
    UserBookmarkService userBookmarkService;

    @Autowired
    PermissionsService permissionsService;

    @Autowired
    LoggedUserUtils loggedUserUtils;

    @Autowired
    IRODSServices irodsServices;

    @Autowired
    CollectionService collectionService;

    private HashMap<String, String> usersToAdd;
    private HashMap<String, String> groupsToAdd;

    private static final String[] PERMISSIONS = { "NONE", "READ", "WRITE", "OWN" };
    private static final String[] PERMISSIONS_WITHOUT_NONE = { "READ", "WRITE", "OWN" };

    private static final String REQUEST_OK = "OK";
    private static final String REQUEST_ERROR = "ERROR";
    private static final Logger logger = LoggerFactory.getLogger(PermissionsController.class);

    /**
     * Gives permission details related to a collection or file that is passed as a parameter
     *
     * @param model
     * @param path
     * @return
     * @throws DataGridConnectionRefusedException
     * @throws JargonException
     * @throws FileNotFoundException
     */
    @RequestMapping(value = "/getPermissionDetails/")
    public String getPermissionDetails(Model model, @RequestParam("path") String path) throws DataGridConnectionRefusedException,
            FileNotFoundException, JargonException {

        logger.debug("Getting permission info for {}", path);

        DataGridCollectionAndDataObject obj = new DataGridCollectionAndDataObject();
        List<DataGridFilePermission> permissions = null;
        List<DataGridGroupPermission> groupPermissions = null;
        List<DataGridUserPermission> userPermissions = null;
        List<DataGridGroupBookmark> bookmarks = null;
        List<DataGridUserBookmark> userBookmarks = null;
        Set<String> groupsWithBookmarks = null;
        Set<String> usersWithBookmarks = null;
        boolean userCanModify = false;
        boolean isCollection = false;

        try {
            permissions = permissionsService.getPathPermissionDetails(path);
            groupPermissions = permissionsService.getGroupsWithPermissions(permissions);
            userPermissions = permissionsService.getUsersWithPermissions(permissions);

            bookmarks = groupBookmarkService.findBookmarksOnPath(path);
            userBookmarks = userBookmarkService.findBookmarksOnPath(path);
            userCanModify = permissionsService.canLoggedUserModifyPermissionOnPath(path);

            groupsWithBookmarks = new HashSet<String>();
            for (DataGridGroupBookmark bookmark : bookmarks) {
                groupsWithBookmarks.add(bookmark.getGroup().getGroupname());
            }

            usersWithBookmarks = new HashSet<String>();
            for (DataGridUserBookmark userBookmark : userBookmarks) {
                usersWithBookmarks.add(userBookmark.getUser().getUsername());
            }

            obj = collectionService.findByName(path);
            permissionsService.resolveMostPermissiveAccessForUser(obj, loggedUserUtils.getLoggedDataGridUser());
        }
        catch (Exception e) {
            logger.error("Could not get permission details {}: {}", path, e.getMessage());

            groupPermissions = new ArrayList<DataGridGroupPermission>();
            userPermissions = new ArrayList<DataGridUserPermission>();
            groupsWithBookmarks = new HashSet<String>();
            usersWithBookmarks = new HashSet<String>();
        }

        model.addAttribute("usersWithBookmarks", usersWithBookmarks);
        model.addAttribute("groupsWithBookmark", groupsWithBookmarks);
        model.addAttribute("groupPermissions", groupPermissions);
        model.addAttribute("userPermissions", userPermissions);
        model.addAttribute("userCanModify", userCanModify);
        model.addAttribute("permissions", PERMISSIONS);
        model.addAttribute("permissionsWithoutNone", PERMISSIONS_WITHOUT_NONE);
        model.addAttribute("collectionAndDataObject", obj);
        model.addAttribute("isCollection", isCollection);

        return "permissions/permissionDetails :: permissionDetails";
    }

    @RequestMapping(value = "/changePermissionForGroup/")
    @ResponseBody
    public String changePermisionForGroup(@RequestParam("permissionData") String permissionData, @RequestParam("recursive") boolean recursive)
            throws DataGridConnectionRefusedException, JargonException {
        return changePermissionForUserOrGroupOnPath(permissionData, recursive);
    }

    @RequestMapping(value = "/changePermissionForUser/")
    @ResponseBody
    public String changePermisionForUser(@RequestParam("permissionData") String permissionData, @RequestParam("recursive") boolean recursive)
            throws DataGridConnectionRefusedException, JargonException {
        return changePermissionForUserOrGroupOnPath(permissionData, recursive);
    }

    /**
     * Renders table that allows client to select which groups he wants
     * to set new permissions to.
     *
     * @return
     */
    @RequestMapping(value = "/getListOfGroupsForPermissionsCreation/")
    public String getListOfGroupsForPermissionsCreation(Model model) {
        List<DataGridGroup> groups = groupService.findAll();

        model.addAttribute("groups", groups);
        model.addAttribute("groupsToAdd", groupsToAdd);
        model.addAttribute("permissions", PERMISSIONS_WITHOUT_NONE);

        return "permissions/groupsForPermissionCreation";
    }

    /**
     * Renders table that allows client to select which users he wants
     * to set new permissions to.
     *
     * @return
     */
    @RequestMapping(value = "/getListOfUsersForPermissionsCreation/")
    public String getListOfUsersForPermissionsCreation(Model model) {
        List<DataGridUser> users = userService.findAll();

        model.addAttribute("users", users);
        model.addAttribute("usersToAdd", usersToAdd);
        model.addAttribute("permissions", PERMISSIONS_WITHOUT_NONE);

        return "permissions/usersForPermissionCreation";
    }

    @RequestMapping(value = "/addGroupPermissions/")
    @ResponseBody
    public String addGroupToCreationList(@RequestParam("permission") String permission, @RequestParam("groups") String groups,
            @RequestParam("path") String path, @RequestParam("bookmark") boolean bookmark, @RequestParam("recursive") boolean recursive)
            throws FileNotFoundException, JargonException, DataGridConnectionRefusedException {

        boolean operationResult = true;
        String[] groupParts = groups.split(",");

        for (String group : groupParts) {
            operationResult &= permissionsService.setPermissionOnPath(permission, group, path, recursive);
        }

        // Updating bookmarks for the recently-created permissions
        if (bookmark) {
            Set<String> bookmarks = new HashSet<String>();
            bookmarks.add(path);

            // Getting list of groups and updating bookmarks
            List<DataGridGroup> groupObjects = groupService.findByGroupNameList(groupParts);
            for (DataGridGroup g : groupObjects) {
                groupBookmarkService.updateBookmarks(g, bookmarks, null);
            }
        }

        return operationResult ? REQUEST_OK : REQUEST_ERROR;
    }

    @RequestMapping(value = "/addUserPermissions/")
    @ResponseBody
    public String addUserToCreationList(@RequestParam("permission") String permission, @RequestParam("users") String users,
            @RequestParam("path") String path, @RequestParam("bookmark") boolean bookmark, @RequestParam("recursive") boolean recursive)
            throws FileNotFoundException, JargonException, DataGridConnectionRefusedException {

        boolean operationResult = true;
        String[] usernames = users.split(",");

        for (String username : usernames) {
            operationResult &= permissionsService.setPermissionOnPath(permission, username, path, recursive);

            // Updating bookmarks for the recently-created permissions
            if (bookmark) {
                Set<String> bookmarks = new HashSet<String>();
                bookmarks.add(path);

                // Getting list of users and updating bookmarks
                List<DataGridUser> dataGridUsers = userService.findByUsername(username);
                if (dataGridUsers != null && !dataGridUsers.isEmpty()) {
                    userBookmarkService.updateBookmarks(dataGridUsers.get(0), bookmarks, null);
                }
            }
        }

        return operationResult ? REQUEST_OK : REQUEST_ERROR;

    }

    /* ********************************************************************* */
    /* **************************** PRIVATE METHOS ************************* */
    /* ********************************************************************* */

    /**
     * Sends the permissions change to the services layer and returns the
     * result of the operation.
     *
     * @param permissionData
     * @return
     * @throws FileNotFoundException
     * @throws JargonException
     * @throws DataGridConnectionRefusedException
     */
    private String changePermissionForUserOrGroupOnPath(String permissionData, boolean recursive) throws FileNotFoundException, JargonException,
            DataGridConnectionRefusedException {

        // Getting information about the new permission to be applied and the path
        // of the current object (collection or data object)
        String[] permissionParts = permissionData.split("#");
        String newPermission = permissionParts[0];
        String path = permissionParts[1];
        String userOrGroupName = permissionParts[2];

        // Trying to apply modifications on the data grid
        boolean operationResult = permissionsService.setPermissionOnPath(newPermission, userOrGroupName, path, recursive);

        return operationResult ? REQUEST_OK : REQUEST_ERROR;
    }

}
