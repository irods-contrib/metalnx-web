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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Entity
@Audited
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = { "username", "additional_info" }) )
public class DataGridUser implements Serializable, Comparable<DataGridUser> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotAudited
    private Long id;

    @Column(name = "data_grid_id", unique = true, nullable = false)
    private long dataGridId;

    @Column(name = "username", nullable = false, length = 60, unique = true)
    private String username;

    private String password;

    @Column(name = "additional_info", nullable = true, length = 128)
    private String additionalInfo;

    @Column(name = "enabled", nullable = false)
    private boolean enabled;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", nullable = true)
    private String email;

    @Column(name = "locale")
    private String locale = "en";

    @Column(name = "forceFileOverwriting", nullable = false)
    private boolean forceFileOverwriting = false;

    @Column(name = "user_type", nullable = false, length = 60)
    private String userType;

    @Column(name = "organizational_role", nullable = true, length = 60)
    private String organizationalRole;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "userProfile_id", nullable = true, updatable = true)
    private UserProfile userProfile;

    @Column(name = "user_company", nullable = true, length = 60)
    private String company;

    @Column(name = "user_department", nullable = true, length = 60)
    private String department;

    @Column(name = "user_title", nullable = true, length = 60)
    private String title;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.DETACH, orphanRemoval = true)
    private Set<DataGridUserBookmark> bookmarks;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.DETACH, orphanRemoval = true)
    private Set<DataGridUserFavorite> favorites;

    private static final long serialVersionUID = -500578459147421831L;

    public DataGridUser() {

    }

    public DataGridUser(String username, String password, boolean enabled) {
        this.username = username;
        this.enabled = enabled;
    }

    public String getDisplayName() {
        if (firstName != null && !firstName.isEmpty()) {
            return firstName;
        }

        return username;
    }

    /**
     * Gets the user bookmarks sorted in ascending order
     *
     * @return the userBookmarks
     */
    public List<DataGridUserBookmark> getBookmarksSorted() {
        List<DataGridUserBookmark> bookmarksSorted = new ArrayList<DataGridUserBookmark>(bookmarks);
        Collections.sort(bookmarksSorted);
        return bookmarksSorted;
    }

    /**
     * @return the userBookmarks
     */
    public Set<DataGridUserBookmark> getBookmarks() {
        return bookmarks;
    }

    /**
     * @param userBookmarks
     *            the userBookmarks to set
     */
    public void setUserBookmarks(Set<DataGridUserBookmark> userBookmarks) {
        bookmarks = userBookmarks;
    }

    /**
     * Gets the user favorites sorted in ascending order
     *
     * @return the userFavorites
     */
    public List<DataGridUserFavorite> getFavoritesSorted() {
        List<DataGridUserFavorite> favoritesSorted = new ArrayList<DataGridUserFavorite>(favorites);
        Collections.sort(favoritesSorted);
        return favoritesSorted;
    }

    /**
     * @return the userFavorites
     */
    public Set<DataGridUserFavorite> getFavorites() {
        return favorites;
    }

    /**
     * @param userFavorites
     *            the userFavorites to set
     */
    public void setUserFavorites(Set<DataGridUserFavorite> userFavorites) {
        favorites = userFavorites;
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
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @return the additionalInfo
     */
    public String getAdditionalInfo() {
        return additionalInfo;
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
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
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @param additionalInfo
     *            the additionalInfo to set
     */
    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    /**
     * @param enabled
     *            the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return the firstName
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return the lastName
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * @param lastName
     *            the lastName to set
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email
     *            the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the userProfile
     */
    public UserProfile getUserProfile() {
        return userProfile;
    }

    /**
     * @param userProfile
     *            the userProfile to set
     */
    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    /**
     * @return the locale
     */
    public String getLocale() {
        return locale;
    }

    /**
     * @param locale
     *            the locale to set
     */
    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    /**
     * @return the organizationalRole
     */
    public String getOrganizationalRole() {
        return organizationalRole;
    }

    /**
     * @param organizationalRole
     *            the organizationalRole to set
     */
    public void setOrganizationalRole(String organizationalRole) {
        this.organizationalRole = organizationalRole;
    }

    /**
     * @return the company
     */
    public String getCompany() {
        return company;
    }

    /**
     * @return the department
     */
    public String getDepartment() {
        return department;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param company
     *            the company to set
     */
    public void setCompany(String company) {
        this.company = company;
    }

    /**
     * @param department
     *            the department to set
     */
    public void setDepartment(String department) {
        this.department = department;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int compareTo(DataGridUser o) {
        return username.compareTo(o.getUsername());
    }

    public boolean isAdmin() {
        return userType.compareTo("rodsadmin") == 0;
    }

    /**
     * @return the forceFileOverwriting
     */
    public boolean isForceFileOverwriting() {
        return forceFileOverwriting;
    }

    /**
     * @param forceFileOverwriting
     *            the forceFileOverwriting to set
     */
    public void setForceFileOverwriting(boolean forceFileOverwriting) {
        this.forceFileOverwriting = forceFileOverwriting;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

}
