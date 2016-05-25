package com.emc.metalnx.modelattribute.breadcrumb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Responsible for managing path items on the colelctions breadcrumb
 */
public class DataGridBreadcrumb {

    private List<DataGridBreadcrumbItem> items;

    public static final String PATH_SEPARATOR = "/";

    public DataGridBreadcrumb(String path) {
    	items = new ArrayList<>();
    	
    	if (PATH_SEPARATOR.equals(path)) {
    		items.add(new DataGridBreadcrumbItem(PATH_SEPARATOR));
    		return;
    	}
    	
        List<String> pathItems = Arrays.asList(path.split(PATH_SEPARATOR));
        pathItems = pathItems.subList(1, pathItems.size());

        // Create intermediate items for current path
        for (int i = 0; i < pathItems.size(); i++) {
            items.add(new DataGridBreadcrumbItem(joinAsPath(pathItems.subList(0, i + 1))));
        }
    }

    public List<DataGridBreadcrumbItem> getItems() {
        return items;
    }

    /**
     * Auxiliary method for joining strings as a path
     * @param items list of {@link String}
     * @return path {@link String}
     */
    private String joinAsPath(List<String> items) {
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            sb.append(PATH_SEPARATOR);
            sb.append(item);
        }
        return sb.toString();
    }

}
