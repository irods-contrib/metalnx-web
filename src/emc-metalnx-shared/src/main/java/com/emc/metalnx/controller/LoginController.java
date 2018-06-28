 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.controller;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Controller
@RequestMapping(value = "/login")
public class LoginController {
	
	private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String loginView(final Model model) {
		logger.info("LoginContoller loginView()");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof UsernamePasswordAuthenticationToken) {
			boolean isUserAdmin = ((UserTokenDetails) auth.getDetails()).getUser().isAdmin();
			return isUserAdmin ? "redirect:/dashboard/" : "redirect:/browse/home";
		}

		return "login/index";
	}

	@RequestMapping(value = "/exception", method = RequestMethod.GET)
	public ModelAndView loginErrorHandler(final Exception e) {
		logger.info("LoginContoller loginErrorHandler()");
		ModelAndView model = new ModelAndView("login/index");
		model.addObject("usernameOrPasswordInvalid", true);

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

		return model;
	}

	@RequestMapping(value = "/databaseNotResponding", method = RequestMethod.GET)
	public ModelAndView databaseNotRespondingErrorHandler(final Exception e) {
		logger.info("LoginContoller databaseNotResponding()");
		ModelAndView model = new ModelAndView("login/index");
		model.addObject("databaseNotResponding", true);

		return model;
	}

}
