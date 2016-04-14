package com.emc.metalnx.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.services.interfaces.UserService;

public class DataGridAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

	@Autowired
	UserService userService;
	
	@Autowired
	LocaleResolver localeResolver;
	
	@Value("${irods.zoneName}")
	private String zoneName;
	
	private static final Logger logger = LoggerFactory.getLogger(DataGridAuthenticationSuccessHandler.class);
	
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
			throws IOException, ServletException {
		
		logger.debug("Authentication successful!");
		
		String username = (String) authentication.getPrincipal();
		logger.debug("Applying user preferences to user [" + username + "]");
		
		DataGridUser loggedUser = userService.findByUsernameAndAdditionalInfo(username, zoneName);
		localeResolver.setLocale(request, response, StringUtils.parseLocaleString(loggedUser.getLocale()));
		response.sendRedirect("/emc-metalnx-web/dashboard/");

	}

}
