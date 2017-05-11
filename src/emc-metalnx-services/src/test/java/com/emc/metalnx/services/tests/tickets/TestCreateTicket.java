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
import com.emc.metalnx.core.domain.exceptions.DataGridMissingPathOnTicket;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketService;
import org.irods.jargon.core.exception.JargonException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestCreateTicket {
    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private IRODSServices irodsServices;

    private String targetPath;
    private long time;
    private TestTicketUtils ticketUtils;

    @Before
    public void setUp() throws DataGridException, JargonException {
        time = System.currentTimeMillis();
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);

        ticketUtils = new TestTicketUtils(irodsServices);
    }

    @Test
    public void testCreateTicket() throws DataGridMissingPathOnTicket, DataGridConnectionRefusedException {
        assertTrue(ticketService.create(new DataGridTicket(targetPath)));
        //ticketUtils.deleteTicket(ticketString);
    }

    @Test
    public void testCreateNullTicket() throws DataGridMissingPathOnTicket, DataGridConnectionRefusedException {
        assertFalse(ticketService.create(null));
    }

    @Test(expected = DataGridMissingPathOnTicket.class)
    public void testCreateTicketWithMissingPath() throws DataGridMissingPathOnTicket, DataGridConnectionRefusedException {
        ticketService.create(new DataGridTicket());
    }
}
