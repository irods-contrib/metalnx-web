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
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Audited
@Table(name = "groups")
public class DataGridGroup implements Serializable, Comparable<DataGridGroup> {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotAudited
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "data_grid_id", unique = true, nullable = false)
    private long dataGridId;

    @Column(name = "groupname", unique = true, nullable = false, length = 60)
    private String groupname;

    @Column(name = "additional_info", nullable = true, length = 60)
    private String additional_info;

    @ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST })
    @JoinTable(name = "user_profile_groups", joinColumns = { @JoinColumn(name = "group_id") }, inverseJoinColumns = { @JoinColumn(name = "profile_id") })
    private Set<UserProfile> userProfiles;

    @JsonIgnore
    @OneToMany(mappedBy = "group", fetch = FetchType.EAGER, cascade = CascadeType.DETACH, orphanRemoval = true)
    private Set<DataGridGroupBookmark> groupBookmarks;

    public DataGridGroup() {

    }

    public DataGridGroup(String groupname, String additional_info) {
        this.groupname = groupname;
        this.additional_info = additional_info;
    }

    public String getDisplayName() {

        return groupname;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the dataGridId
     */
    public long getDataGridId() {
        return dataGridId;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @param dataGridId
     *            the dataGridId to set
     */
    public void setDataGridId(long dataGridId) {
        this.dataGridId = dataGridId;
    }

    /**
     * @return the groupName
     */
    public String getGroupname() {
        return groupname;
    }

    /**
     * @param the
     *            groupName to set
     */
    public void setGroupname(String groupName) {
        groupname = groupName;
    }

    /**
     * @return group's zone
     */
    public String getAdditionalInfo() {
        return additional_info;
    }

    /**
     * @param the
     *            zone to set
     */
    public void setAdditionalInfo(String additional_info) {
        this.additional_info = additional_info;
    }

    /**
     * @return the groupBookmarks
     */
    public Set<DataGridGroupBookmark> getGroupBookmarks() {
        return groupBookmarks;
    }

    /**
     * @param groupBookmarks
     *            the groupBookmarks to set
     */
    public void setGroupBookmarks(Set<DataGridGroupBookmark> groupBookmarks) {
        this.groupBookmarks = groupBookmarks;
    }

    @Override
    public int compareTo(DataGridGroup dgg) {
        return groupname.compareTo(dgg.getGroupname());
    }

    @Override
    public String toString() {
        return groupname;
    }
}
