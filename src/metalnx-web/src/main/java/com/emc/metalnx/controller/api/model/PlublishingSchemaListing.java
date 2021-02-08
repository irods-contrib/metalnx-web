package com.emc.metalnx.controller.api.model;

import java.util.ArrayList;
import java.util.List;

public class PlublishingSchemaListing {
	private List<PlublishingSchemaEntry> publishingSchemaEntry = new ArrayList<>();
	
	public List<PlublishingSchemaEntry> getPublishingSchemaEntry() {
		return publishingSchemaEntry;
	}
	
	public void setPublishingSchemaEntry(List<PlublishingSchemaEntry> publishingSchemaEntry) {
		this.publishingSchemaEntry = publishingSchemaEntry;
	}
}
