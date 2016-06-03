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
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridMetadata;
import com.emc.metalnx.core.domain.entity.DataGridMetadataSearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.MetadataService;
import com.emc.metalnx.services.interfaces.PermissionsService;
import com.emc.metalnx.services.interfaces.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@SessionAttributes({ "sourcePaths" })
@RequestMapping(value = "/metadata")
public class MetadataController {

    @Autowired
    MetadataService metadataService;

    @Autowired
    CollectionService collectionService;

    @Autowired
    UserService userService;

    @Autowired
    PermissionsService permissionsService;

    @Autowired
    LoggedUserUtils loggedUserUtils;

    @Autowired
    CollectionController collectionController;

    private String jsonMetadataSearch;

    // UI mode that will be shown when the rods user switches mode from admin to user and vice-versa
    public static final String UI_USER_MODE = "user";
    public static final String UI_ADMIN_MODE = "admin";

    // CSV constants
    private static final String METADATA_CSV_FILENAME_FORMAT = "metadata-search-result_%s_%s.csv";
    private static final String METADATA_CSV_DATE_FORMAT = "yyyyMMdd_HHmmss";
    private static final String METADATA_CSV_HEADER = "Filename;Path;Owner;Kind;Modified;Size;Matches\n";

    @Value("${irods.zoneName}")
    private String zoneName;

    // current page the user currently is (pagination)
    private int currPage = 1;

    // current metadata search
    List<DataGridMetadataSearch> currSearch;

