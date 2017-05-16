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
import com.emc.metalnx.core.domain.exceptions.*;
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
public class TestCreateTicketWithRestrictions {
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

    private String targetPath;
    private TestTicketUtils ticketUtils;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Before
    public void setUp() throws DataGridException, JargonException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);
    }

    @After
    public void tearDown() throws JargonException {
        ticketUtils.deleteAllTickets();
    }

    @Test
    public void testCreateTicketWithExpirationDate() throws DataGridNullTicketException, DataGridMissingTicketString,
            DataGridConnectionRefusedException, DataGridTicketNotFoundException, DataGridMissingPathOnTicketException {
        Date date = new Date();
        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.setExpirationDate(date);

        DataGridTicket newTicket = ticketService.create(dgt);

        String currDate = dateFormat.format(date);
        String ticketCreatedDate = dateFormat.format(newTicket.getExpirationDate());

        assertEquals(currDate, ticketCreatedDate);
        assertTicket(newTicket);
    }

    @Test
    public void testCreateTicketWithUsesLimit() throws DataGridNullTicketException, DataGridMissingTicketString,
            DataGridConnectionRefusedException, DataGridTicketNotFoundException, DataGridMissingPathOnTicketException {
        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.setUsesLimit(USES_LIMIT);

        DataGridTicket newTicket = ticketService.create(dgt);

        assertEquals(USES_LIMIT, newTicket.getUsesLimit());
        assertTicket(newTicket);
    }

    @Test
    public void testCreateTicketWithWriteFileLimit() throws DataGridNullTicketException, DataGridMissingTicketString,
            DataGridConnectionRefusedException, DataGridTicketNotFoundException, DataGridMissingPathOnTicketException {
        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.setWriteFileLimit(WRITE_FILE_LIMIT);

        DataGridTicket newTicket = ticketService.create(dgt);

        assertEquals(WRITE_FILE_LIMIT, newTicket.getWriteFileLimit());
        assertTicket(newTicket);
    }

    @Test
    public void testCreateTicketWithWriteByteLimit() throws DataGridNullTicketException, DataGridMissingTicketString,
            DataGridConnectionRefusedException, DataGridTicketNotFoundException, DataGridMissingPathOnTicketException {
        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.setWriteByteLimit(WRITE_BYTE_LIMIT);

        DataGridTicket newTicket = ticketService.create(dgt);

        assertEquals(WRITE_BYTE_LIMIT, newTicket.getWriteByteLimit());
        assertTicket(newTicket);
    }

    @Test
    public void testCreateTicketWithHostRestriction() throws DataGridMissingPathOnTicketException,
            DataGridConnectionRefusedException, DataGridNullTicketException {
        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.addHost(host);

        DataGridTicket newTicket = ticketService.create(dgt);

        assertEquals(1, newTicket.getHosts().size());
        assertTicket(newTicket);
    }

    @Test
    public void testCreateTicketWithUserRestriction() throws DataGridMissingPathOnTicketException,
            DataGridConnectionRefusedException, DataGridNullTicketException {
        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.addUser(username);

        DataGridTicket newTicket = ticketService.create(dgt);

        assertEquals(1, newTicket.getUsers().size());
        assertTrue(newTicket.getUsers().contains(username));
        assertTicket(newTicket);
    }

    @Test
    public void testCreateTicketWithGroupRestriction() throws DataGridMissingPathOnTicketException,
            DataGridConnectionRefusedException, DataGridNullTicketException {
        DataGridTicket dgt = new DataGridTicket(targetPath);
        dgt.addGroup(PUBLIC_GROUP);

        DataGridTicket newTicket = ticketService.create(dgt);

        assertEquals(1, newTicket.getGroups().size());
        assertTrue(newTicket.getGroups().contains(PUBLIC_GROUP));
        assertTicket(newTicket);
    }

    private void assertTicket(DataGridTicket newTicket) {
        assertFalse(newTicket.getTicketString().isEmpty());
        assertTrue(newTicket.getPath().equals(targetPath));
        assertTrue(newTicket.getOwner().equals(username));
        assertTrue(newTicket.isTicketCreated());
    }
}
