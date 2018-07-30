 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.tests.tickets;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketInvalidUserException;
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
    private File localFile;

    @Before
    public void setUp() throws DataGridException, JargonException, IOException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);
        ticketString = ticketUtils.createTicket(parentPath, username, TicketCreateModeEnum.WRITE);
        localFile = ticketUtils.createLocalFile();
        filePath = String.format("%s/%s", targetPath, localFile.getName());
    }

    @After
    public void tearDown() throws JargonException, DataGridConnectionRefusedException {
        FileUtils.deleteQuietly(localFile);
        ticketUtils.deleteTicket(ticketString);
        ticketUtils.deleteIRODSFile(filePath);
    }

    @Test
    public void testUploadFileUsingATicket() throws DataGridConnectionRefusedException,
            DataGridTicketUploadException, DataGridTicketInvalidUserException, JargonException {
        ticketClientService.transferFileToIRODSUsingTicket(ticketString, localFile, targetPath);
        IRODSFile ticketIRODSFile = irodsServices.getIRODSFileFactory().instanceIRODSFile(filePath);
        assertTrue(ticketIRODSFile.exists());
    }
}
