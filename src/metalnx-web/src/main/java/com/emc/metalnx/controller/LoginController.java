/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.controller;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.services.auth.UserTokenDetails;
import com.emc.metalnx.services.interfaces.ConfigService;

@Controller
@RequestMapping(value = "/login")
public class LoginController {

	public static final String AJAX_ORIG_PATH = "ajaxOrigPath";

	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

	@Autowired
	private ConfigService configService;

	@Autowired
	private LoggedUserUtils loggedUserUtils;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public ModelAndView loginView(final Model inModel, final HttpServletRequest request,
			final HttpServletResponse response) {
		logger.info("LoginContoller loginView()");
		ModelAndView model = null;
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
		logger.info("last saved request was:{}", savedRequest);

		Map<String, String[]> params = request.getParameterMap();
		String redirect = "";

		if (params.containsKey(AJAX_ORIG_PATH)) {
			logger.info("have an original path from an ajax call to use to construct the redirect");
			String origPath = params.get(AJAX_ORIG_PATH)[0];
			if (origPath.indexOf("isAjax=true}") > -1) {
				logger.info("don't redirect to ajax paths");
				boolean isUserAdmin = ((UserTokenDetails) auth.getDetails()).getUser().isAdmin();
				if (isUserAdmin) {
					if (configService.isDashboardEnabled()) {
						redirect = "redirect:/dashboard/";
					} else {
						redirect = "redirect:/browse/home";
					}
				} else {
					redirect = "redirect:/browse/home";
				}
				model = new ModelAndView(redirect);

			} else {
				StringBuilder sb = new StringBuilder();
				sb.append("redirect:");
				sb.append(trimContextString(params.get(AJAX_ORIG_PATH)[0]));
				sb.append("?");
				sb.append(request.getQueryString());
				logger.debug("redirect to:{}", sb.toString());
				model = new ModelAndView(sb.toString());
			}
		} else if (auth instanceof UsernamePasswordAuthenticationToken) {
			boolean isUserAdmin = ((UserTokenDetails) auth.getDetails()).getUser().isAdmin();
			if (isUserAdmin) {
				if (configService.isDashboardEnabled()) {
					redirect = "redirect:/dashboard/";
				} else {
					redirect = "redirect:/browse/home";
				}
			} else {
				redirect = "redirect:/browse/home";

			}
			model = new ModelAndView(redirect);
		} else {
			model = new ModelAndView("login/index");
			addAuthTypesAndDefaultAuthToModel(model);
		}

		return model;
	}

	/*
	 * interim method to lop off the context string which comes from the javascript
	 * ajax processing.
	 */
	private String trimContextString(String pathString) {
		logger.debug("trimContextString()");
		if (pathString == null || pathString.isEmpty()) {
			throw new IllegalArgumentException("null or empty pathString");
		}

		logger.debug("pathString:{}", pathString);

		int idx = pathString.indexOf("/metalnx");
		if (idx == -1) {
			throw new IllegalArgumentException("not a path string, expected metalnx context");
		}

		String newPath = pathString.substring(idx + 8);
		logger.debug("newPath:{}", newPath);
		return newPath;

	}

	private void addAuthTypesAndDefaultAuthToModel(ModelAndView model) {
		/*
		 * Auth schemes are provided in metalnx.properties and can be updated to provide
		 * user friendly auth types, like "Example Corp. Login"
		 */
		model.addObject("authTypes", configService.listAuthTypeMappings());
		model.addObject("defaultAuthType", configService.getDefaultIrodsAuthScheme());

		/*
		 * 1- check if anon is permitted in props - we may consider adding an
		 * allow.anonymous or something in metalnx.properties mike can wire that in if
		 * need be
		 * 
		 * 2- propogate the property in global config and turn on anon login box in
		 * index.html under src/main/resources/login for that html block (add a 'login
		 * as guest button there')
		 * 
		 * 3- in IRODSAuthenticationProvider hoist up add'l attributes and look for auth
		 * login request attribute
		 * 
		 * 4- process normal anon account
		 * 
		 * 
		 */

	}

	@RequestMapping(value = "/exception", method = RequestMethod.GET)
	public ModelAndView loginErrorHandler(final Exception e) {
		logger.info("LoginContoller loginErrorHandler()");
		/*
		 * see if I'm really logged in as a hacky way of getting around this bug
		 * https://github.com/irods-contrib/metalnx-web/issues/162
		 */
		DataGridUser loggedInUser = loggedUserUtils.getLoggedDataGridUser();
		if (loggedInUser != null) {
			logger.warn("is already logged in go ahead and just hit the main page...");
			String redirect = "redirect:/browse/home";
			ModelAndView model = new ModelAndView(redirect);
			return model;
		}

		ModelAndView model = new ModelAndView("login/index");
		model.addObject("usernameOrPasswordInvalid", true);
		addAuthTypesAndDefaultAuthToModel(model);

		return model;
	}

	@RequestMapping(value = "/invalidSession/", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
	@ResponseBody
	public String invalidSessionHandler(final HttpServletRequest request, final HttpServletResponse response) {
		logger.info("LoginContoller invalidSessionHandler()");
		SavedRequest savedRequest = new HttpSessionRequestCache().getRequest(request, response);
		logger.info("last saved request was:{}", savedRequest);
		logger.info("request url was:{}", request.getRequestURL());

		return "<script>window.location='/metalnx/login/'</script>";
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
