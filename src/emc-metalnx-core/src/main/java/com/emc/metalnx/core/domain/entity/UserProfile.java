 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

@Entity
@Audited
@Table(name = "user_profile")
public class UserProfile {

	@Id
	@Column(name = "id")
	@NotAudited
	@GeneratedValue
	private Long profileId;

	@Column(name = "profile_name", nullable = false, length = 64)
	private String profileName;

	@Column(name = "description", nullable = false, length = 512)
	private String description;

	@ManyToMany(fetch = FetchType.EAGER, cascade = { CascadeType.PERSIST })
	@JoinTable(name = "user_profile_groups", joinColumns = { @JoinColumn(name = "profile_id") }, inverseJoinColumns = {
			@JoinColumn(name = "group_id") })
	private Set<DataGridGroup> groups;

	@OneToMany(mappedBy = "userProfile", fetch = FetchType.EAGER)
	private Set<DataGridUser> users;

	public UserProfile() {

	}

	public UserProfile(String profileName, String description) {
		this.profileName = profileName;
		this.description = description;
	}

	public UserProfile(String profileName, String description, Set<DataGridGroup> groups, Set<DataGridUser> users) {
		this.profileName = profileName;
		this.description = description;
		this.groups = groups;
		this.users = users;
	}

	/**
	 * @return the profileId
	 */
	public Long getProfileId() {
		return profileId;
	}

	/**
	 * @param profileId
	 *            the profileId to set
	 */
	public void setProfileId(Long profileId) {
		this.profileId = profileId;
	}

	/**
	 * @return the profileName
	 */
	public String getProfileName() {
		return profileName;
	}

	/**
	 * @param profileName
	 *            the profileName to set
	 */
	public void setProfileName(String profileName) {
		this.profileName = profileName;
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
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the groups
	 */
	public Set<DataGridGroup> getGroups() {
		return groups;
	}

	/**
	 * @param groups
	 *            the groups to set
	 */
	public void setGroups(Set<DataGridGroup> groups) {
		this.groups = groups;
	}

	/**
	 * @return the users
	 */
	public Set<DataGridUser> getUsers() {
		return users;
	}

	/**
	 * @param users
	 *            the users to set
	 */
	public void setUsers(Set<DataGridUser> users) {
		this.users = users;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("UserProfile [");
		if (profileId != null) {
			builder.append("profileId=").append(profileId).append(", ");
		}
		if (profileName != null) {
			builder.append("profileName=").append(profileName).append(", ");
		}
		if (description != null) {
			builder.append("description=").append(description).append(", ");
		}
		if (groups != null) {
			builder.append("groups=").append(toString(groups, maxLen)).append(", ");
		}
		if (users != null) {
			builder.append("users=").append(toString(users, maxLen));
		}
		builder.append("]");
		return builder.toString();
	}

	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}
}
