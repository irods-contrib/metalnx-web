/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.interfaces;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.BulkFileOperationsAO;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.RemoteExecutionOfCommandsAO;
import org.irods.jargon.core.pub.ResourceAO;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.TrashOperationsAO;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.ZoneAO;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteService;
import org.irods.jargon.datautils.filesampler.FileSamplerService;
import org.irods.jargon.ticket.TicketAdminService;
import org.irods.jargon.zipservice.api.JargonZipService;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;

/**
 * Service that allows the user to get an instance of each iRODS AO by session.
 *
 */
public interface IRODSServices {

	/**
	 * Gets an instance of the ticket admin service.
	 * 
	 * @return TicketAdminService instance
	 * @throws DataGridConnectionRefusedException
	 *             if Metalnx cannot connect to the grid.
	 */
	TicketAdminService getTicketAdminService() throws DataGridConnectionRefusedException;

	/**
	 * Finds what version of iRODS Metalnx is running against.
	 * 
	 * @return a String representing the version of iRODS. (major.minor.path)
	 * @throws DataGridConnectionRefusedException
	 *             if Metalnx cannot connect to the grid
	 */
	String findIRodsVersion() throws DataGridConnectionRefusedException;

	/**
	 * Gets BulkFileOperationsAO from iRODS. Used for multiple files download.
	 *
	 * @return BulkFileOperationsAO instance
	 * @throws DataGridConnectionRefusedException
	 */
	BulkFileOperationsAO getBulkFileOperationsAO() throws DataGridConnectionRefusedException;

	/**
	 * Gets the current user's zone
	 *
	 * @return zone name
	 */
	String getCurrentUserZone();

	/**
	 * Gets the logged user
	 *
	 * @return logged user name
	 */
	String getCurrentUser();

	/**
	 * Gets the UserAO from iRODS based on the logged user.
	 *
	 * @return the UserAO object
	 * @throws DataGridConnectionRefusedException
	 */
	UserAO getUserAO() throws DataGridConnectionRefusedException;

	/**
	 * Gets the GroupAO from iRODS based on the logged user.
	 *
	 * @return the UserAO object
	 * @throws DataGridConnectionRefusedException
	 */
	UserGroupAO getGroupAO() throws DataGridConnectionRefusedException;

	/**
	 * Returns the AO of the Collections API
	 *
	 * @return CollectionAO object
	 * @throws DataGridConnectionRefusedException
	 */
	CollectionAO getCollectionAO() throws DataGridConnectionRefusedException;

	/**
	 * Returns the AO of the CollectionAndDataObjectListAndSearch API
	 *
	 * @return
	 * @throws DataGridConnectionRefusedException
	 */
	CollectionAndDataObjectListAndSearchAO getCollectionAndDataObjectListAndSearchAO()
			throws DataGridConnectionRefusedException;

	/**
	 * Gets the IRODSFileSystemAO
	 *
	 * @return IRODSFileSystemAO object
	 * @throws DataGridConnectionRefusedException
	 */
	IRODSFileSystemAO getIRODSFileSystemAO() throws DataGridConnectionRefusedException;

	/**
	 * Get access to the iRods File Factory
	 *
	 * @return IRODSFileFactory object
	 * @throws DataGridConnectionRefusedException
	 */
	IRODSFileFactory getIRODSFileFactory() throws DataGridConnectionRefusedException;

	/**
	 * This is an access object that can be used to move data to, from, and between
	 * iRODS resources.
	 *
	 * @return DataTransferOperations object
	 * @throws DataGridConnectionRefusedException
	 */
	DataTransferOperations getDataTransferOperations() throws DataGridConnectionRefusedException;

	/**
	 * Get access to the Stream2Stream (useful for file upload)
	 *
	 * @return Stream2StreamAO object
	 * @throws DataGridConnectionRefusedException
	 */
	Stream2StreamAO getStream2StreamAO() throws DataGridConnectionRefusedException;

