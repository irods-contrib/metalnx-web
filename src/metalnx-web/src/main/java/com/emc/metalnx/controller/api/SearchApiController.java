/**
 * 
 */
package com.emc.metalnx.controller.api;

import javax.servlet.http.HttpServletRequest;

import org.irods.jargon.extensions.searchplugin.SearchIndexInventory;
import org.irods.jargon.extensions.searchplugin.SearchIndexInventoryEntry;
import org.irods.jargon.extensions.searchplugin.model.IndexSchemaDescription;
import org.irods.metalnx.pluggablesearch.PluggableSearchWrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.emc.metalnx.controller.api.model.SearchSchemaEntry;
import com.emc.metalnx.controller.api.model.SearchSchemaListing;
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
	private PluggableSearchWrapperService pluggableSearchWrapperService;

	private ObjectMapper mapper = new ObjectMapper();

	/**
	 * Return an inventory of the available indexes
	 * 
	 * @param request {@link HttpServletRequest}
	 * @return {@code String} with json
	 * @throws DataGridException {@link DataGridException}
	 */
	@RequestMapping(value = "indexes")
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

	@RequestMapping(value = "textSearch")
	@ResponseBody
	public String textSearch(@RequestParam("endpointUrl") final String endpointUrl,
			@RequestParam("searchQuery") final String searchQuery, @RequestParam("indexName") final String indexName)
			throws DataGridException {
		log.info("testSearch()");

		if (endpointUrl == null || endpointUrl.isEmpty()) {
			throw new IllegalArgumentException("null endpointUrl");
		}

		if (searchQuery == null || searchQuery.isEmpty()) {
			throw new IllegalArgumentException("null searchQuery");
		}

		if (indexName == null || indexName.isEmpty()) {
			throw new IllegalArgumentException("null indexName");
		}

		log.info("endpointUrl:{}", endpointUrl);
		log.info("searchQuery:{}", searchQuery);
		log.info("indexName:{}", indexName);

		/*
		 * Look for the indepoint and index, big trouble if I don't find it
		 */

		SearchIndexInventory searchIndexInventory = pluggableSearchWrapperService.getSearchIndexInventory();
		SearchIndexInventoryEntry indexInventoryEntry = searchIndexInventory.getIndexInventoryEntries()
				.get(endpointUrl);

		if (indexInventoryEntry == null) {
			log.error("cannot find inventory entry for search endpoint:{}", endpointUrl);
			throw new DataGridException("Search endpoint unavailable");
		}

		boolean foundit = false;

		for (IndexSchemaDescription indexSchemaDescription : indexInventoryEntry.getIndexInformation().getIndexes()) {
			if (indexSchemaDescription.getId().equals(indexName)) {
				foundit = true;
				log.info("found search schema and endpoint for the query:{}", indexSchemaDescription);

			}
		}

		return null;

	}

	public PluggableSearchWrapperService getPluggableSearchWrapperService() {
		return pluggableSearchWrapperService;
	}

	public void setPluggableSearchWrapperService(PluggableSearchWrapperService pluggableSearchWrapperService) {
		this.pluggableSearchWrapperService = pluggableSearchWrapperService;
	}

}
