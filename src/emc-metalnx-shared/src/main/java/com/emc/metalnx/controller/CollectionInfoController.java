package com.emc.metalnx.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.IRODSDomainObject;
import org.irods.jargon.extensions.dataprofiler.DataProfile;
import org.irods.jargon.extensions.dataprofiler.DataProfilerFactory;
import org.irods.jargon.extensions.dataprofiler.DataProfilerService;
import org.irods.jargon.extensions.dataprofiler.DataProfilerSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;

import com.emc.metalnx.core.domain.entity.IconObject;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.modelattribute.breadcrumb.DataGridBreadcrumb;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.IconService;
import com.emc.metalnx.services.interfaces.PermissionsService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@SessionAttributes({ "sourcePaths" , "topnavHeader" })
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

	private static final Logger logger = LoggerFactory.getLogger(CollectionInfoController.class);

	@RequestMapping(value = "/**", method = RequestMethod.GET)
	public String getTestCollectionInfo(final Model model, HttpServletRequest request)
			throws DataGridException, DataGridConnectionRefusedException, JargonException {

		logger.info("CollectionInfoController getTestCollectionInfo() starts !!");
		final String path = "/" + extractFilePath(request);
		IconObject icon = null;
		String mimeType = "" ;
		String template = "";
				
		@SuppressWarnings("rawtypes")
		DataProfile dataProfile = collectionService.getCollectionDataProfile(path);
				
		if (dataProfile != null && dataProfile.isFile()) {				
			mimeType = dataProfile.getDataType().getMimeType();
		}		
		icon = collectionService.getIcon(mimeType);
		
		model.addAttribute("icon", icon);
		model.addAttribute("dataProfile", dataProfile);
		model.addAttribute("breadcrumb", new DataGridBreadcrumb(dataProfile.getAbsolutePath()));
		
		if (!dataProfile.isFile())
			template = "collections/collectionInfo";
		if (dataProfile.isFile())
			template = "collections/fileInfo";

		return template;

	}

	@RequestMapping(value = "/collectionFileInfo/", method = RequestMethod.POST)
	public String getCollectionFileInfo(final Model model, @RequestParam("path") final String path)
			throws DataGridException {
		logger.info("CollectionInfoController getCollectionFileInfo() starts :: " + path);

		IconObject icon = null;
		String mimeType = "" ;
		
		@SuppressWarnings("rawtypes")
		DataProfile dataProfile = collectionService.getCollectionDataProfile(path);		
		
		if (dataProfile != null && dataProfile.isFile()) {				
			mimeType = dataProfile.getDataType().getMimeType();
		}		
		icon = collectionService.getIcon(mimeType);
	
		model.addAttribute("icon", icon);
		model.addAttribute("dataProfile", dataProfile);

		logger.info("getCollectionFileInfo() ends !!");
		return "collections/info :: infoView";
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
}
