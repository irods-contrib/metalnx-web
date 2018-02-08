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

package com.emc.metalnx.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.utils.CollectionAndPath;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FavoritesService;
import com.emc.metalnx.services.interfaces.GroupBookmarkService;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.MetadataService;
import com.emc.metalnx.services.interfaces.PermissionsService;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.RuleDeploymentService;
import com.emc.metalnx.services.interfaces.UserBookmarkService;
import com.emc.metalnx.services.interfaces.UserService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@SessionAttributes({ "sourcePaths" })
@RequestMapping(value = "/collections")
public class CollectionController {

	@Autowired
	CollectionService cs;

	@Autowired
	ResourceService resourceService;

	@Autowired
	UserService userService;

	@Autowired
	GroupService groupService;

	@Autowired
	GroupBookmarkService groupBookmarkService;

	@Autowired
	UserBookmarkService userBookmarkService;

	@Autowired
	MetadataService metadataService;

	@Autowired
	GroupBookmarkController groupBookmarkController;

	@Autowired
	PermissionsService permissionsService;

	@Autowired
	IRODSServices irodsServices;

	@Autowired
	FavoritesService favoritesService;

	@Autowired
	LoggedUserUtils loggedUserUtils;

	@Autowired
	RuleDeploymentService ruleDeploymentService;

	// parent path of the current directory in the tree view
	private String parentPath;

	// path to the current directory in the tree view
	private String currentPath;

	// Auxiliary structure to manage download, upload, copy and move operations
	private List<String> sourcePaths;

	// ui mode that will be shown when the rods user switches mode from admin to
	// user and vice-versa
	public static final String UI_USER_MODE = "user";
	public static final String UI_ADMIN_MODE = "admin";

	public static final int MAX_HISTORY_SIZE = 10;

	private boolean cameFromMetadataSearch;
	private boolean cameFromFilePropertiesSearch;
	private boolean cameFromBookmarks;

	private static final Logger logger = LoggerFactory.getLogger(CollectionController.class);

	@PostConstruct
	public void init() throws DataGridException {

		cameFromMetadataSearch = false;
		cameFromFilePropertiesSearch = false;
		cameFromBookmarks = false;

	}

	/**
	 * Responds the collections/ request
	 *
	 * @param model
	 * @return the collection management template
	 * @throws JargonException
	 * @throws DataGridException
	 */
	@RequestMapping(value = "/**", method = RequestMethod.GET)
	public String indexViaUrl(final Model model, final HttpServletRequest request) {
		logger.info("index()");

		try {
			final String path = "/" + extractFilePath(request);
			logger.info("path ::" + path);

			DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
			String uiMode = (String) request.getSession().getAttribute("uiMode");

			sourcePaths = MiscIRODSUtils.breakIRODSPathIntoComponents(path);
			CollectionAndPath collectionAndPath = MiscIRODSUtils.separateCollectionAndPathFromGivenAbsolutePath(path);
			this.parentPath = collectionAndPath.getCollectionParent();
			this.currentPath = path;

			if (uiMode == null || uiMode.isEmpty()) {
				boolean isUserAdmin = loggedUser != null && loggedUser.isAdmin();
				uiMode = isUserAdmin ? UI_ADMIN_MODE : UI_USER_MODE;
			}

			/*
			 * See if it's a file or coll. A file redirects to the info page
			 * 
			 */

			if (cs.isDataObject(path)) {
				logger.info("redirect to info page");
				StringBuilder sb = new StringBuilder();
				sb.append("redirect:/collectionInfo");
				sb.append(path);
				return sb.toString();
			}

			if (uiMode.equals(UI_USER_MODE)) {
				model.addAttribute("homePath", cs.getHomeDirectyForCurrentUser());
				model.addAttribute("publicPath", cs.getHomeDirectyForPublic());
			}

			model.addAttribute("cameFromFilePropertiesSearch", cameFromFilePropertiesSearch);
			model.addAttribute("cameFromMetadataSearch", cameFromMetadataSearch);
			model.addAttribute("cameFromBookmarks", cameFromBookmarks);
			model.addAttribute("uiMode", uiMode);
			model.addAttribute("currentPath", currentPath);
			model.addAttribute("parentPath", parentPath);
			model.addAttribute("resources", resourceService.findAll());
			model.addAttribute("overwriteFileOption", loggedUser != null && loggedUser.isForceFileOverwriting());
		} catch (JargonException | DataGridException e) {
			logger.error("error establishing collection location", e);
			model.addAttribute("unexpectedError", true);
		}

		cameFromMetadataSearch = false;
		cameFromFilePropertiesSearch = false;
		cameFromBookmarks = false;

		logger.info("returning to collections/collectionManagement");

		return "collections/collectionManagement";

	}

