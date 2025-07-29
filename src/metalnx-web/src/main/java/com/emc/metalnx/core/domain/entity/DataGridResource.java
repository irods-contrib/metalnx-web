 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataGridResource implements Serializable, Comparable<DataGridResource> {

	// resource id in the data grid
	private long id;

	// resource name ("demoResc")
	private String name;

	// resource zone name
	private String zone;

	// resource type ("unix file system", "replication", etc)
	private String type;

	// resource path
	private String path;

	// resource free space
	private long freeSpace;

	// when the free space was calculated
	private Date freeSpaceTimeStamp;

	// other resources existing inside this resource
	private List<String> children;

	// resource parent name
	private String parent;

	// resource status ("up", "down")
	private String status;

	// resource host name
	private String host;

	// when the resource was created
	private Date createTime;

	// last time the resource was modified
	private Date modifyTime;

	// any information related to this resource
	private String info;

	// number of records existing in the resource
	private int totalRecords;

	// comment about a resource
	private String comment;

	// Context string of a resource
	private String contextString;

	private static final long serialVersionUID = 1L;

	public DataGridResource(long id, String name, String zone, String type, String path, long freeSpace,
			Date freeSpaceTimeStamp, List<String> children, String parent, String status, String host, Date createTime,
			Date modifyTime, String info, int totalRecords, String contextString) {
		super();
		this.id = id;
		this.name = name;
		this.zone = zone;
		this.type = type;
		this.path = path;
		this.freeSpace = freeSpace;
		this.freeSpaceTimeStamp = freeSpaceTimeStamp;
		this.children = children;
		this.parent = parent;
		this.status = status;
		this.host = host;
		this.contextString = contextString;
	}

	public DataGridResource(long id, String name, String zone, String type, String path) {
		this(id, name, zone, type, "unknown", -1, null, null, "unknown", "unknown", "unknown", new Date(), new Date(),
				"unknown", 0, null);
	}

	public DataGridResource() {
		// empty constructor
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridResource [id=").append(id).append(", ");
		if (name != null) {
			builder.append("name=").append(name).append(", ");
		}
		if (zone != null) {
			builder.append("zone=").append(zone).append(", ");
		}
		if (type != null) {
			builder.append("type=").append(type).append(", ");
		}
		if (path != null) {
			builder.append("path=").append(path).append(", ");
		}
		builder.append("freeSpace=").append(freeSpace).append(", ");
		if (freeSpaceTimeStamp != null) {
			builder.append("freeSpaceTimeStamp=").append(freeSpaceTimeStamp).append(", ");
		}
		if (children != null) {
			builder.append("children=").append(children.subList(0, Math.min(children.size(), maxLen))).append(", ");
		}
		if (parent != null) {
			builder.append("parent=").append(parent).append(", ");
		}
		if (status != null) {
			builder.append("status=").append(status).append(", ");
		}
		if (host != null) {
			builder.append("host=").append(host).append(", ");
		}
		if (createTime != null) {
			builder.append("createTime=").append(createTime).append(", ");
		}
		if (modifyTime != null) {
			builder.append("modifyTime=").append(modifyTime).append(", ");
		}
		if (info != null) {
			builder.append("info=").append(info).append(", ");
		}
		builder.append("totalRecords=").append(totalRecords).append(", ");
		if (comment != null) {
			builder.append("comment=").append(comment).append(", ");
		}
		if (contextString != null) {
			builder.append("contextString=").append(contextString);
		}
		builder.append("]");
		return builder.toString();
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
	 * @return the zone
	 */
	public String getZone() {
		return zone;
	}

	/**
	 * @param zone
	 *            the zone to set
	 */
	public void setZone(String zone) {
		this.zone = zone;
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
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the freeSpace
	 */
	public long getFreeSpace() {
		return freeSpace;
	}

	/**
	 * @param freeSpace
	 *            the freeSpace to set
	 */
	public void setFreeSpace(long freeSpace) {
		this.freeSpace = freeSpace;
	}

	/**
	 * @return the freeSpaceDate
	 */
	public Date getFreeSpaceDate() {
		return freeSpaceTimeStamp;
	}

	/**
	 * @param freeSpaceDate
	 *            the freeSpaceDate to set
	 */
	public void setFreeSpaceDate(Date freeSpaceDate) {
		freeSpaceTimeStamp = freeSpaceDate;
	}

	/**
	 * @return the children
	 */
	public List<String> getChildren() {
		if (children == null)
			return new ArrayList<>();
		return children;
	}

	/**
	 * @param children
	 *            the children to set
	 */
	public void setChildren(List<String> children) {
		this.children = children;
	}

	public void addChildResc(String childResc) {
		if (children == null)
			children = new ArrayList<>();
		children.add(childResc);
	}

	/**
	 * @return the parent
	 */
	public String getParent() {
		if (parent == null)
			return "";
		return parent;
	}

	/**
	 * @param parent
	 *            the parent to set
	 */
	public void setParent(String parent) {
		this.parent = parent;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status
	 *            the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the freeSpaceTimeStamp
	 */
	public Date getFreeSpaceTimeStamp() {
		return freeSpaceTimeStamp;
	}

	/**
	 * @param freeSpaceTimeStamp
	 *            the freeSpaceTimeStamp to set
	 */
	public void setFreeSpaceTimeStamp(Date freeSpaceTimeStamp) {
		this.freeSpaceTimeStamp = freeSpaceTimeStamp;
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

	/**
	 * @return the info
	 */
	public String getInfo() {
		return info;
	}

	/**
	 * @param info
	 *            the info to set
	 */
	public void setInfo(String info) {
		this.info = info;
	}

	/**
	 * @return the totalRecords
	 */
	public int getTotalRecords() {
		return totalRecords;
	}

	/**
	 * @param totalRecords
	 *            the totalRecords to set
	 */
	public void setTotalRecords(int totalRecords) {
		this.totalRecords = totalRecords;
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
	 * @return the contextString
	 */
	public String getContextString() {
		return contextString;
	}

	/**
	 * @param contextString
	 *            the contextString to set
	 */
	public void setContextString(String contextString) {
		this.contextString = contextString;
	}

	/**
	 * @return the isiHost
	 */
	public String getIsiHost() {
		String isiHost = "";

		if (contextString == null || contextString.isEmpty()) {
			return "";
		}

		if (contextString.contains("isi_host")) {
			String[] contextStringSplitted = contextString.split(";");
			// checking if after splitting we have the parts: isi_host, isi_port, isi_user
			if (contextStringSplitted.length == 3) {
				// isi_host=<host_ip>, getting the host_ip value
				if (contextStringSplitted[0].length() == 2) {
					isiHost = contextStringSplitted[0].split("=")[1];
				} else {
					isiHost = null;
				}
			}
		}

		return isiHost;
	}

	/**
	 * @return the isiPort
	 */
	public String getIsiPort() {
		String isiPort = "";

		if (contextString == null || contextString.isEmpty()) {
			return "";
		}

		if (contextString.contains("isi_port")) {
			String[] contextStringSplitted = contextString.split(";");
			// checking if after splitting we have the parts: isi_host, isi_port, isi_user
			if (contextStringSplitted.length == 3) {
				// isi_port=<port>, getting the port value
				isiPort = contextStringSplitted[1].split("=")[1];
			}
		}

		return isiPort;
	}

	/**
	 * @return the isiUser
	 */
	public String getIsiUser() {
		String isiUser = "";

		if (contextString == null || contextString.isEmpty()) {
			return "";
		}

		if (contextString.contains("isi_user")) {
			String[] contextStringSplitted = contextString.split(";");
			// checking if after splitting we have the parts: isi_host, isi_port, isi_user
			if (contextStringSplitted.length == 3) {
				// isi_user=<username>, getting the username value
				isiUser = contextStringSplitted[2].split("=")[1];
			}
		}

		return isiUser;
	}

	/**
	 * Checks whether the resource is a root resource or not. Root resource is a
	 * resource that is not a child of any other resource.
	 *
	 * @return
	 */
	public boolean isFirstLevelResc() {
		String parent = getParent();
		boolean isFirstLevel = false;

		if (parent == null || parent.isEmpty() || parent.equals(zone)) {
			isFirstLevel = true;
		}

		return isFirstLevel;
	}

	@Override
	public int compareTo(DataGridResource dataGridResource) {
		return getName().toLowerCase().compareTo(dataGridResource.getName().toLowerCase());
	}

	@Override
	public boolean equals(Object object) {
		boolean isEqual = false;

		if (object != null && object instanceof DataGridResource) {
			isEqual = id == ((DataGridResource) object).getId();
		}

		return isEqual;
	}

}
