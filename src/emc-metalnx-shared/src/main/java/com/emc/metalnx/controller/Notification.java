package com.emc.metalnx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/notification")
public class Notification {
	
	private static final Logger logger = LoggerFactory.getLogger(BrowseController.class);
	
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getAllNotifications(final Model model) {
		
		logger.info("calling notofocation services");
		return "notifications/notification";
	}

}
