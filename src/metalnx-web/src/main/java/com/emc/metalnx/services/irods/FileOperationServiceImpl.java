/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.irods;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.TrashOperationsAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.pub.io.IRODSFileInputStream;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileCopyUtils;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridChecksumException;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.FileOperationService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.MetadataService;
import com.emc.metalnx.services.interfaces.RuleService;

@Service
@Transactional
public class FileOperationServiceImpl implements FileOperationService {

	private static final String CONTENT_TYPE = "application/octet-stream";
	private static final String HEADER_FORMAT = "attachment;filename=\"%s\"";
	private static final Logger logger = LogManager.getLogger(FileOperationServiceImpl.class);

	@Autowired
	private IRODSServices irodsServices;

	@Autowired
	private MetadataService metadataService;

	@Autowired
	private RuleService rs;

	@Override
	public boolean copy(String sourcePath, String dstPath, boolean copyWithMetadata)
			throws DataGridConnectionRefusedException {
		IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();
		DataTransferOperations dataTransferOperations = irodsServices.getDataTransferOperations();

		boolean isCopied = false;
		try {
			IRODSFile source = irodsFileFactory.instanceIRODSFile(sourcePath);
			IRODSFile target = irodsFileFactory.instanceIRODSFile(dstPath);
			dataTransferOperations.copy(source, target, null, null);
			isCopied = true;

			if (copyWithMetadata) {
				String objName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1, sourcePath.length());
				dstPath = String.format("%s/%s", dstPath, objName);
				metadataService.copyMetadata(sourcePath, dstPath);
			}
		} catch (JargonException e) {
			logger.error("Could not copy item from " + sourcePath + " to " + dstPath + ": ", e.getMessage());
		}

