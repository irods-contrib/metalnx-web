package com.emc.metalnx.controller;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.datacommons.model.Notification;
import org.irods.jargon.extensions.notification.NotificationService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.services.interfaces.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/notifications")
public class NotificationController {

	@Autowired
	UserService userService;

	@Autowired
	LoggedUserUtils loggedUserUtils;

	@Autowired
	private NotificationService notificationService;

	private static final Logger logger = LoggerFactory.getLogger(NotificationController.class);

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String notifications(Model model) {
		logger.info("notifications()");
		return "notifications/v-notification";
	}

	/*
	 * Get all notifications for logged in user
	 */
	@RequestMapping(value = "/messages", method = RequestMethod.GET)
	@ResponseBody
	public String getAllNotifications(Model model) {
		logger.info("getAllNotifications()");
		DataGridUser loggedUser = null;
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		String resultJson = "";

		try {
			loggedUser = loggedUserUtils.getLoggedDataGridUser();
			logger.info("loggedUser:{}", loggedUser);

			List<Notification> notifications = notificationService.getAllNotification(loggedUser.getUsername());
			logger.info("notification data size :: " + notifications.size());

			for (Notification ns : notifications) {
				Map<String, Object> allNotification = new LinkedHashMap<String, Object>();
				allNotification.put("subject", ns.getSubject());
				allNotification.put("uuid", ns.getUuid());
				allNotification.put("message", ns.getMessage());
				allNotification.put("dateCreated", ns.getDateCreated().toString());
				allNotification.put("seen", ns.isSeen());
				resultList.add(allNotification);
			}

			resultJson = mapper.writeValueAsString(resultList);

		} catch (JsonProcessingException e) {
			logger.info("Could not parse Object to Json: ", e.getMessage());
		} catch (Exception e) {
			logger.error("exception geting user and user mode info", e);
			throw e;
		}
		return resultJson;

	}

	/*
	 * Get count for unseen notifications for logged user
	 */
	@RequestMapping(value = "/unseen-count/", method = RequestMethod.GET)
	public ResponseEntity<Integer> getUnseen(Model model) {
		logger.info("getUnseen()");
		DataGridUser loggedUser = null;
		try {
			loggedUser = loggedUserUtils.getLoggedDataGridUser();
			Map<String, Integer> result = notificationService.getUnseenCounts(loggedUser.getUsername());
			int unseenCounts = result.get("unseenCount");
			ResponseEntity<Integer> responseEntity = new ResponseEntity<>(unseenCounts, HttpStatus.OK);
			return responseEntity;
		} catch (Exception e) {
			logger.error("exception geting user and user mode info", e);
			throw e;
		}

	}

	/*
	 * Delete corresponding notifications from uuid/s in list
	 */
	@RequestMapping(value = "/deleteNotifications", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Integer>> deleteNotifications(final Model model,
			@RequestParam("uuids[]") final String[] uuids) {
		logger.info("deleteNotifications()");
		List<String> uuidList = new ArrayList<>();
		try {
			for (String uuid : uuids) {
				uuidList.add(uuid);
			}
			Map<String, Integer> result = notificationService.deleteNotifications(uuidList);
			ResponseEntity<Map<String, Integer>> responseEntity = new ResponseEntity<>(result, HttpStatus.OK);
			logger.info(result.toString());
			return responseEntity;
		} catch (Exception e) {
			logger.error("exception geting user and user mode info", e);
			throw e;
		}

	}

	/*
	 * Mark corresponding notifications as seen from uuid/s in list
	 */
	@RequestMapping(value = "/markSeenNotifications", method = RequestMethod.POST)
	public ResponseEntity<Map<String, Integer>> markSeenNotifications(final Model model,
			@RequestParam("uuids[]") final String[] uuids) {
		logger.info("markSeenNotifications()");
		List<String> uuidList = new ArrayList<>();
		try {
			for (String uuid : uuids) {
				uuidList.add(uuid);
			}
			Map<String, Integer> result = notificationService.markToSeen(uuidList);
			ResponseEntity<Map<String, Integer>> responseEntity = new ResponseEntity<>(result, HttpStatus.OK);
			logger.info(result.toString());
			return responseEntity;
		} catch (Exception e) {
			logger.error("exception geting user and user mode info", e);
			throw e;
		}

	}

}
