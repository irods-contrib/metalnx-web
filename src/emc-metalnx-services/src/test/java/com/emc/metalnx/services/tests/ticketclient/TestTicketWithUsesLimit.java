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

package com.emc.metalnx.services.tests.ticketclient;

import com.emc.metalnx.core.domain.entity.DataGridTicket;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridMissingPathOnTicketException;
import com.emc.metalnx.core.domain.exceptions.DataGridNullTicketException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketService;
import com.emc.metalnx.services.tests.tickets.TestTicketUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.ticket.Ticket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestTicketWithUsesLimit {
    private static final int USES_LIMIT = 5;

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private IRODSServices irodsServices;

    private String targetPath;
    private TestTicketUtils ticketUtils;
    private DataGridTicket dgt;

    @Before
    public void setUp() throws DataGridException, JargonException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);

        dgt = new DataGridTicket(targetPath);
        dgt.setUsesLimit(USES_LIMIT);
    }

    @After
    public void tearDown() throws JargonException {
        ticketUtils.deleteAllTickets();
    }

    @Test
    public void testCreateTicketWithExpirationDate() throws DataGridConnectionRefusedException,
            DataGridMissingPathOnTicketException, DataGridNullTicketException, JargonException {
        Ticket ticketWithUses = ticketUtils.findTicket(ticketService.create(dgt));

        assertEquals(USES_LIMIT, ticketWithUses.getUsesLimit());
        assertFalse(ticketWithUses.getTicketString().isEmpty());
        assertTrue(ticketWithUses.getIrodsAbsolutePath().equals(targetPath));
        assertTrue(ticketWithUses.getOwnerName().equals(username));
    }
}