		return isCopied;
	}

	@Override
	public boolean copy(List<String> sourcePaths, String dstPath, boolean copyWithMetadata)
			throws DataGridConnectionRefusedException {
		boolean isCopied = true;

		for (String sourcePath : sourcePaths) {
			isCopied &= this.copy(sourcePath, dstPath, copyWithMetadata);
		}

		return isCopied;
	}

	@Override
	public boolean deleteItem(String path, boolean force) throws DataGridConnectionRefusedException {
		if (path == null || path.isEmpty())
			return false;

		IRODSFileSystemAO irodsFileSystemAO = irodsServices.getIRODSFileSystemAO();
		IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();
		boolean itemDeleted = false;

		try {
			IRODSFile itemToBeRemoved = irodsFileFactory.instanceIRODSFile(path);

			if (irodsFileSystemAO.isDirectory(itemToBeRemoved)) {
				// if force set to true, we do an irm -rf
				if (force) {
					logger.info("Deleting directory (force) {}", path);
					irodsFileSystemAO.directoryDeleteForce(itemToBeRemoved);
				}
				// irm
				else {
					logger.info("Deleting directory {}", path);
					irodsFileSystemAO.directoryDeleteNoForce(itemToBeRemoved);
				}
			} else {
				// if force set to false, we do an irm
				if (force) {
					logger.info("Deleting data obj (force) {}", path);
					irodsFileSystemAO.fileDeleteForce(itemToBeRemoved);
				}
				// irm
				else {
					logger.info("Deleting data obj {}", path);
					irodsFileSystemAO.fileDeleteNoForce(itemToBeRemoved);
				}
			}

			itemDeleted = true;

		} catch (JargonException e) {
			logger.error("Could not delete item " + path + ": ", e);
		}

		return itemDeleted;
	}

	@Override
	public boolean deleteCollection(String collectionPath, boolean forceFlag) throws DataGridException {
		IRODSFileSystemAO irodsFileSystemAO = irodsServices.getIRODSFileSystemAO();
		IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();

		try {
			IRODSFile collectionToBeRemoved = irodsFileFactory.instanceIRODSFile(collectionPath);

			if (forceFlag) {
				irodsFileSystemAO.directoryDeleteForce(collectionToBeRemoved);
			} else {
				irodsFileSystemAO.directoryDeleteNoForce(collectionToBeRemoved);
			}
			return true;
		} catch (JargonException e) {
			logger.error("Could not delete collection: ", e.getMessage());
		}

		return false;
	}

	@Override
	public boolean deleteDataObject(String dataObjectPath, boolean forceFlag) throws DataGridException {

		boolean dataObjDeleted = false;

		try {
			IRODSFileSystemAO irodsFileSystemAO = irodsServices.getIRODSFileSystemAO();
			IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();

			IRODSFile fileToBeRemoved = irodsFileFactory.instanceIRODSFile(dataObjectPath);
			if (forceFlag) {
				irodsFileSystemAO.fileDeleteForce(fileToBeRemoved);
			} else {
				irodsFileSystemAO.fileDeleteNoForce(fileToBeRemoved);
			}

			dataObjDeleted = true;
		} catch (JargonException e) {
			logger.error("Could not delete data object: {}", e.getMessage());
		}

		return dataObjDeleted;
	}

	@Override
	public boolean deleteReplica(String path, String fileName, int replicaNumber, boolean inAdminMode)
			throws DataGridConnectionRefusedException {

		boolean deleteSuccess = false;
		try {
			String parentPath = path.substring(0, path.lastIndexOf("/"));
			irodsServices.getDataObjectAO().trimDataObjectReplicas(parentPath, fileName, "", -1, replicaNumber,
					inAdminMode);
			deleteSuccess = true;
		} catch (DataNotFoundException e) {
			logger.error("Data object could not be found: " + e.toString());
		} catch (JargonException e) {
			logger.error("Delete replica operation failed: " + e.toString());
		}
		return deleteSuccess;
	}

	@Override
	public boolean download(String path, HttpServletResponse response, boolean removeTempCollection)
			throws DataGridException, JargonException {

		logger.debug("Downloading file path: {}", path);

		boolean isDownloadSuccessful = false;

		if (path == null || path.isEmpty() || response == null) {
			return false;
		}

		logger.debug("Copying file into the HTTP response");
		isDownloadSuccessful = copyFileIntoHttpResponse(path, response);

		// getting the temporary collection name from the path
		String tempColl = path.substring(0, path.lastIndexOf("/"));

		logger.debug("Removing any temporary collections and compressed files created for downloading");
		if (removeTempCollection) {
			logger.debug("Removing temporary dataObj");
			deleteDataObject(path, removeTempCollection);

			logger.debug("Removing temporary collection");
			deleteCollection(tempColl, removeTempCollection);
		}

		return isDownloadSuccessful;
	}

	@Override
	public boolean emptyTrash(DataGridUser user, String collectionPath)
			throws DataGridConnectionRefusedException, JargonException {

		logger.info("emptyTrash()");
		if (user == null || collectionPath == null || collectionPath.isEmpty()) {
			return false;
		}

		logger.info("user:{}", user);
		logger.info("collectionPath:{}", collectionPath);

		boolean itemsDeleted = false;

		try {

			TrashOperationsAO trashOperationsAO = irodsServices.getTrashOperationsAO();

			/*
			 * if (user.isAdmin()) { logger.info("delete as admin");
			 * trashOperationsAO.emptyAllTrashAsAdmin(irodsServices.getCurrentUserZone(),
			 * 0); // trashOperationsAO.emptyTrashAtPathAdminMode(collectionPath, "", //
			 * irodsServices.getCurrentUserZone(), 0); itemsDeleted = true; } else {
			 */
			logger.info("delete as user");
			trashOperationsAO.emptyTrashAtPathForLoggedInUser(collectionPath, irodsServices.getCurrentUserZone(), 0);
			itemsDeleted = true;
			/* } */
		} catch (JargonException je) {
			logger.error("jargon exception emptying trash", je);
			throw je;
		}

		return itemsDeleted;
	}

	@Override
	public boolean move(String sourcePath, String targetPath) throws DataGridConnectionRefusedException {

		IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();

		DataTransferOperations dataTransferOperations = irodsServices.getDataTransferOperations();

		try {
			IRODSFile source = irodsFileFactory.instanceIRODSFile(sourcePath);

			if (source.isDirectory()) {
				targetPath += "/" + FilenameUtils.getBaseName(sourcePath);
			}

			IRODSFile target = irodsFileFactory.instanceIRODSFile(targetPath);

			dataTransferOperations.move(source, target);

			return true;
		} catch (JargonException e) {
			logger.error("Could not move item from " + sourcePath + " to " + targetPath + ": ", e.getMessage());
		}

		return false;
	}

	@Override
	public void replicateDataObject(String path, String targetResource, boolean inAdminMode) throws DataGridException {
		logger.info("Replicating {} into the resource {} [admin mode: {}]", path, targetResource, inAdminMode);
		// TODO: respect admin flag when replication services enhanced in Jargon
		try {
			DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
			dataObjectAO.replicateIrodsDataObject(path, targetResource);
		} catch (JargonException e) {
			logger.info("File replication failed ({}) into the resource {} [admin mode: {}]", path, targetResource,
					inAdminMode);
			throw new DataGridException("File replication failed.");
		}
	}

	@Override
	public void computeChecksum(String path, String filename)
			throws DataGridChecksumException, DataGridConnectionRefusedException {
		if (path == null || path.isEmpty() || filename == null || filename.isEmpty())
			throw new DataGridChecksumException("Could not calculate checksum. File path is invalid.");

		logger.info("Computing checksum for {} ({})", filename, path);

		IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();
		DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
		IRODSFile file;

		try {
			file = irodsFileFactory.instanceIRODSFile(path, filename);
			dataObjectAO.computeMD5ChecksumOnDataObject(file);
		} catch (JargonException e) {
			logger.error("Could not calculate checksum: {}", e.getMessage());
			throw new DataGridChecksumException("Could not calculate checksum.");
		}
	}

	/**
	 * Copies a buffered input stream from a file to a HTTP response for
	 * downloading.
	 *
	 * @param path     path to the file in iRODS to be added to the HTTP response
	 * @param response HTTP response to let the user download the file
	 * @return True, if the file was successfully added to the HTTP response. False,
	 *         otherwise.
	 * @throws DataGridConnectionRefusedException is Metalnx cannot connect to the
	 *                                            data grid
	 */
	private boolean copyFileIntoHttpResponse(String path, HttpServletResponse response)
			throws DataGridConnectionRefusedException {

		boolean isCopySuccessFul = true;
		IRODSFileInputStream irodsFileInputStream = null;
		IRODSFile irodsFile = null;

		logger.debug("Trying to copy path stream {} to user", path);

		try {
			String fileName = path.substring(path.lastIndexOf("/") + 1, path.length());
			logger.debug("The filename is [{}]", fileName);

			logger.debug("Initiating iRodsFileFactory");
			IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();

			logger.debug("Getting iRodsFileFactory instance for {}", path);
			irodsFile = irodsFileFactory.instanceIRODSFile(path);

			logger.debug("Creating stream from {}", irodsFile);
			irodsFileInputStream = irodsFileFactory.instanceIRODSFileInputStream(irodsFile);

			// set file mime type
			response.setContentType(CONTENT_TYPE);
			response.setHeader("Content-Disposition", String.format(HEADER_FORMAT, fileName));

			long length = irodsFile.length();

			if (length <= Integer.MAX_VALUE) {
				response.setContentLength((int) length);
			} else {
				response.addHeader("Content-Length", Long.toString(length));
			}

			FileCopyUtils.copy(irodsFileInputStream, response.getOutputStream());

		} catch (IOException e) {
			logger.error("Could not put the file in the Http response ", e);
			isCopySuccessFul = false;
		} catch (JargonException e) {
			logger.error("Could not copy file in the Http response: ", e.getMessage());
			isCopySuccessFul = false;
		} catch (NullPointerException e) {
			logger.error("Could not copy file in the Http response: ", e.getMessage());
			isCopySuccessFul = false;
		}

		finally {
			try {
				if (irodsFileInputStream != null)
					irodsFileInputStream.close();
				if (irodsFile != null)
					irodsFile.close();
			} catch (Exception e) {
				logger.error("Could not close stream(s): ", e.getMessage());
			}
		}

		return isCopySuccessFul;
	}
}
