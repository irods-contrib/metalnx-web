 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.tests.ticketclient;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketInvalidUserException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketUploadException;
import com.emc.metalnx.services.auth.UserTokenDetails;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketClientService;
import com.emc.metalnx.services.tests.tickets.TestTicketUtils;

import junit.framework.Assert;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestTicketAuthenticatedAccess {
	private static final String RESOURCE = "demoResc";

	@Value("${irods.zoneName}")
	private String zone;

	@Value("${irods.host}")
	private String host;

	@Value("${irods.port}")
	private String port;

	@Value("${jobs.irods.username}")
	private String username;

	@Value("${jobs.irods.password}")
	private String password;

	@Autowired
	private TicketClientService ticketClientService;

	@Autowired
	private IRODSServices irodsServices;

	private String ticketString, targetPath, filePath;
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

		IRODSAccount authIrodsAccount = IRODSAccount.instance(host, Integer.valueOf(port), username, password,
				targetPath, zone, RESOURCE);

		UserTokenDetails userTokenDetails = Mockito.mock(UserTokenDetails.class);

		Authentication authentication = Mockito.mock(Authentication.class);
		SecurityContext securityContext = Mockito.mock(SecurityContext.class);
		SecurityContextHolder.setContext(securityContext);
		Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
		Mockito.when(authentication.getDetails()).thenReturn(userTokenDetails);
		Mockito.when(userTokenDetails.getIrodsAccount()).thenReturn(authIrodsAccount);
	}

	@After
	public void tearDown() throws JargonException, DataGridConnectionRefusedException {
		FileUtils.deleteQuietly(localFile);
		ticketUtils.deleteIRODSFile(filePath);
		ticketUtils.deleteTicket(ticketString);
	}

	@Test
	public void testTransferFileWithTicketAsAuthenticatedUser() throws DataGridTicketUploadException,
			DataGridTicketInvalidUserException, DataGridConnectionRefusedException, JargonException {
		ticketClientService.transferFileToIRODSUsingTicket(ticketString, localFile, targetPath);
		DataObject obj = irodsServices.getDataObjectAO().findByCollectionNameAndDataName(targetPath,
				localFile.getName());
		Assert.assertEquals(username, obj.getDataOwnerName());
	}
}
