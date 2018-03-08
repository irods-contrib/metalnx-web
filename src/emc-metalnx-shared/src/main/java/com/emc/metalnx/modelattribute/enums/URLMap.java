/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.modelattribute.enums;

public class URLMap {

	public static final String URL_DASHBOARD = "/dashboard/";

	public static final String URL_RULES = "/rules/";

	public static final String URL_USERS_MANAGEMENT = "/users/";
	public static final String URL_ADD_USER = "add/";
	public static final String URL_MODIFY_USER = "modify/";
	public static final String URL_DELETE_USER = "delete/";
	public static final String URL_FIND_USER = "/users/find/";
	public static final String URL_FIND_ALL_USER = "/users/findAll/";
	public static final String URL_USERS_CSV_REPORT = "/users/usersToCSVFile/";
	public static final String URL_USER_BOOKMARKS = "/userBookmarks/";
	public static final String URL_STARRED_ITEMS = "/favorites/";

	public static final String URL_USER_PROFILE_MANAGEMENT = "/users/profile/";
	public static final String URL_ADD_USER_PROFILE = "/users/profile/create/";
	public static final String URL_USER_PROFILE_VALIDATE_PROFILE_NAME = "/emc-metalnx-web/users/profile/isValidProfileName/";
	public static final String URL_USER_PROFILES_CSV_REPORT = "/users/profile/profilesToCSVFile/";

	public static final String URL_REMOVE_USER_PROFILE = "/emc-metalnx-web/users/profile/remove/";
	public static final String URL_MODIFY_USER_PROFILE = "/emc-metalnx-web/users/profile/modify/";
	public static final String URL_USER_VALIDATE_USERNAME = "/emc-metalnx-web/users/isValidUsername/";

	public static final String URL_GROUPS_MANAGEMENT = "/groups/";
	public static final String URL_ADD_GROUP = "add/";
	public static final String URL_MODIFY_GROUP = "modify/";
	public static final String URL_DELETE_GROUP = "delete/";
	public static final String URL_GROUP_VALIDATE_GROUPNAME = "/emc-metalnx-web/groups/isValidGroupname/";
	public static final String URL_GROUPS_CSV_REPORT = "/groups/groupsToCSVFile/";
	public static final String URL_GROUPS_BOOKMARKS = "/groupBookmarks/groups/";

	public static final String URL_COLLECTIONS_INFO = "/info/";
	public static final String URL_COLLECTIONS_MANAGEMENT = "/collections/";
	public static final String URL_ADD_COLLECTION = "/emc-metalnx-web/browse/add/";
	public static final String URL_MODIFY_COLLECTION = "/emc-metalnx-web/fileOperation/modify/";
	public static final String URL_DELETE_COLLECTION = "/emc-metalnx-web/fileOperation/delete/";
	public static final String URL_COLLECTION_VALIDATE_NAME = "/emc-metalnx-web/browse/isValidCollectionName/";

	public static final String URL_METADATA_SEARCH = "/metadata/";

	public static final String URL_TEMPLATE_MANAGEMENT = "/templates/";
	public static final String URL_ADD_TEMPLATE = "add/";
	public static final String URL_MODIFY_TEMPLATE = "modify/";
	public static final String URL_DELETE_TEMPLATE = "delete/";
	public static final String URL_DELETE_TEMPLATE_FIELD_FROM_DB = "/emc-metalnx-web/templates/removeFieldFromDB";
	public static final String URL_DELETE_TEMPLATE_FIELD = "/emc-metalnx-web/templates/removeFieldFromTemplate";
	public static final String URL_TEMPLATE_VALIDATE_NAME = "/emc-metalnx-web/templates/isValidTemplateName/";
	public static final String URL_EXPORT_TEMPLATE_XML = "exportTemplatesToXMLFile/";

	public static final String URL_RESOURCES_MANAGEMENT = "/resources/";
	public static final String URL_ADD_RESOURCE = "/resources/add/";
	public static final String URL_ADD_RESOURCE_ACTION = "/resources/add/action/";
	public static final String URL_MODIFY_RESOURCE = "/resources/modify/";
	public static final String URL_MODIFY_RESOURCE_ACTION = "/resources/modify/action/";
	public static final String URL_DELETE_RESOURCE = "/emc-metalnx-web/resources/delete/";
	public static final String URL_RESOURCES_SERVERS = "/resources/servers/";
	public static final String URL_RESOURCES_MAP = "/resources/map/";
	public static final String URL_RESOURCE_VALIDATE_NAME = "/emc-metalnx-web/resources/isValidResourceName/";

	public static final String URL_SPECIFIC_QUERIES_MANAGEMENT = "/specificqueries/";
	public static final String URL_ADD_SPECIFIC_QUERY_PAGE = "/specificqueries/add/";
	public static final String URL_ADD_SPECIFIC_QUERY = "/specificqueries/add/action/";
	public static final String URL_MODIFY_SPECIFIC_QUERY_PAGE = "/emc-metalnx-web/specificqueries/modify/";
	public static final String URL_MODIFY_SPECIFIC_QUERY = "/specificqueries/modify/action/";
	public static final String URL_DELETE_SPECIFIC_QUERY = "/emc-metalnx-web/specificqueries/remove/";
	public static final String URL_SPECIFIC_QUERY_VALIDATE = "/emc-metalnx-web/specificqueries/validate/";

	public static final String URL_TRASH_COLLECTION_USER = "/trash/getTrash/";	
	public static final String URL_PUBLIC_COLLECTION_USER = "/public/getPublic/";
	
	public static final String URL_HOME_COLLECTION_USER = "/browse/home/";	
	public static final String URL_ADD_COLLECTION_USER = "/emc-metalnx-web/browse/add/";
	public static final String URL_MODIFY_COLLECTION_USER = "/emc-metalnx-web/fileOperation/modify/";
	public static final String URL_DELETE_COLLECTION_USER = "/fileOperation/delete/";
	public static final String URL_COLLECTION_VALIDATE_NAME_USER = "/emc-metalnx-web/browse/isValidCollectionName/";
	public static final String URL_LOGOUT = "/logout/";

	public static final String URL_TICKETS = "/tickets/";
	public static final String URL_TICKETS_FORM = "/emc-metalnx-web/tickets/ticketForm";
	public static final String URL_TICKETS_DELETE = "/emc-metalnx-web/tickets/";
	public static final String URL_TICKETS_CLIENT = "/ticketclient/";
}
