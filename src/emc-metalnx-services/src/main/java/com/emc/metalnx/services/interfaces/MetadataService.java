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

/**
 *
 */
package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridMetadata;
import com.emc.metalnx.core.domain.entity.DataGridMetadataSearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

import java.util.List;


public interface MetadataService {

    /**
     * Get all collections and data objects that match any metadata search criteria given as a
     * parameters
     *
     * @param searchList  list of metadata search criteria
     * @param pageContext pagination context for proper counting display at the front end
     * @param pageNum     page required
     * @param pageSize    max number of items to display in a page
     * @return list of collections and data objects
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridCollectionAndDataObject> findByMetadata(List<DataGridMetadataSearch> searchList,
                                                                DataGridPageContext pageContext, int pageNum, int pageSize)
            throws DataGridConnectionRefusedException;

    /**
     * Get all metadata (Attribute/Value/Unit) related to a Collection or DataObject in iRODS
     *
     * @param path path of the Collection or DataObject from which we will get all metadata related to it
     * @return list of metadata objects from a Collection or DataObject
     * @throws DataGridConnectionRefusedException
     */
    public List<DataGridMetadata> findMetadataValuesByPath(String path) throws DataGridConnectionRefusedException;

    /**
     * Add metadata (Attribute/Value/Unit) to a Collection or DataObject in iRODS
     *
     * @param path      path of the Collection or DataObject which we will be adding a metadata
     * @param attribute name of attribute that is going to be added
     * @param value     the value of the attribute added
     * @param unit      if there is a unit to this attribute, it can be inserted here
     * @return boolean value meaning success or failure in add operation
     * @throws DataGridConnectionRefusedException
     */
    public boolean addMetadataToPath(String path, String attribute, String value, String unit) throws DataGridConnectionRefusedException;

    /**
     * Modify metadata (Attribute/Value/Unit) by passing the path of the Collection or DataObject,
     * the metadata that is going to be updated and the new metadata that will replace the old one as parameters
     *
     * @param path         path of the Collection or DataObject from which we will modify a metadata
     * @param oldAttribute name of the attribute that is going to be updated
     * @param oldValue     the value of the attribute that is going to be modified
     * @param oldUnit      if there is a unit in the metadata that is going to be altered, it has to be inserted here
     * @param newAttribute new name of attribute from the metadata that is going to be modified
     * @param newValue     the new value of the metadata
     * @param newUnit      update the unit parameter of the metadata
     * @return boolean value meaning success or failure in add operation
     * @throws DataGridConnectionRefusedException
     */
    public boolean modMetadataFromPath(String path, String oldAttribute, String oldValue,
                                       String oldUnit, String newAttribute, String newValue, String newUnit)
            throws DataGridConnectionRefusedException;

    /**
     * Removes a metadata (Attribute/Value/Unit) from a path.
     *
     * @param path      path to remove metadata attached to
     * @param attribute metadata attribute
     * @param value     metadata value
     * @param unit      metadata unit
     * @return True, if the metadata (AVU) was removed successfully. False, otherwise.
     * @throws DataGridConnectionRefusedException
     */
    public boolean delMetadataFromPath(String path, String attribute, String value, String unit)
            throws DataGridConnectionRefusedException;

    /**
     * Populates a list of objects or collections with current user's permission
     * @param objectList
     * @throws DataGridConnectionRefusedException
     */
    public void populateVisibilityForCurrentUser(List<DataGridCollectionAndDataObject> objectList) throws DataGridConnectionRefusedException;

}
