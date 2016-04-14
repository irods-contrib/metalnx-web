package com.emc.metalnx.services.interfaces;

import org.irods.jargon.core.pub.BulkFileOperationsAO;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.RemoteExecutionOfCommandsAO;
import org.irods.jargon.core.pub.ResourceAO;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.ZoneAO;
import org.irods.jargon.core.pub.io.IRODSFileFactory;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

/**
 * Service that allows the user to get an instance of each iRODS AO by session.
 *
 */
public interface IRODSServices {

    /**
     * Gets BulkFileOperationsAO from iRODS. Used for multiple files download.
     *
     * @return BulkFileOperationsAO instance
     * @throws DataGridConnectionRefusedException
     */
    public BulkFileOperationsAO getBulkFileOperationsAO() throws DataGridConnectionRefusedException;

    /**
     * Gets the current user's zone
     *
     * @return zone name
     */
    public String getCurrentUserZone();

    /**
     * Gets the logged user
     *
     * @return logged user name
     */
    public String getCurrentUser();

    /**
     * Gets the UserAO from iRODS based on the logged user.
     *
     * @return the UserAO object
     * @throws DataGridConnectionRefusedException
     */
    public UserAO getUserAO() throws DataGridConnectionRefusedException;

    /**
     * Gets the GroupAO from iRODS based on the logged user.
     *
     * @return the UserAO object
     * @throws DataGridConnectionRefusedException
     */
    public UserGroupAO getGroupAO() throws DataGridConnectionRefusedException;

    /**
     * Returns the AO of the Collections API
     *
     * @return CollectionAO object
     * @throws DataGridConnectionRefusedException
     */
    public CollectionAO getCollectionAO() throws DataGridConnectionRefusedException;

    /**
     * Returns the AO of the CollectionAndDataObjectListAndSearch API
     *
     * @return
     * @throws DataGridConnectionRefusedException
     */
    public CollectionAndDataObjectListAndSearchAO getCollectionAndDataObjectListAndSearchAO()
            throws DataGridConnectionRefusedException;

    /**
     * Gets the IRODSFileSystemAO
     *
     * @return IRODSFileSystemAO object
     * @throws DataGridConnectionRefusedException
     */
    public IRODSFileSystemAO getIRODSFileSystemAO() throws DataGridConnectionRefusedException;

    /**
     * Get access to the iRods File Factory
     *
     * @return IRODSFileFactory object
     * @throws DataGridConnectionRefusedException
     */
    public IRODSFileFactory getIRODSFileFactory() throws DataGridConnectionRefusedException;

    /**
     * This is an access object that can be used to move data to, from, and between iRODS resources.
     *
     * @return DataTransferOperations object
     * @throws DataGridConnectionRefusedException
     */
    public DataTransferOperations getDataTransferOperations()
            throws DataGridConnectionRefusedException;

    /**
     * Get access to the Stream2Stream (useful for file upload)
     *
     * @return Stream2StreamAO object
     * @throws DataGridConnectionRefusedException
     */
    public Stream2StreamAO getStream2StreamAO() throws DataGridConnectionRefusedException;

    public SpecificQueryAO getSpecificQueryAO() throws DataGridConnectionRefusedException;

    public RemoteExecutionOfCommandsAO getRemoteExecutionOfCommandsAO()
            throws DataGridConnectionRefusedException;

    /**
     * Gets the ResourceAO from iRODS based on the logged user.
     *
     * @return Resource access object
     * @throws DataGridConnectionRefusedException
     */
    public ResourceAO getResourceAO() throws DataGridConnectionRefusedException;

    /**
     * Gets the ZoneAO from iRODS based on the logged user.
     *
     * @return Zone access object
     * @throws DataGridConnectionRefusedException
     */
    public ZoneAO getZoneAO() throws DataGridConnectionRefusedException;

    /**
     * Gets the DataObjectAO from iRODS based on the logged user.
     *
     * @return Data Object access object
     * @throws DataGridConnectionRefusedException
     */
    public DataObjectAO getDataObjectAO() throws DataGridConnectionRefusedException;

    /**
     * Gets the RuleProcessingAO from iRODS based on the logged user.
     *
     * @return Rule Processing Access Object
     * @throws DataGridConnectionRefusedException
     */
    public RuleProcessingAO getRuleProcessingAO() throws DataGridConnectionRefusedException;

    /**
     * Sets the default storage resource for the current iRODS Account.
     *
     * @param newResourceName
     */
    public void setDefaultStorageResource(String newResourceName);

    /**
     * Gets the default storage resource for the current iRODS Account.
     */
    public String getDefaultStorageResource();

}
