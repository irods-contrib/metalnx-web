/**
 * 
 */
package com.emc.metalnx.controller.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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

}
