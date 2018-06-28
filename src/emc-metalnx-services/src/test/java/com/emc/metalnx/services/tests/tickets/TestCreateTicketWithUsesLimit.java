 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.tests.tickets;

import com.emc.metalnx.core.domain.entity.DataGridTicket;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.TicketService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.ticket.Ticket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestCreateTicketWithUsesLimit {
    private static final int USES_LIMIT = 5;

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private IRODSServices irodsServices;

    private String targetPath, ticketString;
    private TestTicketUtils ticketUtils;
    private DataGridTicket dgt;

    @Before
    public void setUp() throws DataGridException, JargonException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);

        dgt = new DataGridTicket(targetPath);
        dgt.setUsesLimit(USES_LIMIT);
    }

    @After
    public void tearDown() throws JargonException {
        ticketUtils.deleteTicket(ticketString);
    }

    @Test
    public void testCreateTicketWithExpirationDate() throws DataGridConnectionRefusedException, DataGridTicketException,
            JargonException {
        ticketString = ticketService.create(dgt);
        Ticket ticketWithUses = ticketUtils.findTicket(ticketString);

        assertEquals(USES_LIMIT, ticketWithUses.getUsesLimit());
        assertFalse(ticketWithUses.getTicketString().isEmpty());
        assertTrue(ticketWithUses.getIrodsAbsolutePath().equals(targetPath));
        assertTrue(ticketWithUses.getOwnerName().equals(username));
    }
}
