/**
 * 
 */
package com.emc.metalnx.controller.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.irods.metalnx.pluggablesearch.PluggableSearchWrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Backend API support for search operations
 * 
 * @author Mike Conway - NIEHS
 *
 */

@Controller
//@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/api/search")
public class SearchApiController {

	public static final Logger log = LoggerFactory.getLogger(SearchApiController.class);

	@Autowired
	PluggableSearchWrapperService pluggableSearchWrapperService;

	@RequestMapping(value = "indexes")
	@ResponseBody
	public String getPaginatedJSONObjs(final HttpServletRequest request) throws DataGridException {

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
			dataGridCollectionAndDataObjects = cs.getSubCollectionsAndDataObjectsUnderPath(path); // TODO: temporary add
			// paging service

			logger.debug("dataGridCollectionAndDataObjects:{}", dataGridCollectionAndDataObjects);
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
		} catch (DataGridConnectionRefusedException e) {
			logger.error("connection refused", e);
			throw e;
		} catch (Exception e) {
			logger.error("Could not get collections/data objs under path {}: {}", currentPath, e.getMessage());
			throw new DataGridException("exception getting paginated objects", e);
		}

		try {
			jsonString = mapper.writeValueAsString(jsonResponse);
		} catch (JsonProcessingException e) {
			logger.error("Could not parse hashmap in collections to json: {}", e.getMessage());
			throw new DataGridException("exception in json parsing", e);
		}

		return jsonString;
	}

}
