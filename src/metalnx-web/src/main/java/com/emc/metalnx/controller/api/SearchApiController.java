/**
 * 
 */
package com.emc.metalnx.controller.api;

import javax.servlet.http.HttpServletRequest;

import org.irods.jargon.extensions.searchplugin.SearchIndexInventory;
import org.irods.jargon.extensions.searchplugin.SearchIndexInventoryEntry;
import org.irods.jargon.extensions.searchplugin.model.IndexSchemaDescription;
import org.irods.jargon.extensions.searchplugin.model.SearchAttributes;
import org.irods.metalnx.pluggablesearch.PluggableSearchWrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.emc.metalnx.controller.api.model.SearchSchemaEntry;
import com.emc.metalnx.controller.api.model.SearchSchemaListing;
import com.emc.metalnx.controller.api.model.TextSearchFormData;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
	private PluggableSearchWrapperService pluggableSearchWrapperService;

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	IRODSServices irodsServices;

	/**
	 * Return an inventory of the available indexes
	 * 
	 * @param request {@link HttpServletRequest}
	 * @return {@code String} with json
	 * @throws DataGridException {@link DataGridException}
	 */
	@RequestMapping(value = "/indexes")
	@ResponseBody
	public String retrieveIndexes(final HttpServletRequest request) throws DataGridException {

		log.info("retrieveIndexes()");

		SearchIndexInventory searchIndexInventory = pluggableSearchWrapperService.getSearchIndexInventory();

		SearchSchemaListing searchSchema = new SearchSchemaListing();
		for (String key : searchIndexInventory.getIndexInventoryEntries().keySet()) {
			SearchIndexInventoryEntry indexInventoryEntry = searchIndexInventory.getIndexInventoryEntries().get(key);
			for (IndexSchemaDescription descr : indexInventoryEntry.getIndexInformation().getIndexes()) {
				SearchSchemaEntry searchSchemaEntry = new SearchSchemaEntry();
				searchSchemaEntry.setEndpointUrl(indexInventoryEntry.getEndpointUrl());
				searchSchemaEntry.setSchemaDescription(descr.getInfo());
				searchSchemaEntry.setSchemaId(descr.getId());
				searchSchemaEntry.setSchemaName(descr.getName());
				searchSchema.getSearchSchemaEntry().add(searchSchemaEntry);
			}

		}

		String jsonString;

		try {
			jsonString = mapper.writeValueAsString(searchSchema);
			log.debug("jsonString:{}", jsonString);
		} catch (JsonProcessingException e) {
			log.error("Could not parse index inventory: {}", e.getMessage());
			throw new DataGridException("exception in json parsing", e);
		}

		return jsonString;
	}

	@RequestMapping(value = "/attributes")
	@ResponseBody
	public String retrieveIndexeAttributes(final HttpServletRequest request,
			@RequestParam("index_name") final String indexName, @RequestParam("endpointUrl") final String endpointUrl)
			throws DataGridException {
		log.info("retrieveIndexeAttributes()");
		String jsonString;
		if (indexName == null || endpointUrl == null) {
			throw new IllegalArgumentException("null indexName or endpointUrl");
		}

		log.info("indexName: {}", indexName);

		SearchAttributes searchAttributes = new SearchAttributes();
		searchAttributes = pluggableSearchWrapperService.listAttributes(endpointUrl, indexName);

		try {
			jsonString = mapper.writeValueAsString(searchAttributes);
			log.debug("jsonString:{}", jsonString);
		} catch (JsonProcessingException e) {
			log.error("Could not parse index inventory: {}", e.getMessage());
			throw new DataGridException("exception in json parsing", e);
		}
		return jsonString;
	}

	private void populateMetalnxRelativeUrls(JsonNode rootNode) {
		log.info("populateMetalnxRelativeUrls()");
		for (JsonNode searchResultsNode : rootNode.get("search_result")) {
			for (JsonNode property : searchResultsNode.get("properties").get("propertySet")) {
				if ("absolutePath".equals(property.get("name").asText())) {
					((ObjectNode) searchResultsNode).put("metalnx_relative_url",
							"/metalnx/collections?path=" + property.get("value").asText());
					break;
				}
			}
		}
	}

	@RequestMapping(value = "/textSearch", method = RequestMethod.POST)
	@ResponseBody
	public String textSearch(@RequestBody TextSearchFormData textSearchFormData) throws DataGridException {

		log.info("testSearch()");

		if (textSearchFormData == null) {
			throw new IllegalArgumentException("null textSearchFormData");
		}

		log.info("textSearchFormData:{}", textSearchFormData);

		/*
		 * Look for the endpoint and index, big trouble if I don't find it
		 */

		SearchIndexInventory searchIndexInventory = pluggableSearchWrapperService.getSearchIndexInventory();
		SearchIndexInventoryEntry indexInventoryEntry = searchIndexInventory.getIndexInventoryEntries()
				.get(textSearchFormData.getEndpointUrl());

		if (indexInventoryEntry == null) {
			log.error("cannot find inventory entry for search endpoint:{}", textSearchFormData.getEndpointUrl());
			throw new DataGridException("Search endpoint unavailable");
		}

		boolean foundit = false;

		for (IndexSchemaDescription indexSchemaDescription : indexInventoryEntry.getIndexInformation().getIndexes()) {
			if (indexSchemaDescription.getId().equals(textSearchFormData.getIndexId())) {
				foundit = true;
				log.info("found search schema and endpoint for the query:{}", indexSchemaDescription);
				break;
			}
		}

		// see if I found the schema, if I did, do the query

		if (!foundit) {
			log.error("did not find search index:{}", textSearchFormData.getIndexId());
			throw new DataGridException("unable to search, cannot find index id");
		}

		log.info("carry out search");

		String searchServiceResult = this.pluggableSearchWrapperService.simpleTextSearch(
				textSearchFormData.getEndpointUrl(), textSearchFormData.getIndexId(),
				textSearchFormData.getSearchQuery(), textSearchFormData.getOffset(), textSearchFormData.getLength(),
				irodsServices.getCurrentUser());

		log.info("search was completed");

		try {
			log.info("get JsonNode");
			JsonNode metalnxJSON = mapper.readTree(searchServiceResult);
			populateMetalnxRelativeUrls(metalnxJSON);
			log.info("relative urls populated");
			searchServiceResult = mapper.writeValueAsString(metalnxJSON);

		} catch (JsonProcessingException e) {
			log.error("Search result JSON did not parse correctly - {}", e.getMessage());
			throw new DataGridException("Search result JSON did not parse correctly");
		}

		log.info("searchServiceResult:{}", searchServiceResult);
		return searchServiceResult;

	}

	public PluggableSearchWrapperService getPluggableSearchWrapperService() {
		return pluggableSearchWrapperService;
	}

	public void setPluggableSearchWrapperService(PluggableSearchWrapperService pluggableSearchWrapperService) {
		this.pluggableSearchWrapperService = pluggableSearchWrapperService;
	}

	public IRODSServices getIrodsServices() {
		return irodsServices;
	}

	public void setIrodsServices(IRODSServices irodsServices) {
		this.irodsServices = irodsServices;
	}

}
