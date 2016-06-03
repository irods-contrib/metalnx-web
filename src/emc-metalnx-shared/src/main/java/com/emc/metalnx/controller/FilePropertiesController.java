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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridFilePropertySearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum;
import com.emc.metalnx.core.domain.entity.enums.FilePropertyField;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FilePropertyService;
import com.emc.metalnx.services.interfaces.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping(value = "/fileproperty")
public class FilePropertiesController {

    @Autowired
    FilePropertyService filePropertyService;

    @Autowired
    CollectionService collectionService;

    @Autowired
    UserService userService;

    @Autowired
    LoggedUserUtils loggedUserUtils;

    @Value("${irods.zoneName}")
    private String zoneName;

    // ui mode that will be shown when the rods user switches mode from admin to user and vice-versa
    public static final String UI_USER_MODE = "user";
    public static final String UI_ADMIN_MODE = "admin";

    // current page the user currently is (pagination)
    private int currentPage = 1;

    // current metadata search
    List<DataGridFilePropertySearch> currentFilePropertySearch;

    private String jsonFilePropertySearch;

    private static final Logger logger = LoggerFactory.getLogger(FilePropertiesController.class);

    @RequestMapping(value = "/")
    public String index(Model model, HttpServletRequest request,
            @RequestParam(value = "backFromCollections", required = false) boolean backFromCollections) {

        DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
        String uiMode = (String) request.getSession().getAttribute("uiMode");
        if (uiMode == null || uiMode.isEmpty()) {
            if (loggedUser.isAdmin()) {
                uiMode = UI_ADMIN_MODE;
            }
            else {
                uiMode = UI_USER_MODE;
            }
        }

        if (backFromCollections) {
            model.addAttribute("jsonFilePropertySearch", jsonFilePropertySearch);
        }
        model.addAttribute("uiMode", uiMode);
        return "metadata/metadataDisplay";
    }

    @RequestMapping(value = "/search")
    @ResponseBody
    public String search(Model model, final RedirectAttributes redirectAttributes,
            @RequestParam(value = "jsonFilePropertySearch", required = false) String jsonFilePropertySearch, @RequestParam("draw") int draw,
            @RequestParam("start") int start, @RequestParam("length") int length) throws DataGridConnectionRefusedException {

        if (jsonFilePropertySearch != null) {
            currentPage = (int) (Math.floor(start / length) + 1);
            this.jsonFilePropertySearch = jsonFilePropertySearch;
        }

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonResponse = new HashMap<String, Object>();
        jsonResponse.put("draw", String.valueOf(draw));
        jsonResponse.put("recordsTotal", String.valueOf(0));
        jsonResponse.put("recordsFiltered", String.valueOf(0));
        jsonResponse.put("data", new ArrayList<String>());
        String jsonString = "";

        try {
            JsonNode jsonNode = mapper.readTree(this.jsonFilePropertySearch);

            currentFilePropertySearch = new ArrayList<DataGridFilePropertySearch>();

            JsonNode attributes = jsonNode.get("attribute");
            JsonNode operators = jsonNode.get("operator");
            JsonNode values = jsonNode.get("value");
            for (int i = 0; i < attributes.size(); i++) {
                DataGridFilePropertySearch ms = new DataGridFilePropertySearch(FilePropertyField.valueOf(attributes.get(i).textValue()),
                        DataGridSearchOperatorEnum.valueOf(operators.get(i).textValue()), values.get(i).textValue());
                currentFilePropertySearch.add(ms);
            }

            DataGridPageContext pageContext = new DataGridPageContext();

            List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = filePropertyService.findByFileProperties(
                    currentFilePropertySearch, pageContext, currentPage, length);

            jsonResponse.put("recordsTotal", String.valueOf(pageContext.getTotalNumberOfItems()));
            jsonResponse.put("recordsFiltered", String.valueOf(pageContext.getTotalNumberOfItems()));
            jsonResponse.put("data", dataGridCollectionAndDataObjects);

        }
        catch (DataGridConnectionRefusedException e) {
            throw e;
        }
        catch (Exception e) {
            logger.error("Could not search by metadata: ", e.getMessage());
        }

        try {
            jsonString = mapper.writeValueAsString(jsonResponse);
        }
        catch (JsonProcessingException e) {
            logger.error("Could not parse hashmap in file properties search to json: {}", e.getMessage());
        }

        return jsonString;
    }

    @RequestMapping(value = "/downloadCSVResults/")
    public void searchToCSVFile(HttpServletResponse response) throws DataGridConnectionRefusedException, IOException {

        ServletOutputStream outputStream = response.getOutputStream();
        String loggedUser = getLoggedDataGridUser().getUsername();
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String filename = String.format("search-result_%s_%s.csv", loggedUser, date);

        // Setting CSV Mime type
        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment;filename=" + filename);

        // Building search parameters lines
        outputStream.print("Search condition #;Condition\n");
        int i = 1;
        for (DataGridFilePropertySearch field : currentFilePropertySearch) {
            String condition = field.toString();
            outputStream.print(String.format("%d;%s\n", i, condition));
            i++;
        }
        outputStream.print("\n");
        outputStream.flush();

        // Executing query
        DataGridPageContext pageContext = new DataGridPageContext();
        List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = filePropertyService.findByFileProperties(currentFilePropertySearch,
                pageContext, 1, Integer.MAX_VALUE);

        // Printing number of results
        outputStream.print("Number of results\n");
        outputStream.print(String.format("%d\n", pageContext.getTotalNumberOfItems()));
        outputStream.print("\n");
        outputStream.flush();

        // Printing results
        outputStream.print("Filename;Path;Owner;Kind;Modified;Size;Matches\n");
        for (DataGridCollectionAndDataObject obj : dataGridCollectionAndDataObjects) {
            outputStream.print(obj.getName() + ";");
            outputStream.print(obj.getPath() + ";");
            outputStream.print(obj.getOwner() + ";");
            outputStream.print((obj.isCollection() ? "collection" : "data object") + ";");
            outputStream.print(obj.getModifiedAtFormattedForCSVReport() + ";");
            outputStream.print(String.valueOf(obj.getSize()) + ";");
            outputStream.print(String.valueOf(obj.getNumberOfMatches()));
            outputStream.print("\n");
            outputStream.flush();
        }
    }

    private DataGridUser getLoggedDataGridUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) auth.getPrincipal();

        return userService.findByUsernameAndAdditionalInfo(username, zoneName);
    }

}
