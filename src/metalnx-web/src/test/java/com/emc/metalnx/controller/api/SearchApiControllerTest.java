package com.emc.metalnx.controller.api;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.irods.jargon.extensions.searchplugin.SearchIndexInventory;
import org.irods.jargon.extensions.searchplugin.SearchIndexInventoryEntry;
import org.irods.jargon.extensions.searchplugin.model.IndexSchemaDescription;
import org.irods.jargon.extensions.searchplugin.model.Indexes;
import org.irods.metalnx.pluggablesearch.PluggableSearchWrapperService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SearchApiControllerTest {

	@Test
	public void testRetrieveIndexes() throws Exception {
		SearchApiController searchApiController = new SearchApiController();
		PluggableSearchWrapperService searchWrapper = Mockito.mock(PluggableSearchWrapperService.class);

		Indexes indexes = new Indexes();
		indexes.setId("xxx");
		indexes.setInfo("index here");
		indexes.setMaintainer("bob");
		indexes.setName("myendpoint");
		indexes.setIndexes(new ArrayList<IndexSchemaDescription>());
		IndexSchemaDescription schema = new IndexSchemaDescription();
		schema.setId("id");
		schema.setInfo("info");
		schema.setName("name");
		schema.setVersion("1.0");
		indexes.getIndexes().add(schema);

		SearchIndexInventoryEntry searchIndexInventoryEntry = new SearchIndexInventoryEntry("endpoint", 5000, indexes);
		SearchIndexInventory searchIndexInventory = new SearchIndexInventory();
		searchIndexInventory.getIndexInventoryEntries().put("endpoint", searchIndexInventoryEntry);
		Mockito.when(searchWrapper.getSearchIndexInventory()).thenReturn(searchIndexInventory);

		searchApiController.setPluggableSearchWrapperService(searchWrapper);
		String actual = searchApiController.retrieveIndexes(Mockito.mock(HttpServletRequest.class));
		Assert.assertNotNull("null result", actual);

	}

}
