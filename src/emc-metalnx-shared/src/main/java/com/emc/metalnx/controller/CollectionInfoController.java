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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;


@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/collectionInfo")
public class CollectionInfoController {
	
	@Autowired
	private CollectionController collectionController;
	
	@Autowired
	private MetadataController metadataController;
	
	@Autowired
	private PermissionsController permissionsController;
		
	private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);
	
	
	@RequestMapping(value = "/**", method = RequestMethod.GET)
	public String getTestCollectionInfo(final Model model, HttpServletRequest request) 
			throws DataGridConnectionRefusedException {
		
			
		logger.info("------CollectionInfoController getTestCollectionInfo() starts !!");
		final String path = "/"+extractFilePath(request);
		
		logger.info("path ::" + path) ;
		model.addAttribute("name", "This is comming from the CollectionInfoController() - Test the main controller");
		logger.info("------CollectionInfoController getTestCollectionInfo() ends !!");
		return metadataController.getMetadata(model, path);
		
	}
	
	@RequestMapping(value = "/collectionFileInfo/**", method = RequestMethod.GET)
	public String getCollectionFileInfo(final Model model, @RequestParam("path") final String path)
			throws DataGridConnectionRefusedException {		
		System.out.println("------CollectionInfoController getCollectionFileInfo() starts :: " +path);
		
		model.addAttribute("name", "Info");
		return "collections/info";
	}
	
	@RequestMapping(value = "/collectionMetadata/**", method = RequestMethod.GET)
	public String getCollectionMetadata(final Model model, @RequestParam("path") final String path)
			throws DataGridConnectionRefusedException {

		System.out.println("------CollectionInfoController collectionMetadata() starts :: " +path);
		
		model.addAttribute("name", "Metadata");
		return "collections/info";
	}
	
	@RequestMapping(value = "/collectionPermisssionDetails/", method = RequestMethod.GET)
	public String getCollectionPermissionDetails(final Model model, @RequestParam("path") final String path) 
			throws DataGridConnectionRefusedException {
		
		System.out.println("------CollectionInfoController collectionPermisssionDetails() starts :: " +path);
		
		model.addAttribute("name", "Permission");
		
		return "collections/info";
	}
	
	private static String extractFilePath(HttpServletRequest request) {
        String path = (String) request.getAttribute(
                HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchPattern = (String) request.getAttribute(
                HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        AntPathMatcher apm = new AntPathMatcher(); 
        return apm.extractPathWithinPattern(bestMatchPattern, path);
    }
}
