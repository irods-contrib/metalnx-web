/**
 * 
 */
package com.emc.metalnx.services.irods.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Misc utils for creating sql queries
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class MiscSpecificQueryUtils {

	public static final Map<Integer, String> getMapColumnsForCollections() {
		Map<Integer, String> datatableColumns = new HashMap<>();
		datatableColumns.put(0, "coll_name");
		datatableColumns.put(1, "coll_name");
		datatableColumns.put(2, "coll_owner_name");
		datatableColumns.put(4, "modify_ts");
		datatableColumns.put(5, "coll_type");

		return datatableColumns;
	}

	public static final Map<Integer, String> getMapColumnsForDataObjects() {
		Map<Integer, String> datatableColumns = new HashMap<>();
		datatableColumns.put(0, "data_name");
		datatableColumns.put(1, "data_name");
		datatableColumns.put(2, "data_owner_name");
		datatableColumns.put(4, "modify_ts");
		datatableColumns.put(5, "data_size");

		return datatableColumns;
	}

}
