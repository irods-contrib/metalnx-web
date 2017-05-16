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
import com.emc.metalnx.core.domain.exceptions.*;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketService;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
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
    private static final int OFFSET = 0;
    private static final String GRID_FILE_SEPARATOR = "/";

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

    @Override
    public boolean delete(String ticketId) throws DataGridConnectionRefusedException {
        if(ticketId == null || ticketId.isEmpty()) {
            logger.error("Could not delete ticket: Ticket ID null or emtpy");
            return false;
        }

        boolean ticketDeleted = false;

        TicketAdminService tas = irodsServices.getTicketAdminService();

        try {
            ticketDeleted = tas.deleteTicket(ticketId);
        } catch (JargonException e) {
            logger.info("Could not delete ticket {}: {}.", ticketId, e.getMessage());
        }

        return ticketDeleted;
    }

    @Override
    public String create(DataGridTicket dgTicket) throws DataGridMissingPathOnTicketException,
            DataGridConnectionRefusedException, DataGridNullTicketException {
        if(dgTicket == null) {
            throw new DataGridNullTicketException("Could not create ticket: null ticket provided.");
        }

        if(dgTicket.getPath().isEmpty()) {
            throw new DataGridMissingPathOnTicketException("Could not create ticket: path is empty");
        }

        TicketCreateModeEnum ticketType = TicketCreateModeEnum.UNKNOWN;
        if(dgTicket.getType() == DataGridTicket.TicketType.READ) ticketType = TicketCreateModeEnum.READ;
        if(dgTicket.getType() == DataGridTicket.TicketType.WRITE) ticketType = TicketCreateModeEnum.WRITE;

        String path = dgTicket.getPath();
        int idxOfSeparator = path.lastIndexOf(GRID_FILE_SEPARATOR);
        String parentPath = path.substring(0, idxOfSeparator);
        String item = path.substring(idxOfSeparator + 1, path.length());

        TicketAdminService tas = irodsServices.getTicketAdminService();
        String ticketString = "";
        
        try {
            IRODSFile irodsFile = irodsServices.getIRODSFileFactory().instanceIRODSFile(parentPath, item);
            ticketString = tas.createTicket(ticketType, irodsFile, dgTicket.getTicketString());
            dgTicket.setTicketString(ticketString); // set ticket string created by the grid

            modify(dgTicket);
        } catch (JargonException | DataGridMissingTicketString | DataGridTicketNotFoundException e) {
            logger.error("Could not create a ticket: {}", e);
        }

        return ticketString;
    }

    @Override
    public DataGridTicket find(String ticketId) throws DataGridConnectionRefusedException,
            DataGridTicketNotFoundException {
        DataGridTicket dgTicket = null;

        TicketAdminService tas = irodsServices.getTicketAdminService();

        try {
            Ticket t = tas.getTicketForSpecifiedTicketString(ticketId);
            dgTicket = convertTicketToDataGridTicket(t);

            dgTicket.setHosts(tas.listAllHostRestrictionsForSpecifiedTicket(ticketId, OFFSET));
            dgTicket.setUsers(tas.listAllUserRestrictionsForSpecifiedTicket(ticketId, OFFSET));
            dgTicket.setGroups(tas.listAllGroupRestrictionsForSpecifiedTicket(ticketId, OFFSET));
        } catch (DataNotFoundException e) {
            throw new DataGridTicketNotFoundException("Ticket does not exist");
        } catch (JargonException e) {
            logger.error("Could not find ticket with string: {}", ticketId);
        }

        return dgTicket;
    }

    @Override
    public DataGridTicket modify(DataGridTicket t) throws DataGridConnectionRefusedException,
            DataGridNullTicketException, DataGridMissingTicketString, DataGridTicketNotFoundException {
        if(t == null) {
            throw new DataGridNullTicketException("Null ticket instance");
        }

        if(t.getTicketString().isEmpty()) {
            throw new DataGridMissingTicketString("Ticket ID or String missing");
        }

        String ticketString = t.getTicketString();

        TicketAdminService tas = irodsServices.getTicketAdminService();
        DataGridTicket dgTicket = null;
        try {
            updateHostRestrictions(t);
            updateUserRestrictions(t);
            updateGroupRestrictions(t);

            Ticket ticketUpdated = tas.compareGivenTicketToActualAndUpdateAsNeeded(convertDataGridTicketToTicket(t));
            dgTicket = convertTicketToDataGridTicket(ticketUpdated);

            dgTicket.setHosts(tas.listAllHostRestrictionsForSpecifiedTicket(ticketString, OFFSET));
            dgTicket.setUsers(tas.listAllUserRestrictionsForSpecifiedTicket(ticketString, OFFSET));
            dgTicket.setGroups(tas.listAllGroupRestrictionsForSpecifiedTicket(ticketString, OFFSET));
        } catch (DataNotFoundException e) {
            throw new DataGridTicketNotFoundException("Ticket does not exist");
        } catch (JargonException e) {
            logger.error("Could not modify ticket");
        }

        if(dgTicket != null) dgTicket.setTicketModified(true);

        return dgTicket;
    }

    private void updateHostRestrictions(DataGridTicket t) throws JargonException,
            DataGridConnectionRefusedException {
        String ticketString = t.getTicketString();
        TicketAdminService tas = irodsServices.getTicketAdminService();
        List<String> currHosts = tas.listAllHostRestrictionsForSpecifiedTicket(ticketString, OFFSET);

        for(String host: t.getHosts()) {
            if(!currHosts.contains(host)) tas.addTicketHostRestriction(ticketString, host);
        }

        for(String host: currHosts) {
            if(!t.getHosts().contains(host)) tas.removeTicketHostRestriction(ticketString, host);
        }
    }

    private void updateUserRestrictions(DataGridTicket t) throws JargonException,
            DataGridConnectionRefusedException {
        String ticketString = t.getTicketString();
        TicketAdminService tas = irodsServices.getTicketAdminService();
        List<String> currUsers = tas.listAllUserRestrictionsForSpecifiedTicket(ticketString, OFFSET);

        for(String user: t.getUsers()) {
            if(!currUsers.contains(user)) tas.addTicketUserRestriction(ticketString, user);
        }

        for(String user: currUsers) {
            if(!t.getUsers().contains(user)) tas.removeTicketUserRestriction(ticketString, user);
        }
    }

    private void updateGroupRestrictions(DataGridTicket t) throws JargonException,
            DataGridConnectionRefusedException {
        String ticketString = t.getTicketString();
        TicketAdminService tas = irodsServices.getTicketAdminService();
        List<String> currGroups = tas.listAllGroupRestrictionsForSpecifiedTicket(ticketString, OFFSET);

        for(String group: t.getGroups()) {
            if(!currGroups.contains(group)) tas.addTicketGroupRestriction(ticketString, group);
        }

        for(String group: currGroups) {
            if(!t.getGroups().contains(group)) tas.removeTicketGroupRestriction(ticketString, group);
        }
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
        dgTicket.setPath(t.getIrodsAbsolutePath());
        dgTicket.setTicketString(t.getTicketString());
        dgTicket.setExpirationDate(t.getExpireTime());
        dgTicket.setUsesLimit(t.getUsesLimit());
        dgTicket.setUsesCount(t.getUsesCount());
        dgTicket.setWriteByteLimit(t.getWriteByteLimit());
        dgTicket.setWriteByteCount(t.getWriteByteCount());
        dgTicket.setWriteFileLimit(t.getWriteFileLimit());
        dgTicket.setWriteFileCount(t.getWriteFileCount());

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

    private Ticket convertDataGridTicketToTicket(DataGridTicket dgTicket) {
        Ticket ticket = new Ticket();

        ticket.setTicketString(dgTicket.getTicketString());
        ticket.setOwnerName(dgTicket.getOwner());
        ticket.setIrodsAbsolutePath(dgTicket.getPath());
        ticket.setTicketString(dgTicket.getTicketString());
        ticket.setExpireTime(dgTicket.getExpirationDate());
        ticket.setUsesLimit(dgTicket.getUsesLimit());
        ticket.setUsesCount(dgTicket.getUsesCount());
        ticket.setWriteByteLimit(dgTicket.getWriteByteLimit());
        ticket.setWriteByteCount(dgTicket.getWriteByteCount());
        ticket.setWriteFileLimit(dgTicket.getWriteFileLimit());
        ticket.setWriteFileCount(dgTicket.getWriteFileCount());

        TicketCreateModeEnum ticketMode;

        if(dgTicket.getType() == DataGridTicket.TicketType.READ) ticketMode = TicketCreateModeEnum.READ;
        else if (dgTicket.getType() == DataGridTicket.TicketType.WRITE) ticketMode = TicketCreateModeEnum.WRITE;
        else ticketMode = TicketCreateModeEnum.UNKNOWN;

        ticket.setType(ticketMode);

        if(dgTicket.isCollection()) ticket.setObjectType(Ticket.TicketObjectType.COLLECTION);

        return ticket;
    }
}
