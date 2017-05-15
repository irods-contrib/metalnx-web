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
        return "tickets/tickets";
    }

    @RequestMapping(value = "/findAll", method = RequestMethod.GET)
    @ResponseBody
    public String findAll() throws DataGridConnectionRefusedException {
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

    @RequestMapping(value = "/{ticketId}", method = RequestMethod.DELETE, produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteTicket(@PathVariable String ticketId) throws DataGridConnectionRefusedException {
        boolean ticketDeleted = ticketService.delete(ticketId);
        String json = "";

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> jsonResponse = new HashMap<>();
            jsonResponse.put("ticketId", ticketId);
            jsonResponse.put("ticketDeleted", ticketDeleted);
            json = mapper.writeValueAsString(jsonResponse);
        } catch (JsonProcessingException e) {
            logger.error("Could not parse hashmap to find all tickets: {}", e.getMessage());
        }

        ResponseEntity<String> response;
        if(ticketDeleted) response = new ResponseEntity<>(json, HttpStatus.OK);
        else response = new ResponseEntity<>(HttpStatus.NO_CONTENT); // Ticket was not deleted -> HTTP 204 returned

        return response;
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    @ResponseBody
    public DataGridTicket createTicket(@RequestBody DataGridTicket ticket) throws DataGridConnectionRefusedException {
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
        return "tickets/ticketForm";
    }


}
