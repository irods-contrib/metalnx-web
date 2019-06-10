 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.modelattribute.collection;

/**
 * Class that represents a form that contains information about a collection or
 * data object that is about to change. For collections it contains the name and
 * the inherit option and for data object it only contains name.
 */
public class CollectionOrDataObjectForm {

	private String collectionName;
	private String path;
	private String parentPath;
	private boolean inheritOption;
	private boolean isCollection;

	/**
	 * @return the collectionName
	 */
	public String getCollectionName() {
		return collectionName;
	}

	/**
	 * @param collectionName
	 *            the collectionName to set
	 */
	public void setCollectionName(final String collectionName) {
		this.collectionName = collectionName.trim();
	}

	/**
	 * @return the inheritOption
	 */
	public boolean getInheritOption() {
		return inheritOption;
	}

	/**
	 * @param inheritOption
	 *            the inheritOption to set
	 */
	public void setInheritOption(final boolean inheritOption) {
		this.inheritOption = inheritOption;
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
	public void setPath(final String path) {
		this.path = path;
	}

	/**
	 * @return the parentPath
	 */
	public String getParentPath() {
		return parentPath;
	}

	/**
	 * @param parentPath
	 *            the parentPath to set
	 */
	public void setParentPath(final String parentPath) {
		this.parentPath = parentPath;
	}

	/**
	 * @return the isCollection
	 */
	public boolean isCollection() {
		return isCollection;
	}

	/**
	 * @param isCollection
	 *            the isCollection to set
	 */
	public void setCollection(final boolean isCollection) {
		this.isCollection = isCollection;
	}
}
