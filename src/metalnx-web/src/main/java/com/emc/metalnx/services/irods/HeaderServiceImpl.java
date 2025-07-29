package com.emc.metalnx.services.irods;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.entity.HeaderObject;
import com.emc.metalnx.core.domain.entity.IconObject;
import com.emc.metalnx.services.interfaces.HeaderService;

@Service
@Transactional
public class HeaderServiceImpl implements HeaderService{
	
	private static final Map<String,HeaderObject> headerMap = createMap();
	private static Map<String, HeaderObject> createMap()
	{
		Map<String,HeaderObject> headerMap = new HashMap<String, HeaderObject>();
		headerMap.put("dashboard", new HeaderObject("dashboard.page.title", "fa fa-tachometer" , "dashboard.popover"));
		headerMap.put("resources", new HeaderObject("resources.management.page.title", "fa fa-database" , "resources.management.popover"));
		headerMap.put("rules", new HeaderObject("rules.menu.title", "fa fa-file-text" , "rules.title.popover"));
		headerMap.put("users", new HeaderObject("users.management.page.title", "fa fa-user" , "users.management.page.title.popover"));
		headerMap.put("groups", new HeaderObject("groups.management.page.title", "fa fa-group" , "groups.management.page.title.popover"));
		headerMap.put("profiles", new HeaderObject("users.profile.management.page.title", "fa fa-user" , "users.profile.management.page.title.popover"));
		headerMap.put("collections", new HeaderObject("collections.management.page.title", "fa fa-folder" , "collection.title.popover"));
		headerMap.put("search", new HeaderObject("metadata.search.page.title", "fa fa-search" , "metadata.search.page.title.popover"));
		headerMap.put("template", new HeaderObject("metadata.template.management.page.title", "fa fa-cubes" , "metadata.template.management.page.title.popover"));
		headerMap.put("shared", new HeaderObject("group.collections.view.page.title", "fa fa-share-alt" , "group.collections.view.page.title.popover"));
		headerMap.put("favorite", new HeaderObject("favorites.page.title", "fa fa-star" , "favorites.page.title.popover"));
		headerMap.put("public", new HeaderObject("sidebar.user.public", "fa fa-globe" , "collection.title.popover"));
		headerMap.put("trash", new HeaderObject("sidebar.user.trash", "fa fa-trash" , "collection.title.popover"));
		headerMap.put("prefrences", new HeaderObject("user.preferences.label", "fa fa-gear fa-fw" , "prferences.title.popover"));
		
		return headerMap;
	}

	@Override
	public HeaderObject getheader(String name) {
		HeaderObject headerObj = null;
		
		if(headerMap.containsKey(name)) 
			headerObj = headerMap.get(name);			
		else
			headerObj = new HeaderObject("Unknown" , "fa fa-question-circle" , "Unknown");
		
		return headerObj;
	}

}
