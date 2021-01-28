package com.emc.metalnx.controller.api;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.datautils.shoppingcart.FileShoppingCart;
import org.irods.jargon.datautils.shoppingcart.ShoppingCartEntry;
import org.irods.jargon.datautils.shoppingcart.ShoppingCartService;
import org.irods.jargon.extensions.exportplugin.ExportIndexInventory;
import org.irods.jargon.extensions.exportplugin.ExportIndexInventoryEntry;
import org.irods.jargon.extensions.exportplugin.model.IndexSchemaDescription;
import org.irods.metalnx.pluggableexport.PluggableExportWrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.controller.BrowseController;
import com.emc.metalnx.controller.api.model.ExportSchemaEntry;
import com.emc.metalnx.controller.api.model.ExportSchemaListing;
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
	private PluggableExportWrapperService pluggableExportWrapperService;
	
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
		log.info("cart retrieved:() ", fileShoppingCart);
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
	 * Return an inventory of the available indexes
	 * 
	 * @param request {@link HttpServletRequest}
	 * @return {@code String} with json
	 * @throws DataGridException {@link DataGridException}
	 */
	@RequestMapping(value = "/indexes")
	@ResponseBody
	public String retrieveIndexes(final HttpServletRequest request) throws DataGridException {

		log.info("retrieveIndexes()");
		ExportIndexInventory exportIndexInventory = pluggableExportWrapperService.getExportIndexInventory();

		ExportSchemaListing exportSchema = new ExportSchemaListing();
		for (String key : exportIndexInventory.getIndexInventoryEntries().keySet()) {
			ExportIndexInventoryEntry indexInventoryEntry = exportIndexInventory.getIndexInventoryEntries().get(key);
			for (IndexSchemaDescription desrc : indexInventoryEntry.getIndexInformation().getIndexes()) {
				ExportSchemaEntry exportSchemaEntry = new ExportSchemaEntry();
				exportSchemaEntry.setEndpointUrl(indexInventoryEntry.getEndpointUrl());
				exportSchemaEntry.setSchemaDescription(desrc.getInfo());
				exportSchemaEntry.setSchemaId(desrc.getId());
				exportSchemaEntry.setSchemaName(desrc.getName());
				exportSchema.getExportSchemaEntry().add(exportSchemaEntry);
			}
		}
		String jsonString;

		try {
			jsonString = mapper.writeValueAsString(exportSchema);
			log.debug("jsonString: {}", jsonString);
		} catch (JsonProcessingException e) {
			log.error("Could not parse index inventory: {}", e.getMessage());
			throw new DataGridException("exception in json parsing", e);
		}
		
		return jsonString;

	}

}