	/**
	 * Legacy index method used in other controllers, eventually factor out TODO:
	 * factor this out and make explicit via urls etc - mcc
	 * 
	 * @param model
	 * @param request
	 * @return
	 */
	public String index(final Model model, final HttpServletRequest request) {
		logger.info("index()");
		try {
			sourcePaths.clear();

			if (!cs.isPathValid(currentPath)) {
				currentPath = cs.getHomeDirectyForCurrentUser();
				parentPath = currentPath;
			} else if (cs.isDataObject(currentPath)) {
				parentPath = currentPath.substring(0, currentPath.lastIndexOf("/") + 1);
			}

			DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
			String uiMode = (String) request.getSession().getAttribute("uiMode");

			if (uiMode == null || uiMode.isEmpty()) {
				boolean isUserAdmin = loggedUser != null && loggedUser.isAdmin();
				uiMode = isUserAdmin ? UI_ADMIN_MODE : UI_USER_MODE;
			}

			if (uiMode.equals(UI_USER_MODE)) {
				model.addAttribute("homePath", cs.getHomeDirectyForCurrentUser());
				model.addAttribute("publicPath", cs.getHomeDirectyForPublic());
			}

			model.addAttribute("cameFromFilePropertiesSearch", cameFromFilePropertiesSearch);
			model.addAttribute("cameFromMetadataSearch", cameFromMetadataSearch);
			model.addAttribute("cameFromBookmarks", cameFromBookmarks);
			model.addAttribute("uiMode", uiMode);
			model.addAttribute("currentPath", currentPath);
			model.addAttribute("parentPath", parentPath);
			model.addAttribute("resources", resourceService.findAll());
			model.addAttribute("overwriteFileOption", loggedUser != null && loggedUser.isForceFileOverwriting());

			cameFromMetadataSearch = false;
			cameFromFilePropertiesSearch = false;
			cameFromBookmarks = false;
		} catch (DataGridException e) {
			logger.error("Could not respond to request for collections: {}", e);
			model.addAttribute("unexpectedError", true);
		}
		logger.info("returning to collections/collectionManagement");
		return "collections/collectionManagement";
	}

	@RequestMapping(value = "redirectFromMetadataToCollections/")
	@ResponseStatus(value = HttpStatus.OK)
	public void redirectFromMetadataToCollections(@RequestParam final String path) {
		assignNewValuesToCurrentAndParentPath(path);
		cameFromMetadataSearch = true;
	}

	@RequestMapping(value = "redirectFromFavoritesToCollections/")
	@ResponseStatus(value = HttpStatus.OK)
	public void redirectFromFavoritesToCollections(@RequestParam final String path) {
		assignNewValuesToCurrentAndParentPath(path);
	}

	@RequestMapping(value = "redirectFromGroupsBookmarksToCollections/")
	@ResponseStatus(value = HttpStatus.OK)
	public void redirectFromGroupsBookmarksToCollections(@RequestParam final String path) {
		cameFromBookmarks = true;
		assignNewValuesToCurrentAndParentPath(path);
	}

