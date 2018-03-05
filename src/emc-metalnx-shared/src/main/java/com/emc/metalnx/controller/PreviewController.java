package com.emc.metalnx.controller;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.extensions.dataprofiler.DataProfilerFactory;
import org.irods.jargon.extensions.dataprofiler.DataProfilerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.IconService;
import com.emc.metalnx.services.interfaces.PermissionsService;
import com.emc.metalnx.services.interfaces.PreviewService;

@Controller
@RequestMapping(value = "/preview")
public class PreviewController {

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
	private String previewMimeType;

	private static final Logger logger = LoggerFactory.getLogger(PreviewController.class);

	/**
	 * Responds the preview/ request
	 *
	 * @param model
	 * @return the collection management template
	 * @throws JargonException
	 * @throws DataGridException
	 */

	/*@RequestMapping(value = "/imagePreview", method = RequestMethod.GET)
	public String getImagePreview(final HttpServletResponse response, @ModelAttribute("path") String path,
			@ModelAttribute("mimeType") String mimeType)
			throws JargonException {
		previewFilePath = path;
		logger.info("getPreviewFilePath() for {} ::" + path);
		return "collections/imagePreview :: imagePreview";
	}
	
	@RequestMapping(value = "/FilePreview", method = RequestMethod.GET)
	public String testPreviewPath(final HttpServletResponse response, @ModelAttribute("path") String path,
			@ModelAttribute("mimeType") String mimeType)
			throws JargonException {
		previewFilePath = path;
		logger.info("getPreviewFilePath() for {} ::" + path);
		return "collections/imagePreview :: filePreview";
	}*/
	
	@RequestMapping(value = "/templateByMimeType", method = RequestMethod.GET)
	public String getTemplate(final HttpServletResponse response, @ModelAttribute("path") String path,
			@ModelAttribute("mimeType") String mimeType)
			throws JargonException {
		
		previewFilePath = path;
		previewMimeType = mimeType;
		
		String template = previewService.getTemplate(mimeType);
		
		logger.info("getTemplate for {} ::" + path + " and mimetype :: " +mimeType);
		return template;
	}
	

	@RequestMapping(value = "/fileObjectPreview", method = RequestMethod.GET)
	public void getPreview(final HttpServletResponse response) throws JargonException {
		previewService.filePreview(previewFilePath,previewMimeType, response);
	}

}
