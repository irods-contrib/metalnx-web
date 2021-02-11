package com.emc.metalnx.controller.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.datautils.shoppingcart.FileShoppingCart;
import org.irods.jargon.datautils.shoppingcart.ShoppingCartEntry;
import org.irods.jargon.datautils.shoppingcart.ShoppingCartService;
import org.irods.jargon.extensions.publishingplugin.PublishingIndexInventory;
import org.irods.jargon.extensions.publishingplugin.PublishingInventoryEntry;
import org.irods.metalnx.pluggableplublishing.PluggablePublishingWrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.controller.BrowseController;
import com.emc.metalnx.controller.api.model.PlublishingSchemaEntry;
import com.emc.metalnx.controller.api.model.PlublishingSchemaListing;
import com.emc.metalnx.controller.api.model.PublishActionData;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/api/shoppingCart")
public class ShoppingCartApiController {

	public static final Logger log = LoggerFactory.getLogger(ShoppingCartApiController.class);

	@Autowired
	private BrowseController browseController;

	@Autowired
	private PluggablePublishingWrapperService pluggablePublishingWrapperService;

	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	IRODSServices irodsServices;

	@RequestMapping(value = "/getCart", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public List<String> showCart(final Model model) throws DataGridException, JargonException {
		log.info("showCart()");
		String key = "metalnx-cart";

		ShoppingCartService shoppingCartService = irodsServices.getShoppingCartService();
		FileShoppingCart fileShoppingCart = shoppingCartService.retreiveShoppingCartAsLoggedInUser(key);
		log.info("cart retrieved:() ", fileShoppingCart.toString());
		return fileShoppingCart.getShoppingCartFileList();
	}

	@RequestMapping(value = "/updateCart", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public String updateCart(final Model model, @RequestParam("paths[]") final String[] paths)
			throws DataGridException, JargonException {
		log.info("initiating updateCart()");

		String key = "metalnx-cart";
		ShoppingCartService shoppingCartService = irodsServices.getShoppingCartService();
		FileShoppingCart fileShoppingCart = FileShoppingCart.instance();

		for (String path : paths) {
			log.info("Adding path to cart:: " + path.toString());
			fileShoppingCart.addAnItem(ShoppingCartEntry.instance(path.toString()));
		}
		String cartPath = shoppingCartService.serializeShoppingCartAsLoggedInUser(fileShoppingCart, key);
		log.info("Path to the cart: " + cartPath);

		return browseController.getSubDirectories(model, browseController.getCurrentPath());
	}

	/**
	 * Return information of the available publishing plugin
	 * 
	 * @param request {@link HttpServletRequest}
	 * @return {@code String} with json
	 * @throws DataGridException {@link DataGridException}
	 */
	@RequestMapping(value = "/info")
	@ResponseBody
	public String retrievePublishingInfo(final HttpServletRequest request) throws DataGridException {

		log.info("retrievePublishingInfo()");

		PublishingIndexInventory publishingIndexInventory = pluggablePublishingWrapperService
				.getPublishingIndexInventory();

		PlublishingSchemaListing publishingschema = new PlublishingSchemaListing();
		for (String key : publishingIndexInventory.getPublishingInventoryEntries().keySet()) {
			PublishingInventoryEntry publishingInventoryEntry = publishingIndexInventory.getPublishingInventoryEntries()
					.get(key);
			PlublishingSchemaEntry publishingSchemaEntry = new PlublishingSchemaEntry();
			publishingSchemaEntry.setEndpointUrl(publishingInventoryEntry.getEndpointUrl());
			publishingSchemaEntry
					.setSchemaDescription(publishingInventoryEntry.getPublishingEndpointDescription().getInfo());
			publishingSchemaEntry.setSchemaId(publishingInventoryEntry.getPublishingEndpointDescription().getId());
			publishingSchemaEntry.setSchemaName(publishingInventoryEntry.getPublishingEndpointDescription().getName());
			publishingSchemaEntry.setResponseType(publishingInventoryEntry.getPublishingEndpointDescription()
					.getResponseType().getResponseType().getValue());
			publishingschema.getPublishingSchemaEntry().add(publishingSchemaEntry);
		}

		String jsonString;

		try {
			jsonString = mapper.writeValueAsString(publishingschema);
			log.debug("jsonString:{}", jsonString);
		} catch (JsonProcessingException e) {
			log.error("Could not parse index inventory: {}", e.getMessage());
			throw new DataGridException("exception in json parsing", e);
		}

		return jsonString;
	}

	@RequestMapping(value = "/publisher", method = RequestMethod.POST)
	@ResponseBody
	public String executePublisher(@RequestBody PublishActionData publishActionData) throws DataGridException {
		log.info("executePublisher()");

		/*
		 * TODO: use additional publish request body properties to be used later for
		 * more added functionality
		 */
		log.info("publishActionData: {}", publishActionData.toString());
		PublishingIndexInventory publishingIndexInventory = pluggablePublishingWrapperService
				.getPublishingIndexInventory();
		PublishingInventoryEntry publishInventoryEntry = publishingIndexInventory.getPublishingInventoryEntries()
				.get(publishActionData.getEndpointUrl());

		if (publishInventoryEntry == null) {
			log.error("Cannot find publish inventory");
			throw new DataGridException("publish endpoint unavailable");
		}

		String userName = irodsServices.getCurrentUser();
		String cartId = irodsServices.getCurrentUser() + "-metalnx-cart.dat";
		String schemaId = publishInventoryEntry.getPublishingEndpointDescription().getId();
		String endpointUrl = publishInventoryEntry.getEndpointUrl();

		return this.pluggablePublishingWrapperService.executePublish(endpointUrl, schemaId, userName, cartId);
	}
}
