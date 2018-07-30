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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.Assert.assertFalse;

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

    private String targetPath, ticketString;
    private TestTicketUtils ticketUtils;

    @Before
    public void setUp() throws DataGridException, JargonException {
        String parentPath = String.format("/%s/home", zone);
        targetPath = String.format("%s/%s", parentPath, username);
        ticketUtils = new TestTicketUtils(irodsServices);
    }

    @After
    public void tearDown() throws JargonException {
        if (ticketString != null && !ticketString.isEmpty() ) {
            ticketUtils.deleteTicket(ticketString);
        }
    }

    @Test
    public void testCreateTicket() throws DataGridConnectionRefusedException, DataGridTicketException {
        ticketString = ticketService.create(new DataGridTicket(targetPath));
        assertFalse(ticketString.isEmpty());
    }

    @Test(expected = DataGridTicketException.class)
    public void testCreateNullTicket() throws DataGridConnectionRefusedException, DataGridTicketException {
        ticketString = ticketService.create(null);
    }

    @Test(expected = DataGridTicketException.class)
    public void testCreateTicketWithMissingPath() throws DataGridConnectionRefusedException, DataGridTicketException {
        ticketString = ticketService.create(new DataGridTicket());
    }
}
