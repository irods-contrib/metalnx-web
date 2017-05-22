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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Client access to the grid via Tickets.
 */
public interface TicketClientService {

    /**
     * Transfers a file to the grid using a ticket.
     * @param ticketString ticket string
     * @param file file to be transferred to the grid
     * @param destPath path where the file will be uploaded to
     */
    void transferFileToIRODSUsingTicket(String ticketString, File file, String destPath);

    /**
     * Gets a file from the grid.
     * @param ticketString ticket string to access a collection or an object
     * @param path path to get files from
     * @return {@code InputStream} to stream the content
     */
    InputStream getFileFromIRODSUsingTicket(String ticketString, String path) throws IOException;

    /**
     * Deletes the temporary directory created after downloading files from the grid using a ticket.
     */
    void deleteTempTicketDir();
}
