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
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Audited
@Table(name = "user_bookmarks")
public class DataGridUserBookmark implements Serializable, Comparable<DataGridUserBookmark> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotAudited
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "user_id", nullable = false, updatable = true)
    private DataGridUser user;

    @Column(name = "path", nullable = false, length = 512)
    private String path;

    @Column(name = "name", nullable = false, length = 512)
    private String name;

    @Column(name = "is_notified", nullable = true)
    private Boolean isNotified;

    @Column(name = "is_collection", nullable = false)
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
     * @return the user
     */
    @JsonIgnore
    public DataGridUser getUser() {
        return user;
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

    public String getCreateTsFormatted() {
        return new SimpleDateFormat("MMM dd yyyy, HH:mm").format(createTs);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
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
     * @param id
     *            the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(DataGridUser user) {
        this.user = user;
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
     * @param createTs
     *            the createTs to set
     */
    public void setCreateTs(Date createTs) {
        this.createTs = createTs;
    }

    /**
     * Gets the icon to be displayed for a bookmarks based on its extension
     *
     * @return String containing the icon name to be displayed
     */
    public String getDisplayIcon() {
        return DataGridCoreUtils.getIconToDisplay(this.getPath());
    }

    @Override
    public int compareTo(DataGridUserBookmark dgub) {
        return this.getName().compareTo(dgub.getName());
    }
}
