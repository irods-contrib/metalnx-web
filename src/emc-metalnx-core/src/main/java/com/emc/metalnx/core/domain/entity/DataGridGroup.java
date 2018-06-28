 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
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
	@JoinTable(name = "user_profile_groups", joinColumns = { @JoinColumn(name = "group_id") }, inverseJoinColumns = {
			@JoinColumn(name = "profile_id") })
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
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridGroup [");
		if (id != null) {
			builder.append("id=").append(id).append(", ");
		}
		builder.append("dataGridId=").append(dataGridId).append(", ");
		if (groupname != null) {
			builder.append("groupname=").append(groupname).append(", ");
		}
		if (additional_info != null) {
			builder.append("additional_info=").append(additional_info).append(", ");
		}
		if (userProfiles != null) {
			builder.append("userProfiles=").append(toString(userProfiles, maxLen)).append(", ");
		}
		if (groupBookmarks != null) {
			builder.append("groupBookmarks=").append(toString(groupBookmarks, maxLen));
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
