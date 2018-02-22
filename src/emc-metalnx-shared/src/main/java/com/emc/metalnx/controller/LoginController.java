/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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

import com.emc.metalnx.services.auth.IRODSAuthenticationProvider;
import com.emc.metalnx.services.auth.UserTokenDetails;

@Controller
@RequestMapping(value = "/login")
public class LoginController {

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String loginView(final Model model) {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth instanceof UsernamePasswordAuthenticationToken) {
			System.out.println("is instance of authtocken");
			boolean isUserAdmin = ((UserTokenDetails) auth.getDetails()).getUser().isAdmin();
			return isUserAdmin ? "redirect:/dashboard/" : "redirect:/browse/home";
		}else {
			System.out.println("is not instance of authtocken");
		}
		
		return "login/index";
	}

	@RequestMapping(value = "/exception", method = RequestMethod.GET)
	public ModelAndView loginErrorHandler(final Exception e) {

		ModelAndView model = new ModelAndView("login/index");
		model.addObject("usernameOrPasswordInvalid", true);

		return model;
	}

	@RequestMapping(value = "/invalidSession/", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.REQUEST_TIMEOUT)
	@ResponseBody
	public String invalidSessionHandler(final HttpServletRequest response) {
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

		ModelAndView model = new ModelAndView("login/index");
		model.addObject("databaseNotResponding", true);

		return model;
	}

}