	SpecificQueryAO getSpecificQueryAO() throws DataGridConnectionRefusedException;

	RemoteExecutionOfCommandsAO getRemoteExecutionOfCommandsAO() throws DataGridConnectionRefusedException;

	/**
	 * Gets the ResourceAO from iRODS based on the logged user.
	 *
	 * @return Resource access object
	 * @throws DataGridConnectionRefusedException
	 */
	ResourceAO getResourceAO() throws DataGridConnectionRefusedException;

	/**
	 * Gets the ZoneAO from iRODS based on the logged user.
	 *
	 * @return Zone access object
	 * @throws DataGridConnectionRefusedException
	 */
	ZoneAO getZoneAO() throws DataGridConnectionRefusedException;

	/**
	 * Gets the DataObjectAO from iRODS based on the logged user.
	 *
	 * @return Data Object access object
	 * @throws DataGridConnectionRefusedException
	 */
	DataObjectAO getDataObjectAO() throws DataGridConnectionRefusedException;

	/**
	 * Gets the RuleProcessingAO from iRODS based on the logged user.
	 *
	 * @return Rule Processing Access Object
	 * @throws DataGridConnectionRefusedException
	 */
	RuleProcessingAO getRuleProcessingAO() throws DataGridConnectionRefusedException;

	/**
	 * Sets the default storage resource for the current iRODS Account.
	 *
	 * @param newResourceName
	 */
	void setDefaultStorageResource(String newResourceName);

	/**
	 * Gets the default storage resource for the current iRODS Account.
	 */
	String getDefaultStorageResource();

	/**
	 * Gets the grid environmental info.
	 * 
	 * @return
	 * @throws DataGridConnectionRefusedException
	 */
	EnvironmentalInfoAO getEnvironmentalInfoAO() throws DataGridConnectionRefusedException;

	/**
	 * Verifies whether or not the version of iRODS is at least 4.2.0.
	 * 
	 * @return True if iRODS version is >= 4.2.0. False, otherwise.
	 * @throws DataGridConnectionRefusedException
	 *             if Metalnx cannot connect to the grid.
	 */
	boolean isAtLeastIrods420() throws DataGridConnectionRefusedException;

	/**
	 * Obtain a reference to the <code>IRODSAccessObjectFactory</code> with hooks to
	 * manage connections, properties, etc
	 * 
	 * @return {@link IRODSAccessObjectFactory} reference
	 */
	IRODSAccessObjectFactory getIrodsAccessObjectFactory();

	/**
	 * Obtain a reference to the {@link TrashOperationsAO}
	 * 
	 * @return {@link TrashOperationsAO}
	 * @throws DataGridConnectionRefusedException
	 * @throws JargonException
	 */
	TrashOperationsAO getTrashOperationsAO() throws DataGridConnectionRefusedException, JargonException;

	/**
	 * 
	 * @return
	 * @throws DataGridConnectionRefusedException
	 */
	FileSamplerService getFileSamplerService() throws DataGridConnectionRefusedException;

	/**
	 * Return an instance of the autocomplete service from Jargon
	 * 
	 * @return {@link AvuAutocompleteService}
	 * @throws JargonException
	 */
	AvuAutocompleteService getAvuAutocompleteService() throws JargonException;

	/**
	 * Return an instance of the JargonZipService that handles bundles
	 * 
	 * @return {@link JargonZipService}
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	JargonZipService getJargonZipService() throws JargonException;

	/**
	 * Return an iRODS admin account
	 * 
	 * @return {@link IRODSAccount} for the configured iRODS admin
	 * @throws JargonException
	 *             {@link JargonException}
	 */
	IRODSAccount getIrodsAdminAccount() throws DataGridException;

	/**
	 * Handy method to determine if the logged in user is acting in the role of
	 * administrator
	 * 
	 * @return {@code boolean} indicating {@code true} if user is an admin
	 */
	boolean isActingAsAdmin();

}
