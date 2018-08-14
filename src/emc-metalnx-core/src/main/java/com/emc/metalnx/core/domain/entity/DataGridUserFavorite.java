 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */


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
import javax.persistence.UniqueConstraint;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.emc.metalnx.core.domain.utils.DataGridCoreUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
//@Audited
@Table(name = "user_favorites", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "path_hash" }))
public class DataGridUserFavorite implements Serializable, Comparable<DataGridUserFavorite> {

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

	@Column(name = "is_collection", nullable = true)
	private Boolean isCollection;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at", nullable = false, length = 60, updatable = false)
	private Date createTs;

	@Column(name = "path_hash", nullable = false)
	private int pathHash;

	private static final long serialVersionUID = -7923823760209937080L;

	/**
	 * @return the id
	 */
	@JsonIgnore
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
	 * @return the isCollection
	 */
	public boolean getIsCollection() {
		return isCollection;
	}

	/**
	 * @return the createTs
	 */
	@JsonIgnore
	public Date getCreateTs() {
		return createTs;
	}

	public String getCreateTsFormatted() {
		return new SimpleDateFormat("MMM dd yyyy, HH:mm").format(createTs);
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
	 * @param fileName
	 *            the fileName to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param isNotified
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
	public String getName() {
		return this.name;
	}

	/**
	 * @return the pathHash
	 */
	public int getPathHash() {
		return pathHash;
	}

	/**
	 * @param pathHash
	 *            the pathHash to set
	 */
	public void setPathHash(int pathHash) {
		this.pathHash = pathHash;
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
	public int compareTo(DataGridUserFavorite dgub) {
		return this.getName().compareTo(dgub.getName());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridUserFavorite [");
		if (id != null) {
			builder.append("id=").append(id).append(", ");
		}
		if (path != null) {
			builder.append("path=").append(path).append(", ");
		}
		if (name != null) {
			builder.append("name=").append(name).append(", ");
		}
		if (isCollection != null) {
			builder.append("isCollection=").append(isCollection).append(", ");
		}
		if (createTs != null) {
			builder.append("createTs=").append(createTs).append(", ");
		}
		builder.append("pathHash=").append(pathHash).append("]");
		return builder.toString();
	}

}
