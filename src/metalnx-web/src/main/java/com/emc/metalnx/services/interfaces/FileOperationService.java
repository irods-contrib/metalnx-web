 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 
package com.emc.metalnx.services.interfaces;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.exception.JargonException;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridChecksumException;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;

public interface FileOperationService {

	/**
	 * Copy a file or collection between two locations in the data grid.
	 *
	 * @param sourcePath
	 *            origin path
	 * @param dstPath
	 *            destination path
	 * @param copyWithMetadata
	 *            flag that says whether or not we are copying the file along with
	 *            its metadata tags
	 * @return True, if file or collection was moved. False, otherwise.
	 * @throws DataGridConnectionRefusedException
	 *             if Metalnx cannot connect to the data grid
	 */
	boolean copy(String sourcePath, String dstPath, boolean copyWithMetadata) throws DataGridConnectionRefusedException;

	/**
	 * Copy a set of files or collections between two locations in the data grid.
	 *
	 * @param sourcePaths
	 *            list of paths to be copied
	 * @param dstPath
	 *            path where the files/collections will be copied
	 * @param copyWithMetadata
	 *            flag that says whether or not we are copying the file along with
	 *            its metadata tags
	 * @return True, if all files or collections were moved. False, otherwise.
	 * @throws DataGridConnectionRefusedException
	 *             if Metalnx cannot connect to the data grid
	 */
	boolean copy(List<String> sourcePaths, String dstPath, boolean copyWithMetadata)
			throws DataGridConnectionRefusedException;

	/**
	 * Delete a file or collection in iRODS
	 *
	 * @param path
	 *            path to the collection or data object to be deleted
	 * @param force
	 *            delete collection/data object with force flag set
	 * @throws DataGridConnectionRefusedException
	 *             if Metalnx cannot connect to the data grid
	 */
	boolean deleteItem(String path, boolean force) throws DataGridConnectionRefusedException;

	/**
	 * Delete a collection in iRODS
	 *
	 * @param collectionPath
	 *            path to the collection to be deleted
	 * @param forceFlag
	 *            when set to true, force delete an object (-f). When set to false,
	 *            delete object no with force
	 * @throws DataGridException
	 *             if an error occurred during deletion
	 */
	boolean deleteCollection(String collectionPath, boolean forceFlag) throws DataGridException;

	
	/**
	 * Delete a data object in iRODS
	 *
	 * @param dataObjectPath
	 *            path to the data object that will de removed
	 * @param forceFlag
	 *            when set to true, force delete an object (-f). When set to false,
	 *            delete object no with force
	 * @throws DataGridException
	 *             if an error occurred during deletion
	 */
	boolean deleteDataObject(String dataObjectPath, boolean forceFlag) throws DataGridException;

	/**
	 * Delete a replica of a data object
	 *
	 * @param path
	 *            path to the parent of the data object to be deleted
	 * @param replicaNumber
	 *            number of the replica that is going to be deleted
	 * @param inAdminMode
	 *            run the command as admin (-M option)
	 * @return true if the operation was successful and false otherwise
	 * @throws DataGridConnectionRefusedException
	 *             if Metalnx cannot connect to the data grid
	 */
	boolean deleteReplica(String path, String fileName, int replicaNumber, boolean inAdminMode)
			throws DataGridConnectionRefusedException;

	/**
	 * Download a file or collection from the data grid.
	 *
	 * @param path
	 *            file to download
	 * @param httpResponse
	 *            response to an http request
	 * @param removeTempCollection
	 *            flag when set to true tells Metalnx to remove temporary
	 *            collections and tar files created for downloading. When set to
	 *            false, just puts the file into the HTTP response
	 * @return True, if file or collection was downloaded. False, otherwise.
	 * @throws DataGridException
	 *             if an error happen in the data grid
	 * @throws IOException
	 *             cannot create the tar ball file
	 * @throws JargonException
	 */
	boolean download(String path, HttpServletResponse httpResponse, boolean removeTempCollection)
			throws DataGridException, IOException, JargonException;

	/**
	 * Removes all items existing in the trash folder of a given user.
	 *
	 * @param user
	 *            user who will get the trash cleaned
	 * @param currentPath
	 *            path from which the trash path will be extracted
	 * @return True, if all trash items were removed. False, otherwise.
	 * @throws DataGridConnectionRefusedException
	 *             if Metalnx cannot connect to the data grid
	 * @throws JargonException
	 */
	boolean emptyTrash(DataGridUser user, String currentPath)
			throws DataGridConnectionRefusedException, JargonException;

	/**
	 * Move a file or collection between two locations in the data grid.
	 *
	 * @param sourcePath
	 *            origin path
	 * @param targetPath
	 *            destination path where the file will be moved to
	 * @return True, if file or collection was moved. False, otherwise.
	 * @throws DataGridException
	 *             if an error occurred during the move operation
	 */
	boolean move(String sourcePath, String targetPath) throws DataGridException;

	/**
	 * Replicates a file into another resource.
	 *
	 * @param path
	 *            path to the file to be replicated
	 * @param targetResource
	 *            resource where the replica will be stored
	 * @param inAdminMode
	 *            replicate object in admin mode (-M option)
	 * 
	 * @throws DataGridException
	 *             {@link DataGridException}
	 */
	void replicateDataObject(String path, String targetResource, boolean inAdminMode) throws DataGridException;

	/**
	 * Computes checksum for a given path.
	 * 
	 * @param path
	 *            path to the file in the grid
	 * @param filename
	 *            name of the file to compute checksum
	 * @throws DataGridChecksumException
	 *             is thrown if checksum cannot be calculated
	 * @throws DataGridConnectionRefusedException
	 *             is thrown when Metalnx cannot connect to the data grid
	 */
	void computeChecksum(String path, String filename)
			throws DataGridChecksumException, DataGridConnectionRefusedException;
}
