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

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.DataGridUserBookmark;
import com.emc.metalnx.services.interfaces.AdminServices;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserBookmarkService;
import com.emc.metalnx.services.interfaces.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@SessionAttributes({ "addBookmarks", "removeBookmarks" })
@RequestMapping(value = "/userBookmarks")
public class UserBookmarkController {
    @Autowired
    UserBookmarkService userBookmarkService;

    @Autowired
    UserService userService;

    @Autowired
    GroupService groupService;

    @Autowired
    IRODSServices irodsServices;

    @Autowired
    AdminServices adminServices;

    private Set<String> addBookmark = new HashSet<String>();
    private Set<String> removeBookmark = new HashSet<String>();

    private int totalUserBookmarks;
    private int totalUserBookmarksFiltered;

    private static final String REQUEST_OK = "OK";
    private static final String REQUEST_ERROR = "ERROR";

    public final static Logger logger = LoggerFactory.getLogger(UserBookmarkController.class);

    @RequestMapping(value = "/")
    public String listBookmarks(Model model) {
        String loggedUsername = irodsServices.getCurrentUser();
        String loggedUserZoneName = irodsServices.getCurrentUserZone();
        DataGridUser user = userService.findByUsernameAndAdditionalInfo(loggedUsername, loggedUserZoneName);

        List<DataGridUserBookmark> userBookmakrs = user.getBookmarksSorted();

        model.addAttribute("userBookmarks", userBookmakrs);
        model.addAttribute("foundUserBookmarks", !userBookmakrs.isEmpty());

        return "user/bookmarks/userBookmarks";
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

    @RequestMapping(value = "/addBookmarkToUser/")
    @ResponseBody
    public String addBookmarkToGroup(@RequestParam("username") String username, @RequestParam("path") String path) {
        String zoneName = irodsServices.getCurrentUserZone();
        DataGridUser user = userService.findByUsernameAndAdditionalInfo(username, zoneName);

        if (user == null) {
            return REQUEST_ERROR;
        }

        Set<String> toAdd = new HashSet<String>();
        toAdd.add(path);

        return userBookmarkService.updateBookmarks(user, toAdd, null) ? REQUEST_OK : REQUEST_ERROR;
    }

    @RequestMapping(value = "/removeBookmarkFromUser/")
    @ResponseBody
    public String removeBookmarkFromGroup(@RequestParam("username") String username, @RequestParam("path") String path) {
        logger.info("Request for removing a {} bookmark from {}", path, username);

        String zoneName = irodsServices.getCurrentUserZone();
        DataGridUser user = userService.findByUsernameAndAdditionalInfo(username, zoneName);

        if (user == null) {
            return REQUEST_ERROR;
        }

        Set<String> toRemove = new HashSet<String>();
        toRemove.add(path);

        return userBookmarkService.updateBookmarks(user, null, toRemove) ? REQUEST_OK : REQUEST_ERROR;
    }

    @RequestMapping(value = "/bookmarksPaginated")
    @ResponseBody
    public String bookmarksPaginated(HttpServletRequest request) {
        int draw = Integer.parseInt(request.getParameter("draw"));
        int start = Integer.parseInt(request.getParameter("start"));
        int length = Integer.parseInt(request.getParameter("length"));
        String searchString = request.getParameter("search[value]");
        int orderColumn = Integer.parseInt(request.getParameter("order[0][column]"));
        String orderDir = request.getParameter("order[0][dir]");
        boolean onlyCollections = Boolean.parseBoolean(request.getParameter("onlyCollections"));
        String loggedUsername = irodsServices.getCurrentUser();
        String loggedUserZoneName = irodsServices.getCurrentUserZone();
        DataGridUser user = userService.findByUsernameAndAdditionalInfo(loggedUsername, loggedUserZoneName);
        String[] orderBy = { "name", "path", "created_at", "is_collection" };

        // checking if there is another column to order by
        boolean has2ndColumnToOrderBy = request.getParameter("order[1][column]") != null;

        List<DataGridUserBookmark> userBookmarks = null;
        if (has2ndColumnToOrderBy) {
            List<String> orderByList = new ArrayList<String>();
            List<String> orderDirList = new ArrayList<String>();

            int firstCol = Integer.parseInt(request.getParameter("order[0][column]"));
            int secondCol = Integer.parseInt(request.getParameter("order[1][column]"));

            orderByList.add(orderBy[firstCol]);
            orderDirList.add(request.getParameter("order[0][dir]"));
            orderByList.add(orderBy[secondCol]);
            orderDirList.add(request.getParameter("order[1][dir]"));

            userBookmarks = userBookmarkService.findBookmarksPaginated(user, start, length, searchString, orderByList, orderDirList, onlyCollections);
        }
        else {
            userBookmarks = userBookmarkService.findBookmarksPaginated(user, start, length, searchString, orderBy[orderColumn], orderDir,
                    onlyCollections);
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonResponse = new HashMap<String, Object>();
        String jsonString = "";
        if ("".equals(searchString)) {
            totalUserBookmarks = user.getBookmarks().size();
            totalUserBookmarksFiltered = user.getBookmarks().size();
        }
        else {
            totalUserBookmarksFiltered = userBookmarks.size();
        }

        jsonResponse.put("draw", String.valueOf(draw));
        jsonResponse.put("recordsTotal", String.valueOf(totalUserBookmarks));
        jsonResponse.put("recordsFiltered", String.valueOf(totalUserBookmarksFiltered));
        jsonResponse.put("data", userBookmarks);

        try {
            jsonString = mapper.writeValueAsString(jsonResponse);
        }
        catch (JsonProcessingException e) {
            logger.error("Could not parse user bookmarks to json: {}", e.getMessage());
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

}
