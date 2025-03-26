/**
 * 
 */
package com.emc.metalnx.controller;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.emc.metalnx.controller.api.SearchApiController;
import com.emc.metalnx.core.domain.exceptions.DataGridException;

/**
 * Support for thymeleaf page handling for search. Actual api used is in the
 * {@link SearchApiController}
 * 
 * @author Mike Conway - NIEHS
 *
 */

@Controller
//@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/gallery")
public class GalleryController {

	private static final Logger log = LogManager.getLogger(GalleryController.class);

	@Value("${pluggablesearch.enabled}")
	private boolean pluggableSearchEnabled = false;

	@RequestMapping(method = RequestMethod.GET)
	public String index(Model model) throws DataGridException {
		log.info("index()");

		return "browsev2/galleryMain";
	}

}