	@RequestMapping(value = "redirectFromUserBookmarksToCollections/")
	@ResponseStatus(value = HttpStatus.OK)
	public void redirectFromUserBookmarksToCollections(@RequestParam final String path) {
		cameFromBookmarks = true;
		assignNewValuesToCurrentAndParentPath(path);
	}

	@RequestMapping(value = "redirectFromFilePropertiesToCollections/")
	@ResponseStatus(value = HttpStatus.OK)
	public void redirectFromFilePropertiesToCollections(@RequestParam final String path) {
		assignNewValuesToCurrentAndParentPath(path);
		cameFromFilePropertiesSearch = true;
	}

	/**
	 * Sets the current path and parent path based on a given path.
	 *
	 * @param path
	 *            new path to update current path and parent path
	 */
	private void assignNewValuesToCurrentAndParentPath(final String path) {
		if (path == null || path.isEmpty()) {
			return;
		}

		currentPath = path;
		parentPath = currentPath.substring(0, currentPath.lastIndexOf("/") + 1);
	}

	/**
	 * TODO: refactor into a service object, including obtaining the encoding - mcc
	 * 
	 * @param request
	 * @return
	 * @throws JargonException
	 */
	private String extractFilePath(HttpServletRequest request) throws JargonException {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		try {
			path = URLDecoder.decode(path,
					this.getIrodsServices().getIrodsAccessObjectFactory().getJargonProperties().getEncoding());
		} catch (UnsupportedEncodingException | JargonException e) {
			logger.error("unable to decode path", e);
			throw new JargonException(e);
		}
		String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		AntPathMatcher apm = new AntPathMatcher();
		return apm.extractPathWithinPattern(bestMatchPattern, path);
	}

	public ResourceService getResourceService() {
		return resourceService;
	}

	public void setResourceService(ResourceService resourceService) {
		this.resourceService = resourceService;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public GroupService getGroupService() {
		return groupService;
	}

	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public GroupBookmarkService getGroupBookmarkService() {
		return groupBookmarkService;
	}

	public void setGroupBookmarkService(GroupBookmarkService groupBookmarkService) {
		this.groupBookmarkService = groupBookmarkService;
	}

	public UserBookmarkService getUserBookmarkService() {
		return userBookmarkService;
	}

	public void setUserBookmarkService(UserBookmarkService userBookmarkService) {
		this.userBookmarkService = userBookmarkService;
	}

	public MetadataService getMetadataService() {
		return metadataService;
	}

	public void setMetadataService(MetadataService metadataService) {
		this.metadataService = metadataService;
	}

	public GroupBookmarkController getGroupBookmarkController() {
		return groupBookmarkController;
	}

	public void setGroupBookmarkController(GroupBookmarkController groupBookmarkController) {
		this.groupBookmarkController = groupBookmarkController;
	}

	public PermissionsService getPermissionsService() {
		return permissionsService;
	}

	public void setPermissionsService(PermissionsService permissionsService) {
		this.permissionsService = permissionsService;
	}

	public IRODSServices getIrodsServices() {
		return irodsServices;
	}

	public void setIrodsServices(IRODSServices irodsServices) {
		this.irodsServices = irodsServices;
	}

	public FavoritesService getFavoritesService() {
		return favoritesService;
	}

	public void setFavoritesService(FavoritesService favoritesService) {
		this.favoritesService = favoritesService;
	}

	public LoggedUserUtils getLoggedUserUtils() {
		return loggedUserUtils;
	}

	public void setLoggedUserUtils(LoggedUserUtils loggedUserUtils) {
		this.loggedUserUtils = loggedUserUtils;
	}

	public RuleDeploymentService getRuleDeploymentService() {
		return ruleDeploymentService;
	}

	public void setRuleDeploymentService(RuleDeploymentService ruleDeploymentService) {
		this.ruleDeploymentService = ruleDeploymentService;
	}

	public String getCurrentPath() {
		return currentPath;
	}

	public void setCurrentPath(String currentPath) {
		this.currentPath = currentPath;
	}

}
