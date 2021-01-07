/**
 * 
 */
package com.emc.metalnx.controller.api;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.extensions.thumbnail.GalleryListService;
import org.irods.jargon.extensions.thumbnail.ThumbnailList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.emc.metalnx.services.interfaces.IRODSServices;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Backend API support for search operations
 * 
 * @author Mike Conway - NIEHS
 *
 */

@Controller
//@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/api/gallery")
public class GalleryApiController {

	public static final Logger log = LoggerFactory.getLogger(GalleryApiController.class);
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	IRODSServices irodsServices;

	public IRODSServices getIrodsServices() {
		return irodsServices;
	}

	public void setIrodsServices(IRODSServices irodsServices) {
		this.irodsServices = irodsServices;
	}

	@RequestMapping(method = RequestMethod.GET)
	@ResponseBody
	public String list(@RequestParam("path") String path, @RequestParam("offset") int offset,
			@RequestParam("limit") int limit) throws JargonException {

		log.info("list()");

		if (path == null || path.isEmpty()) {
			throw new IllegalArgumentException("null or empty path");
		}

		log.info("path:{}", path);
		log.info("offset:{}", offset);
		log.info("limit:{}", limit);

		GalleryListService galleryListService = irodsServices.getGalleryListService();
		ThumbnailList thumbnailList = galleryListService.list(path, offset, limit);
		try {
			String retString = objectMapper.writeValueAsString(thumbnailList);
			return retString;
		} catch (JsonProcessingException e) {
			log.error("error deserializing:{}", thumbnailList, e);
			throw new JargonException("unable to serialize", e);

		}
	}

}
