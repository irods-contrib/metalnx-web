package com.emc.metalnx.services.irods.template;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data transfer object suitable for JSON import/export of metadata templates.
 * Consider this a provisional class as a regualar scheme for metadata template
 * handling is being devised.
 * 
 * @author conwaymc
 *
 */
public class SerializedMetadataTemplate {

	@JsonProperty("id")
	private String identifier = "";
	@JsonProperty("template-name")
	private String templateName = "";
	@JsonProperty("template-owner")
	private String templateOwner = "";
	@JsonProperty("description")
	private String description = "";
	@JsonProperty("access-type")
	private String accessType = "";
	@JsonProperty("version")
	private Integer version = 0;
	@JsonProperty("create-date")
	private String iso8601CreateDate = "";
	@JsonProperty("modify-date")
	private String iso8601ModifyDate = "";
	@JsonProperty("template-fields")

	private List<SerializedTemplateField> templateFields = new ArrayList<>();

	public SerializedMetadataTemplate() {
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}

	public String getTemplateOwner() {
		return templateOwner;
	}

	public void setTemplateOwner(String templateOwner) {
		this.templateOwner = templateOwner;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAccessType() {
		return accessType;
	}

	public void setAccessType(String accessType) {
		this.accessType = accessType;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public String getIso8601CreateDate() {
		return iso8601CreateDate;
	}

	public void setIso8601CreateDate(String iso8601CreateDate) {
		this.iso8601CreateDate = iso8601CreateDate;
	}

	public String getIso8601ModifyDate() {
		return iso8601ModifyDate;
	}

	public void setIso8601ModifyDate(String iso8601ModifyDate) {
		this.iso8601ModifyDate = iso8601ModifyDate;
	}

	public List<SerializedTemplateField> getTemplateFields() {
		return templateFields;
	}

	public void setTemplateFields(List<SerializedTemplateField> templateFields) {
		this.templateFields = templateFields;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("SerializedMetadataTempate [");
		if (identifier != null) {
			builder.append("identifier=").append(identifier).append(", ");
		}
		if (templateName != null) {
			builder.append("templateName=").append(templateName).append(", ");
		}
		if (templateOwner != null) {
			builder.append("templateOwner=").append(templateOwner).append(", ");
		}
		if (description != null) {
			builder.append("description=").append(description).append(", ");
		}
		if (accessType != null) {
			builder.append("accessType=").append(accessType).append(", ");
		}
		if (version != null) {
			builder.append("version=").append(version).append(", ");
		}
		if (iso8601CreateDate != null) {
			builder.append("iso8601CreateDate=").append(iso8601CreateDate).append(", ");
		}
		if (iso8601ModifyDate != null) {
			builder.append("iso8601ModifyDate=").append(iso8601ModifyDate).append(", ");
		}
		if (templateFields != null) {
			builder.append("templateFields=")
					.append(templateFields.subList(0, Math.min(templateFields.size(), maxLen)));
		}
		builder.append("]");
		return builder.toString();
	}

}
