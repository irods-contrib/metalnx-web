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

package com.emc.metalnx.services.irods;

import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.TicketClientService;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.ticket.TicketClientOperations;
import org.irods.jargon.ticket.TicketServiceFactory;
import org.irods.jargon.ticket.TicketServiceFactoryImpl;
import org.irods.jargon.ticket.io.FileStreamAndInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class TicketClientServiceImpl implements TicketClientService {
    private static final Logger logger = LoggerFactory.getLogger(TicketClientServiceImpl.class);
    private static final String TEMP_TICKET_DIR = "tmp-ticket-files";

    @Autowired
    private ConfigService configService;

    private static final String ANONYMOUS_HOME_DIRECTORY = "";

    private TicketServiceFactory ticketServiceFactory;
    private TicketClientOperations ticketClientOperations;
    private IRODSAccount irodsAccount;
    private IRODSAccessObjectFactory irodsAccessObjectFactory;
    private String host, zone, defaultStorageResource;
    private int port;

    @PostConstruct
    public void init() {
        host = configService.getIrodsHost();
        zone = configService.getIrodsZone();
        port = Integer.valueOf(configService.getIrodsPort());
        defaultStorageResource = "";
        setUpAnonymousAccess();
    }

    @Override
    public void transferFileToIRODSUsingTicket(String ticketString, File file, String destPath) {
        try {
            IRODSFileFactory irodsFileFactory = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount);
            IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(destPath);
            ticketClientOperations.putFileToIRODSUsingTicket(ticketString, file, irodsFile, null, null);
        } catch (JargonException e) {
            logger.error("Could not transfer file to the grid using a ticket: {}", e);
        }
    }

    @Override
    public InputStream getFileFromIRODSUsingTicket(String ticketString, String path) throws IOException {
        InputStream inputStream = null;

        File tempDir = new File(TEMP_TICKET_DIR);

        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        try {
            IRODSFileFactory irodsFileFactory = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount);
            IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(path);
            FileStreamAndInfo fileStreamAndInfo  = ticketClientOperations.redeemTicketGetDataObjectAndStreamBack(
                    ticketString, irodsFile, tempDir);
            inputStream = fileStreamAndInfo.getInputStream();
        } catch (JargonException e) {
            logger.error("Could not get file from grid using ticket: {}", e);
        }

        if (inputStream == null) {
            deleteTempTicketDir();
        }

        return inputStream;
    }

    @Override
    public void deleteTempTicketDir() {
        File tempDir = new File(TEMP_TICKET_DIR);

        if (tempDir.exists()) {
            tempDir.delete();
        }
    }

    /**
     * Sets up all necessary stuff for an anonymous user to be able to interact with the grid. This interaction means
     * iput & iget.
     */
    private void setUpAnonymousAccess() {
        try {
            IRODSFileSystem irodsFileSystem = IRODSFileSystem.instance();
            irodsAccessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
            irodsAccount = IRODSAccount.instanceForAnonymous(host, port, ANONYMOUS_HOME_DIRECTORY, zone,
                    defaultStorageResource);
            ticketServiceFactory = new TicketServiceFactoryImpl(irodsAccessObjectFactory);
            ticketClientOperations = ticketServiceFactory.instanceTicketClientOperations(irodsAccount);
        } catch (JargonException e) {
            logger.error("Could not set up anonymous access");
        }
    }
}
