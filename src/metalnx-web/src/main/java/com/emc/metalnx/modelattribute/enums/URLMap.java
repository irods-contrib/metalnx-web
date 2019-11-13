 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


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
	public static final String URL_USER_PROFILE_VALIDATE_PROFILE_NAME = "/metalnx/users/profile/isValidProfileName/";
	public static final String URL_USER_PROFILES_CSV_REPORT = "/users/profile/profilesToCSVFile/";

	public static final String URL_REMOVE_USER_PROFILE = "/metalnx/users/profile/remove/";
	public static final String URL_MODIFY_USER_PROFILE = "/metalnx/users/profile/modify/";
	public static final String URL_USER_VALIDATE_USERNAME = "/metalnx/users/isValidUsername/";

	public static final String URL_GROUPS_MANAGEMENT = "/groups/";
	public static final String URL_ADD_GROUP = "add/";
	public static final String URL_MODIFY_GROUP = "modify/";
	public static final String URL_DELETE_GROUP = "delete/";
	public static final String URL_GROUP_VALIDATE_GROUPNAME = "/metalnx/groups/isValidGroupname/";
	public static final String URL_GROUPS_CSV_REPORT = "/groups/groupsToCSVFile/";

	public static final String URL_COLLECTIONS_INFO = "/info/";
	public static final String URL_COLLECTIONS_MANAGEMENT = "/collections/";
	public static final String URL_ADD_COLLECTION = "/metalnx/browse/add/";
	public static final String URL_MODIFY_COLLECTION = "/metalnx/fileOperation/modify/";
	public static final String URL_DELETE_COLLECTION = "/metalnx/fileOperation/delete/";
	public static final String URL_COLLECTION_VALIDATE_NAME = "/metalnx/browse/isValidCollectionName/";

	public static final String URL_METADATA_SEARCH = "/metadata/";

	public static final String URL_TEMPLATE_MANAGEMENT = "/templates/";
	public static final String URL_ADD_TEMPLATE = "add/";
	public static final String URL_MODIFY_TEMPLATE = "modify/";
	public static final String URL_DELETE_TEMPLATE = "delete/";
	public static final String URL_DELETE_TEMPLATE_FIELD_FROM_DB = "/metalnx/templates/removeFieldFromDB";
	public static final String URL_DELETE_TEMPLATE_FIELD = "/metalnx/templates/removeFieldFromTemplate";
	public static final String URL_TEMPLATE_VALIDATE_NAME = "/metalnx/templates/isValidTemplateName/";
	public static final String URL_EXPORT_TEMPLATE_XML = "exportTemplatesToXMLFile/";

	public static final String URL_RESOURCES_MANAGEMENT = "/resources/";
	public static final String URL_ADD_RESOURCE = "/resources/add/";
	public static final String URL_ADD_RESOURCE_ACTION = "/resources/add/action/";
	public static final String URL_MODIFY_RESOURCE = "/resources/modify/";
	public static final String URL_MODIFY_RESOURCE_ACTION = "/resources/modify/action/";
	public static final String URL_DELETE_RESOURCE = "/metalnx/resources/delete/";
	public static final String URL_RESOURCES_SERVERS = "/resources/servers/";
	public static final String URL_RESOURCES_MAP = "/resources/map/";
	public static final String URL_RESOURCE_VALIDATE_NAME = "/metalnx/resources/isValidResourceName/";

	public static final String URL_SPECIFIC_QUERIES_MANAGEMENT = "/specificqueries/";
	public static final String URL_ADD_SPECIFIC_QUERY_PAGE = "/specificqueries/add/";
	public static final String URL_ADD_SPECIFIC_QUERY = "/specificqueries/add/action/";
	public static final String URL_MODIFY_SPECIFIC_QUERY_PAGE = "/metalnx/specificqueries/modify/";
	public static final String URL_MODIFY_SPECIFIC_QUERY = "/specificqueries/modify/action/";
	public static final String URL_DELETE_SPECIFIC_QUERY = "/metalnx/specificqueries/remove/";
	public static final String URL_SPECIFIC_QUERY_VALIDATE = "/metalnx/specificqueries/validate/";

	public static final String URL_TRASH_COLLECTION_USER = "/trash/getTrash/";	
	public static final String URL_PUBLIC_COLLECTION_USER = "/public/getPublic/";
	
	public static final String URL_HOME_COLLECTION_USER = "/browse/home/";	
	public static final String URL_ADD_COLLECTION_USER = "/metalnx/browse/add/";
	public static final String URL_MODIFY_COLLECTION_USER = "/metalnx/fileOperation/modify/";
	public static final String URL_DELETE_COLLECTION_USER = "/fileOperation/delete/";
	public static final String URL_COLLECTION_VALIDATE_NAME_USER = "/metalnx/browse/isValidCollectionName/";
	public static final String URL_LOGOUT = "/logout/";

	public static final String URL_TICKETS = "/tickets/";
	public static final String URL_TICKETS_FORM = "/metalnx/tickets/ticketForm";
	public static final String URL_TICKETS_DELETE = "/metalnx/tickets/";
	public static final String URL_TICKETS_CLIENT = "/ticketclient/";
}
