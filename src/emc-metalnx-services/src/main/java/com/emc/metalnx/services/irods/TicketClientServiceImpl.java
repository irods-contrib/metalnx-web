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

import com.emc.metalnx.core.domain.exceptions.DataGridFileNotFoundException;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.TicketClientService;
import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
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
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class TicketClientServiceImpl implements TicketClientService {
    private static final Logger logger = LoggerFactory.getLogger(TicketClientServiceImpl.class);
    private static final String TEMP_TICKET_DIR = "tmp-ticket-files";
    public static final String ZIP_EXTENSION = ".zip";

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
    public InputStream getFileFromIRODSUsingTicket(String ticketString, String path)
            throws DataGridFileNotFoundException {
        deleteTempTicketDir();

        File tempDir = new File(TEMP_TICKET_DIR);

        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        InputStream inputStream = null;
        try {
            IRODSFileFactory irodsFileFactory = irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount);
            IRODSFile irodsFile = irodsFileFactory.instanceIRODSFile(path);
            ticketClientOperations.getOperationFromIRODSUsingTicket(ticketString, irodsFile, tempDir, null, null);
            inputStream = getInputStream(tempDir, path);
        } catch (FileNotFoundException | java.io.FileNotFoundException e) {
            logger.error("Get file using a ticket: File Not Found: {}", e);
            throw new DataGridFileNotFoundException(e.getMessage());
        } catch (JargonException e) {
            logger.error("Could not get file from grid using ticket: {}", e);
        }

        return inputStream;
    }

    @Override
    public void deleteTempTicketDir() {
        FileUtils.deleteQuietly(new File(TEMP_TICKET_DIR));
    }

    /**
     * Gets an input stream from a file within a directory
     * @param directory directory to look for a particular file or another directory
     * @param path of the file
     * @return stream for the file
     * @throws DataGridFileNotFoundException
     * @throws java.io.FileNotFoundException
     */
    private InputStream getInputStream(File directory, String path) throws
            DataGridFileNotFoundException, java.io.FileNotFoundException {
        String filename = path.substring(path.lastIndexOf("/") + 1, path.length());
        File obj = findFileInDirectory(directory, filename);
        File file = obj;

        if (obj.isDirectory()) {
            file = createZip(directory, obj);
        }

        return new FileInputStream(file);
    }

    private File createZip(File directoryToPlaceZip, File directoryToZip) {
        List<File> files = new ArrayList<>();
        getAllFiles(directoryToZip, files);
        return writeZipFile(directoryToPlaceZip, directoryToZip, files);
    }

    private void getAllFiles(File dir, List<File> files) {
        File[] filesArr = dir.listFiles();

        if (filesArr == null) {
            return;
        }

        for (File file: filesArr) {
            files.add(file);
            if (file.isDirectory()) {
                getAllFiles(file, files);
            }
        }
    }

    private File writeZipFile(File directoryToPlaceZip, File directoryToZip, List<File> fileList) {
        String zipFileName = String.format("%s/%s%s", directoryToPlaceZip.getName(),
                directoryToZip.getName(), ZIP_EXTENSION);

        FileOutputStream fos = null;
        ZipOutputStream zos = null;

        try {
            fos = new FileOutputStream(zipFileName);
            zos = new ZipOutputStream(fos);

            for (File file : fileList) {
                if (!file.isDirectory()) { // we only zip files, not directories
                    addToZip(directoryToZip, file, zos);
                }
            }
        } catch (FileNotFoundException | IOException e) {
            logger.error("Could not create zip file");
        } finally {
            try {
                if (zos != null) zos.close();
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return new File(zipFileName);
    }

    private static void addToZip(File directoryToZip, File file, ZipOutputStream zos) throws FileNotFoundException,
            IOException {

        FileInputStream fis = new FileInputStream(file);

        // we want the zipEntry's path to be a relative path that is relative
        // to the directory being zipped, so chop off the rest of the path
        String zipFilePath = file.getCanonicalPath().substring(directoryToZip.getCanonicalPath().length() + 1,
                file.getCanonicalPath().length());
        System.out.println("Writing '" + zipFilePath + "' to zip file");
        ZipEntry zipEntry = new ZipEntry(zipFilePath);
        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[1024];
        int length;
        while ((length = fis.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
        fis.close();
    }

    /**
     * Finds a file/directory within another directory
     * @param directory directory to look for files
     * @param filename file where are looking for
     * @return File representing the file found within the given directory
     * @throws DataGridFileNotFoundException if Metalnx cannot find the file locally
     */
    private File findFileInDirectory(File directory, String filename) throws DataGridFileNotFoundException {
        File[] files = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String filename) {
                return filename.equals(filename);
            }
        });

        if (files == null || files.length == 0) {
            throw new DataGridFileNotFoundException("Could not find files locally");
        }

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
