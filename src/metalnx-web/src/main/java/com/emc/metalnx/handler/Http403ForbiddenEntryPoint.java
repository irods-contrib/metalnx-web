package com.emc.metalnx.handler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

public class Http403ForbiddenEntryPoint implements AuthenticationEntryPoint {
	private static final Logger logger = LogManager.getLogger(Http403ForbiddenEntryPoint.class);

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException arg2)
			throws IOException, ServletException {
		logger.info("Http403ForbiddenEntryPoint : commence");
		logger.debug("Pre-authenticated entry point called. Rejecting access");

		logger.info("request url was:{}", request.getRequestURL());
		
		// Redirect to the login page without the exception message if
		// the URL path ends with /metalnx or /metalnx/.
		if (urlPathEndsWithAppRootName(request)) {
		    response.sendRedirect("/metalnx/login/");
		    return;
		}

		SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
		logger.info("last saved request was:{}", savedRequest);

		response.sendRedirect("/metalnx/login/exception/");
	}
	
	private boolean urlPathEndsWithAppRootName(HttpServletRequest request)
	{
	    try {
	        String p = new URL(request.getRequestURL().toString()).getPath();
	        return p.endsWith("/metalnx") || p.endsWith("/metalnx/");
	    }
	    catch (MalformedURLException e) {}
	    
	    return false;
	}

}
