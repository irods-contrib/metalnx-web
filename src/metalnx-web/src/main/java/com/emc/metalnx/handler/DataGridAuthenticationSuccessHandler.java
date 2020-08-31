/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.handler;

import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

public class DataGridAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

	private DataGridAuthenticationSuccessHandler() {
		super();
		setUseReferer(true);
	}

}
