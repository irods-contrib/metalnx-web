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

package com.emc.metalnx.services.tests.tickets;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.ticket.Ticket;
import org.irods.jargon.ticket.TicketAdminService;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Utils class for ticket operations during tests.
 */
public class TestTicketUtils {
    private IRODSServices irodsServices;
    private TicketAdminService ticketAdminService;

    public TestTicketUtils(IRODSServices irodsServices) throws DataGridConnectionRefusedException {
        this.irodsServices = irodsServices;
        this.ticketAdminService = irodsServices.getTicketAdminService();
    }

    public String createTicket(String parentPath, String item, TicketCreateModeEnum type) throws JargonException, DataGridConnectionRefusedException {
        IRODSFile irodsFile = irodsServices.getIRODSFileFactory().instanceIRODSFile(parentPath, item);
        return ticketAdminService.createTicket(type, irodsFile, "");
    }

    public String createTicket(String parentPath, String item) throws JargonException, DataGridConnectionRefusedException {
        return createTicket(parentPath, item, TicketCreateModeEnum.READ);
    }

    public void deleteTicket(String ticketString) throws JargonException {
        ticketAdminService.deleteTicket(ticketString);
    }

    public void setUsesLimit(String ticketString, int usesLimit) throws JargonException {
        ticketAdminService.setTicketUsesLimit(ticketString, usesLimit);
    }

    public void setExpirationDate(String ticketString, Date expirationDate)
            throws DataGridConnectionRefusedException, JargonException {
        ticketAdminService.setTicketExpiration(ticketString, expirationDate);
    }

    public void setWriteByteLimit(String ticketString, long writeByteLimit) throws JargonException {
        ticketAdminService.setTicketByteWriteLimit(ticketString, writeByteLimit);
    }

    public void setWriteFileLimit(String ticketString, int writeFileLimit) throws JargonException {
        ticketAdminService.setTicketFileWriteLimit(ticketString, writeFileLimit);
    }

    public void addHostRestriction(String ticketString, String host) throws JargonException {
        ticketAdminService.addTicketHostRestriction(ticketString, host);
    }

    public void addUserRestriction(String ticketString, String username) throws JargonException {
        ticketAdminService.addTicketUserRestriction(ticketString, username);
    }

    public void addGroupRestriction(String ticketString, String group) throws JargonException {
        ticketAdminService.addTicketGroupRestriction(ticketString, group);
    }

    public Ticket findTicket(String ticketString) throws JargonException {
        return ticketAdminService.getTicketForSpecifiedTicketString(ticketString);
    }

    public List<String> listAllHostRestrictionsForSpecifiedTicket(String ticketString) throws JargonException {
        return ticketAdminService.listAllHostRestrictionsForSpecifiedTicket(ticketString, 0);
    }

    public List<String> listAllUserRestrictionsForSpecifiedTicket(String ticketString) throws JargonException {
        return ticketAdminService.listAllUserRestrictionsForSpecifiedTicket(ticketString, 0);
    }

    public List<String> listAllGroupRestrictionsForSpecifiedTicket(String ticketString) throws JargonException {
        return ticketAdminService.listAllGroupRestrictionsForSpecifiedTicket(ticketString, 0);
    }

    public File createLocalFile(String filename) throws IOException {
        File file = new File(filename);
        FileUtils.writeByteArrayToFile(file, "This is a test for ticket".getBytes());
        return file;
    }

    public File createLocalFile() throws IOException {
        return createLocalFile("test-ticket-file-" + System.currentTimeMillis());
    }
}
