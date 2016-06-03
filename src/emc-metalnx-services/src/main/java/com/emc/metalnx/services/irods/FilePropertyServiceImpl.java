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
import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridFilePropertySearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.FilePropertyService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.SpecQueryService;
import com.emc.metalnx.services.interfaces.UserService;
import com.emc.metalnx.services.machine.util.DataGridUtils;

@Service
@Transactional
public class FilePropertyServiceImpl implements FilePropertyService {

    @Autowired
    IRODSServices irodsServices;

    @Autowired
    UserService userService;

    @Autowired
    SpecQueryService specQueryService;

    @Value("${irods.zoneName}")
    private String zoneName;

    private static final Logger logger = LoggerFactory.getLogger(FilePropertyServiceImpl.class);

    @Override
    public List<DataGridCollectionAndDataObject> findByFileProperties(
            List<DataGridFilePropertySearch> searchList, DataGridPageContext pageContext,
            int pageNum, int pageSize) throws DataGridConnectionRefusedException {

        List<DataGridCollectionAndDataObject> dataGridCollectionAndDataObjects = null;
        List<DataGridCollectionAndDataObject> dataGridObjects = null;
        List<DataGridCollectionAndDataObject> dataGridCollections = null;

        int totalCollections = 0;
        int totalDataObjects = 0;
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = (pageNum * pageSize) - 1;
        int endIndexForDataObjs;
        int endIndexForCollections;

        try {
            String zone = irodsServices.getCurrentUserZone();

            totalCollections = specQueryService.countCollectionsMatchingFileProperties(searchList,
                    zone);
            totalDataObjects = specQueryService.countDataObjectsMatchingFileProperties(searchList,
                    zone);

            pageContext.setStartItemNumber(startIndex + 1);
            pageContext.setTotalNumberOfItems(totalCollections + totalDataObjects);

            dataGridCollectionAndDataObjects = new ArrayList<DataGridCollectionAndDataObject>();

            if (endIndex + 1 <= totalCollections) {
                // looking for collections
                SpecificQueryResultSet resultSetColls = specQueryService.searchByFileProperties(
                        searchList, zone, true, pageContext, startIndex, pageSize);

                dataGridCollections = DataGridUtils
                        .mapPropertiesResultSetToDataGridObjects(resultSetColls);

                endIndexForCollections = dataGridCollections.size();

                dataGridCollectionAndDataObjects.addAll(dataGridCollections);

                pageContext.setEndItemNumber(pageContext.getStartItemNumber()
                        + endIndexForCollections - 1);
            } else if (startIndex + 1 > totalCollections) {
                // looking for data objects
                SpecificQueryResultSet resultSetDataObjs = specQueryService.searchByFileProperties(
                        searchList, zone, false, pageContext, startIndex - totalCollections,
                        pageSize);

                dataGridObjects = DataGridUtils
                        .mapPropertiesResultSetToDataGridObjects(resultSetDataObjs);

                pageContext.setEndItemNumber(pageContext.getStartItemNumber()
                        + dataGridObjects.size() - 1);

                dataGridCollectionAndDataObjects.addAll(dataGridObjects);
            } else {
                // looking for collections
                SpecificQueryResultSet resultSetColls = specQueryService.searchByFileProperties(
                        searchList, zone, true, pageContext, startIndex, pageSize);

                dataGridCollections = DataGridUtils
                        .mapPropertiesResultSetToDataGridObjects(resultSetColls);

                endIndexForDataObjs = pageSize - (totalCollections % pageSize);

                // looking for data objects
                SpecificQueryResultSet resultSetDataObjs = specQueryService.searchByFileProperties(
                        searchList, zone, false, pageContext, 0, endIndexForDataObjs);

                dataGridObjects = DataGridUtils
                        .mapPropertiesResultSetToDataGridObjects(resultSetDataObjs);

                endIndexForDataObjs = endIndexForDataObjs > dataGridObjects.size() ? dataGridObjects
                        .size() : endIndexForDataObjs;

                dataGridCollectionAndDataObjects.addAll(dataGridCollections);
                dataGridCollectionAndDataObjects.addAll(dataGridObjects);

                pageContext.setEndItemNumber(pageContext.getStartItemNumber() + endIndexForDataObjs
                        + dataGridCollections.size() - 1);
            }

        }
        catch (JargonException e) {
            logger.error("Could not find data objects by metadata. ", e);
            if (e.getCause() instanceof ConnectException) {
                throw new DataGridConnectionRefusedException();
            }
        }

        this.populateVisibilityForCurrentUser(dataGridCollectionAndDataObjects);
        return dataGridCollectionAndDataObjects;
    }

    private void populateVisibilityForCurrentUser(List<DataGridCollectionAndDataObject> objectList)
            throws DataGridConnectionRefusedException {

        CollectionAO collectionAO = irodsServices.getCollectionAO();
        DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();
        String currentUser = getLoggedDataGridUser().getUsername();

        for (DataGridCollectionAndDataObject obj : objectList) {

            List<UserFilePermission> permissions = null;

            try {
                if (obj.isCollection()) {
                    permissions = collectionAO.listPermissionsForCollection(obj.getPath());
                } else {
                    permissions = dataObjectAO.listPermissionsForDataObject(obj.getPath());
                }
            }
            catch (JargonException e) {
                logger.error("Could not get permission list for object {}", obj.getPath(), e);
            }

            obj.setVisibleToCurrentUser(false);
            if (permissions != null) {
                for (UserFilePermission permission : permissions) {
                    if (permission.getUserName().compareTo(currentUser) == 0) {
                        obj.setVisibleToCurrentUser(true);
                        break;
                    }
                }
            }

        }
    }

    private DataGridUser getLoggedDataGridUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) auth.getPrincipal();

        return userService.findByUsernameAndAdditionalInfo(username, zoneName);
    }

}
