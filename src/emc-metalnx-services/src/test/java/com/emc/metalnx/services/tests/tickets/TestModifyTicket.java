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
import com.emc.metalnx.core.domain.exceptions.DataGridTicketException;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestModifyTicket {
    private static final int USES_LIMIT = 5;
    private static final long WRITE_BYTE_LIMIT = 1024;
    private static final int WRITE_FILE_LIMIT = 5;
    private static final String PUBLIC_GROUP = "public";

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @Value("${irods.host}")
    private String host;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private IRODSServices irodsServices;

    private String targetPath, ticketString;
    private TestTicketUtils ticketUtils;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Before
    public void setUp() throws DataGridException, JargonException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);

        ticketString = ticketUtils.createTicket(parentPath, username);
    }

    @After
    public void tearDown() throws DataGridConnectionRefusedException, JargonException {
        ticketUtils.deleteTicket(ticketString);
    }

    @Test
    public void testModifyTicketExpireDate() throws DataGridConnectionRefusedException, DataGridTicketException {
        Date date = new Date();
        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.setTicketString(ticketString);
        dgt.setExpirationDate(date);

        DataGridTicket dgtModified = ticketService.modify(dgt);

        String currDate = dateFormat.format(date);
        String ticketModifiedDate = dateFormat.format(dgtModified.getExpirationDate());

        assertEquals(currDate, ticketModifiedDate);
        assertFalse(dgtModified.getTicketString().isEmpty());
        assertTrue(dgtModified.getPath().equals(targetPath));
        assertTrue(dgtModified.getOwner().equals(username));
    }

    @Test
    public void testModifyTicketUsesLimit() throws DataGridConnectionRefusedException, DataGridTicketException {

        int newUsesLimit = USES_LIMIT + 1;
        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.setTicketString(ticketString);
        dgt.setUsesLimit(newUsesLimit);

        DataGridTicket ticketModified = ticketService.modify(dgt);

        assertEquals(newUsesLimit, ticketModified.getUsesLimit());
        assertFalse(ticketModified.getTicketString().isEmpty());
        assertTrue(ticketModified.getPath().equals(targetPath));
        assertTrue(ticketModified.getOwner().equals(username));
    }

    @Test
    public void testModifyTicketWriteByteLimit() throws DataGridConnectionRefusedException, DataGridTicketException {

        long newWriteByteLimit = 2 * WRITE_BYTE_LIMIT;
        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.setTicketString(ticketString);
        dgt.setWriteByteLimit(newWriteByteLimit);

        DataGridTicket ticketModified = ticketService.modify(dgt);

        assertEquals(newWriteByteLimit, ticketModified.getWriteByteLimit());
        assertFalse(ticketModified.getTicketString().isEmpty());
        assertTrue(ticketModified.getPath().equals(targetPath));
        assertTrue(ticketModified.getOwner().equals(username));
    }

    @Test
    public void testModifyTicketWriteFileLimit() throws DataGridConnectionRefusedException, DataGridTicketException {

        int newWriteFileLimit = WRITE_FILE_LIMIT + 1;
        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.setTicketString(ticketString);
        dgt.setWriteFileLimit(newWriteFileLimit);

        DataGridTicket ticketModified = ticketService.modify(dgt);

        assertEquals(newWriteFileLimit, ticketModified.getWriteFileLimit());
        assertFalse(ticketModified.getTicketString().isEmpty());
        assertTrue(ticketModified.getPath().equals(targetPath));
        assertTrue(ticketModified.getOwner().equals(username));
    }

    @Test
    public void testModifyTicketHosts() throws DataGridConnectionRefusedException, DataGridTicketException {

        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.setTicketString(ticketString);
        dgt.addHost(host);

        DataGridTicket ticketModified = ticketService.modify(dgt);

        assertEquals(1, ticketModified.getHosts().size());
        assertFalse(ticketModified.getTicketString().isEmpty());
        assertTrue(ticketModified.getPath().equals(targetPath));
        assertTrue(ticketModified.getOwner().equals(username));
    }

    @Test
    public void testModifyTicketUsers() throws DataGridConnectionRefusedException, DataGridTicketException {

        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.setTicketString(ticketString);
        dgt.addUser(username);

        DataGridTicket ticketModified = ticketService.modify(dgt);

        assertEquals(1, ticketModified.getUsers().size());
        assertTrue(ticketModified.getUsers().contains(username));
        assertFalse(ticketModified.getTicketString().isEmpty());
        assertTrue(ticketModified.getPath().equals(targetPath));
        assertTrue(ticketModified.getOwner().equals(username));
    }

    @Test
    public void testModifyTicketGroups() throws DataGridConnectionRefusedException, DataGridTicketException {

        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.setTicketString(ticketString);
        dgt.addGroup(PUBLIC_GROUP);

        DataGridTicket ticketModified = ticketService.modify(dgt);

        assertEquals(1, ticketModified.getGroups().size());
        assertTrue(ticketModified.getGroups().contains(PUBLIC_GROUP));
        assertFalse(ticketModified.getTicketString().isEmpty());
        assertTrue(ticketModified.getPath().equals(targetPath));
        assertTrue(ticketModified.getOwner().equals(username));
    }

}
