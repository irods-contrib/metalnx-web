/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */
package com.emc.metalnx.core.domain.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.emc.metalnx.core.domain.exceptions.DataGridTooLongTemplateNameException;

@Entity
@Audited
@Table(name = "templates")
public class DataGridTemplate implements Serializable, Comparable<DataGridTemplate> {

    private static final long serialVersionUID = 1L;

    @Id
    @NotAudited
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_id", unique = true, nullable = false)
    private Long id;

    @Column(name = "template_name", unique = true, nullable = false, length = 100)
    private String templateName;

    @Column(name = "owner", nullable = false, length = 100)
    private String owner;

    @Column(name = "description", nullable = false, length = 512)
    private String description;

    @Column(name = "usage_info", length = 100)
    private String usageInformation;

    @Column(name = "access_type", length = 32)
    private String accessType;

    @Column(name = "version")
    private Integer version;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_ts", nullable = false, length = 60, updatable = false)
    private Date createTs;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "modify_ts", nullable = false, length = 60)
    private Date modifyTs;

    @OneToMany(mappedBy = "template", fetch = FetchType.EAGER)
    private Set<DataGridTemplateField> fields;

    private static final int TEMPLATE_NAME_MAX_LENGTH = 100;
    private static final int TEMPLATE_DESC_MAX_LENGTH = 100;

    private boolean isModified = false;

    public DataGridTemplate() {
        // empty constructor
    }

    public DataGridTemplate(String templateName) throws DataGridTooLongTemplateNameException {
        if (templateName.length() > TEMPLATE_NAME_MAX_LENGTH) {
            throw new DataGridTooLongTemplateNameException("Template name exceeded " + TEMPLATE_NAME_MAX_LENGTH + " characters.");
        }

        this.templateName = templateName;
    }

    /**
     * @return the owner
     */
    public String getOwner() {
        return owner;
    }

    /**
     * @param owner
     *            the owner to set
     */
    public void setOwner(String owner) {
        this.owner = owner;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     * @throws DataGridTooLongTemplateNameException
     */
    public void setDescription(String description) throws DataGridTooLongTemplateNameException {
        if (description.length() > TEMPLATE_NAME_MAX_LENGTH) {
            throw new DataGridTooLongTemplateNameException("Template description exceeded " + TEMPLATE_DESC_MAX_LENGTH + " characters.");
        }
        this.description = description;
    }

    /**
     * @return the createTs
     */
    public Date getCreateTs() {
        return createTs;
    }

    /**
     * @param createTs
     *            the createTs to set
     */
    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    /**
     * @return the modifyTs
     */
    public Date getModifyTs() {
        return modifyTs;
    }

    /**
     * @param modifyTs
     *            the modifyTs to set
     */
    public void setModifyTs(Date modifyTs) {
        this.modifyTs = modifyTs;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the templateName
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * @param the
     *            templateName to set
     * @throws DataGridTooLongTemplateNameException
     */
    public void setTemplateName(String templateName) throws DataGridTooLongTemplateNameException {
        if (templateName.length() > TEMPLATE_NAME_MAX_LENGTH) {
            throw new DataGridTooLongTemplateNameException("Template name exceeded " + TEMPLATE_NAME_MAX_LENGTH + " characters.");
        }

        this.templateName = templateName;
    }

    /**
     * @return the usageInformation
     */
    public String getUsageInformation() {
        return usageInformation;
    }

    /**
     * @param usageInformation
     *            the usageInformation to set
     */
    public void setUsageInformation(String usageInformation) {
        this.usageInformation = usageInformation;
    }

    /**
     * @return the accessType
     */
    public String getAccessType() {
        return accessType;
    }

    /**
     * @param accessType
     *            the accessType to set
     */
    public void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    /**
     * @return the fields
     */
    public Set<DataGridTemplateField> getFields() {
        return fields;
    }

    /**
     * @param fields
     *            the fields to set
     */
    public void setFields(Set<DataGridTemplateField> fields) {
        this.fields = fields;
    }

    /**
     * @return the version
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @param version
     *            the version to set
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * @return the isModified
     */
    public boolean isModified() {
        return isModified;
    }

    /**
     * @param isModified
     *            the isModified to set
     */
    public void setModified(boolean isModified) {
        this.isModified = isModified;
    }

    @Override
    public int compareTo(DataGridTemplate dgt) {
        return templateName.compareTo(dgt.getTemplateName());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Template: ");
        sb.append(templateName);
        sb.append("\nDescription: ");
        sb.append(description);
        sb.append("\nOwner: ");
        sb.append(owner);
        sb.append("\nAccess Type: ");
        sb.append(accessType);
        sb.append("\nCreated: ");
        sb.append(createTs);
        sb.append("\nModified: ");
        sb.append(modifyTs);
        sb.append("\n");
        return sb.toString();
    }

}
