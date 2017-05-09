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

package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.entity.DataGridTicket;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.ticket.Ticket;
import org.irods.jargon.ticket.TicketAdminService;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class TicketServiceImpl implements TicketService {

    private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);
    public static final int OFFSET = 0;

    @Autowired
    private IRODSServices irodsServices;

    @Override
    public List<DataGridTicket> findAll() throws DataGridConnectionRefusedException {
        logger.info("Find all tickets");

        TicketAdminService tas = irodsServices.getTicketAdminService();
        List<DataGridTicket> dgTickets;
        List<Ticket> tickets;

        try {
            tickets = tas.listAllTickets(OFFSET);
        } catch (JargonException e) {
            logger.info("Could not list all tickets in the grid: {}.", e.getMessage());
            tickets = new ArrayList<>();
        }

        dgTickets = convertListOfTickets(tickets);

        return dgTickets;
    }

    private List<DataGridTicket> convertListOfTickets(List<Ticket> tickets) {
        List<DataGridTicket> dgTickets = new ArrayList<>();

        for(Ticket t: tickets) {
            dgTickets.add(convertTicketToDataGridTicket(t));
        }

        return dgTickets;
    }

    private DataGridTicket convertTicketToDataGridTicket(Ticket t) {
        DataGridTicket dgTicket = new DataGridTicket();

        dgTicket.setTicketString(t.getTicketString());
        dgTicket.setOwner(t.getOwnerName());

        DataGridTicket.TicketType dgTicketType;

        if(t.getType() == TicketCreateModeEnum.READ){
            dgTicketType = DataGridTicket.TicketType.READ;
        }
        else if (t.getType() == TicketCreateModeEnum.WRITE) {
            dgTicketType = DataGridTicket.TicketType.WRITE;
        }
        else {
            dgTicketType = DataGridTicket.TicketType.UNKNOWN;
        }

        dgTicket.setType(dgTicketType);

        if(t.getObjectType() == Ticket.TicketObjectType.COLLECTION)
            dgTicket.setIsCollection(true);

        return dgTicket;
    }
}
