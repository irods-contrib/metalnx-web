 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.utils.JargonVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.modelattribute.enums.URLMap;
import com.emc.metalnx.services.auth.UserTokenDetails;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.UserService;
import com.emc.metalnx.utils.EmcMetalnxVersion;

/**
 * Class that will intercept HTTP responses to clients. Metalnx will use it to
 * close sessions in the grid and add objects pertinent to every response.
 */
public class HttpResponseHandlerInterceptor extends HandlerInterceptorAdapter {

	@Autowired
	private IRODSAccessObjectFactory irodsAccessObjectFactory;
	@Autowired
	private ConfigService configService;
	@Autowired
	private UserService userService;
	@Autowired
	LoggedUserUtils loggedUserUtils;

	private UserTokenDetails userTokenDetails;
	private URLMap urlMap;
	private EmcMetalnxVersion emcmetalnxVersion;

	public final static Logger logger = LoggerFactory.getLogger(HttpResponseHandlerInterceptor.class);

	@Override
	public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
			final ModelAndView modelAndView) throws Exception {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (modelAndView != null && auth != null) {
			if (urlMap == null) {
				urlMap = new URLMap();
			}
			if (emcmetalnxVersion == null) {
				emcmetalnxVersion = new EmcMetalnxVersion();
			}

			modelAndView.getModelMap().addAttribute("urlMap", urlMap);
			modelAndView.getModelMap().addAttribute("emcmetalnxVersion", emcmetalnxVersion);
			modelAndView.getModelMap().addAttribute("jargonVersion", new JargonVersion());
			modelAndView.getModelMap().addAttribute("globalConfig", configService.getGlobalConfig());

			DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();

			logger.debug("added user prefs to model as 'dataGridUser':{}", loggedUser);

			modelAndView.getModelMap().addAttribute("dataGridUser", loggedUser);

			if (auth instanceof UsernamePasswordAuthenticationToken) {
				userTokenDetails = (UserTokenDetails) auth.getDetails();
				modelAndView.getModelMap().addAttribute("userDetails", userTokenDetails.getUser());
			}
		}

		// closing sessions to avoid idle agents
		irodsAccessObjectFactory.closeSessionAndEatExceptions();
	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public void setIrodsAccessObjectFactory(IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	public ConfigService getConfigService() {
		return configService;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}
