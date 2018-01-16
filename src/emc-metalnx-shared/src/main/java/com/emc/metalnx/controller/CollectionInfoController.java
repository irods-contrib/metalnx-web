package com.emc.metalnx.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.PermissionsService;


@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@SessionAttributes({ "sourcePaths" })
@RequestMapping(value = "/collectionInfo")
public class CollectionInfoController {
	
	@Autowired
	private LoggedUserUtils loggedUserUtils;
	
	@Autowired
	CollectionService collectionService;
	
	@Autowired
	PermissionsService permissionsService;
		
	private static final Logger logger = LoggerFactory.getLogger(CollectionInfoController.class);
	
	
	@RequestMapping(value = "/**", method = RequestMethod.GET)
	public String getTestCollectionInfo(final Model model,  HttpServletRequest request) 
			throws DataGridConnectionRefusedException {
		
		logger.info("------CollectionInfoController getTestCollectionInfo() starts !!");				
		final String path = "/"+extractFilePath(request);		
		logger.info("path ::" + path) ;		
		model.addAttribute("summary", "This is comming from the CollectionInfoController() - Test the main controller");
		
		DataGridCollectionAndDataObject dgColObj = null;

		try {
			dgColObj = collectionService.findByName(path);
			permissionsService.resolveMostPermissiveAccessForUser(dgColObj, loggedUserUtils.getLoggedDataGridUser());
		} catch (DataGridException e) {
			logger.error("Could not retrieve collection/dataobject from path: {}", path);
		}
		
		model.addAttribute("collectionAndDataObject", dgColObj);
		if(dgColObj != null) 
			model.addAttribute("flag", true);
		else {
			model.addAttribute("flag", false);		
		}
		
		logger.info("------CollectionInfoController getTestCollectionInfo() ends !!");
		return "collections/info";		
		
	}
	
	/*
	@RequestMapping(value = "/collectionFileInfo/", method = RequestMethod.POST)
	public String getCollectionFileInfo(final Model model, @RequestParam("path") final String path)
			throws DataGridConnectionRefusedException {		
		
		logger.info("------CollectionInfoController getCollectionFileInfo() starts :: " +path);		
		DataGridUser loggedUser = LoggedUserUtils.getLoggedDataGridUser();
		
		
		logger.info("User First Name :: " +loggedUser.getUsername());
		
		model.addAttribute("infoName", "This is Info !!");
		return "collections/info";
		
	}*/
	
	/*@RequestMapping(value = "/collectionMetadata/", method = RequestMethod.POST)
	public String getCollectionMetadata(final Model model, @RequestParam("path") final String path)
			throws DataGridConnectionRefusedException {
		
		logger.info("-----------------------------getCollectionMetadata()-------------------------------- !!");
		logger.info("------CollectionInfoController collectionMetadata() starts :: " +path);
		
		model.addAttribute("metadataName", "This is Metadata !!");
		
		logger.info("MetadataName :: " +model.containsAttribute("MetadataName"));
		//return "collections/info";
		
		return "metadata/test";
		
	}
		
	@RequestMapping(value = "/collectionPermisssionDetails/", method = RequestMethod.POST)
	public String getCollectionPermissionDetails(final Model model, @RequestParam("path") final String path) 
			throws DataGridConnectionRefusedException {
		logger.info("-----------------------------getCollectionPermissionDetails()-------------------------------- !!");
		logger.info("------CollectionInfoController collectionPermisssionDetails() starts :: " +path);
		
		model.addAttribute("permissionName", "This is Permission !!");
		return "collections/info";
		
	}
*/
	private static String extractFilePath(HttpServletRequest request) {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(
                HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher(); 
        return apm.extractPathWithinPattern(bestMatchPattern, path);
    }
}
