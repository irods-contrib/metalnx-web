 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.UserAO;

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
