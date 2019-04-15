package com.emc.metalnx.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

public class Http403ForbiddenEntryPoint implements AuthenticationEntryPoint {
	private static final Logger logger = LoggerFactory.getLogger(Http403ForbiddenEntryPoint.class);

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2)
			throws IOException, ServletException {
		logger.info("Http403ForbiddenEntryPoint : commence");
		logger.debug("Pre-authenticated entry point called. Rejecting access");

		logger.info("request url was:{}", request.getRequestURL());

		SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
		logger.info("last saved request was:{}", savedRequest);

		response.sendRedirect("/metalnx/login/exception/");
	}

}
