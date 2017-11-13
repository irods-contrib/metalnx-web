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

package com.emc.metalnx.integration.test.utils;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.test.generic.UiTestUtilities;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.IRODSSimpleProtocolManager;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonFileOrCollAlreadyExistsException;
import org.irods.jargon.core.pub.*;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Set;


public class FileUtils {

    private static IRODSSimpleProtocolManager irodsSimpleProtocolManager;
    private static IRODSSession irodsSession;
    private static IRODSAccessObjectFactory irodsAccessObjectFactory;

    private static boolean initiated = false;

    private static final int MEGABYTE = 1 * 1024 * 1024;
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    public static final String RESOURCE_PATH = "/upload-tests/%s";

    public static DataObjectAO getDataObjectAO(String user, String pwd) {
    	try {
    		IRODSAccount account = authenticateUser(user, pwd);
			return irodsAccessObjectFactory.getDataObjectAO(account);
		} catch (JargonException e) {
			e.printStackTrace();
		} catch (DataGridException e) {
			e.printStackTrace();
		}
    	
		return null;
    }

    /**
     * Uploads a file to the data grid through the Metalnx UI.
     *
     * @param driver
     * @param files file names under the class path
     * @throws FailingHttpStatusCodeException
     * @throws MalformedURLException
     * @throws IOException
     */
    public static void uploadFileThroughUI(WebDriver driver, String... files)
            throws FailingHttpStatusCodeException, IOException {
        StringBuilder baseDir = new StringBuilder();
        baseDir.append(System.getProperty("user.dir"));
        baseDir.append(File.separator);
        baseDir.append("src");
        baseDir.append(File.separator);
        baseDir.append("test");
        baseDir.append(File.separator);
        baseDir.append("resources");
        baseDir.append(File.separator);
        baseDir.append("upload-tests");
        baseDir.append(File.separator);

        for (String file : files) {
            By tableRow = By.cssSelector("#treeViewTable tbody tr td");

            WebDriverWait wait = new WebDriverWait(driver, 10);
            wait.until(ExpectedConditions.visibilityOfElementLocated(tableRow));
            wait.until(ExpectedConditions.elementToBeClickable(By.id("uploadIcon"))).click();
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#browseButton")));

            JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
            jsExecutor.executeScript("document.getElementById('inputFiles').style.display='block';");

            String path = baseDir.toString() + file;
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#inputFiles"))).sendKeys(path);
            jsExecutor.executeScript("document.getElementById('inputFiles').style.display='none';");

            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#filesList p")));
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("#uploadButton"))).click();

            String base = driver.getWindowHandle();
            Set<String> set = driver.getWindowHandles();
            set.remove(base);
            driver.switchTo().window((String) set.toArray()[0]);
            driver.close();
            driver.switchTo().window(base);

