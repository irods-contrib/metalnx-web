package com.emc.metalnx.controller.api.model;

public class PublishActionData {
	private String endpointUrl = "";
	private String indexId = "";
	private String publishRequestData = "";

	public String getIndexId() {
		return indexId;
	}

	public void setIndexId(String indexId) {
		this.indexId = indexId;
	}

	public String getEndpointUrl() {
		return endpointUrl;
	}

	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}

	public String getPublishRequestData() {
		return publishRequestData;
	}

	public void setPublishRequestData(String publishRequestData) {
		this.publishRequestData = publishRequestData;
	}

	@Override
	public String toString() {
		return "PublishActionData [endpointUrl=" + endpointUrl + ", indexId=" + indexId + ", publishRequestData="
				+ publishRequestData + "]";
	}
}
