package com.emc.metalnx.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.query.MetaDataAndDomainData;
import org.irods.jargon.extensions.dataprofiler.DataProfile;
import org.irods.jargon.extensions.dataprofiler.DataProfilerFactory;
import org.irods.jargon.extensions.dataprofiler.DataProfilerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
//import org.springframework.mail.MailException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;

import com.emc.metalnx.core.domain.entity.IconObject;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.modelattribute.breadcrumb.DataGridBreadcrumb;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.IconService;
import com.emc.metalnx.services.interfaces.PermissionsService;
//import com.service.mail.config.ApplicationConfig;
//import com.service.mail.entity.Mail;
//import com.service.mail.services.MailService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@SessionAttributes({ "sourcePaths" })
@RequestMapping(value = "/collectionInfo")
public class CollectionInfoController {

	@Autowired
	CollectionService collectionService;

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
	ConfigService configService;

	private static final Logger logger = LoggerFactory.getLogger(CollectionInfoController.class);

	@RequestMapping(method = RequestMethod.GET)
	public String index(final Model model, HttpServletRequest request, @RequestParam("path") final String path)
			throws DataGridException, JargonException {

		logger.info("index()");
		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		logger.info("path:{}", path);

		String myPath = URLDecoder.decode(path);

		logger.info("decoded myPath:{}", myPath);

		IconObject icon = null;
		String mimeType = "";
		String template = "";

		boolean access = collectionService.canUserAccessThisPath(path);
		logger.info("Has Access :: {}", access);
		@SuppressWarnings("rawtypes")
		DataProfile dataProfile = null;
		if (access) {

			dataProfile = collectionService.getCollectionDataProfile(myPath);
			if (!dataProfile.isFile())
				template = "collections/collectionInfo";
			if (dataProfile.isFile())
				template = "collections/fileInfo";

		} else {

			if (!configService.getGlobalConfig().isHandleNoAccessViaProxy()) {
				template = "httpErrors/noAccess";
				logger.info("returning to :{}", template);
				return template;
			} else {
				logger.info("collection/file read only view");
				dataProfile = collectionService.getCollectionDataProfileAsProxyAdmin(myPath);
				template = "collections/readOnlyCollectionInfo";

				List<MetaDataAndDomainData> metadataList = dataProfile.getMetadata();
				model.addAttribute("dataGridMetadataList", metadataList);

			}

		}

		if (dataProfile != null && dataProfile.isFile()) {
			mimeType = dataProfile.getDataType().getMimeType();
		}
		icon = collectionService.getIcon(mimeType);

		model.addAttribute("icon", icon);
		model.addAttribute("dataProfile", dataProfile);
		model.addAttribute("breadcrumb", new DataGridBreadcrumb(dataProfile.getAbsolutePath()));

		logger.info("returning to :{}", template);

		return template;

	}


	@RequestMapping(value = "/collectionFileInfo/", method = RequestMethod.POST)
	public String getCollectionFileInfo(final Model model, @RequestParam("path") final String path)
			throws DataGridException {
		logger.info("CollectionInfoController getCollectionFileInfo() starts :: " + path);

		IconObject icon = null;
		String mimeType = "";

		@SuppressWarnings("rawtypes")
		String myPath = URLDecoder.decode(path);

		DataProfile dataProfile = collectionService.getCollectionDataProfile(myPath);

		if (dataProfile != null && dataProfile.isFile()) {
			mimeType = dataProfile.getDataType().getMimeType();
		}
		icon = collectionService.getIcon(mimeType);

		model.addAttribute("icon", icon);
		model.addAttribute("dataProfile", dataProfile);

		logger.info("getCollectionFileInfo() ends !!");
		return "collections/details :: detailsView";
	}

	private String extractFilePath(HttpServletRequest request) throws JargonException {
		String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
		try {
			path = URLDecoder.decode(path,
					this.getIrodsServices().getIrodsAccessObjectFactory().getJargonProperties().getEncoding());
		} catch (UnsupportedEncodingException | JargonException e) {
			logger.error("unable to decode path", e);
			throw new JargonException(e);
		}
		String bestMatchPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
		AntPathMatcher apm = new AntPathMatcher();
		return apm.extractPathWithinPattern(bestMatchPattern, path);
	}

	public DataProfilerFactory getDataProfilerFactory() {
		return dataProfilerFactory;
	}

	public void setDataProfilerFactory(DataProfilerFactory dataProfilerFactory) {
		this.dataProfilerFactory = dataProfilerFactory;
	}

	public IRODSServices getIrodsServices() {
		return irodsServices;
	}

	public void setIrodsServices(IRODSServices irodsServices) {
		this.irodsServices = irodsServices;
	}

	public DataProfilerSettings getDataProfilerSettings() {
		return dataProfilerSettings;
	}

	public void setDataProfilerSettings(DataProfilerSettings dataProfilerSettings) {
		this.dataProfilerSettings = dataProfilerSettings;
	}

	@RequestMapping(value = "/accessRequest", method = RequestMethod.GET)
	public String sendAccessRequest(final Model model, @RequestParam("path") final String path) {
		logger.info("requesting access : {}", path);
		String template = "";

		/*
		 * Mail mail = new Mail(); mail.setMailFrom("hetalben.patel@nih.gov");
		 * mail.setMailTo("hetalben.patel@nih.gov");
		 * mail.setMailSubject("DataCommons Access Request - Test");
		 * mail.setMailContent("This is a test email for granting an access on \n "+path
		 * +"!!!\n\nThanks\nXXX"); AbstractApplicationContext context = null; String
		 * emailResponse = ""; try { context = new
		 * AnnotationConfigApplicationContext(ApplicationConfig.class); MailService
		 * mailService = (MailService) context.getBean("mailService");
		 * mailService.sendEmail(mail); emailResponse =
		 * "Your request has been sent successfully.";
		 * model.addAttribute("emailResponse" , emailResponse); template =
		 * "collections/emailResponse"; }catch(MailException me) { me.printStackTrace();
		 * emailResponse = "Sorry, Email sending fail.Try again later!!";
		 * model.addAttribute("emailResponse" , emailResponse); template =
		 * "collections/emailResponse"; }finally { context.close(); }
		 */

		logger.info("Returning to template :: {}", template);

		return template;
	}

	public ConfigService getConfigService() {
		return configService;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

}
