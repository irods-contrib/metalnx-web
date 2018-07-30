 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.tests.tickets;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.ticket.Ticket;
import org.irods.jargon.ticket.TicketAdminService;
import org.irods.jargon.ticket.packinstr.TicketCreateModeEnum;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Utils class for ticket operations during tests.
 */
public class TestTicketUtils {
    private IRODSServices irodsServices;
    private TicketAdminService ticketAdminService;
    public static final String TICKET_FILE_CONTENT = "This is a test for ticket";

    public TestTicketUtils(IRODSServices irodsServices) throws DataGridConnectionRefusedException {
        this.irodsServices = irodsServices;
        this.ticketAdminService = irodsServices.getTicketAdminService();
    }

    public List<Ticket> listAllTicketsForUser() throws JargonException {
        return ticketAdminService.listAllTickets(0);
    }

    public void deleteIRODSFile(String path) throws JargonException, DataGridConnectionRefusedException {
        IRODSFile ticketIRODSFile = irodsServices.getIRODSFileFactory().instanceIRODSFile(path);
        if(ticketIRODSFile != null && ticketIRODSFile.exists()) {
            irodsServices.getIRODSFileSystemAO().fileDeleteForce(ticketIRODSFile);
        }
    }

    public String createTicket(String parentPath, String item, TicketCreateModeEnum type) throws JargonException, DataGridConnectionRefusedException {
        IRODSFile irodsFile = irodsServices.getIRODSFileFactory().instanceIRODSFile(parentPath, item);
        return ticketAdminService.createTicket(type, irodsFile, "");
    }

    public String createTicket(String parentPath, String item) throws JargonException, DataGridConnectionRefusedException {
        return createTicket(parentPath, item, TicketCreateModeEnum.READ);
    }

    public void deleteTicket(String ticketString) throws JargonException {
        ticketAdminService.deleteTicket(ticketString);
    }

    public void deleteAllTicketsForThisUser() throws JargonException {
        ticketAdminService.deleteAllTicketsForThisUser();
    }

    public void setUsesLimit(String ticketString, int usesLimit) throws JargonException {
        ticketAdminService.setTicketUsesLimit(ticketString, usesLimit);
    }

    public void setExpirationDate(String ticketString, Date expirationDate)
            throws DataGridConnectionRefusedException, JargonException {
        ticketAdminService.setTicketExpiration(ticketString, expirationDate);
    }

    public void setWriteByteLimit(String ticketString, long writeByteLimit) throws JargonException {
        ticketAdminService.setTicketByteWriteLimit(ticketString, writeByteLimit);
    }

    public void setWriteFileLimit(String ticketString, int writeFileLimit) throws JargonException {
        ticketAdminService.setTicketFileWriteLimit(ticketString, writeFileLimit);
    }

    public void addHostRestriction(String ticketString, String host) throws JargonException {
        ticketAdminService.addTicketHostRestriction(ticketString, host);
    }

    public void addUserRestriction(String ticketString, String username) throws JargonException {
        ticketAdminService.addTicketUserRestriction(ticketString, username);
    }

    public void addGroupRestriction(String ticketString, String group) throws JargonException {
        ticketAdminService.addTicketGroupRestriction(ticketString, group);
    }

    public Ticket findTicket(String ticketString) throws JargonException {
        return ticketAdminService.getTicketForSpecifiedTicketString(ticketString);
    }

    public List<String> listAllHostRestrictionsForSpecifiedTicket(String ticketString) throws JargonException {
        return ticketAdminService.listAllHostRestrictionsForSpecifiedTicket(ticketString, 0);
    }

    public List<String> listAllUserRestrictionsForSpecifiedTicket(String ticketString) throws JargonException {
        return ticketAdminService.listAllUserRestrictionsForSpecifiedTicket(ticketString, 0);
    }

    public List<String> listAllGroupRestrictionsForSpecifiedTicket(String ticketString) throws JargonException {
        return ticketAdminService.listAllGroupRestrictionsForSpecifiedTicket(ticketString, 0);
    }

    public File createLocalFile(String filename) throws IOException {
        File file = new File(filename);
        FileUtils.writeByteArrayToFile(file, TICKET_FILE_CONTENT.getBytes());
        return file;
    }

    public File createLocalFile() throws IOException {
        return createLocalFile("test-ticket-file-" + System.currentTimeMillis());
    }
}
