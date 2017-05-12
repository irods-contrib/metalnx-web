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

import java.util.Calendar;
import java.util.Date;

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
    private static final Date EXPIRATION_DATE = new Date();
    private static final long WRITE_BYTE_LIMIT = 1024;
    private static final int USES_COUNT = 0;
    private static final int WRITE_BYTE_COUNT = 0;
    private static final int WRITE_FILE_LIMIT = 5;
    private static final int WRITE_FILE_COUNT = 0;
    private static final String[] HOSTS = {"test-ticket-host1", "test-ticket-host2"};
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

    @Before
    public void setUp() throws DataGridException, JargonException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);

        ticketString = ticketUtils.createTicket(ticketString, parentPath, username);
        ticketUtils.setUsesLimit(ticketString, USES_LIMIT);
        ticketUtils.setExpirationDate(ticketString, EXPIRATION_DATE);
        ticketUtils.setWriteByteLimit(ticketString, WRITE_BYTE_LIMIT);
        ticketUtils.setWriteFileLimit(ticketString, WRITE_FILE_LIMIT);
        ticketUtils.addHostRestriction(ticketString, host);
        ticketUtils.addUserRestriction(ticketString, username);
        ticketUtils.addGroupRestriction(ticketString, PUBLIC_GROUP);
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
        assertDate(EXPIRATION_DATE, dgt.getExpirationDate());
        assertEquals(USES_LIMIT, dgt.getUsesLimit());
        assertEquals(USES_COUNT, dgt.getUsesCount());
        assertEquals(WRITE_BYTE_LIMIT, dgt.getWriteByteLimit());
        assertEquals(WRITE_BYTE_COUNT, dgt.getWriteByteCount());
        assertEquals(WRITE_FILE_LIMIT, dgt.getWriteFileLimit());
        assertEquals(WRITE_FILE_COUNT, dgt.getWriteFileCount());
        assertEquals(1, dgt.getHosts().size());
        assertEquals(1, dgt.getUsers().size());
        assertTrue(dgt.getUsers().contains(username));
        assertTrue(dgt.getGroups().contains(PUBLIC_GROUP));
    }

    @Test(expected = DataGridTicketNotFoundException.class)
    public void testFindNonExistentTicket() throws DataGridConnectionRefusedException, DataGridTicketNotFoundException {
        String random = "" + System.currentTimeMillis();
        ticketService.find(random);
    }

    public void assertDate(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);

        boolean sameYear = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
        boolean sameMonth = cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
        boolean sameDay = cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        boolean sameTime = cal1.get(Calendar.HOUR_OF_DAY) == cal2.get(Calendar.HOUR_OF_DAY) &&
                cal1.get(Calendar.MINUTE) == cal2.get(Calendar.MINUTE);

        assertTrue(sameYear);
        assertTrue(sameMonth);
        assertTrue(sameDay);
        assertTrue(sameTime);
    }
}
