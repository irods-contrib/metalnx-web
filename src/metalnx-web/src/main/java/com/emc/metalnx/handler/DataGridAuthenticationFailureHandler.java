 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.handler;

import com.emc.metalnx.core.domain.exceptions.DataGridDatabaseException;
import com.emc.metalnx.core.domain.exceptions.DataGridServerException;
import com.emc.metalnx.services.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DataGridAuthenticationFailureHandler implements AuthenticationFailureHandler {

	@Autowired
	UserService userService;

	@Autowired
	LocaleResolver localeResolver;

	@Value("${irods.zoneName}")
	private String zoneName;

	private static final Logger logger = LoggerFactory.getLogger(DataGridAuthenticationFailureHandler.class);

	@Override
	public void onAuthenticationFailure(HttpServletRequest request,
			HttpServletResponse response, AuthenticationException exception)
			throws IOException, ServletException {
		logger.info("DataGridAuthenticationFailureHandler: onAuthenticationFailure()");
		
		//if we could not connect to the iCAT Server
		if (exception instanceof DataGridServerException) {
			logger.error("Server not respoding.", exception.getMessage());
			response.sendRedirect("/metalnx/login/serverNotResponding/");
		} else if (exception instanceof DataGridDatabaseException) {
			logger.error("Database not respoding.", exception.getMessage());
			response.sendRedirect("/metalnx/login/databaseNotResponding/");
		} else {
			response.sendRedirect("/metalnx/login/exception/");
		}
	}

}
