/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.irods.jargon.core.connection.AuthScheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.emc.metalnx.services.auth.UserTokenDetails;
import com.emc.metalnx.services.interfaces.ConfigService;

@Controller
@RequestMapping(value = "/login")
public class LoginController {

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private ConfigService configService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView loginView(final Model inModel) {
		logger.info("LoginContoller loginView()");
		ModelAndView model = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth instanceof UsernamePasswordAuthenticationToken) {
			boolean isUserAdmin = ((UserTokenDetails) auth.getDetails()).getUser().isAdmin();
			String redirect = isUserAdmin ? "redirect:/dashboard/" : "redirect:/browse/home";
			model = new ModelAndView(redirect);
		} else {
			model = new ModelAndView("login/index");
			addAuthTypesAndDefaultAuthToModel(model);
		}

		return model;
	}

	private void addAuthTypesAndDefaultAuthToModel(ModelAndView model) {
		List<String> authTypes = new ArrayList<String>();
		authTypes.add(AuthScheme.STANDARD.getTextValue());
		authTypes.add(AuthScheme.PAM.getTextValue());
		model.addObject("authTypes", authTypes);
		model.addObject("defaultAuthType", configService.getIrodsAuthScheme());
	}

	@RequestMapping(value = "/exception", method = RequestMethod.GET)
	public ModelAndView loginErrorHandler(final Exception e) {
		logger.info("LoginContoller loginErrorHandler()");
		ModelAndView model = new ModelAndView("login/index");
		model.addObject("usernameOrPasswordInvalid", true);
		addAuthTypesAndDefaultAuthToModel(model);

		return model;
	}

	@RequestMapping(value = "/invalidSession/", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
	@ResponseBody
	public String invalidSessionHandler(final HttpServletRequest response) {
		logger.info("LoginContoller invalidSessionHandler()");
		return "<script>window.location='/emc-metalnx-web/login/'</script>";
	}

	@RequestMapping(value = "/serverNotResponding", method = RequestMethod.GET)
	public ModelAndView loginServerNotRespondingErrorHandler(final Exception e) {

		ModelAndView model = new ModelAndView("login/index");
		model.addObject("serverNotResponding", true);
		addAuthTypesAndDefaultAuthToModel(model);

		return model;
	}

	@RequestMapping(value = "/databaseNotResponding", method = RequestMethod.GET)
	public ModelAndView databaseNotRespondingErrorHandler(final Exception e) {
		logger.info("LoginContoller databaseNotResponding()");
		ModelAndView model = new ModelAndView("login/index");
		model.addObject("databaseNotResponding", true);
		addAuthTypesAndDefaultAuthToModel(model);

		return model;
	}

	public ConfigService getConfigService() {
		return configService;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

}
