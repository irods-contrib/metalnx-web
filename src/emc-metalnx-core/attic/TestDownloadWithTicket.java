 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.tests.tickets;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketDownloadException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketInvalidUserException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketClientService;
import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.Stream2StreamAO;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestDownloadWithTicket {
    private static final String FILE_CONTENT = "Test for ticket";
    private static final int BUFFER_SIZE = 4 * 1024 * 1024;

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
    private File localFile, fileFromIRods;

    @Before
    public void setUp() throws DataGridException, JargonException, IOException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);
        localFile = ticketUtils.createLocalFile();
        ticketString = ticketUtils.createTicket(parentPath, username, TicketCreateModeEnum.READ);
        uploadFileToIRODS(targetPath, localFile);
        filePath = String.format("%s/%s", targetPath, localFile.getName());
    }

    @After
    public void tearDown() throws JargonException, DataGridConnectionRefusedException {
        FileUtils.deleteQuietly(localFile);
        FileUtils.deleteQuietly(fileFromIRods);
        ticketUtils.deleteTicket(ticketString);
        ticketUtils.deleteIRODSFile(filePath);
    }

    @Test
    public void testDownloadFileUsingATicket() throws DataGridTicketDownloadException, DataGridTicketInvalidUserException,
            IOException {
        fileFromIRods = ticketClientService.getFileFromIRODSUsingTicket(ticketString, filePath);
        assertNotNull(fileFromIRods);
        assertEquals(TestTicketUtils.TICKET_FILE_CONTENT, FileUtils.readFileToString(fileFromIRods, StandardCharsets.UTF_8.name()));
    }

    private void uploadFileToIRODS(String path, File file) throws DataGridConnectionRefusedException, JargonException,
            IOException {
        IRODSFile targetFile = irodsServices.getIRODSFileFactory().instanceIRODSFile(path, file.getName());

        if (targetFile.exists()) {
            return;
        }

        Stream2StreamAO streamAO = irodsServices.getStream2StreamAO();
        InputStream inputStream = new FileInputStream(file);
        streamAO.transferStreamToFileUsingIOStreams(inputStream, (File) targetFile, 0, BUFFER_SIZE);
        inputStream.close();
        targetFile.close();
    }
}
