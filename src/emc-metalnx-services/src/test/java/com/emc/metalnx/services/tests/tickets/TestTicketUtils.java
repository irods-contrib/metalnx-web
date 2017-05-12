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
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;

import java.util.Date;

/**
 * Utils class for ticket operations during tests.
 */
public class TestTicketUtils {
    private IRODSServices irodsServices;

    public TestTicketUtils(IRODSServices irodsServices) {
        this.irodsServices = irodsServices;
    }

    public String createTicket(String ticketString, String parentPath, String item) throws JargonException, DataGridConnectionRefusedException {
        IRODSFile irodsFile = irodsServices.getIRODSFileFactory().instanceIRODSFile(parentPath, item);
        return irodsServices.getTicketAdminService().createTicket(TicketCreateModeEnum.READ, irodsFile, ticketString);
    }

    public void deleteTicket(String ticketString) throws JargonException, DataGridConnectionRefusedException {
        irodsServices.getTicketAdminService().deleteTicket(ticketString);
    }

    public void setUsesLimit(String ticketString, int usesLimit) throws DataGridConnectionRefusedException,
            JargonException {
        irodsServices.getTicketAdminService().setTicketUsesLimit(ticketString, usesLimit);
    }

    public void setExpirationDate(String ticketString, Date expirationDate)
            throws DataGridConnectionRefusedException, JargonException {
        irodsServices.getTicketAdminService().setTicketExpiration(ticketString, expirationDate);
    }

    public void setWriteByteLimit(String ticketString, long writeByteLimit) throws DataGridConnectionRefusedException,
            JargonException {
        irodsServices.getTicketAdminService().setTicketByteWriteLimit(ticketString, writeByteLimit);
    }

    public void setWriteFileLimit(String ticketString, int writeFileLimit) throws DataGridConnectionRefusedException,
            JargonException {
        irodsServices.getTicketAdminService().setTicketFileWriteLimit(ticketString, writeFileLimit);
    }

    public void addHostRestriction(String ticketString, String host) throws DataGridConnectionRefusedException,
            JargonException {
        irodsServices.getTicketAdminService().addTicketHostRestriction(ticketString, host);
    }

    public void addUserRestriction(String ticketString, String username) throws DataGridConnectionRefusedException,
            JargonException {
        irodsServices.getTicketAdminService().addTicketUserRestriction(ticketString, username);
    }
}
