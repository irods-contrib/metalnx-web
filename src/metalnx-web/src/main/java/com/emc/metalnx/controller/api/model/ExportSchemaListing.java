package com.emc.metalnx.controller.api.model;

import java.util.ArrayList;
import java.util.List;

public class ExportSchemaListing {
	private List<ExportSchemaEntry> exportSchemaEntry = new ArrayList<>();
	
	public List<ExportSchemaEntry> getExportSchemaEntry() {
		return exportSchemaEntry;
	}
	
	public void setExportSchemaEntry(List<ExportSchemaEntry> exportSchemaEntry) {
		this.exportSchemaEntry = exportSchemaEntry;
	}
}
