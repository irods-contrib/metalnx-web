/**
 * 
 */
package com.emc.metalnx.controller.api.model;

/**
 * @author Mike Conway - NIEHS
 *
 */
public class TextSearchFormData {

	private String endpointUrl = "";
	private String searchQuery = "";
	private String indexId = "";
	private int length = 0;
	private int offset = 0;

	public String getEndpointUrl() {
		return endpointUrl;
	}

	public void setEndpointUrl(String endpointUrl) {
		this.endpointUrl = endpointUrl;
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public String getIndexId() {
		return indexId;
	}

	public void setIndexId(String indexId) {
		this.indexId = indexId;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TextSearchFormData [endpointUrl=").append(endpointUrl).append(", searchQuery=")
				.append(searchQuery).append(", indexId=").append(indexId).append(", length=").append(length)
				.append(", offset=").append(offset).append("]");
		return builder.toString();
	}

}
