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

import com.emc.metalnx.core.domain.exceptions.DataGridTicketDownloadException;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketInvalidUser;
import com.emc.metalnx.core.domain.exceptions.DataGridTicketUploadException;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.TicketClientService;
import com.emc.metalnx.services.interfaces.ZipService;
import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.*;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.ticket.TicketClientOperations;
import org.irods.jargon.ticket.TicketServiceFactory;
import org.irods.jargon.ticket.TicketServiceFactoryImpl;
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
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class TicketClientServiceImpl implements TicketClientService {
    private static final Logger logger = LoggerFactory.getLogger(TicketClientServiceImpl.class);
    private static final String TEMP_TICKET_DIR = "tmp-ticket-files";

    @Autowired
    private ConfigService configService;

    @Autowired
    private ZipService zipService;

    private static final String ANONYMOUS_HOME_DIRECTORY = "";

    private TicketServiceFactory ticketServiceFactory;
    private TicketClientOperations ticketClientOperations;
    private IRODSAccount irodsAccount;
    private IRODSAccessObjectFactory irodsAccessObjectFactory;
    private String host, zone, defaultStorageResource;
    private int port;
    private Map<Integer, String> ticketErroCodeMap;

    @PostConstruct
    public void init() {
        host = configService.getIrodsHost();
        zone = configService.getIrodsZone();
        port = Integer.valueOf(configService.getIrodsPort());
        defaultStorageResource = "";
        setUpAnonymousAccess();
        ticketErroCodeMap = new HashMap<>();
        ticketErroCodeMap.put(-891000, "Ticket expired");
        ticketErroCodeMap.put(-892000, "Ticket uses exceeded");
        ticketErroCodeMap.put(-893000, "Ticket user excluded");
        ticketErroCodeMap.put(-894000, "Ticket host excluded");
        ticketErroCodeMap.put(-895000, "Ticket group excluded");
        ticketErroCodeMap.put(-896000, "Ticket write uses exceeded");
        ticketErroCodeMap.put(-526020, "Destination not a directory");
    }

    @Override
    public void transferFileToIRODSUsingTicket(String ticketString, File localFile, String destPath)
            throws DataGridTicketUploadException, DataGridTicketInvalidUser {
        if (ticketString == null || ticketString.isEmpty()) {
            throw new DataGridTicketUploadException("Ticket String not provided");
        } else if (destPath == null || destPath.isEmpty()) {
            throw new DataGridTicketUploadException("Ticket path not provided");
        } else if (localFile == null) {
            throw new DataGridTicketUploadException("File not provided");
        }

        try {
            IRODSFileFactory irodsFileFactory = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount);
            String targetPath = String.format("%s/%s", destPath, localFile.getName());
            IRODSFile targetFile = irodsFileFactory.instanceIRODSFile(targetPath);
            ticketClientOperations.putFileToIRODSUsingTicket(ticketString, localFile, targetFile, null, null);
        } catch (InvalidUserException e) {
            logger.error("Invalid user. Cannot download files as anonymous.");
            throw new DataGridTicketInvalidUser("Invalid user anonymous");
        } catch (OverwriteException | DuplicateDataException e) {
            logger.error("Could not transfer file to the grid. File already exists: {}", e);
            throw new DataGridTicketUploadException("File already exists");
        } catch(CatNoAccessException e) {
            logger.error("Could not transfer file to the grid. Cat no access: {}", e);
            throw new DataGridTicketUploadException(e.getMessage());
        } catch (DataNotFoundException e) {
            logger.error("Could not transfer file to the grid. File not found: {}", e);
            throw new DataGridTicketUploadException("File not found");
        } catch (JargonException e) {
            logger.error("Could not transfer file to the grid using a ticket: {}", e);
            int code = e.getUnderlyingIRODSExceptionCode();
            String msg = "Transfer failed";
            if (ticketErroCodeMap.containsKey(code)) {
                msg = ticketErroCodeMap.get(code);
            }
            throw new DataGridTicketUploadException(msg);
        } finally {
            FileUtils.deleteQuietly(localFile);
        }
    }

    @Override
    public File getFileFromIRODSUsingTicket(String ticketString, String path)
            throws DataGridTicketInvalidUser, DataGridTicketDownloadException {
        deleteTempTicketDir();

        File tempDir = new File(TEMP_TICKET_DIR);

        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        File file;
        try {
            IRODSFileFactory irodsFileFactory = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount);
            IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(path);
            ticketClientOperations.getOperationFromIRODSUsingTicket(ticketString, irodsFile, tempDir, null, null);

            String filename = path.substring(path.lastIndexOf("/") + 1, path.length());
            File obj = findFileInDirectory(tempDir, filename);

            if (obj == null) {
                throw new DataGridTicketDownloadException("File not found", path, ticketString);
            }

            file = obj;

            if (obj.isDirectory()) {
                file = zipService.createZip(tempDir, obj);
            }
        } catch (InvalidUserException e) {
            logger.error("Invalid user. Cannot download files as anonymous.");
            throw new DataGridTicketInvalidUser("Invalid user anonymous");
        } catch (FileNotFoundException e) {
            logger.error("Could not get file using ticket. File not found: {}", e);
            throw new DataGridTicketDownloadException("File not found", path, ticketString);
        } catch (JargonException e) {
            logger.error("Get file using a ticket caused an error: {}", e);
            int code = e.getUnderlyingIRODSExceptionCode();

            String msg = "Download failed";
            if (ticketErroCodeMap.containsKey(code)) {
                msg = ticketErroCodeMap.get(code);
            }

            throw new DataGridTicketDownloadException(msg, path, ticketString);
        }

        return file;
    }

    @Override
    public void deleteTempTicketDir() {
        FileUtils.deleteQuietly(new File(TEMP_TICKET_DIR));
    }

    /**
     * Finds a file/directory within another directory
     * @param directory directory to look for files
     * @param filename file where are looking for
     * @return File representing the file found within the given directory
     */
    private File findFileInDirectory(File directory, String filename) {
        File[] files = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return filename.equals(name);
            }
        });

        if (files == null || files.length == 0) return null;

        return files[0];
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
