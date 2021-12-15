package com.emc.metalnx.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.datautils.filesampler.FileSamplerService;
import org.irods.jargon.extensions.dataprofiler.DataProfilerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.IconService;
import com.emc.metalnx.services.interfaces.PermissionsService;
import com.emc.metalnx.services.interfaces.PreviewService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
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
	ServletContext context;

	@Autowired
	PreviewService previewService;
	
	/*@Autowired
	FileSamplerService fileSamplerService;*/
	
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

	@RequestMapping(value = "/templateByMimeType", method = RequestMethod.GET)
	public String getTemplate(final HttpServletResponse response, @ModelAttribute("path") String path,
			@ModelAttribute("mimeType") String mimeType) throws JargonException {

		previewFilePath = URLDecoder.decode(path);
		previewMimeType = mimeType;

		String template = previewService.getTemplate(mimeType);

		logger.info("getTemplate for {} ::" + path + " and mimetype :: " + mimeType);
		return template;
	}

	@RequestMapping(value = "/dataObjectPreview", method = RequestMethod.GET)
	public void getPreview(final HttpServletResponse response) throws JargonException {
		previewService.filePreview(previewFilePath, previewMimeType, response);
	}
	
	@RequestMapping(value = "/permissionType", method = RequestMethod.GET, produces = "text/plain")
	@ResponseBody
	public String getPermission() throws JargonException {
		String permissionType = cs.getPermissionsForPath(previewFilePath);
		return permissionType;
	}

	@RequestMapping(value = "/save" , method = RequestMethod.POST)
	public String save(final Model model , @RequestParam("data") final String data) throws JargonException, 
	DataGridConnectionRefusedException {
			
		logger.info("saving file chnage for :: " +previewFilePath+ " , and data :: " +data);	
		FileSamplerService fileSamplerService =  irodsServices.getFileSamplerService();
		fileSamplerService.saveStringToFile(data, previewFilePath);
		
		model.addAttribute("success", true);
		return "collections/preview :: cmFilePreview";
	}
	
	@RequestMapping(value = "/saveCsv" , method = RequestMethod.POST)
	public String saveCsv(final Model model , @RequestBody String data) throws JargonException, 
	DataGridConnectionRefusedException {
			
		logger.info("saving file chnage for :: " +previewFilePath+ " , and data :: " +data);	
		
		/*MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		MultipartFile multipartFile = multipartRequest.getFile("file");
		
		logger.info("multipartFile:{}", multipartFile);*/
		
		byte[] bytes;
		
			/*bytes = multipartFile.getBytes();
			String completeData = new String(bytes);*/
	        
			FileSamplerService fileSamplerService =  irodsServices.getFileSamplerService();
			fileSamplerService.saveStringToFile(data, previewFilePath);
					
			model.addAttribute("success", true);
		
        
		return "collections/preview :: cmFilePreview";
	}

}
