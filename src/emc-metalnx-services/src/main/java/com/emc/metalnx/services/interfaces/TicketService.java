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

package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.DataGridTicket;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridMissingPathOnTicket;

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
     * @param ticketId string that identifies the ticket uniquely (String or ID)
     * @return True, if the ticket was deleted successfully. False, otherwise.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the grid
     */
    boolean delete(String ticketId) throws DataGridConnectionRefusedException;

    /**
     * Creates a ticket in the grid
     * @param dgTicket Ticket to be created.
     * @return String representing the ticket string if ticket was created successfully.
     *  Empty string is returned if the ticket was not created.
     * @throws DataGridMissingPathOnTicket thrown when the path is missing on the ticket
     * @throws DataGridConnectionRefusedException thrown if Metalnx cannot connect to Metalnx
     */
    String create(DataGridTicket dgTicket) throws DataGridMissingPathOnTicket, DataGridConnectionRefusedException;
}
