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
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.emc.metalnx.core.domain.utils.DataGridCoreUtils;

@Entity
@Audited
@Table(name = "group_bookmarks")
public class DataGridGroupBookmark implements Serializable, Comparable<DataGridGroupBookmark> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotAudited
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "group_id", nullable = false, updatable = true)
    private DataGridGroup group;

    @Column(name = "path", nullable = false, length = 512)
    private String path;

    @Column(name = "is_notified", nullable = true)
    private Boolean isNotified;

    @Column(name = "is_collection", nullable = true)
    private Boolean isCollection;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, length = 60, updatable = false)
    private Date createTs;

    private static final long serialVersionUID = -229875209906357557L;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the group
     */
    public DataGridGroup getGroup() {
        return group;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the isNotified
     */
    public Boolean getIsNotified() {
        return isNotified;
    }

    /**
     * @return the createTs
     */
    public Date getCreateTs() {
        return createTs;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @param group
     *            the group to set
     */
    public void setGroup(DataGridGroup group) {
        this.group = group;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @param isNotified
     *            the isNotified to set
     */
    public void setIsNotified(Boolean isNotified) {
        this.isNotified = isNotified;
    }

    /**
     * @return the isCollection
     */
    public Boolean getIsCollection() {
        return isCollection;
    }

    /**
     * @param isCollection
     *            the isCollection to set
     */
    public void setIsCollection(Boolean isCollection) {
        this.isCollection = isCollection;
    }

    /**
     * @param createTs
     *            the createTs to set
     */
    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    /**
     * Finds the file name based on its path
     *
     * @return file name
     */
    public String getFileName() {
        if (getPath() == null) {
            return new String();
        }

        String fileName = getPath() != null ? getPath() : "";
        fileName = fileName.substring(fileName.lastIndexOf("/") + 1, fileName.length());
        return fileName;
    }

    /**
     * Gets the icon to be displayed for a bookmarks based on its extension
     *
     * @return String containing the icon name to be displayed
     */
    public String getDisplayIcon() {
        return DataGridCoreUtils.getIconToDisplay(getPath());
    }

    /**
     * Formats the date when a collection/data object was modified
     *
     * @return String in the format MM/DD/YYYY HH:MM
     */
    public String getCreatedAtFormatted() {
        return new SimpleDateFormat("MMM dd yyyy, HH:mm").format(createTs);
    }

    @Override
    public int compareTo(DataGridGroupBookmark dgub) {
        return getFileName().compareTo(dgub.getFileName());
    }

}
