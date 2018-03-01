package com.emc.metalnx.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.extensions.datatyper.DataTypeResolutionService;
import org.irods.jargon.extensions.datatyper.DataTypeResolutionServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emc.metalnx.core.domain.entity.enums.DataGridPermType;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.PreviewService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@SessionAttributes({ "sourcePaths" })
@RequestMapping(value = "/preview")
public class PreviewController {

	@Autowired
	PreviewService previewService;

	@Autowired
	IRODSServices irodsServices;

	@Autowired
	CollectionService collectionService;

	@Autowired
	DataTypeResolutionServiceFactory dataTypeResolutionServiceFactory;

	private static final Logger logger = LoggerFactory.getLogger(PreviewController.class);

	/**
	 * Responds the preview/ request
	 *
	 * @param model
	 * @return the collection management template
	 * @throws DataGridConnectionRefusedException
	 * @throws JargonException
	 * @throws DataGridException
	 */

	@RequestMapping(value = "/prepareForPreview", method = RequestMethod.GET)
	public String getPreview(final Model model, @RequestParam("path") final String path,
			RedirectAttributes redirectAttributes) throws DataGridException {

		logger.info("prepareForPreview for {} ::" + path);
		String mimeType = null;
		String template = null;
		boolean permission = previewService.getPermission(path);
		if(permission) {
			try {
				IRODSAccount irodsAccount = irodsServices.getUserAO().getIRODSAccount();
				DataTypeResolutionService dataTypeResolutionService = dataTypeResolutionServiceFactory
						.instanceDataTypeResolutionService(irodsAccount);

				logger.info("dataTypeResolutionService created from factory:{}", dataTypeResolutionService);

				logger.info("doing quick check for mime type");
				mimeType = dataTypeResolutionService.quickMimeType(path);
				logger.info("mimetype:{}", mimeType);

				redirectAttributes.addAttribute("path", path);

				if (mimeType.equalsIgnoreCase("image/png") || mimeType.equalsIgnoreCase("image/gif")
						|| mimeType.equalsIgnoreCase("image/jpeg") || mimeType.equalsIgnoreCase("image/jpg"))
					template = "redirect:/image/previewFilePath";
				else
					template = "collections/imagePreview :: noPreview";

				return template;
			} catch (JargonException e) {
				logger.error("Could not retrieve data from path: {}", path, e);
				throw new DataGridException(e.getMessage());
			} catch (Exception e) {
				logger.error("general exception generating preview", e);
				throw new DataGridException(e.getLocalizedMessage());
			}
		}else {			
			template = "collections/imagePreview :: noPermission";
			
			return template;
		}
		

	}

	public DataTypeResolutionServiceFactory getDataTypeResolutionServiceFactory() {
		return dataTypeResolutionServiceFactory;
	}

	public void setDataTypeResolutionServiceFactory(DataTypeResolutionServiceFactory dataTypeResolutionServiceFactory) {
		this.dataTypeResolutionServiceFactory = dataTypeResolutionServiceFactory;
	}

	/*
	 * @RequestMapping(value = "/prepareForPreview", method = RequestMethod.GET)
	 * public void prepareForPreview(@RequestParam("path") final String path) throws
	 * DataGridException {
	 * 
	 * String mimeType = null; IRODSAccount irodsAccount =
	 * irodsServices.getUserAO().getIRODSAccount(); DataTypeResolutionService
	 * dataTypeResolutionService =
	 * dataTypeResolutionServiceFactory.instanceDataTypeResolutionService(
	 * irodsAccount);
	 * 
	 * try { mimeType = dataTypeResolutionService.quickMimeType(path);
	 * logger.info("mimetype :: " +mimeType); } catch (JargonException e) {
	 * logger.error("Could not retrieve data from path: {}", path, e); throw new
	 * DataGridException(e.getMessage()); }
	 * 
	 * if(mimeType.equalsIgnoreCase("image/png") ||
	 * mimeType.equalsIgnoreCase("image/gif") ||
	 * mimeType.equalsIgnoreCase("image/jpeg") ||
	 * mimeType.equalsIgnoreCase("image/jpg")) getPreview(path); else noPreview();
	 * 
	 * }
	 * 
	 * public String noPreview() { return "collections/imagePreview :: noPreview"; }
	 * 
	 * 
	 * 
	 * //@RequestMapping(value = "/prepareForPreview", method = RequestMethod.GET)
	 * public String getPreview(String path) throws DataGridException{
	 * 
	 * previewFilePath = path; logger.info("prepareForPreview for {} ::" +path);
	 * 
	 * 
	 * return "redirect:/image/previewFilePath";
	 * 
	 * //return "collections/imagePreview :: imagePreview"; }
	 */

	// return "collections/imagePreview :: imagePreview";

	/*
	 * @RequestMapping(value = "/prepareForPreview", method = RequestMethod.GET)
	 * public void prepareForPreview(@RequestParam("path") final String path,
	 * RedirectAttributes redirectAttributes) throws DataGridException {
	 * 
	 * String mimeType = null; IRODSAccount irodsAccount =
	 * irodsServices.getUserAO().getIRODSAccount(); DataTypeResolutionService
	 * dataTypeResolutionService =
	 * dataTypeResolutionServiceFactory.instanceDataTypeResolutionService(
	 * irodsAccount);
	 * 
	 * try { mimeType = dataTypeResolutionService.quickMimeType(path);
	 * logger.info("mimetype :: " +mimeType); } catch (JargonException e) {
	 * logger.error("Could not retrieve data from path: {}", path, e); throw new
	 * DataGridException(e.getMessage()); }
	 * 
	 * if(mimeType.equalsIgnoreCase("image/png") ||
	 * mimeType.equalsIgnoreCase("image/gif") ||
	 * mimeType.equalsIgnoreCase("image/jpeg") ||
	 * mimeType.equalsIgnoreCase("image/jpg")) getPreview(path,redirectAttributes);
	 * else noPreview();
	 * 
	 * }
	 * 
	 * public String noPreview() { return "collections/imagePreview :: noPreview"; }
	 * 
	 * 
	 * 
	 * //@RequestMapping(value = "/prepareForPreview", method = RequestMethod.GET)
	 * public String getPreview(String path,RedirectAttributes redirectAttributes)
	 * throws DataGridException{
	 * 
	 * logger.info("prepareForPreview for {} ::" +path);
	 * redirectAttributes.addAttribute("path", path);
	 * 
	 * return "redirect:/image/previewFilePath";
	 * 
	 * //return "collections/imagePreview :: imagePreview"; }
	 */

}
