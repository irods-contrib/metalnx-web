/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.controller;

import org.irods.jargon.core.exception.JargonException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.IRODSServices;

/**
 * Actions on collection permission inheritance
 * 
 * @author conwaymc
 *
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping("/inheritance")
public class InheritanceController {

	@Autowired
	CollectionService cs;

	@Autowired
	IRODSServices irodsServices;

	@Autowired
	LoggedUserUtils loggedUserUtils;

	private static final Logger logger = LogManager.getLogger(InheritanceController.class);

	/**
	 * 
	 * @param targetPath
	 * @param inherit
	 * @param recursive
	 * @throws DataGridException
	 * @throws JargonException
	 */
	@RequestMapping(method = RequestMethod.POST)
	// @ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity<?> modify(Model model, @RequestParam("path") final String targetPath,
			@RequestParam("inherit") final boolean inherit, @RequestParam("recursive") final boolean recursive)
			throws DataGridException, JargonException {

		logger.info("modify()");

		// if (targetPath == null || targetPath.isEmpty()) {
		// throw new IllegalArgumentException("null targetPath");
		// }

		logger.info("targetPath:{}", targetPath);
		logger.info("inherit:{}", inherit);
		logger.info("recursive: {}", recursive);

		cs.modifyInheritance(targetPath, inherit, recursive);

		logger.info("inheritance set");
		return new ResponseEntity<>(HttpStatus.OK);

	}

	public CollectionService getCs() {
		return cs;
	}

	public void setCs(CollectionService cs) {
		this.cs = cs;
	}

	public IRODSServices getIrodsServices() {
		return irodsServices;
	}

	public void setIrodsServices(IRODSServices irodsServices) {
		this.irodsServices = irodsServices;
	}

	public LoggedUserUtils getLoggedUserUtils() {
		return loggedUserUtils;
	}

	public void setLoggedUserUtils(LoggedUserUtils loggedUserUtils) {
		this.loggedUserUtils = loggedUserUtils;
	}

}
