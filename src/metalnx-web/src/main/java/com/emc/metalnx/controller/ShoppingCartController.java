 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.core.domain.exceptions.DataGridException;


@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/shoppingCart")
public class ShoppingCartController {
	private static final Logger logger = LoggerFactory.getLogger(ShoppingCartController.class);
	
	@RequestMapping(method = RequestMethod.GET)
	public String getCart(Model model) throws DataGridException {
		logger.info("getCart()");
		logger.info("Get shopping cart");
		return "shoppingCart/shoppingCart";	
	}
}
