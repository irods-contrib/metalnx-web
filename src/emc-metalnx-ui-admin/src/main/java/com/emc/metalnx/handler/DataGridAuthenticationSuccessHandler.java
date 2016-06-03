/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

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
