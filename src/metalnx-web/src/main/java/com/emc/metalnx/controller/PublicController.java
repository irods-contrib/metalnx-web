package com.emc.metalnx.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.HeaderService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.ResourceService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@SessionAttributes({ "sourcePaths" })
@RequestMapping(value = "/public")
public class PublicController {
	@Autowired
	CollectionService cs;

	@Autowired
	IRODSServices irodsServices;

	@Autowired
	ResourceService resourceService;

	@Autowired
	HeaderService headerService;

	// parent path of the current directory in the tree view
	private String parentPath;

	// path to the current directory in the tree view
	private String currentPath;

	// Auxiliary structure to manage download, upload, copy and move operations
	private List<String> sourcePaths;

	// variable to save trash path for the logged user
	private String userTrashPath = "";

	private static final Logger logger = LoggerFactory.getLogger(PublicController.class);

	/**
	 * Responds the collections/public request
	 *
	 * @param model
	 * @return the collection management template
	 */
	@RequestMapping(value = "/getPublic")
	public String publicCollection(final Model model) throws DataGridException {
		logger.info("publicCollection()");
		// cleaning session variables
		// sourcePaths.clear();

		currentPath = cs.getHomeDirectyForPublic();
		parentPath = currentPath;
		return "redirect:/collections?path=" + currentPath;
	}
}
