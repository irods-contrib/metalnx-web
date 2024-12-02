/* Copyright (c) 2018-2021, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.JargonQueryException;
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

import com.emc.metalnx.controller.utils.GenQuerySearchUtil;
import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FilePropertyService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping(value = "/fileproperty")
public class FilePropertiesController {

    @Autowired
    IRODSServices irodsServices;

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

    // ui mode that will be shown when the rods user switches mode from admin to
    // user and vice-versa
    public static final String UI_USER_MODE = "user";
    public static final String UI_ADMIN_MODE = "admin";

    // current search criteria
    private GenQuerySearchUtil.SearchInput currentSearchInput;

    private String jsonFilePropertySearch;

    private static final Logger logger = LoggerFactory.getLogger(FilePropertiesController.class);

    @RequestMapping(value = "/")
    public String index(final Model model,
                        final HttpServletRequest request,
                        @RequestParam(value = "backFromCollections", required = false) final boolean backFromCollections)
    {
        DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
        String uiMode = (String) request.getSession().getAttribute("uiMode");
        if (uiMode == null || uiMode.isEmpty()) {
            if (loggedUser.isAdmin()) {
                uiMode = UI_ADMIN_MODE;
            } else {
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
    public String search(@RequestParam(value = "jsonFilePropertySearch", required = false) final String jsonFilePropertySearch,
                         @RequestParam("draw") final int draw,
                         @RequestParam("start") final int start,
                         @RequestParam("length") final int length)
        throws DataGridConnectionRefusedException, JargonException
    {
        if (jsonFilePropertySearch != null) {
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
            JsonNode attributes = jsonNode.get("attribute");
            JsonNode operators = jsonNode.get("operator");
            JsonNode values = jsonNode.get("value");

            GenQuerySearchUtil.SearchInput searchInput = new GenQuerySearchUtil.SearchInput();

            searchInput.account = irodsServices.getCollectionAO().getIRODSAccount();
            searchInput.offset = start;
            searchInput.count = length;
            searchInput.attributes = attributes;
            searchInput.operators = operators;
            searchInput.values = values;

            currentSearchInput = new GenQuerySearchUtil.SearchInput(searchInput);

            GenQuerySearchUtil.SearchOutput searchOutput = GenQuerySearchUtil.search(searchInput);

            jsonResponse.put("recordsTotal", String.valueOf(searchOutput.matches));
            jsonResponse.put("recordsFiltered", String.valueOf(searchOutput.matches));
            jsonResponse.put("data", searchOutput.objects);
        }
        catch (DataGridConnectionRefusedException e) {
            logger.error("data grid error in search", e);
            throw e;
        }
        catch (JargonException e) {
            logger.error("Could not search by metadata: ", e);
            throw e;
        }
        catch (JsonProcessingException e) {
            logger.error("Could not search by metadata: ", e);
            throw new JargonException(e);
        }
        catch (GenQueryBuilderException e)
        {
            logger.error("Could not search by metadata: ", e);
            throw new JargonException(e);
        }
        catch (JargonQueryException e)
        {
            logger.error("Could not search by metadata: ", e);
            throw new JargonException(e);
        }
        catch (ParseException e)
        {
            logger.error("Could not search by metadata: ", e);
            throw new JargonException(e);
        }

        try {
            jsonString = mapper.writeValueAsString(jsonResponse);
        }
        catch (JsonProcessingException e) {
            logger.error("Could not parse hashmap in file properties search to json: {}", e.getMessage());
            throw new JargonException(e);
        }

        return jsonString;
    }

    @RequestMapping(value = "/downloadCSVResults/")
    public void searchToCSVFile(final HttpServletResponse response)
        throws Exception
    {
        ServletOutputStream outputStream = response.getOutputStream();
        String loggedUser = getLoggedDataGridUser().getUsername();
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filename = String.format("search-result_%s_%s.csv", loggedUser, date);

        // Setting CSV Mime type
        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment;filename=" + filename);

        // Building search parameters lines
        outputStream.print("Search condition #;Condition\n");
        for (int i = 0; i < currentSearchInput.attributes.size(); ++i) {
            String a = currentSearchInput.attributes.get(i).textValue();
            String o = currentSearchInput.operators.get(i).textValue();
            String v = currentSearchInput.values.get(i).textValue();
            outputStream.print(String.format("%d;attribute=%s, operator=%s, value=%s\n", i, a, o, v));
        }
        outputStream.print("\n");
        outputStream.flush();

        // Execute query
        GenQuerySearchUtil.SearchInput searchInput = new GenQuerySearchUtil.SearchInput(currentSearchInput);
        searchInput.offset = 0;
        searchInput.count = 2048;

        try {
            GenQuerySearchUtil.SearchOutput searchOutput;
            boolean printNumberOfResults = true;

            do {
                searchOutput = GenQuerySearchUtil.search(searchInput);

                // Print number of results
                if (printNumberOfResults) {
                    printNumberOfResults = false;
                    outputStream.print("Number of Results\n");
                    outputStream.print(String.format("%d\n", searchOutput.matches));
                    outputStream.print("\n");
                    outputStream.print("Filename;Path;Owner;Kind;Modified;Size\n");
                }

                // Print results
                for (DataGridCollectionAndDataObject obj : searchOutput.objects) {
                    outputStream.print(obj.getName());
                    outputStream.print(";");

                    outputStream.print(obj.getPath());
                    outputStream.print(";");

                    outputStream.print(obj.getOwner());
                    outputStream.print(";");

                    outputStream.print(obj.isCollection() ? "Collection" : "Data Object");
                    outputStream.print(";");

                    outputStream.print(obj.getModifiedAtFormattedForCSVReport());
                    outputStream.print(";");

                    outputStream.print(obj.getSize());
                    outputStream.print("\n");
                }

                outputStream.flush();

                searchInput.offset += searchInput.count;
            }
            while (!searchOutput.objects.isEmpty());
        }
        catch (GenQueryBuilderException | JargonException | JargonQueryException | ParseException e) {
            logger.error("CSV export failed.", e);
            throw e;
        }
    }

    private DataGridUser getLoggedDataGridUser()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) auth.getPrincipal();
        return userService.findByUsernameAndZone(username, zoneName);
    }

}
