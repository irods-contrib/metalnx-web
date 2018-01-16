package com.emc.metalnx.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/collectionInfo")
public class CollectionInfoController {

	private static final Logger logger = LoggerFactory.getLogger(MetadataController.class);

	@RequestMapping(value = "/**", method = RequestMethod.GET)
	public String getTestCollectionInfo(final Model model, HttpServletRequest request)
			throws DataGridConnectionRefusedException {

		return null;

	}

	private static String extractFilePath(HttpServletRequest request) {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		AntPathMatcher apm = new AntPathMatcher();
		return apm.extractPathWithinPattern(bestMatchPattern, path);
	}
}
