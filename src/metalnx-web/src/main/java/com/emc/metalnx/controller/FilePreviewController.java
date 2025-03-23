package com.emc.metalnx.controller;

import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.exception.JargonException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/File")
public class FilePreviewController {

	private String previewFilePath;
	
	private static final Logger logger = LogManager.getLogger(FilePreviewController.class);
	
	
	
}
