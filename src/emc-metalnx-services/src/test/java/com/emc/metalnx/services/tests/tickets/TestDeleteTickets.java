 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.tests.tickets;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

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

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketService;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestDeleteTickets {
	@Value("${irods.zoneName}")
	private String zone;

	@Value("${jobs.irods.username}")
	private String username;

	@Autowired
	private TicketService ticketService;

	@Autowired
	private IRODSServices irodsServices;

	private TestTicketUtils ticketUtils;
	private List<String> ticketStrings;

	@Before
	public void setUp() throws DataGridException, JargonException {
		String parentPath = String.format("/%s/home", zone);
		ticketUtils = new TestTicketUtils(irodsServices);
		ticketStrings = new ArrayList<>();
		ticketStrings.add(ticketUtils.createTicket(parentPath, username));
		ticketStrings.add(ticketUtils.createTicket(parentPath, username, TicketCreateModeEnum.WRITE));
		irodsServices.getIrodsAccessObjectFactory().closeSessionAndEatExceptions();
	}

	@After
	public void tearDown() throws DataGridConnectionRefusedException, JargonException {
		ticketUtils.deleteAllTicketsForThisUser();
	}

	@Ignore
	public void testBulkDelete() throws DataGridConnectionRefusedException, JargonException {
		ticketService.bulkDelete(ticketStrings);
		assertTrue(ticketUtils.listAllTicketsForUser().isEmpty());
	}

	@Test
	public void testDeleteSingleTicket() throws DataGridConnectionRefusedException, JargonException {
		ticketService.delete(ticketStrings.get(0));
		assertFalse(ticketUtils.listAllTicketsForUser().isEmpty());
	}

	@Test
	public void testDeleteWithNullString() throws DataGridConnectionRefusedException {
		assertFalse(ticketService.delete(null));
	}

	@Test
	public void testBulkDeleteWithNullString() throws DataGridConnectionRefusedException {
		assertFalse(ticketService.bulkDelete(null));
	}
}
