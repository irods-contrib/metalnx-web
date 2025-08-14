 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity;

import java.util.Date;

/**
 * Represents an abstraction of a Zone in a data grid system
 * 
 * @author guerra
 *
 */
public class DataGridZone {

	private long id;
	private String name;
	private String type;
	private String connectionString;
	private String comment;
	private Date createTime;
	private Date modifyTime;

	public DataGridZone() {
		// empty constructor
	}

	public DataGridZone(String name, String type) {
		this.name = name;
		this.type = type;
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the connectionString
	 */
	public String getConnectionString() {
		return connectionString;
	}

	/**
	 * @param connectionString
	 *            the connectionString to set
	 */
	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}

	/**
	 * @return the comment
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment
	 *            the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	/**
	 * @return the createTime
	 */
	public Date getCreateTime() {
		return createTime;
	}

	/**
	 * @param createTime
	 *            the createTime to set
	 */
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	/**
	 * @return the modifyTime
	 */
	public Date getModifyTime() {
		return modifyTime;
	}

	/**
	 * @param modifyTime
	 *            the modifyTime to set
	 */
	public void setModifyTime(Date modifyTime) {
		this.modifyTime = modifyTime;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridZone [id=").append(id).append(", ");
		if (name != null) {
			builder.append("name=").append(name).append(", ");
		}
		if (type != null) {
			builder.append("type=").append(type).append(", ");
		}
		if (connectionString != null) {
			builder.append("connectionString=").append(connectionString).append(", ");
		}
		if (comment != null) {
			builder.append("comment=").append(comment).append(", ");
		}
		if (createTime != null) {
			builder.append("createTime=").append(createTime).append(", ");
		}
		if (modifyTime != null) {
			builder.append("modifyTime=").append(modifyTime);
		}
		builder.append("]");
		return builder.toString();
	}

}
