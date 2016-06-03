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

package com.emc.metalnx.services.interfaces;

import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.UserAO;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

/**
 * Service that allows the user to get an instance of each iRODS AO by session.
 *
 */
public interface AdminServices {

    /**
     * Gets the UserAO from iRODS based on the logged user.
     *
     * @return the UserAO object
     * @throws DataGridConnectionRefusedException
     */
    public UserAO getUserAO() throws DataGridConnectionRefusedException;

    /**
     * Gets SpecificQueryAO from iRODS based on the admin user set only for Metalnx purposes.
     *
     * @return instance of SpecificQueryAO
     * @throws DataGridConnectionRefusedException
     */
    public SpecificQueryAO getSpecificQueryAO() throws DataGridConnectionRefusedException;

    /**
     * Gets the DataObjectAO from iRODS based on the admin user set only for Metalnx purposes.
     *
     * @return Data Object access object
     * @throws DataGridConnectionRefusedException
     */
    public DataObjectAO getDataObjectAO() throws DataGridConnectionRefusedException;

    /**
     * Gets CollectionAndDataObjectListAndSearchAO from iRODS based on an admin user set
     * only for Metalnx purposes.
     *
     * @throws DataGridConnectionRefusedException
     */
    public CollectionAndDataObjectListAndSearchAO getCollectionAndDataObjectListAndSearchAO() throws DataGridConnectionRefusedException;

}
