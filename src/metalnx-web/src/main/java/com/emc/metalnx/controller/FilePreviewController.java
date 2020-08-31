package com.emc.metalnx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/File")
public class FilePreviewController {

	private String previewFilePath;
	
	private static final Logger logger = LoggerFactory.getLogger(FilePreviewController.class);
	
	
	
}
