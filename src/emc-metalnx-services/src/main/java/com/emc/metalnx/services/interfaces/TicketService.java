 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.DataGridTicket;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketNotFoundException;

import java.util.List;

/**
 * Service for tickets in the grid.
 */
public interface TicketService {

    /**
     * Finds all tickets existing in the system.
     * The tickets found depend on the user who requests the list of tickets. RODS_ADMINs can see all tickets while
     * RODS_USERs can only see the tickets they have created.
     * @return List of tickets if any found. Empty list is returned if no tickets are found.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the grid.
     */
    List<DataGridTicket> findAll() throws DataGridConnectionRefusedException;

    /**
     * Deletes a ticket from the grid by the ticket string
     * @param ticketString string that identifies the ticket uniquely
     * @return True, if the ticket was deleted successfully. False, otherwise.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the grid
     */
    boolean delete(String ticketString) throws DataGridConnectionRefusedException;

    /**
     * Deletes a list of tickets from the grid.
     *
     * Obs: if the user is a rods admin, all tickets existing in the grid can be deleted.
     * @param ticketStrings list of ticket strings that will be deleted
     * @return True, if all given tickets were deleted successfully. False, otherwise.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the grid.
     */
    boolean bulkDelete(List<String> ticketStrings) throws DataGridConnectionRefusedException;

    /**
     * Creates a ticket in the grid
     * @param dgTicket Ticket to be created.
     * @return String representing the ticket string.
     * @throws DataGridConnectionRefusedException thrown if Metalnx cannot connect to Metalnx
     * @throws DataGridTicketException thrown if an error occurs when setting any ticket parameter
     */
    String create(DataGridTicket dgTicket) throws DataGridConnectionRefusedException, DataGridTicketException;

    /**
     * Finds a specific ticket by its id or string.
     * @param ticketId ticket ID or string
     * @return Ticket object if a ticket with the given ID exists. Null is returned otherwise.
     * @throws DataGridConnectionRefusedException thrown if Metalnx cannot connect to the grid
     * @throws DataGridTicketNotFoundException thrown if ticket cannot be found
     */
    DataGridTicket find(String ticketId) throws DataGridConnectionRefusedException, DataGridTicketNotFoundException;

    /**
     * Modifies a ticket in the grid
     * @param t Ticket to be modified.
     *                 Ticket ID or String has to exist in the grid in order for the ticket to be modified.
     * @return DataGridTicket representing the ticket just modified.
     *  Null is returned if the ticket was not modified.
     * @throws DataGridConnectionRefusedException thrown if Metalnx cannot connect to Metalnx
     * @throws DataGridTicketException thrown if an error occurs when modifying any ticket parameter
     */
    DataGridTicket modify(DataGridTicket t) throws DataGridConnectionRefusedException, DataGridTicketException;
}
