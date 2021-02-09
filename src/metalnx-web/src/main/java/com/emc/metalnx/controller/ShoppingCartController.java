/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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
	private static final Logger log = LoggerFactory.getLogger(ShoppingCartController.class);

	@Value("${pluggableshoppingcart.enabled}")
	private boolean pluggableShoppingcartEnabled = false;

	@RequestMapping(method = RequestMethod.GET)
	public String shoppingCartMain(Model model) throws DataGridException {
		log.info("shoppingCartMain()");

		if (!pluggableShoppingcartEnabled) {
			log.error("Shopping cart is not configured for this grid");
			return "shoppingCart/shoppingCartNotConfigured";
		}

		return "shoppingCart/shoppingCartMain";
	}
}
