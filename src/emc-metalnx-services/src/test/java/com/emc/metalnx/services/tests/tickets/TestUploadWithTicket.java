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
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketUploadException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketClientService;
import org.apache.commons.io.FileUtils;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static junit.framework.Assert.assertTrue;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestUploadWithTicket {
    private static final String TEST_FILE_NAME = "test-ticket.txt";

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @Autowired
    private IRODSServices irodsServices;

    @Autowired
    private TicketClientService ticketClientService;

    private String targetPath, filePath, ticketString;
    private TestTicketUtils ticketUtils;
    private IRODSFile ticketIRODSFile;
    private File localFile;

    @Before
    public void setUp() throws DataGridException, JargonException, IOException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        filePath = String.format("%s/%s", targetPath, TEST_FILE_NAME);
        ticketUtils = new TestTicketUtils(irodsServices);
        ticketString = ticketUtils.createTicket(parentPath, username, TicketCreateModeEnum.WRITE);

        Path path = Paths.get(TEST_FILE_NAME);
        String data = "Test for ticket";
        Files.write(path, data.getBytes());

        localFile = new File(TEST_FILE_NAME);
    }

    @After
    public void tearDown() throws JargonException, DataGridConnectionRefusedException {
        FileUtils.deleteQuietly(localFile);

        ticketUtils.deleteTicket(ticketString);

        if(ticketIRODSFile != null && ticketIRODSFile.exists()) {
            irodsServices.getIRODSFileSystemAO().fileDeleteForce(ticketIRODSFile);
        }
    }

    @Test
    public void testUploadFileUsingATicket() throws DataGridConnectionRefusedException, JargonException, DataGridTicketUploadException {
        ticketClientService.transferFileToIRODSUsingTicket(ticketString, localFile, targetPath);
        ticketIRODSFile = irodsServices.getIRODSFileFactory().instanceIRODSFile(filePath);
        assertTrue(ticketIRODSFile.exists());
    }
}
