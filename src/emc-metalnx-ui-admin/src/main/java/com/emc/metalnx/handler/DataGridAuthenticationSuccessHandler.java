/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public class DataGridAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private static final Logger logger = LoggerFactory.getLogger(DataGridAuthenticationSuccessHandler.class);
	/*
	 * @Override public void onAuthenticationSuccess(HttpServletRequest request,
	 * HttpServletResponse response, Authentication authentication) throws
	 * IOException, ServletException {
	 * 
	 * logger.debug("Authentication successful!");
	 * 
	 * String username = (String) authentication.getPrincipal();
	 * logger.debug("Applying user preferences to user [" + username + "]");
	 * 
	 * DataGridUser loggedUser =
	 * userService.findByUsernameAndAdditionalInfo(username, zoneName);
	 * localeResolver.setLocale(request, response,
	 * StringUtils.parseLocaleString(loggedUser.getLocale()));
	 * if(loggedUser.isAdmin()){
	 * response.sendRedirect("/emc-metalnx-web/dashboard/"); }else{
	 * response.sendRedirect("/emc-metalnx-web/browse/home"); }
	 * 
	 * }
	 */

	/**
	 * 
	 */
	private DataGridAuthenticationSuccessHandler() {
		super();
		setUseReferer(true);
	}

}
