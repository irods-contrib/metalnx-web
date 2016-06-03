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
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.irods.jargon.core.exception.JargonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.emc.metalnx.core.domain.entity.DataGridGroup;
import com.emc.metalnx.core.domain.entity.DataGridGroupBookmark;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.AdminServices;
import com.emc.metalnx.services.interfaces.GroupBookmarkService;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@SessionAttributes({ "addBookmarks", "removeBookmarks" })
@RequestMapping(value = "/groupBookmarks")
public class GroupBookmarkController {
    @Autowired
    GroupBookmarkService groupBookmarkService;

    @Autowired
    GroupService groupService;

    @Autowired
    IRODSServices irodsServices;

    @Autowired
    AdminServices adminServices;

    private Set<String> addBookmark = new HashSet<String>();
    private Set<String> removeBookmark = new HashSet<String>();

    private static final String REQUEST_OK = "OK";
    private static final String REQUEST_ERROR = "ERROR";

    private int totalGroupBookmarks = 0;
    private int totalGroupBookmarksFiltered = 0;

    public final static Logger logger = LoggerFactory.getLogger(GroupBookmarkController.class);

    /**
     * List users by page number
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/groups/")
    public String listGroupsByQuery(Model model) throws DataGridConnectionRefusedException {
        String currentUser = "";
        String currentZone = "";
        List<DataGridGroup> groups = null;
        try {
            currentUser = irodsServices.getCurrentUser();
            currentZone = irodsServices.getCurrentUserZone();
            groups = groupBookmarkService.getGroupsBookmarks(currentUser, currentZone);
        }
        catch (JargonException e) {
            logger.error("Could not get groups bookmarks for {}", currentUser, e);
            groups = new ArrayList<DataGridGroup>();
        }

        model.addAttribute("groups", groups);

        return "user/groups/groupsCollections";
    }

    @RequestMapping(value = "/addBookmark/")
    @ResponseStatus(value = HttpStatus.OK)
    public void addBookmark(@RequestParam("path") String path, Model model) {

        // Remove from the in-memory list before checking database
        if (removeBookmark.contains(path)) {
            removeBookmark.remove(path);
        }
        else {
            addBookmark.add(path);
            model.addAttribute("addBookmark", addBookmark);
        }
    }

    @RequestMapping(value = "/removeBookmark/")
    @ResponseStatus(value = HttpStatus.OK)
    public void removeBookmark(@RequestParam("path") String path) {

        // Removing from the in-memory bookmarks list before checking database
        if (addBookmark.contains(path)) {
            addBookmark.remove(path);
        }
        else {
            removeBookmark.add(path);
        }
    }

    @RequestMapping(value = "/addBookmarkToGroup/")
    @ResponseBody
    public String addBookmarkToGroup(@RequestParam("groupName") String groupName, @RequestParam("path") String path) {

        String zoneName = irodsServices.getCurrentUserZone();
        DataGridGroup group = groupService.findByGroupnameAndZone(groupName, zoneName);

        if (group == null) {
            return REQUEST_ERROR;
        }

        Set<String> toAdd = new HashSet<String>();
        toAdd.add(path);

        groupBookmarkService.updateBookmarks(group, toAdd, null);

        return REQUEST_OK;
    }

    @RequestMapping(value = "/removeBookmarkFromGroup/")
    @ResponseBody
    public String removeBookmarkFromGroup(@RequestParam("groupName") String groupName, @RequestParam("path") String path) {

        String zoneName = irodsServices.getCurrentUserZone();
        DataGridGroup group = groupService.findByGroupnameAndZone(groupName, zoneName);

        if (group == null) {
            return REQUEST_ERROR;
        }

        Set<String> toRemove = new HashSet<String>();
        toRemove.add(path);

        boolean operationResult = groupBookmarkService.updateBookmarks(group, null, toRemove);

        return operationResult ? REQUEST_OK : REQUEST_ERROR;
    }

    @RequestMapping(value = "/groupsBookmarksPaginated")
    @ResponseBody
    public String listGroupsByQueryPaginatedModel(HttpServletRequest request) throws DataGridConnectionRefusedException {

        int draw = Integer.parseInt(request.getParameter("draw"));
        int start = Integer.parseInt(request.getParameter("start"));
        int length = Integer.parseInt(request.getParameter("length"));
        String searchString = request.getParameter("search[value]");
        int orderColumn = Integer.parseInt(request.getParameter("order[0][column]"));
        String orderDir = request.getParameter("order[0][dir]");
        boolean onlyCollections = Boolean.parseBoolean(request.getParameter("onlyCollections"));
        String currentUser = irodsServices.getCurrentUser();
        String currentZone = irodsServices.getCurrentUserZone();
        String[] orderBy = { "Dggb.name", "Dggb.path", "Dgg.groupname", "Dggb.createTs" };
        List<DataGridGroupBookmark> groupBookmarks = null;

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonResponse = new HashMap<String, Object>();
        String jsonString = "";

        try {
            groupBookmarks = groupBookmarkService.getGroupsBookmarksPaginated(currentUser, currentZone, start, length, searchString,
                    orderBy[orderColumn], orderDir, onlyCollections);

            if ("".equals(searchString)) {
                totalGroupBookmarks = groupBookmarkService.countTotalGroupBookmarks(currentUser, currentZone);
                totalGroupBookmarksFiltered = totalGroupBookmarks;
            }
            else {
                totalGroupBookmarksFiltered = groupBookmarks.size();
            }
        }
        catch (JargonException e) {
            logger.error("Could not get groups bookmarks for {}", currentUser);
            groupBookmarks = new ArrayList<DataGridGroupBookmark>();
        }

        jsonResponse.put("draw", String.valueOf(draw));
        jsonResponse.put("recordsTotal", String.valueOf(totalGroupBookmarks));
        jsonResponse.put("recordsFiltered", String.valueOf(totalGroupBookmarksFiltered));
        jsonResponse.put("data", groupBookmarks);

        try {
            jsonString = mapper.writeValueAsString(jsonResponse);
        }
        catch (Exception e) {
            logger.error("Could not parse hashmap in favorites to json", e.getMessage());
        }
        return jsonString;
    }

    public void clearBookmarksLists() {
        addBookmark.clear();
        removeBookmark.clear();
    }

    /**
     * @return the addBookmark
     */
    public Set<String> getAddBookmark() {
        return addBookmark;
    }

    /**
     * @return the removeBookmark
     */
    public Set<String> getRemoveBookmark() {
        return removeBookmark;
    }

    /**
     * Add a given path to the list of bookmarks
     *
     * @param path
     *            path to any collection or file in the grid to be bookmarked
     */
    public boolean addPathAsGroupBookmark(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        return addBookmark.add(path);
    }

}
