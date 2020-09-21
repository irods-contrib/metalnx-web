package com.emc.metalnx.controller.api.model;

public class PlublishingSchemaEntry {
	
	private String endpointUrl = "";
	private String schemaId = "";
	private String schemaName = "";
	private String schemaDescription = "";
	private String responseType = "";
	
	public String getEndpointUrl() {
		return endpointUrl;
	}
	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}
	public String getSchemaId() {
		return schemaId;
	}
	public void setSchemaId(String schemaId) {
		this.schemaId = schemaId;
	}
	public String getSchemaName() {
		return schemaName;
	}
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}
	public String getSchemaDescription() {
		return schemaDescription;
	}
	public void setSchemaDescription(String schemaDescription) {
		this.schemaDescription = schemaDescription;
	}
	public String getResponseType() {
		return responseType;
	}
	public void setResponseType(String responseType) {
		this.responseType = responseType;
	}
}
