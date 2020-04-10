package com.emc.metalnx.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@SessionAttributes({})
@RequestMapping(value = "/home")
public class HomeController {

	public HomeController() {

	}

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String showHome(Model model) {
		return "home/homeMain";
	}

}
