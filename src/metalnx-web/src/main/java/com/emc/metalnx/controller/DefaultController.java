/**
 * 
 */
package com.emc.metalnx.controller;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.HeaderService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserService;

/**
 * @author Mike Conway - NIEHS
 *
 */
@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/")
public class DefaultController {

	@Autowired
	UserService userService;

	@Autowired
	IRODSServices irodsServices;

	@Autowired
	LoggedUserUtils loggedUserUtils;

	@Autowired
	HeaderService headerService;

	@Autowired
	ConfigService configService;

	private static final Logger logger = LogManager.getLogger(DefaultController.class);

	/**
	 * 
	 */
	public DefaultController() {
	}

	@RequestMapping(method = RequestMethod.GET)
	public String index() {
		logger.info("DefaultController index()");
		if (loggedUserUtils.getLoggedDataGridUser().isAdmin()) {
			if (configService.isDashboardEnabled()) {
				return "redirect:/dashboard/";
			} else {
				return "redirect:/browse/home";
			}
		} else {
			return "redirect:/browse/home";
		}
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public IRODSServices getIrodsServices() {
		return irodsServices;
	}

	public void setIrodsServices(IRODSServices irodsServices) {
		this.irodsServices = irodsServices;
	}

	public LoggedUserUtils getLoggedUserUtils() {
		return loggedUserUtils;
	}

	public void setLoggedUserUtils(LoggedUserUtils loggedUserUtils) {
		this.loggedUserUtils = loggedUserUtils;
	}

	public HeaderService getHeaderService() {
		return headerService;
	}

	public void setHeaderService(HeaderService headerService) {
		this.headerService = headerService;
	}

}
