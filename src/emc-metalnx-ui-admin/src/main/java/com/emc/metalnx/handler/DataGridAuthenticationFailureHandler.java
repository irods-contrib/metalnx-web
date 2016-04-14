package com.emc.metalnx.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.web.servlet.LocaleResolver;

import com.emc.metalnx.services.exceptions.DataGridAuthenticationException;
import com.emc.metalnx.services.interfaces.UserService;

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

		//if we could not connect to the iCAT Server
		if(exception instanceof DataGridAuthenticationException) {
			logger.error("Server not respoding.", exception.getMessage());
			
			response.sendRedirect("/emc-metalnx-web/login/serverNotResponding/");
		}
		else {
			response.sendRedirect("/emc-metalnx-web/login/exception/");			
		}
	}

}
