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
}
