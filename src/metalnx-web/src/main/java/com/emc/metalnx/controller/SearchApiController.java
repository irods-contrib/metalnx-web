/**
 * 
 */
package com.emc.metalnx.controller;

import org.irods.metalnx.pluggablesearch.PluggableSearchWrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Backend API support for search operations
 * 
 * @author Mike Conway - NIEHS
 *
 */

@Controller
//@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/api/search")
public class SearchApiController {

	public static final Logger log = LoggerFactory.getLogger(SearchApiController.class);

	@Autowired
	PluggableSearchWrapperService pluggableSearchWrapperService;

}
