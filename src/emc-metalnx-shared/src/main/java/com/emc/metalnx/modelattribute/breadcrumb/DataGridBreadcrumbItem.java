package com.emc.metalnx.modelattribute.breadcrumb;

/**
 * Represents a path item on the breadcrumb
 */
public class DataGridBreadcrumbItem {

    private String name;
    private String path;

    public DataGridBreadcrumbItem(String path) {
        this.path = path;

        // Getting last item of the path based on the last occurent of PATH_SEPARATOR
        this.name = path.substring(path.lastIndexOf(DataGridBreadcrumb.PATH_SEPARATOR) + 1, path.length());
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

}
