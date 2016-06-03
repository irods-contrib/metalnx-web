/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.services.irods;

import java.net.ConnectException;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.BulkFileOperationsAO;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.auth.UserTokenDetails;
import com.emc.metalnx.services.interfaces.IRODSServices;

@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class IRODSServicesImpl implements IRODSServices {

    @Autowired
    IRODSAccessObjectFactory irodsAccessObjectFactory;

    private UserTokenDetails userTokenDetails = (UserTokenDetails) SecurityContextHolder
            .getContext().getAuthentication().getDetails();
    private IRODSAccount irodsAccount = this.userTokenDetails.getIrodsAccount();

    private static final Logger logger = LoggerFactory.getLogger(IRODSServicesImpl.class);

    @Override
    public BulkFileOperationsAO getBulkFileOperationsAO() throws DataGridConnectionRefusedException {
        BulkFileOperationsAO bulkFileOperationsAO = null;

        try {
            // Returning UserAO instance
            bulkFileOperationsAO = irodsAccessObjectFactory.getBulkFileOperationsAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate UserAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }

        return bulkFileOperationsAO;
    }

    @Override
    public String getCurrentUser() {
        return irodsAccount.getUserName();
    }

    @Override
    public String getCurrentUserZone() {
        return irodsAccount.getZone();
    }

    @Override
    public UserAO getUserAO() throws DataGridConnectionRefusedException {
        try {
            // Returning UserAO instance
            return irodsAccessObjectFactory.getUserAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate UserAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public UserGroupAO getGroupAO() throws DataGridConnectionRefusedException {
        try {

            // Returning UserAO instance
            return irodsAccessObjectFactory.getUserGroupAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate UserAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public CollectionAO getCollectionAO() throws DataGridConnectionRefusedException {
        try {

            // Returning CollectionAO instance
            return irodsAccessObjectFactory.getCollectionAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate CollectionAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public CollectionAndDataObjectListAndSearchAO getCollectionAndDataObjectListAndSearchAO()
            throws DataGridConnectionRefusedException {

        try {

            // Returning CollectionAndDataObjectListAndSearchAO instance
            return irodsAccessObjectFactory.getCollectionAndDataObjectListAndSearchAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }

        return null;
    }

    @Override
    public IRODSFileSystemAO getIRODSFileSystemAO() throws DataGridConnectionRefusedException {

        try {

            // Returning CollectionAndDataObjectListAndSearchAO instance
            return irodsAccessObjectFactory.getIRODSFileSystemAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public IRODSFileFactory getIRODSFileFactory() throws DataGridConnectionRefusedException {
        try {

            // Returning CollectionAndDataObjectListAndSearchAO instance
            return irodsAccessObjectFactory.getIRODSFileFactory(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate IRODSFileFactory: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public DataTransferOperations getDataTransferOperations()
            throws DataGridConnectionRefusedException {
        try {

            // Returning CollectionAndDataObjectListAndSearchAO instance
            return irodsAccessObjectFactory.getDataTransferOperations(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate DataTransferOperations: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }

        return null;
    }

    @Override
    public Stream2StreamAO getStream2StreamAO() throws DataGridConnectionRefusedException {

        try {

            return irodsAccessObjectFactory.getStream2StreamAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate Stream2StreamAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }

        return null;
    }

    @Override
    public SpecificQueryAO getSpecificQueryAO() throws DataGridConnectionRefusedException {
        try {

            // Returning CollectionAndDataObjectListAndSearchAO instance
            return irodsAccessObjectFactory.getSpecificQueryAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public RemoteExecutionOfCommandsAO getRemoteExecutionOfCommandsAO()
            throws DataGridConnectionRefusedException {
        try {

            // Returning CollectionAndDataObjectListAndSearchAO instance
            return irodsAccessObjectFactory.getRemoteExecutionOfCommandsAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate RemoteExecutionOfCommandsAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public ResourceAO getResourceAO() throws DataGridConnectionRefusedException {
        try {

            // Returning CollectionAndDataObjectListAndSearchAO instance
            return irodsAccessObjectFactory.getResourceAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public ZoneAO getZoneAO() throws DataGridConnectionRefusedException {
        try {

            // Returning CollectionAndDataObjectListAndSearchAO instance
            return irodsAccessObjectFactory.getZoneAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }

        return null;
    }

    @Override
    public DataObjectAO getDataObjectAO() throws DataGridConnectionRefusedException {
        try {

            // Returning CollectionAndDataObjectListAndSearchAO instance
            return irodsAccessObjectFactory.getDataObjectAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate CollectionAndDataObjectListAndSearchAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }

        return null;
    }

    @Override
    public RuleProcessingAO getRuleProcessingAO() throws DataGridConnectionRefusedException {
        try {
            // Returning RuleProcessingAO instance
            return irodsAccessObjectFactory.getRuleProcessingAO(irodsAccount);

        }
        catch (JargonException e) {
            logger.error("Could not instantiate RuleProcessingAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public void setDefaultStorageResource(String newResourceName) {
        this.irodsAccount.setDefaultStorageResource(newResourceName);
    }

    @Override
    public String getDefaultStorageResource() {
        return this.irodsAccount.getDefaultStorageResource();
    }

}
