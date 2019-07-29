/**
 * 
 */
package com.emc.metalnx.controller.model;

/**
 * Model for inheritance updates
 * 
 * @author conwaymc
 *
 */
public class InheritanceModel {

	private String path = "";
	private boolean recursive = true;
	private boolean inherit = true;

	public InheritanceModel() {
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public boolean isInherit() {
		return inherit;
	}

	public void setInherit(boolean inherit) {
		this.inherit = inherit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("InheritanceModel [");
		if (path != null) {
			builder.append("path=").append(path).append(", ");
		}
		builder.append("recursive=").append(recursive).append(", inherit=").append(inherit).append("]");
		return builder.toString();
	}

}
