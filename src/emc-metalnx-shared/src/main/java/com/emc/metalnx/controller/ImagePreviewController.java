package com.emc.metalnx.controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.extensions.dataprofiler.DataProfilerFactory;
import org.irods.jargon.extensions.dataprofiler.DataProfilerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.IconService;
import com.emc.metalnx.services.interfaces.PermissionsService;
import com.emc.metalnx.services.interfaces.PreviewService;

@Controller
@RequestMapping(value = "/image")
public class ImagePreviewController {
	
	@Autowired
	CollectionService cs;

	@Autowired
	PermissionsService permissionsService;

	@Autowired
	DataProfilerFactory dataProfilerFactory;

	@Autowired
	IRODSServices irodsServices;

	@Autowired
	IconService iconService;

	@Autowired
	DataProfilerSettings dataProfilerSettings;

	@Autowired
	ServletContext context;

	@Autowired
	PreviewService previewService; 

	private String previewFilePath;

	private static final Logger logger = LoggerFactory.getLogger(ImagePreviewController.class);

	/**
	 * Responds the preview/ request
	 *
	 * @param model
	 * @return the collection management template
	 * @throws JargonException
	 * @throws DataGridException
	 */


	
	@RequestMapping(value = "/previewFilePath", method = RequestMethod.GET)
	public String getPreviewFilePath(final HttpServletResponse response,@ModelAttribute("path") String path) throws JargonException {		
		previewFilePath = path;		
		logger.info("getPreviewFilePath() for {} ::" +path);							
		return "collections/imagePreview :: imagePreview";
	}
	
	
	@RequestMapping(value = "/preview", method = RequestMethod.GET)
	public void getImagePreview(final HttpServletResponse response) throws JargonException {				
		logger.info("getImagePreview() for {} ::" +previewFilePath);		
		previewService.filePreview(previewFilePath, response);						
	}
	
	
}
