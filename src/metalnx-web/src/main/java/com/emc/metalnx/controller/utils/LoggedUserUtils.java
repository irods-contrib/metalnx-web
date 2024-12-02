/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.controller.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.services.interfaces.UserService;

@Service
public class LoggedUserUtils {

	private static final Logger logger = LoggerFactory.getLogger(LoggedUserUtils.class);

	@Autowired
	UserService userService;

	@Value("${irods.zoneName}")
	private String zoneName;

	public DataGridUser getLoggedDataGridUser() {
		logger.info("getLoggedDataGridUser()");

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			logger.warn("no user available");
			return null;
		}

		String username = (String) auth.getPrincipal();
		logger.info("auth:{}", auth);

		return userService.findByUsernameAndZone(username, zoneName);
	}
}
