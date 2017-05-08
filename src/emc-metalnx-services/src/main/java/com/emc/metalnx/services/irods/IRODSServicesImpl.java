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

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.auth.UserTokenDetails;
import com.emc.metalnx.services.interfaces.IRODSServices;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.IrodsVersion;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.*;
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

import java.net.ConnectException;

@Service("irodsServices")
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class IRODSServicesImpl implements IRODSServices {

    @Autowired
    IRODSAccessObjectFactory irodsAccessObjectFactory;

    private UserTokenDetails userTokenDetails;
    private IRODSAccount irodsAccount;

    private static final Logger logger = LoggerFactory.getLogger(IRODSServicesImpl.class);

    public IRODSServicesImpl() {
        this.userTokenDetails = (UserTokenDetails) SecurityContextHolder.getContext().getAuthentication().getDetails();
        this.irodsAccount = this.userTokenDetails.getIrodsAccount();
    }

    public IRODSServicesImpl(IRODSAccount acct) {
        this.irodsAccount = acct;
    }

    @Override
    public String findIRodsVersion() throws DataGridConnectionRefusedException {
        String version = "";

        try {
            EnvironmentalInfoAO envInfoAO = irodsAccessObjectFactory.getEnvironmentalInfoAO(irodsAccount);
            IrodsVersion iv = envInfoAO.getIRODSServerPropertiesFromIRODSServer().getIrodsVersion();
            version = String.format("%s.%s.%s", iv.getMajorAsString(), iv.getMinorAsString(), iv.getPatchAsString());
        } catch (JargonException e) {
            logger.error("Could not find iRODS version: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }

        return version;
    }

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

    @Override
    public EnvironmentalInfoAO getEnvironmentalInfoAO() throws DataGridConnectionRefusedException {
        EnvironmentalInfoAO env = null;

        try {
            env = irodsAccessObjectFactory.getEnvironmentalInfoAO(this.irodsAccount);
        } catch (JargonException e) {
            logger.error("Could not instantiate EnvironmentalInfoAO: ", e);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException(e.getMessage());
            }
        }
        return env;
    }

    @Override
    public boolean isAtLeastIrods420() throws DataGridConnectionRefusedException {
        boolean isAtLeastIrods420 = false;

        try {
            EnvironmentalInfoAO env = irodsAccessObjectFactory.getEnvironmentalInfoAO(this.irodsAccount);
            if(env != null) isAtLeastIrods420 = env.getIRODSServerPropertiesFromIRODSServer().isAtLeastIrods420();
        } catch (JargonException e) {
            logger.error("Could not get environmental information from grid: {}", e.getMessage());
        }

        return isAtLeastIrods420;
    }
}
