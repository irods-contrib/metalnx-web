/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.handler;

import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

public class DataGridAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private DataGridAuthenticationSuccessHandler() {
		super();
		setUseReferer(true);
	}

}
