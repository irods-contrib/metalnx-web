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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.DataGridUserFavorite;
import com.emc.metalnx.services.interfaces.FavoritesService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/favorites")
public class FavoritesController {
    @Autowired
    FavoritesService favoritesService;

    @Autowired
    UserService userService;

    @Autowired
    IRODSServices irodsServices;

    private static final String REQUEST_OK = "OK";
    private static final String REQUEST_ERROR = "ERROR";

    private int totalFavorites;
    private int totalFavoritesFiltered;

    private static final Logger logger = LoggerFactory.getLogger(FavoritesController.class);

    /**
     * Responds to the list favorites request
     *
     * @param model
     * @return the template with a list of favorite items
     */
    @RequestMapping(value = "/")
    public String listfavorites(Model model) {
        String loggedUsername = irodsServices.getCurrentUser();
        String loggedUserZoneName = irodsServices.getCurrentUserZone();
        DataGridUser user = userService.findByUsernameAndAdditionalInfo(loggedUsername, loggedUserZoneName);

        List<DataGridUserFavorite> userFavorites = user.getFavoritesSorted();

        model.addAttribute("userFavorites", userFavorites);

        return "user/favorites/favorites";
    }

    /**
     * Add a path to the favorites list
     *
     * @param model
     * @param path
     *            path to be added to the favorites
     */
    @RequestMapping(value = "/addFavoriteToUser/")
    @ResponseBody
    public String addFavoriteToUser(@RequestParam("path") String path) {
        String zoneName = irodsServices.getCurrentUserZone();
        String username = irodsServices.getCurrentUser();

        logger.info("Request for adding a {} favorite from {}", path, username);
        DataGridUser user = userService.findByUsernameAndAdditionalInfo(username, zoneName);

        Set<String> toAdd = new HashSet<String>();
        toAdd.add(path);

        boolean operationResult = favoritesService.updateFavorites(user, toAdd, null);

        return operationResult ? REQUEST_OK : REQUEST_ERROR;
    }

    /**
     * Remove a path to the favorites list
     *
     * @param model
     * @param path
     *            path to be removed from the favorites
     */
    @RequestMapping(value = "/removeFavoriteFromUser/")
    @ResponseBody
    public String removeFavoriteFromUser(@RequestParam("path") String path) {
        String username = irodsServices.getCurrentUser();
        logger.info("Request for removing a {} favorite from {}", path, username);

        String zoneName = irodsServices.getCurrentUserZone();
        DataGridUser user = userService.findByUsernameAndAdditionalInfo(username, zoneName);

        Set<String> toRemove = new HashSet<String>();
        toRemove.add(path);

        boolean operationResult = favoritesService.updateFavorites(user, null, toRemove);

        return operationResult ? REQUEST_OK : REQUEST_ERROR;
    }

    @RequestMapping(value = "/favoritesPaginated")
    @ResponseBody
    public String favoritesPaginated(HttpServletRequest request) {

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

        List<DataGridUserFavorite> userFavorites = favoritesService.findFavoritesPaginated(user, start, length, searchString, orderBy[orderColumn],
                orderDir, onlyCollections);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonResponse = new HashMap<String, Object>();
        String jsonString = "";
        if ("".equals(searchString)) {
            totalFavorites = user.getFavorites().size();
            totalFavoritesFiltered = user.getFavorites().size();
        }
        else {
            totalFavoritesFiltered = userFavorites.size();
        }

        jsonResponse.put("draw", String.valueOf(draw));
        jsonResponse.put("recordsTotal", String.valueOf(totalFavorites));
        jsonResponse.put("recordsFiltered", String.valueOf(totalFavoritesFiltered));
        jsonResponse.put("data", userFavorites);

        try {
            jsonString = mapper.writeValueAsString(jsonResponse);
        }
        catch (Exception e) {
            logger.error("Could not parse hashmap in favorites to json", e.getMessage());
        }
        return jsonString;
    }
}