    private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);

    @RequestMapping(value = "/")
    public String index(Model model, HttpServletRequest request,
            @RequestParam(value = "backFromCollections", required = false) boolean backFromCollections) throws DataGridConnectionRefusedException,
            DataGridException {

        DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
        String uiMode = (String) request.getSession().getAttribute("uiMode");
        if (uiMode == null || uiMode.isEmpty()) {
            if (loggedUser.isAdmin()) {
                uiMode = UI_ADMIN_MODE;
            }
            else {
                uiMode = UI_USER_MODE;
                model.addAttribute("homePath", collectionService.getHomeDirectyForCurrentUser());
                model.addAttribute("publicPath", collectionService.getHomeDirectyForPublic());
            }
        }

        if (backFromCollections) {
            model.addAttribute("jsonMetadataSearch", jsonMetadataSearch);
        }
        model.addAttribute("uiMode", uiMode);
        return "metadata/metadataDisplay";
    }

    @RequestMapping(value = "/search/", method = RequestMethod.POST)
    @ResponseBody
    public String searchByMetadata(@RequestParam(required = false) String jsonMetadataSearch, @RequestParam("draw") int draw,
            @RequestParam("start") int start, @RequestParam("length") int length) throws DataGridConnectionRefusedException {

        List<DataGridCollectionAndDataObject> dgCollDataObjs = new ArrayList<DataGridCollectionAndDataObject>();
        DataGridPageContext pageContext = new DataGridPageContext();

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> jsonResponse = new HashMap<String, Object>();
        jsonResponse.put("draw", String.valueOf(draw));
        jsonResponse.put("recordsTotal", String.valueOf(0));
        jsonResponse.put("recordsFiltered", String.valueOf(0));
        jsonResponse.put("data", new ArrayList<String>());
        String jsonString = "";

        try {
            if (jsonMetadataSearch != null) {
                currPage = (int) (Math.floor(start / length) + 1);
                this.jsonMetadataSearch = jsonMetadataSearch;
            }

            // Creating parser
            JsonNode jsonObject = mapper.readTree(this.jsonMetadataSearch);

            currSearch = new ArrayList<DataGridMetadataSearch>();

            JsonNode attributes = jsonObject.get("attribute");
            JsonNode operators = jsonObject.get("operator");
            JsonNode values = jsonObject.get("value");
            JsonNode units = jsonObject.get("unit");

            for (int i = 0; i < attributes.size(); i++) {
                String attr = attributes.get(i).textValue();
                String val = values.get(i).textValue();
                String unit = units.get(i).textValue();
                String opt = operators.get(i).textValue();
                DataGridSearchOperatorEnum op = DataGridSearchOperatorEnum.valueOf(opt);
                DataGridMetadataSearch ms = new DataGridMetadataSearch(attr, val, unit, op);
                currSearch.add(ms);
            }

            dgCollDataObjs = metadataService.findByMetadata(currSearch, pageContext, currPage, length);
            metadataService.populateVisibilityForCurrentUser(dgCollDataObjs);

            jsonResponse.put("recordsTotal", String.valueOf(pageContext.getTotalNumberOfItems()));
            jsonResponse.put("recordsFiltered", String.valueOf(pageContext.getTotalNumberOfItems()));
            jsonResponse.put("data", dgCollDataObjs);

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
            logger.error("Could not parse hashmap in metadata search to json: {}", e.getMessage());
        }

        return jsonString;
    }

    @RequestMapping(value = "/downloadCSVResults/")
    public void exportSearchResultsToCSVFile(HttpServletResponse response) throws DataGridConnectionRefusedException, IOException {

        String loggedUser = loggedUserUtils.getLoggedDataGridUser().getUsername();
        String date = new SimpleDateFormat(METADATA_CSV_DATE_FORMAT).format(new Date());

        String filename = String.format(METADATA_CSV_FILENAME_FORMAT, loggedUser, date);

        // Setting CSV Mime type
        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment;filename=" + filename);

        ServletOutputStream outputStream = response.getOutputStream();

        // Building search parameters lines
        outputStream.print("Search condition #;Condition\n");

        ListIterator<DataGridMetadataSearch> resultEnumeration = currSearch.listIterator();
        while (resultEnumeration.hasNext()) {
            String condition = resultEnumeration.next().toString();
            outputStream.print(String.format("%d;%s\n", resultEnumeration.nextIndex() + 1, condition));
        }
        outputStream.print("\n");
        outputStream.flush();

        // Executing query
        DataGridPageContext pageContext = new DataGridPageContext();
        List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = metadataService.findByMetadata(currSearch, pageContext, 1,
                Integer.MAX_VALUE);

        // Printing number of results
        outputStream.print("Number of results\n");
        outputStream.print(String.format("%d\n", pageContext.getTotalNumberOfItems()));
        outputStream.print("\n");

        // Printing results
        outputStream.print(METADATA_CSV_HEADER);
        for (DataGridCollectionAndDataObject obj : dataGridCollectionAndDataObjects) {

            String kind = obj.isCollection() ? "collection" : "data object";

            StringBuilder row = new StringBuilder();
            row.append(obj.getName() + ";");
            row.append(obj.getPath() + ";");
            row.append(obj.getOwner() + ";");
            row.append(kind + ";");
            row.append(obj.getModifiedAtFormattedForCSVReport() + ";");
            row.append(String.valueOf(obj.getSize()) + ";");
            row.append(String.valueOf(obj.getNumberOfMatches()));
            row.append("\n");
            outputStream.print(row.toString());
            outputStream.flush();
        }
    }

    @RequestMapping(value = "/getMetadata/")
    public String getMetadata(Model model, @RequestParam("path") String path) throws DataGridConnectionRefusedException {

        List<DataGridMetadata> metadataList = metadataService.findMetadataValuesByPath(path);
        DataGridCollectionAndDataObject dgColObj;
        try {
            dgColObj = collectionService.findByName(path);
            permissionsService.resolveMostPermissiveAccessForUser(dgColObj, loggedUserUtils.getLoggedDataGridUser());
        }
        catch (DataGridException e) {
            logger.error("Could not retrieve collection/dataobject from path: {}", path);
            throw new DataGridConnectionRefusedException();
        }

        model.addAttribute("permissionOnCurrentPath", collectionService.getPermissionsForPath(path));
        model.addAttribute("dataGridMetadataList", metadataList);
        model.addAttribute("currentPath", path);
        model.addAttribute("collectionAndDataObject", dgColObj);
        return "metadata/metadataTable";
    }

    @RequestMapping(value = "/addMetadata/")
    public String setMetadata(Model model, @RequestParam("path") String path, @RequestParam("attribute") String attribute,
            @RequestParam("value") String value, @RequestParam("unit") String unit) throws DataGridConnectionRefusedException {

        if (metadataService.addMetadataToPath(path, attribute, value, unit)) {
            model.addAttribute("addMetadataReturn", "success");
        }
        else {
            model.addAttribute("addMetadataReturn", "failure");
        }
        return getMetadata(model, path);
    }

    @RequestMapping(value = "/modMetadata/")
    public String modMetadata(Model model, @RequestParam("path") String path, @RequestParam("oldAttribute") String oldAttribute,
            @RequestParam("oldValue") String oldValue, @RequestParam("oldUnit") String oldUnit, @RequestParam("newAttribute") String newAttribute,
            @RequestParam("newValue") String newValue, @RequestParam("newUnit") String newUnit) throws DataGridConnectionRefusedException {

        if (metadataService.modMetadataFromPath(path, oldAttribute, oldValue, oldUnit, newAttribute, newValue, newUnit)) {
            model.addAttribute("modMetadataReturn", "success");
        }
        else {
            model.addAttribute("modMetadataReturn", "failure");
        }
        return getMetadata(model, path);
    }

    @RequestMapping(value = "/delMetadataListPrototype")
    public String delMetadataListPrototype(HttpServletRequest request, Model model) throws DataGridConnectionRefusedException {

        String path = request.getParameter("path");
        Integer length = Integer.valueOf(request.getParameter("length"));
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();

        for (int i = 0; i < length; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("attribute", request.getParameter("params[" + i + "][attribute]"));
            map.put("value", request.getParameter("params[" + i + "][value]"));
            map.put("unit", request.getParameter("params[" + i + "][unit]"));
            list.add(map);
        }

        for (Map<String, String> map : list) {
            if (!metadataService.delMetadataFromPath(path, map.get("attribute"), map.get("value"), map.get("unit"))) {
                model.addAttribute("delMetadataReturn", "failure");
            }
        }
        return getMetadata(model, path);
    }

    @RequestMapping(value = "/exportToCSV")
    public void exportToCSV(HttpServletResponse response) throws IOException, DataGridConnectionRefusedException {
        String filePath = collectionController.getCurrentPath();

        List<DataGridMetadata> metadataList = metadataService.findMetadataValuesByPath(filePath);

        setReponseHeaderForCSVExport(response);
        ServletOutputStream outputStream = response.getOutputStream();

        outputStream.print("File path\n");
        outputStream.print(String.format("%s\n\n", filePath));

        outputStream.print("Attribute Name,Value,Unit\n");
        for (DataGridMetadata m : metadataList) {
            outputStream.print(String.format("%s,%s,%s\n", m.getAttribute(), m.getValue(), m.getUnit()));
        }
        outputStream.flush();
    }

    /*
     * *************************************************************************
     * ***************************** PRIVATE METHODS ***************************
     * *************************************************************************
     */

    private void setReponseHeaderForCSVExport(HttpServletResponse response) {
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String filename = String.format("metadata_%s.csv", date);

        // Setting CSV Mime type
        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment;filename=" + filename);
    }
}
