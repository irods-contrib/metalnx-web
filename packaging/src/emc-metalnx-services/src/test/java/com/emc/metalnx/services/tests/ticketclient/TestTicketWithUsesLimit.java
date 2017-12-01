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

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketInvalidUserException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketUploadException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketClientService;
import com.emc.metalnx.services.tests.tickets.TestTicketUtils;
import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestTicketWithUsesLimit {
    private static final int USES_LIMIT = 1;

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @Autowired
    private TicketClientService ticketClientService;

    @Autowired
    private IRODSServices irodsServices;

    private String ticketString, targetPath, filePath1, filePath2;
    private TestTicketUtils ticketUtils;
    private File localFile, localFile2;

    @Before
    public void setUp() throws DataGridException, JargonException, IOException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);
        ticketString = ticketUtils.createTicket(parentPath, username, TicketCreateModeEnum.WRITE);
        ticketUtils.setUsesLimit(ticketString, USES_LIMIT);
        localFile = ticketUtils.createLocalFile();
        localFile2 = ticketUtils.createLocalFile("ticket-file-2-" + System.currentTimeMillis());
        filePath1 = String.format("%s/%s", targetPath, localFile.getName());
        filePath2 = String.format("%s/%s", targetPath, localFile2.getName());
    }

    @After
    public void tearDown() throws JargonException, DataGridConnectionRefusedException {
        FileUtils.deleteQuietly(localFile);
        FileUtils.deleteQuietly(localFile2);
        ticketUtils.deleteTicket(ticketString);
        ticketUtils.deleteIRODSFile(filePath1);
        ticketUtils.deleteIRODSFile(filePath2);
    }

    @Ignore
    @Test(expected = DataGridTicketUploadException.class)
    public void testTicketWithUsesLimit() throws DataGridTicketUploadException, DataGridTicketInvalidUserException {
        ticketClientService.transferFileToIRODSUsingTicket(ticketString, localFile, targetPath);
        ticketClientService.transferFileToIRODSUsingTicket(ticketString, localFile2, targetPath);
    }
}
