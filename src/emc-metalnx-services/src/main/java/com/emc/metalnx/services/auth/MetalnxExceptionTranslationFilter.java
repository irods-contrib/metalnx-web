/**
 * 
 */
package com.emc.metalnx.services.auth;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.savedrequest.RequestCache;

/**
 * exception translation filter prevents caching of login exceptions overriding
 * the cached request
 * 
 * @author conwaymc
 *
 */

public class MetalnxExceptionTranslationFilter extends ExceptionTranslationFilter {

	private static final Logger logger = LoggerFactory.getLogger(MetalnxExceptionTranslationFilter.class);

	/**
	 * @param authenticationEntryPoint
	 */
	public MetalnxExceptionTranslationFilter(AuthenticationEntryPoint authenticationEntryPoint) {
		super(authenticationEntryPoint);
	}

	/**
	 * @param authenticationEntryPoint
	 * @param requestCache
	 */
	public MetalnxExceptionTranslationFilter(AuthenticationEntryPoint authenticationEntryPoint,
			RequestCache requestCache) {
		super(authenticationEntryPoint, requestCache);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.security.web.access.ExceptionTranslationFilter#doFilter(
	 * javax.servlet.ServletRequest, javax.servlet.ServletResponse,
	 * javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
			throws IOException, ServletException {
		logger.info("doFilter()");
		logger.info("request:{}", request);
		super.doFilter(request, response, filterChain);
	}

}
