package com.emc.metalnx.controller;

import javax.servlet.http.HttpServletRequest;

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

@Controller
@RequestMapping(value = "/login")
public class LoginController {

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String loginView(Model model) {
		
		Authentication authObject = SecurityContextHolder.getContext().getAuthentication();
		
		if (authObject instanceof UsernamePasswordAuthenticationToken) {
			return "redirect:/dashboard/";
		}
		
		return "login/index";
	}

	@RequestMapping(value = "/exception", method = RequestMethod.GET)
	public ModelAndView loginErrorHandler(Exception e) {
		
		ModelAndView model = new ModelAndView("login/index");
		model.addObject("usernameOrPasswordInvalid", true);
		
	    return model;
	}
	
	@RequestMapping(value = "/invalidSession/", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
	@ResponseBody
	public String invalidSessionHandler(HttpServletRequest response) {
	    return "<script>window.location='/emc-metalnx-web/login/'</script>";
	}

	@RequestMapping(value = "/serverNotResponding", method = RequestMethod.GET)
	public ModelAndView loginServerNotRespondingErrorHandler(Exception e) {
		
		ModelAndView model = new ModelAndView("login/index");
		model.addObject("serverNotResponding", true);
		
	    return model;
	}
	
}
