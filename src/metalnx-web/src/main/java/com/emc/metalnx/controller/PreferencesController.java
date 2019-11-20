/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.modelattribute.preferences.UserPreferences;
import com.emc.metalnx.services.interfaces.HeaderService;
import com.emc.metalnx.services.interfaces.UserService;

@Controller
@RequestMapping(value = "/preferences")
public class PreferencesController {

	@Autowired
	private UserService userService;

	@Autowired
	private LocaleResolver localeResolver;

	@Autowired
	LoggedUserUtils loggedUserUtils;

	@Autowired
	HeaderService headerService;

	public final static Logger logger = LoggerFactory.getLogger(PreferencesController.class);

	// ui mode that will be shown when the rods user switches mode from admin to
	// user and vice-versa
	public static final String UI_USER_MODE = "user";
	public static final String UI_ADMIN_MODE = "admin";

	@Value("${irods.zoneName}")
	private String zoneName;

	@RequestMapping(value = "/")
	public String index(final Model model, final HttpServletRequest request) {
		logger.info("index()");
		DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
		String locale = loggedUser.getLocale();

		String uiMode = (String) request.getSession().getAttribute("uiMode");

		if (uiMode == null || uiMode.isEmpty()) {
			if (loggedUser.isAdmin()) {
				uiMode = UI_ADMIN_MODE;
			} else {
				uiMode = UI_USER_MODE;
			}
		}

		UserPreferences userPreferences = new UserPreferences();
		userPreferences.setLocaleLanguage(locale);
		userPreferences.setForceFileOverwriting(loggedUser.isForceFileOverwriting());
		userPreferences.setAdvancedView(loggedUser.isAdvancedView());
		userPreferences.setMetadataUnitView(loggedUser.isMetadataUnitView());
		logger.debug("logged in user preferences:{}", loggedUser);
		model.addAttribute("preferences", userPreferences);
		model.addAttribute("uiMode", uiMode);

		return "preferences/index";
	}

	@RequestMapping(value = "/action/")
	public String action(final Model model, @ModelAttribute final UserPreferences preferences,
			final HttpServletRequest request, final HttpServletResponse response) throws DataGridException {

		logger.info("action()");
		logger.info("preferences:{}", preferences);

		DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
		logger.debug("current logged in user:{}", loggedUser);
		loggedUser.setLocale(preferences.getLocaleLanguage());
		loggedUser.setForceFileOverwriting(preferences.isForceFileOverwriting());
		loggedUser.setAdvanceView(preferences.isAdvancedView());
		loggedUser.setMetadataUnitView(preferences.isMetadataUnitView());
		logger.debug("modified logged in user:{}", loggedUser);

		userService.modifyUser(loggedUser);
		logger.info("preferences were saved");

		localeResolver.setLocale(request, response, StringUtils.parseLocaleString(preferences.getLocaleLanguage()));
		return "redirect:/preferences/";
	}

	@RequestMapping(value = "/chat/")
	public String webSocketTest() {
		return "preferences/chat";
	}
}
