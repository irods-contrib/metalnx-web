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

import com.emc.metalnx.core.domain.entity.DataGridTicket;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketNotFoundException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketService;
import org.irods.jargon.core.exception.JargonException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertTrue;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestFindTicket {
    private static final int USES_LIMIT = 5;

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private IRODSServices irodsServices;

    private String targetPath, ticketString;
    private TestTicketUtils ticketUtils;

    @Before
    public void setUp() throws DataGridException, JargonException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);

        ticketString = ticketUtils.createTicket(ticketString, parentPath, username);
        ticketUtils.setUsesLimit(ticketString, USES_LIMIT);
    }

    @After
    public void tearDown() throws DataGridConnectionRefusedException, JargonException {
        ticketUtils.deleteTicket(ticketString);
    }

    @Test
    public void testFindTicket() throws DataGridConnectionRefusedException, DataGridTicketNotFoundException {
        DataGridTicket dgt = ticketService.find(ticketString);
        assertNotNull(dgt);
        assertFalse(dgt.getTicketString().isEmpty());
        assertTrue(dgt.getPath().equals(targetPath));
        assertTrue(dgt.getOwner().equals(username));
        assertEquals(USES_LIMIT, dgt.getUsesLimit());
    }

    @Test(expected = DataGridTicketNotFoundException.class)
    public void testFindNonExistentTicket() throws DataGridConnectionRefusedException, DataGridTicketNotFoundException {
        String random = "" + System.currentTimeMillis();
        ticketService.find(random);
    }
}
