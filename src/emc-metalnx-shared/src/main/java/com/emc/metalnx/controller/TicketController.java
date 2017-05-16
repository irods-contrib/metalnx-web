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

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridTicket;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridMissingPathOnTicketException;
import com.emc.metalnx.core.domain.exceptions.DataGridNullTicketException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketNotFoundException;
import com.emc.metalnx.services.interfaces.TicketService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/tickets")
public class TicketController {
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);

    @Autowired
    private TicketService ticketService;

    @Autowired
    LoggedUserUtils loggedUserUtils;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index() throws DataGridConnectionRefusedException {
        logger.info("Get tickets page");
        return "tickets/tickets";
    }

    /**
     * Finds all tickets in the grid.
     * @return List of tickets in JSON
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the grid
     */
    @RequestMapping(value = "/", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String findAll() throws DataGridConnectionRefusedException {
        logger.info("Find all tickets");
        List<DataGridTicket> tickets = ticketService.findAll();
        String ticketsAsJSON = "";

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("data", tickets);
            ticketsAsJSON = mapper.writeValueAsString(jsonResponse);
        } catch (JsonProcessingException e) {
            logger.error("Could not parse hashmap to find all tickets: {}", e.getMessage());
        }

        return ticketsAsJSON;
    }

    /**
     * Finds a specific ticket in the grid by its id or string
     * @param ticketId ticket id or string
     * @return Ticket as JSON
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the grid
     */
    @RequestMapping(value = "/{ticketid}", method = RequestMethod.GET, produces= MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<DataGridTicket> find(@PathVariable("ticketid") String ticketId) throws DataGridConnectionRefusedException {
        logger.info("Find ticket by its ID or String");
        DataGridTicket dgTicket = null;

        try {
            dgTicket = ticketService.find(ticketId);
        } catch (DataGridTicketNotFoundException e) {
            logger.error("Could not find ticket with id: {}", ticketId);
        }

        if(dgTicket == null) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(dgTicket, HttpStatus.OK);
    }

    @RequestMapping(value = "/{ticketId}", method = RequestMethod.DELETE)
    public ResponseEntity<String> deleteTicket(@PathVariable String ticketId) throws DataGridConnectionRefusedException {
        logger.info("Delete ticket by its ID or String");
        boolean ticketDeleted = ticketService.delete(ticketId);

        if(!ticketDeleted) return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public DataGridTicket createTicket(@RequestBody DataGridTicket ticket) throws DataGridConnectionRefusedException {
        logger.info("Create new ticket");
        DataGridTicket newTicket = null;
        try {
            ticket.setOwner(loggedUserUtils.getLoggedDataGridUser().getUsername());
            newTicket = ticketService.create(ticket);
        } catch (DataGridMissingPathOnTicketException | DataGridNullTicketException e) {
            logger.error("Could not create ticket: {}", e);
        }

        return newTicket;
    }

    @RequestMapping(value = "/ticketForm", method = RequestMethod.GET)
    public String createTicketForm(Model model) throws DataGridConnectionRefusedException {
        DataGridTicket ticket = new DataGridTicket();
        model.addAttribute("ticket", ticket);
        model.addAttribute("requestMapping","tickets/");
        return "tickets/ticketForm";
    }


}
