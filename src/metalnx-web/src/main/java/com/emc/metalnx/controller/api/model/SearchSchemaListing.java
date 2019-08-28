/**
 * 
 */
package com.emc.metalnx.controller.api.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Conway - NIEHS
 *
 */
public class SearchSchemaListing {
	private List<SearchSchemaEntry> searchSchemaEntry = new ArrayList<>();

	public List<SearchSchemaEntry> getSearchSchemaEntry() {
		return searchSchemaEntry;
	}

	public void setSearchSchemaEntry(List<SearchSchemaEntry> searchSchemaEntry) {
		this.searchSchemaEntry = searchSchemaEntry;
	}

}
