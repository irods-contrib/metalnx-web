 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.modelattribute.breadcrumb;

import java.net.URLEncoder;

/**
 * Represents a path item on the breadcrumb
 */
public class DataGridBreadcrumbItem {

	private final String name;
	private final String path;
	private final String encPath;

	public DataGridBreadcrumbItem(final String path) {
		this.path = path;

		// Getting last item of the path based on the last occurent of PATH_SEPARATOR
		name = path.substring(path.lastIndexOf(DataGridBreadcrumb.PATH_SEPARATOR) + 1, path.length());
		this.encPath = URLEncoder.encode(path);
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	/**
	 * @return the encPath
	 */
	public String getEncPath() {
		return encPath;
	}

}