            wait.until(ExpectedConditions.visibilityOfElementLocated(tableRow));
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable_filter input"))).click();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable_filter input"))).clear();
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("#treeViewTable_filter input"))).sendKeys(file);
            wait.until(ExpectedConditions.visibilityOfElementLocated(tableRow));
        }
    }

    /**
     * Uploads a list of files to the home directory of the current administrator user
     *
     * @param files
     *            array of {@link String} containing the files residing on the /upload-tests/ directory inside the classpath
     * @return confirmation {@link boolean} the confirmation that the operation is successful
     * @throws DataGridException
     */
    public static boolean uploadToHomeDirAsAdmin(String... files) throws DataGridException {
        String user = UiTestUtilities.RODS_USERNAME;
        String password = UiTestUtilities.RODS_PASSWORD;
        String path = String.format("/%s/home/%s", UiTestUtilities.IRODS_ZONE, user);

        return uploadWithUserToResc(user, password, path, UiTestUtilities.DEFAULT_RESC, files);
    }

    /**
     * Uploads a file to the given user's home directory
     *
     * @param user
     *            {@link String} the username
     * @param password
     *            {@link String} the password of the user
     * @param files
     *            array of {@link String} containing the files residing on the /upload-tests/ directory inside the classpath
     * @return rc {@link boolean} the confirmation that the operation is successful
     * @throws DataGridException
     */
    public static boolean uploadToHomeDirAsUser(String user, String password, String... files) throws DataGridException {
        String path = String.format("/%s/home/%s", UiTestUtilities.IRODS_ZONE, user);
        return uploadWithUserToResc(user, password, path, UiTestUtilities.DEFAULT_RESC, files);
    }

    /**
     * Uploads a list of files to the given path as a rodsadmin user
     *
     * @param path
     *            {@link String} the destination path
     * @param files
     *            array of {@link String} containing the files residing on the /upload-tests/ directory inside the classpath
     * @return rc {@link boolean} the confirmation that the operation is successful
     * @throws DataGridException
     */
    public static boolean uploadToDirAsAdmin(String path, String... files) throws DataGridException {
        String user = UiTestUtilities.RODS_USERNAME;
        String password = UiTestUtilities.RODS_PASSWORD;
        return uploadWithUserToResc(user, password, path, UiTestUtilities.DEFAULT_RESC, files);
    }

    /**
     * Uploads a list of files to the specified path as the given user
     *
     * @param user
     *            {@link String} the username
     * @param password
     *            {@link String} the password
     * @param path
     *            {@link String} the destination path
     * @param files
     *            array of {@link String} containing the files residing on the /upload-tests/ directory inside the classpath
     * @return rc {@link boolean} the confirmation that the operation is successful
     * @throws DataGridException
     */
    public static boolean uploadToDirAsUser(String user, String password, String path, String... files) throws DataGridException {
        return uploadWithUserToResc(user, password, path, UiTestUtilities.DEFAULT_RESC, files);
    }

    /**
     * Uploads a list of file to the rodsadmin's home folder on the given resource name
     *
     * @param rescName
     *            {@link String} the password
     * @param files
     *            array of {@link String} containing the files residing on the /upload-tests/ directory inside the classpath
     * @return rc {@link boolean} the confirmation that the operation is successful
     * @throws DataGridException
     */
    public static boolean uploadToHomeDirAsAdminOnResc(String rescName, String... files) throws DataGridException {
        String user = UiTestUtilities.RODS_USERNAME;
        String password = UiTestUtilities.RODS_PASSWORD;
        String path = String.format("/%s/home/%s", UiTestUtilities.IRODS_ZONE, user);
        return uploadWithUserToResc(user, password, path, rescName, files);
    }

    /**
     * Uploads a list of file to the given user's home folder on the given resource name
     *
     * @param user
     *            {@link String} the username
     * @param password
     *            {@link String} the password
     * @param rescName
     *            {@link String} the target resource name
     * @param files
     *            array of {@link String} containing the files residing on the /upload-tests/ directory inside the classpath
     * @return rc {@link boolean} the confirmation that the operation is successful
     * @throws DataGridException
     */
    public static boolean uploadToHomeDirAsUserOnResc(String user, String password, String rescName, String... files) throws DataGridException {
        String path = String.format("/%s/home/%s", UiTestUtilities.IRODS_ZONE, user);
        return uploadWithUserToResc(user, password, path, rescName, files);
    }

    /**
     * Uploads a list of files to the given directory on a given resource server as an admin user
     *
     * @param path
     *            {@link String} the destination path
     * @param rescName
     *            {@link String} the target resource name
     * @param files
     *            array of {@link String} containing the files residing on the /upload-tests/ directory inside the classpath
     * @return rc {@link boolean} the confirmation that the operation is successful
     * @throws DataGridException
     */
    public static boolean uploadToDirAsAdminOnResc(String path, String rescName, String... files) throws DataGridException {
        String user = UiTestUtilities.RODS_USERNAME;
        String password = UiTestUtilities.RODS_PASSWORD;
        return uploadWithUserToResc(user, password, path, rescName, files);
    }

    /**
     * Uploads a list of files to the given directory on a given resource server as the given user
     *
     * @param user
     *            {@link String} the username
     * @param password
     *            {@link String} the password
     * @param path
     *            {@link String} the destination path
     * @param rescName
     *            {@link String} the target resource name
     * @param files
     *            array of {@link String} containing the files residing on the /upload-tests/ directory inside the classpath
     * @return rc {@link boolean} the confirmation that the operation is successful
     * @throws DataGridException
     */
    public static boolean uploadToDirAsUserOnResc(String user, String password, String path, String rescName, String... files)
            throws DataGridException {
        return uploadWithUserToResc(user, password, path, rescName, files);
    }

    /**************************************************************************************/

    /**************************** REMOVE METHODS SECTION **********************************/
    /**************************************************************************************/

    /**
     * Remove the given list of files from the rodsadmin's home folder
     *
     * @param files
     *            array of {@link String} containing the file names to be removed from the grid
     * @throws DataGridException
     */
    public static void removeFilesFromHomeAsAdmin(String... files) throws DataGridException {
        String path = String.format("/%s/home/%s", UiTestUtilities.IRODS_ZONE, UiTestUtilities.RODS_USERNAME);
        remove(UiTestUtilities.RODS_USERNAME, UiTestUtilities.RODS_PASSWORD, path, false, files);
    }

    /**
     * Removes the list of files from the data grid as the given user
     *
     * @param user
     *            {@link String} the username
     * @param pwd
     *            {@link String} the password
     * @param files
     *            array of {@link String} containing the file names to be removed from the grid
     * @throws DataGridException
     */
    public static void removeFilesFromHomeAsUser(String user, String pwd, String... files) throws DataGridException {
        String path = String.format("/%s/home/%s", UiTestUtilities.IRODS_ZONE, user);
        remove(user, pwd, path, false, files);
    }

    /**
     * Removes the list of files from the grid on the specified path as a grid administrator
     *
     * @param path
     *            {@link String} the target path
     * @param files
     *            array of {@link String} containing the file names to be removed from the grid
     * @throws DataGridException
     */
    public static void removeFilesFromDirAsAdmin(String path, String... files) throws DataGridException {
        remove(UiTestUtilities.RODS_USERNAME, UiTestUtilities.RODS_PASSWORD, path, false, files);
    }

    /**
     * Removes the given list of files from the grid as the given user
     *
     * @param user
     *            {@link String} the username
     * @param pwd
     *            {@link String} the password
     * @param path
     *            {@link String} the target path
     * @param files
     *            array of {@link String} containing the file names to be removed from the grid
     * @throws DataGridException
     */
    public static void removeFilesFromDirAsUser(String user, String pwd, String path, String... files) throws DataGridException {
        remove(user, pwd, path, false, files);
    }

    /**
     * Remove (with force option) the given list of files from the rodsadmin's home folder
     *
     * @param files
     *            array of {@link String} containing the file names to be removed from the grid
     * @throws DataGridException
     */
    public static void forceRemoveFilesFromHomeAsAdmin(String... files) throws DataGridException {
        String path = String.format("/%s/home/%s", UiTestUtilities.IRODS_ZONE, UiTestUtilities.RODS_USERNAME);
        remove(UiTestUtilities.RODS_USERNAME, UiTestUtilities.RODS_PASSWORD, path, true, files);
    }

    /**
     * Removes (with force option) the list of files from the data grid as the given user
     *
     * @param user
     *            {@link String} the username
     * @param pwd
     *            {@link String} the password
     * @param files
     *            array of {@link String} containing the file names to be removed from the grid
     * @throws DataGridException
     */
    public static void forceRemoveFilesFromHomeAsUser(String user, String pwd, String... files) throws DataGridException {
        String path = String.format("/%s/home/%s", UiTestUtilities.IRODS_ZONE, user);
        remove(user, pwd, path, true, files);
    }

    /**
     * Removes (with force option) the list of files from the grid on the specified path as a grid administrator
     *
     * @param path
     *            {@link String} the target path
     * @param files
     *            array of {@link String} containing the file names to be removed from the grid
     * @throws DataGridException
     */
    public static void forceRemoveFilesFromDirAsAdmin(String path, String... files) throws DataGridException {
        remove(UiTestUtilities.RODS_USERNAME, UiTestUtilities.RODS_PASSWORD, path, true, files);
    }

    /**
     * Removes (with force option) the given list of files from the grid as the given user
     *
     * @param user
     *            {@link String} the username
     * @param pwd
     *            {@link String} the password
     * @param path
     *            {@link String} the target path
     * @param files
     *            array of {@link String} containing the file names to be removed from the grid
     * @throws DataGridException
     */
    public static void forceRemoveFilesFromDirAsUser(String user, String pwd, String path, String... files) throws DataGridException {
        remove(user, pwd, path, true, files);
    }

    /**************************************************************************************/
    /**************************** PRIVATE METHOD SECTION **********************************/
    /**************************************************************************************/

    /**
     * Auxiliary method that initializes the Jargon context
     * for this class. It aims to create the Access Object
     * Factory which gives us access to all other access objects
     * available on Jargon.
     *
     * @throws DataGridException
     */
    private static void init() throws DataGridException {
        try {
            if (!initiated) {
                irodsSimpleProtocolManager = new IRODSSimpleProtocolManager();
                irodsSimpleProtocolManager.initialize();
                irodsSession = new IRODSSession(irodsSimpleProtocolManager);
                irodsAccessObjectFactory = new IRODSAccessObjectFactoryImpl(irodsSession);
                initiated = true;
            }
        }
        catch (JargonException e) {
            String message = "Could not instantiate Jargon Protocol Handler";
            logger.error(message, e);
            throw new DataGridException(message);
        }
    }

    /**
     * Authenticates user agains iRODS and returns the authenticated
     * IRODSAccount object.
     *
     * @param user
     *            {@link String} containing the username to be used on login
     * @param password
     *            {@link String} containing the password to be used on login
     * @return
     * @throws DataGridException
     */
    private static IRODSAccount authenticateUser(String user, String password) throws DataGridException {
        // Connection information
        String host = UiTestUtilities.IRODS_HOST;
        int port = UiTestUtilities.IRODS_PORT;
        String zone = UiTestUtilities.IRODS_ZONE;
        String homeDir = String.format("/%s/home/%s", zone, user);
        String defStorage = "demoResc";

        IRODSAccount irodsAccount = null;
        AuthResponse response = null;

        if (!initiated) {
            init();
        }

        // Creating iRODSAccount instance
        try {
            irodsAccount = IRODSAccount.instance(host, port, user, password, homeDir, zone, defStorage);
            irodsAccount.setAuthenticationScheme(AuthScheme.STANDARD);
            response = irodsAccessObjectFactory.authenticateIRODSAccount(irodsAccount);
            logger.info("User {} connected and authenticated", user);
        }
        catch (JargonException e) {
            logger.error("Could not authenticate user {} instance due to: ", user, e);
        }

        return response.getAuthenticatedIRODSAccount();
    }

    /**
     * Closes the session on iRODS using an authenticated account
     *
     * @param account
     *            {@link IRODSAccount} the authenticated account
     * @throws DataGridException
     */
    private static void closeSession(IRODSAccount account) throws DataGridException {

        if (!initiated) {
            init();
        }

        logger.info("Closing session for user {}", account.getUserName());
        irodsAccessObjectFactory.closeSessionAndEatExceptions(account);
        irodsAccessObjectFactory.closeSessionAndEatExceptions();
    }

    /**
     * Generic method that uploads a list of files to a given directory using
     * the specified user on a given resource server.
     *
     * @param user
     *            {@link String} the username
     * @param pwd
     *            {@link String} the password
     * @param path
     *            {@link String} the destination path
     * @param resc
     *            {@link String} the resource name
     * @param files
     *            array of {@link String} containing the file names present on the classpath:/upload-tests/ directory
     * @return rc {@link boolean} the confirmation that the operation is successful
     * @throws DataGridException
     */
    private static boolean uploadWithUserToResc(String user, String pwd, String path, String resc, String... files) throws DataGridException {

        IRODSAccount account = authenticateUser(user, pwd);
        boolean result = true;

        try {
            Stream2StreamAO streamAO = irodsAccessObjectFactory.getStream2StreamAO(account);
            IRODSFileFactory fileFactory = irodsAccessObjectFactory.getIRODSFileFactory(account);

            for (String file : files) {
                logger.info("Trying to upload file {} to path {} as {}", file, path, user);
                IRODSFile targetFile = fileFactory.instanceIRODSFile(path, file);
                targetFile.setResource(resc);

                logger.info("Checking if file already exists.");
                if (targetFile.exists()) {
                    logger.error("File {}/{} already exists. Not uploading.", path, file);
                    throw new JargonFileOrCollAlreadyExistsException(String.format("File %s already exists on %s", file, path));
                }

                logger.info("Reading original file from classpath.");
                String originalFilePath = String.format(RESOURCE_PATH, file);
                InputStream in = ClassLoader.class.getResourceAsStream(originalFilePath);

                logger.info("Transfering file to iRODS");
                streamAO.transferStreamToFileUsingIOStreams(in, (File) targetFile, 0, MEGABYTE);
                logger.info("Done!");

                in.close();
                targetFile.close();
            }

        }
        catch (JargonException | IOException e) {
            logger.info("Could not upload to path {}", path, e);
            result = false;
        }

        closeSession(account);

        return result;
    }

    /**
     * Removes the given file from the iRODS grid
     *
     * @param user
     *            {@link String} the username
     * @param pwd
     *            {@link String} the password
     * @param path
     *            {@link String} the target path
     * @param force
     *            {@link boolean} flag indicating whether the operation must be forced or not
     * @param files
     *            array of {@link String} containing the file names to be removed from the grid
     * @throws DataGridException
     */
    private static void remove(String user, String pwd, String path, boolean force, String... files) throws DataGridException {
        IRODSAccount account = authenticateUser(user, pwd);

        try {
            IRODSFileFactory fileFactory = irodsAccessObjectFactory.getIRODSFileFactory(account);
            IRODSFileSystemAO irodsFileSystemAO = irodsAccessObjectFactory.getIRODSFileSystemAO(account);

            for (String file : files) {
                logger.info("Trying to remove file {} to path {} as {}", file, path, user);
                IRODSFile targetFile = fileFactory.instanceIRODSFile(path, file);

                if (force) {
                    irodsFileSystemAO.fileDeleteForce(targetFile);
                }
                else if (irodsFileSystemAO.isFileExists(targetFile)) {
                    irodsFileSystemAO.fileDeleteNoForce(targetFile);
                }
            }

        }
        catch (JargonException e) {
            logger.info("Could not remove file path {}", path, e);
        }

        closeSession(account);
    }

}
