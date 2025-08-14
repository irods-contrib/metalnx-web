 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.entity;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class DataGridCollectionAndDataObject implements Serializable {

	private static final String DATE_FORMAT_STR = "MMM dd yyyy HH:mm";

	private String name;
	private String path;
	private String parentPath;
	private String owner;
	private Date modifiedAt;
	private Date createdAt;
	private boolean isCollection;
	private String checksum;
	private String replicaNumber;
	private int numberOfReplicas;
	private String displaySize;
	private String mostPermissiveAccessForCurrentUser;
	private long size;
	private String resourceName;
	private boolean inheritanceOption;
	private boolean visibleToCurrentUser;
	/**
	 * Indicates that this collection is actually a proxy to a collection the user
	 * cannot see in iRODS, but is necessary in order to drill down when StrictACLs
	 * are active
	 */
	private boolean proxy = false;

	// number of matches on a metadata search
	private int numberOfMatches;

	private static final long serialVersionUID = 1L;

	public DataGridCollectionAndDataObject() {

	}

	public DataGridCollectionAndDataObject(String path, String parentPath, boolean isCollection) {
		this.path = path;
		this.parentPath = parentPath;
		this.name = findCollectionName(path);
		this.isCollection = isCollection;
	}

	public DataGridCollectionAndDataObject(String path, String name, String parentPath, boolean isCollection) {
		this.path = path;
		this.name = name;
		this.parentPath = parentPath;
		this.isCollection = isCollection;
	}

	/**
	 * Finds a collection name based on its path
	 *
	 * @param collectionPath
	 * @return
	 */
	public String findCollectionName(String collectionPath) {
		String[] pathParts = collectionPath.split("/");
		return pathParts[pathParts.length - 1];
	}

	/**
	 * @return the collectionName
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the collectionName to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the collectionPath
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the collectionPath to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the parentCollectionPath
	 */
	public String getParentPath() {
		return parentPath;
	}

	/**
	 * @param parentPath
	 *            the parentCollectionPath to set
	 */
	public void setParentPath(String parentPath) {
		this.parentPath = parentPath;
	}

	/**
	 * @return the isCollection
	 */
	public boolean isCollection() {
		return isCollection;
	}

	/**
	 * Checks if current object corresponds data object in the grid.
	 * 
	 * @return True, if current object is a data object in the grid. False,
	 *         otherwise.
	 */
	public boolean isDataObject() {
		return !isCollection;
	}

	/**
	 * @param isCollection
	 *            the isCollection to set
	 */
	public void setCollection(boolean isCollection) {
		this.isCollection = isCollection;
	}

	/**
	 * Gets the icon type that will be shown on the UI.
	 *
	 * @return the icon type as String
	 */
	public String getIconToDisplay() {

		if (isCollection)
			return "fa fa-folder";

		String icon = "";
		String extension = this.getFileExtension(this.getName());

		switch (extension) {

		case "jpg":
		case "jpeg":
		case "png":
		case "gif":
			icon = "fa fa-file-image-o";
			break;

		case "html":
		case "htm":
		case "xml":
		case "tex":
			icon = "fa fa-code";
			break;

		case "c":
		case "cpp":
		case "java":
		case "py":
			icon = "fa fa-file-code-o";
			break;

		case "docx":
		case "doc":
			icon = "fa fa-file-word-o";
			break;

		case "xlsx":
		case "xls":
			icon = "fa fa-file-excel-o";
			break;

		case "pptx":
		case "ppt":
			icon = "fa fa-file-powerpoint-o";
			break;

		case "pdf":
			icon = "fa fa-file-pdf-o";
			break;

		case "zip":
		case "rar":
			icon = "fa fa-file-archive-o";
			break;

		default:
			icon = "fa fa-file";
			break;
		}

		return icon;

	}

	private String getFileExtension(String fileName) {

		int lastIndexOfFileName = fileName.lastIndexOf(".");
		if (lastIndexOfFileName != -1 && lastIndexOfFileName != 0) {
			return fileName.substring(lastIndexOfFileName + 1);
		}

		else
			return "unknown";

	}

	/**
	 * @return the modified
	 */
	public Date getModifiedAt() {
		return modifiedAt;
	}

	/**
	 * @param modifiedAt
	 *            the modified to set
	 */
	public void setModifiedAt(Date modifiedAt) {
		this.modifiedAt = modifiedAt;
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
	 * @return the created
	 */
	public Date getCreatedAt() {
		return createdAt;
	}

	/**
	 * @param createdAt
	 *            the created to set
	 */
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	/**
	 * Formats the date when a collection/data object was modified
	 *
	 * @return String in the format MM/DD/YYYY HH:MM
	 */
	public String getModifiedAtFormatted() {
		if (modifiedAt == null)
			return "";
		return new SimpleDateFormat(DATE_FORMAT_STR).format(modifiedAt);
	}

	/**
	 * Formats the date when a collection/data object was modified for the CSV
	 * report
	 *
	 * @return String in the format MM/DD/YYYY HH:MM
	 */
	public String getModifiedAtFormattedForCSVReport() {
		return new SimpleDateFormat("MMM-dd-yyyy_HH-mm").format(modifiedAt);
	}

	/**
	 * Formats the date when a collection/data object was created
	 *
	 * @return the modified
	 */
	@JsonIgnore
	public String getCreatedAtFormatted() {
		if (createdAt == null)
			return "";
		return new SimpleDateFormat(DATE_FORMAT_STR).format(createdAt);
	}

	/**
	 * @return the checksum
	 */
	public String getChecksum() {
		return checksum;
	}

	/**
	 * @param checksum
	 *            the checksum to set
	 */
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	/**
	 * @return the replicaNumber
	 */
	public String getReplicaNumber() {
		return replicaNumber;
	}

	/**
	 * @param replicaNumber
	 *            the replicaNumber to set
	 */
	public void setReplicaNumber(String replicaNumber) {
		this.replicaNumber = replicaNumber;
	}

	/**
	 * @return the numerOfReplicas
	 */
	public int getNumberOfReplicas() {
		return numberOfReplicas;
	}

	/**
	 * @param numerOfReplicas
	 *            the numerOfReplicas to set
	 */
	public void setNumberOfReplicas(int numerOfReplicas) {
		this.numberOfReplicas = numerOfReplicas;
	}

	/**
	 * @return the displaySize
	 */
	public String getDisplaySize() {
		return displaySize;
	}

	/**
	 * @param displaySize
	 *            the displaySize to set
	 */
	public void setDisplaySize(String displaySize) {
		this.displaySize = displaySize;
	}

	/**
	 * @return the size
	 */
	public long getSize() {
		return size;
	}

	/**
	 * @param size
	 *            the size to set
	 */
	public void setSize(long size) {
		this.size = size;
	}

	/**
	 * @return the resourceName
	 */
	public String getResourceName() {
		return resourceName;
	}

	/**
	 * @param resourceName
	 *            the resourceName to set
	 */
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}

	/**
	 * @return the inheritance option
	 */
	public boolean isInheritanceOption() {
		return inheritanceOption;
	}

	/**
	 * @param inheritanceOption
	 *            the inheritance option to set
	 */
	public void setInheritanceOption(boolean inheritanceOption) {
		this.inheritanceOption = inheritanceOption;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridCollectionAndDataObject [");
		if (name != null) {
			builder.append("name=").append(name).append(", ");
		}
		if (path != null) {
			builder.append("path=").append(path).append(", ");
		}
		if (parentPath != null) {
			builder.append("parentPath=").append(parentPath).append(", ");
		}
		if (owner != null) {
			builder.append("owner=").append(owner).append(", ");
		}
		if (modifiedAt != null) {
			builder.append("modifiedAt=").append(modifiedAt).append(", ");
		}
		if (createdAt != null) {
			builder.append("createdAt=").append(createdAt).append(", ");
		}
		builder.append("isCollection=").append(isCollection).append(", ");
		if (checksum != null) {
			builder.append("checksum=").append(checksum).append(", ");
		}
		if (replicaNumber != null) {
			builder.append("replicaNumber=").append(replicaNumber).append(", ");
		}
		builder.append("numberOfReplicas=").append(numberOfReplicas).append(", ");
		if (displaySize != null) {
			builder.append("displaySize=").append(displaySize).append(", ");
		}
		if (mostPermissiveAccessForCurrentUser != null) {
			builder.append("mostPermissiveAccessForCurrentUser=").append(mostPermissiveAccessForCurrentUser)
					.append(", ");
		}
		builder.append("size=").append(size).append(", ");
		if (resourceName != null) {
			builder.append("resourceName=").append(resourceName).append(", ");
		}
		builder.append("inheritanceOption=").append(inheritanceOption).append(", visibleToCurrentUser=")
				.append(visibleToCurrentUser).append(", proxy=").append(proxy).append(", numberOfMatches=")
				.append(numberOfMatches).append("]");
		return builder.toString();
	}

	/**
	 * @return the numberOfMatches
	 */
	public int getNumberOfMatches() {
		return numberOfMatches;
	}

	/**
	 * @param numberOfMatches
	 *            the numberOfMatches to set
	 */
	public void setNumberOfMatches(int numberOfMatches) {
		this.numberOfMatches = numberOfMatches;
	}

	/**
	 * @return the visibleToCurrentUser
	 */
	public boolean isVisibleToCurrentUser() {
		return visibleToCurrentUser;
	}

	/**
	 * @param visibleToCurrentUser
	 *            the visibleToCurrentUser to set
	 */
	public void setVisibleToCurrentUser(boolean visibleToCurrentUser) {
		this.visibleToCurrentUser = visibleToCurrentUser;
	}

	public String getMostPermissiveAccessForCurrentUser() {
		return mostPermissiveAccessForCurrentUser;
	}

	public void setMostPermissiveAccessForCurrentUser(String mostPermissiveAccessForCurrentUser) {
		this.mostPermissiveAccessForCurrentUser = mostPermissiveAccessForCurrentUser;
	}

	public boolean isProxy() {
		return proxy;
	}

	public void setProxy(boolean isProxy) {
		this.proxy = isProxy;
	}
}
