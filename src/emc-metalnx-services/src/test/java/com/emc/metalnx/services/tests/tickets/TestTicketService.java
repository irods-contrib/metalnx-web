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

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridTicket;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FileOperationService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.List;

import static junit.framework.Assert.*;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestTicketService {
    private static final boolean FORCE_FLAG = true;
    private static final boolean IS_COLLECTION = true;

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    private String targetPath, ticketString, parentPath;

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private FileOperationService fileOperationService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private IRODSServices irodsServices;

    @Before
    public void setUp() throws DataGridException, JargonException {
        parentPath = String.format("/%s/home/%s", zone, username);
        long time = System.currentTimeMillis();
        String collname = String.format("test-ticket-%d", time);
        targetPath = String.format("%s/%s", parentPath, collname);

        fileOperationService.deleteCollection(targetPath, FORCE_FLAG);
        collectionService.createCollection(new DataGridCollectionAndDataObject(targetPath, parentPath, IS_COLLECTION));

        createTicket(time, collname);
    }

    public void createTicket(long time, String collname) throws JargonException, DataGridConnectionRefusedException {
        IRODSFile irodsFile = irodsServices.getIRODSFileFactory().instanceIRODSFile(parentPath, collname);
        ticketString = String.format("ticket-%d", time);
        irodsServices.getTicketAdminService().createTicket(TicketCreateModeEnum.READ, irodsFile, ticketString);
    }

    @After
    public void tearDown() throws DataGridException {
        fileOperationService.deleteCollection(targetPath, FORCE_FLAG);
    }

    @Test
    public void testListingAllTickets() throws DataGridConnectionRefusedException {
        List<DataGridTicket> tickets = ticketService.findAll();
        assertNotNull(tickets);
        assertFalse(tickets.isEmpty());

        for(DataGridTicket t: tickets) {
            assertNotNull(t.getPath());
            assertFalse(t.getTicketString().isEmpty());
            assertFalse(t.getOwner().isEmpty());
            assertFalse(t.getTicketString().isEmpty());
        }
    }

    @Test
    public void testDeleteTicket() throws DataGridConnectionRefusedException {
        assertTrue(ticketService.delete(ticketString));
    }

    @Test
    public void testDeleteTicketWithEmptyString() throws DataGridConnectionRefusedException {
        assertFalse(ticketService.delete(""));
    }

    @Test
    public void testDeleteTicketWithNullString() throws DataGridConnectionRefusedException {
        assertFalse(ticketService.delete(null));
    }
}
