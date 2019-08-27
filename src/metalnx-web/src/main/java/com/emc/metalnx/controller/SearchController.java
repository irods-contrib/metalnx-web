/**
 * 
 */
package com.emc.metalnx.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.emc.metalnx.controller.api.SearchApiController;

/**
 * Support for thymeleaf page handling for search. Actual api used is in the
 * {@link SearchApiController}
 * 
 * @author Mike Conway - NIEHS
 *
 */

@Controller
//@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/search")
public class SearchController {

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index(Model model) {
		return "search/searchMain";
	}

}
