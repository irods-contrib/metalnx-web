package com.emc.metalnx.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.datacommons.model.Notification;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.NotificationService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/notification")
public class NotificationController{
	
	private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);
	
	@Autowired
	private NotificationService notificationService;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getAllNotifications(Model model) {
		
		List<Notification> notifications = notificationService.getAllNotification("pateldes");
		logger.info("notification data size :: " + notifications.size());
		for(Notification ns : notifications) {
			logger.info("Subject :: " +ns.getSubject());
			logger.info("UUID :: " +ns.getUuid());
			
		}
		model.addAttribute("notifications", notifications);
		
		logger.info("calling notofocation services");
		return "notifications/notification";
	}
	
	@RequestMapping(value = "/unseen/", method = RequestMethod.GET)
	public ResponseEntity<Integer> getUnseen(Model model){
		
		Map<String, Integer> result = notificationService.getUnseenCounts("pateldes");		
		int unseenCounts = result.get("unseenCount");
		//model.addAttribute("unseenCounts", unseenCounts);
		logger.info("Unseen Notifications :: " + unseenCounts);
		ResponseEntity<Integer> responseEntity = new ResponseEntity<>(unseenCounts,HttpStatus.OK);
		return responseEntity;
	}

	@RequestMapping(value = "/markToSeen")
	public String markToSeen(final HttpServletRequest request, final Model model){

		Integer length = Integer.valueOf(request.getParameter("length"));
		List<String> list = new ArrayList<>();

		for (int i = 0; i < length; i++) {
			list.add(request.getParameter("params[" + i + "][uuid]"));
		}
		
		for(String elem : list) {
			logger.info("element: " +elem);
		}
		notificationService.markToSeen(list);
		//model.addAttribute("delMetadataReturn", "failure");
			
		
		return getAllNotifications(model);
	}
	@RequestMapping(value = "/deleteNotifications")
	public String deleteNotifications(final HttpServletRequest request, final Model model){

		Integer length = Integer.valueOf(request.getParameter("length"));
		List<String> list = new ArrayList<>();

		for (int i = 0; i < length; i++) {
			list.add(request.getParameter("params[" + i + "][uuid]"));
		}
		
		for(String elem : list) {
			logger.info("element: " +elem);
		}
		notificationService.deleteNotifications(list);
		//model.addAttribute("delMetadataReturn", "failure");
			
		
		return getAllNotifications(model);
	}

}
