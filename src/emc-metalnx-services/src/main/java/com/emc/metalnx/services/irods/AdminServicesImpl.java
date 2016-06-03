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

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.UserAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.AdminServices;

@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class AdminServicesImpl implements AdminServices {

    @Autowired
    IRODSAccessObjectFactory irodsAccessObjectFactory;

    private IRODSAccount irodsAccount;

    @Value("${irods.host}")
    private String irodsHost;

    @Value("${irods.port}")
    private String irodsPort;

    @Value("${irods.zoneName}")
    private String irodsZone;

    @Value("${jobs.irods.username}")
    private String irodsJobUser;

    @Value("${jobs.irods.password}")
    private String irodsJobPassword;

    @Value("${jobs.irods.auth.scheme}")
    private String irodsAuthScheme;

    private static final Logger logger = LoggerFactory.getLogger(AdminServicesImpl.class);

    @Override
    public UserAO getUserAO() throws DataGridConnectionRefusedException {
        try {
            authenticateIRODSAccount();

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
    public SpecificQueryAO getSpecificQueryAO() throws DataGridConnectionRefusedException {
        try {
            authenticateIRODSAccount();

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
    public DataObjectAO getDataObjectAO() throws DataGridConnectionRefusedException {
        try {
            authenticateIRODSAccount();

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

    private void authenticateIRODSAccount() throws JargonException {
        AuthResponse authResponse = null;

        if (irodsAccount == null) {
            // Getting iRODS protocol set
            IRODSAccount tempAccount = IRODSAccount.instance(irodsHost, Integer.parseInt(irodsPort), irodsJobUser, irodsJobPassword, "", irodsZone,
                    "demoResc");

            tempAccount.setAuthenticationScheme(AuthScheme.findTypeByString(irodsAuthScheme));
            authResponse = irodsAccessObjectFactory.authenticateIRODSAccount(tempAccount);

            if (authResponse.isSuccessful()) {
                irodsAccount = authResponse.getAuthenticatedIRODSAccount();
            }
        }
    }

    @Override
    public CollectionAndDataObjectListAndSearchAO getCollectionAndDataObjectListAndSearchAO() throws DataGridConnectionRefusedException {
        try {
            authenticateIRODSAccount();

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

}
