 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridTicket;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketNotFoundException;
import com.emc.metalnx.core.domain.exceptions.UnsupportedDataGridFeatureException;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.TicketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/tickets")
public class TicketController {
	private static final Logger logger = LogManager.getLogger(TicketController.class);

	@Autowired
	private TicketService ticketService;

	@Autowired
	private LoggedUserUtils loggedUserUtils;

	@Autowired
	private ConfigService configService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index() throws DataGridConnectionRefusedException, UnsupportedDataGridFeatureException {
		if (!configService.getGlobalConfig().isTicketsEnabled()) {
			logger.error("tickets are not enabled");
			throw new UnsupportedDataGridFeatureException("tickets disabled");
		}
		logger.info("Get tickets page");
		return "tickets/tickets";
	}

	@RequestMapping(value = "/ticketForm", method = RequestMethod.GET)
	public String createTicketForm(final Model model,
			@RequestParam(value = "ticketstring", required = false) final String ticketString)
			throws DataGridConnectionRefusedException, DataGridTicketNotFoundException,
			UnsupportedDataGridFeatureException {

		if (!configService.getGlobalConfig().isTicketsEnabled()) {
			logger.error("tickets are not enabled");
			throw new UnsupportedDataGridFeatureException("tickets disabled");
		}

		DataGridTicket ticket;

		if (ticketString != null && !ticketString.isEmpty()) {
			ticket = ticketService.find(ticketString);
		} else {
			ticket = new DataGridTicket();
		}

		model.addAttribute("ticket", ticket);
		model.addAttribute("requestMapping", "tickets/");
		return "tickets/ticketForm";
	}

	/**
	 * Finds all tickets in the grid.
	 *
	 * @return List of tickets in JSON
	 * @throws DataGridConnectionRefusedException
	 *             if Metalnx cannot connect to the grid
	 * @throws UnsupportedDataGridFeatureException
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String findAll()
			throws DataGridConnectionRefusedException, JsonProcessingException, UnsupportedDataGridFeatureException {
		logger.info("Find all tickets");
		if (!configService.getGlobalConfig().isTicketsEnabled()) {
			logger.error("tickets are not enabled");
			throw new UnsupportedDataGridFeatureException("tickets disabled");
		}
		List<DataGridTicket> tickets = ticketService.findAll();

		Map<String, Object> ticketsAsJSON = new HashMap<>();
		ticketsAsJSON.put("data", tickets);

		return new ObjectMapper().writeValueAsString(ticketsAsJSON);
	}

	/**
	 * Finds a specific ticket in the grid by its id or string
	 *
	 * @param ticketId
	 *            ticket id or string
	 * @return Ticket as JSON
	 * @throws DataGridConnectionRefusedException
	 *             if Metalnx cannot connect to the grid
	 * @throws UnsupportedDataGridFeatureException
	 */
	@RequestMapping(value = "/{ticketid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<DataGridTicket> find(@PathVariable("ticketid") final String ticketId)
			throws DataGridConnectionRefusedException, DataGridTicketNotFoundException,
			UnsupportedDataGridFeatureException {
		logger.info("Find ticket by its ID or String");
		if (!configService.getGlobalConfig().isTicketsEnabled()) {
			logger.error("tickets are not enabled");
			throw new UnsupportedDataGridFeatureException("tickets disabled");
		}
		DataGridTicket dgTicket = ticketService.find(ticketId);
		return new ResponseEntity<>(dgTicket, HttpStatus.OK);
	}

	@RequestMapping(value = "/{ticketId}", method = RequestMethod.DELETE)
	public ResponseEntity<String> deleteTicket(@PathVariable final String ticketId)
			throws DataGridConnectionRefusedException, UnsupportedDataGridFeatureException {
		logger.info("Delete ticket by its ID or String");
		if (!configService.getGlobalConfig().isTicketsEnabled()) {
			logger.error("tickets are not enabled");
			throw new UnsupportedDataGridFeatureException("tickets disabled");
		}
		boolean ticketDeleted = ticketService.delete(ticketId);

		if (!ticketDeleted) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}

		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = "/", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public ResponseEntity<String> bulkDeleteTickets(@RequestBody final List<String> ticketStrings)
			throws DataGridConnectionRefusedException, UnsupportedDataGridFeatureException {
		logger.info("Delete tickets of user: {}", loggedUserUtils.getLoggedDataGridUser().getUsername());
		if (!configService.getGlobalConfig().isTicketsEnabled()) {
			logger.error("tickets are not enabled");
			throw new UnsupportedDataGridFeatureException("tickets disabled");
		}
		boolean ticketsDeleted = ticketService.bulkDelete(ticketStrings);
		if (!ticketsDeleted) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@RequestMapping(value = "/", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.CREATED)
	@ResponseBody
	public DataGridTicket createTicket(@RequestBody final DataGridTicket ticket)
			throws DataGridConnectionRefusedException, DataGridTicketException, UnsupportedDataGridFeatureException {
		logger.info("Create new ticket");
		if (!configService.getGlobalConfig().isTicketsEnabled()) {
			logger.error("tickets are not enabled");
			throw new UnsupportedDataGridFeatureException("tickets disabled");
		}
		ticket.setOwner(loggedUserUtils.getLoggedDataGridUser().getUsername());
		ticketService.create(ticket);
		return ticket;
	}

	@RequestMapping(value = "/", method = RequestMethod.PUT)
	@ResponseStatus(value = HttpStatus.NO_CONTENT)
	public void modifyTicket(@RequestBody final DataGridTicket ticket)
			throws DataGridConnectionRefusedException, DataGridTicketException, UnsupportedDataGridFeatureException {
		logger.info("Modify ticket");
		if (!configService.getGlobalConfig().isTicketsEnabled()) {
			logger.error("tickets are not enabled");
			throw new UnsupportedDataGridFeatureException("tickets disabled");
		}
		ticketService.modify(ticket);
	}

	@RequestMapping(value = "/validatehost", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public HostInfo validateTicketHostname(@RequestParam("hostname") final String hostname)
			throws UnknownHostException, UnsupportedDataGridFeatureException {
		logger.info("Validating ticket hostname {}", hostname);
		if (!configService.getGlobalConfig().isTicketsEnabled()) {
			logger.error("tickets are not enabled");
			throw new UnsupportedDataGridFeatureException("tickets disabled");
		}
		return new HostInfo(hostname);
	}

	public TicketService getTicketService() {
		return ticketService;
	}

	public void setTicketService(final TicketService ticketService) {
		this.ticketService = ticketService;
	}

	public LoggedUserUtils getLoggedUserUtils() {
		return loggedUserUtils;
	}

	public void setLoggedUserUtils(final LoggedUserUtils loggedUserUtils) {
		this.loggedUserUtils = loggedUserUtils;
	}

	public ConfigService getConfigService() {
		return configService;
	}

	public void setConfigService(final ConfigService configService) {
		this.configService = configService;
	}
}

class HostInfo {

	private String hostname;
	private String ip;

	HostInfo(final String hostname) throws UnknownHostException {
		InetAddress address = InetAddress.getByName(hostname);
		this.hostname = hostname;
		ip = address.getHostAddress();
	}

	public String getHostname() {
		return hostname;
	}

	public String getIp() {
		return ip;
	}
}