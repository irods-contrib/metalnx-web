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
