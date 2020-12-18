/**
 * 
 */
package com.emc.metalnx.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.IRODSServices;

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

	@Autowired
	IRODSServices irodsServices;

	public IRODSServices getIrodsServices() {
		return irodsServices;
	}

	public void setIrodsServices(IRODSServices irodsServices) {
		this.irodsServices = irodsServices;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	@ResponseBody
	public String list(final Model model, @RequestParam("path") String path, int offset, int limit)
			throws DataGridException {

		log.info("list()");

		// List<GalleryEntry>() entries = galleryService.list(path, offset, limit);

		// return ObjectMapper.write(entries);
		return null;
	}

}
