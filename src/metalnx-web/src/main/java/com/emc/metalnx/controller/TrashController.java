package com.emc.metalnx.controller;

import java.net.URLEncoder;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.HeaderService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.ResourceService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/trash")
public class TrashController {

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

	private static final Logger logger = LogManager.getLogger(TrashController.class);

	/**
	 * Responds the collections/trash request
	 *
	 * @param model
	 * @return the collection management template
	 * @throws DataGridException
	 */
	@RequestMapping(value = "/getTrash")
	public String trashCollection(final Model model) throws DataGridException {
		logger.info("trashCollection()");
		// cleaning session variables
		// sourcePaths.clear();

		if (userTrashPath == null || userTrashPath.equals("")) {
			userTrashPath = String.format("/%s/trash/home/%s", irodsServices.getCurrentUserZone(),
					irodsServices.getCurrentUser());
		}
		currentPath = userTrashPath;
		parentPath = currentPath;

		model.addAttribute("currentPath", currentPath);

		return "redirect:/collections?path=" + currentPath;
	}
}
