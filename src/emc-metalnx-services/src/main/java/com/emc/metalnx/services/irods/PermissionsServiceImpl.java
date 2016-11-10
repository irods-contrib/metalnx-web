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

import com.emc.metalnx.core.domain.dao.GroupBookmarkDao;
import com.emc.metalnx.core.domain.dao.GroupDao;
import com.emc.metalnx.core.domain.entity.*;
import com.emc.metalnx.core.domain.entity.enums.DataGridPermType;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.PermissionsService;
import org.irods.jargon.core.exception.FileNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.FilePermissionEnum;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.IRODSFileSystemAO;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.UserFilePermission;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class PermissionsServiceImpl implements PermissionsService {

    /*
     * PERMISSIONS in iRODS
     * READ: Download, Copy, and Replicate
     * WRITE: Download, Copy, Replicate, Metadata (templates)
     * OWN: Download, Copy, Replicate, Metadata (templates), Move, Edit, and Delete
     */

    @Autowired
    IRODSServices irodsServices;

    @Autowired
    GroupBookmarkDao groupBookmarkDao;

    @Autowired
    GroupDao groupDao;

    @Value("${irods.zoneName}")
    private String zoneName;

    // String representing the rodsgroup type on the UserFilePermission enum
    private static final String RODS_GROUP = "rodsgroup";

    private static final Logger logger = LoggerFactory.getLogger(PermissionsServiceImpl.class);

    @Override
    public List<DataGridFilePermission> getPathPermissionDetails(String path, String username) throws JargonException,
            DataGridConnectionRefusedException {

        logger.debug("Getting permissions details for object {}", path);
        List<UserFilePermission> filePermissionList = this.getFilePermissionListForObject(path, username);
        return mapListToListDataGridFilePermission(filePermissionList);
    }

    @Override
    public List<DataGridFilePermission> getPathPermissionDetails(String path) throws JargonException,
            DataGridConnectionRefusedException {

        logger.info("Getting permissions details for object {}", path);
        List<UserFilePermission> filePermissionList = this.getFilePermissionListForObject(path);
        return mapListToListDataGridFilePermission(filePermissionList);
    }

    @Override
    public boolean canLoggedUserModifyPermissionOnPath(String path) throws DataGridConnectionRefusedException {

        String userName = irodsServices.getCurrentUser();
        final IRODSFileFactory irodsFileFactory = irodsServices.getIRODSFileFactory();
        final IRODSFileSystemAO irodsFileSystemAO = irodsServices.getIRODSFileSystemAO();

        try {
            int resultingPermission;
            final IRODSFile fileObj = irodsFileFactory.instanceIRODSFile(path);

            if (irodsFileSystemAO.isDirectory(fileObj)) {
                resultingPermission = irodsFileSystemAO.getDirectoryPermissionsForGivenUser(fileObj, userName);
            } else {
                resultingPermission = irodsFileSystemAO.getFilePermissionsForGivenUser(fileObj, userName);
            }

            return resultingPermission > FilePermissionEnum.WRITE.getPermissionNumericValue();

        } catch (final Exception e) {
            logger.error("Could not get permissions for current user: {}", e.getMessage());
        }

        return false;
    }

    /***********************************************************************************/
    /************************* PERMISSIONS/GROUPS PROCESSING ***************************/
    /***********************************************************************************/

    @Override
    public List<DataGridGroupPermission> getGroupsWithPermissions(List<DataGridFilePermission> ufps) {

        // Maps from data grid ID to DataGridGroupPermission object for retrieving the group names
        HashMap<String, DataGridGroupPermission> idGroupsPermissions = new HashMap<String, DataGridGroupPermission>();

        for (DataGridFilePermission ufp : ufps) {

            // Getting only the groups, ignoring users
            if (ufp.getUserType().compareTo(RODS_GROUP) == 0) {

                String groupDataGridId = ufp.getUserId();

                // If the ID is not known yet, we need to create a new entry for it
                if (!idGroupsPermissions.containsKey(groupDataGridId)) {
                    DataGridGroupPermission dggp = new DataGridGroupPermission();
                    dggp.setDataGridId(Integer.parseInt(groupDataGridId));
                    dggp.setPermission(ufp.getPermission());

                    // Creating new entry for group
                    idGroupsPermissions.put(groupDataGridId, dggp);
                }
            }
        }

        // Making sure we have groups to query for
        if (idGroupsPermissions.size() > 0) {

            // Getting list of unique IDs on an array
            String[] groupIds = idGroupsPermissions.keySet().toArray(new String[idGroupsPermissions.size()]);

            // One single DB query to get group names
            List<DataGridGroup> groupObjects = groupDao.findByDataGridIdList(groupIds);

            // Setting group names to the elements on the hash map
            for (DataGridGroup group : groupObjects) {
                idGroupsPermissions.get(String.valueOf(group.getDataGridId())).setGroupName(group.getGroupname());
            }
        }

        // Casting hash map values list to array list and returning
        return new ArrayList<DataGridGroupPermission>(idGroupsPermissions.values());
    }

    /***********************************************************************************/
    /************************* PERMISSIONS/USERS PROCESSING ****************************/
    /***********************************************************************************/

    @Override
    public List<DataGridUserPermission> getUsersWithPermissions(List<DataGridFilePermission> ufps) {

        // List containing all the users with some kind of permissions
        List<DataGridUserPermission> usersWithPermissions = new ArrayList<DataGridUserPermission>();

        for (DataGridFilePermission ufp : ufps) {

            // Getting only the users, ignoring groups
            if (ufp.getUserType().compareTo(RODS_GROUP) != 0) {
                DataGridUserPermission dgup = new DataGridUserPermission();
                dgup.setDataGridId(Integer.parseInt(ufp.getUserId()));
                dgup.setUserName(ufp.getUsername());
                dgup.setUserSystemRole(ufp.getUserType());
                dgup.setPermission(ufp.getPermission());
                usersWithPermissions.add(dgup);
            }
        }

        return usersWithPermissions;
    }

    @Override
    public boolean setPermissionOnPath(DataGridPermType permType, String uName, String path, boolean recursive, boolean inAdminMode)
            throws DataGridConnectionRefusedException {

        logger.info("Attempting to set {} permission on path {} for user/group {}", permType, path, uName);

        boolean operationResult = true;

        try {
            IRODSFile irodsFilePath = irodsServices.getIRODSFileFactory().instanceIRODSFile(path);

            if (irodsFilePath.isDirectory()) {
                logger.debug("{} is a collection", path);
                operationResult = chmodCollection(permType, path, recursive, uName, inAdminMode);
            } else {
                logger.debug("{} is a data object", path);
                operationResult = chmodDataObject(permType, path, uName, inAdminMode);
            }

            // If the permissions is set to NONE, remove all the bookmarks associated to the group
            // and the path
            if (permType.equals(DataGridPermType.NONE)) {
                // Making sure we are dealing with a group
                String currentZone = irodsServices.getCurrentUserZone();
                DataGridGroup group = groupDao.findByGroupnameAndZone(uName, currentZone);
                if (group != null) {
                    groupBookmarkDao.removeByGroupAndPath(group, path);
                }
            }

        } catch (JargonException e) {
            logger.error("Could not set {} permission on path {} for user/group {}", permType, path, uName, e);
            operationResult = false;
        }

        if (operationResult) {
            logger.info("Successfully set the permission {} for user {} on path {}", permType, uName, path);
        }

        return operationResult;
    }

    private boolean chmodCollection(DataGridPermType permType, String path, boolean recursive, String uName, boolean inAdminMode) throws DataGridConnectionRefusedException {
        String currentZone = irodsServices.getCurrentUserZone();
        CollectionAO collectionAO = irodsServices.getCollectionAO();
        boolean isPermissionSet = false;

        try {
            logger.debug("Setting {} permission on collection {} for user/group as ADMIN{}", permType, path, uName);

            if(!inAdminMode) {
                FilePermissionEnum filePermission = FilePermissionEnum.valueOf(permType.toString());
                collectionAO.setAccessPermission(currentZone, path, uName, recursive, filePermission);
            }
            else if(permType.equals(DataGridPermType.READ))
                collectionAO.setAccessPermissionReadAsAdmin(currentZone, path, uName, recursive);

            else if(permType.equals(DataGridPermType.WRITE))
                collectionAO.setAccessPermissionWriteAsAdmin(currentZone, path, uName, recursive);

            else if(permType.equals(DataGridPermType.OWN))
                collectionAO.setAccessPermissionOwnAsAdmin(currentZone, path, uName, recursive);

            else collectionAO.removeAccessPermissionForUserAsAdmin(currentZone, path, uName, recursive);

            isPermissionSet = true;
        } catch (JargonException e) {
            logger.error("Could not set {} permission on path {} for user/group {}", permType, path, uName, e);
        }

        return isPermissionSet;
    }

    private boolean chmodDataObject(DataGridPermType permType, String path, String uName, boolean inAdminMode) throws DataGridConnectionRefusedException {
        String currentZone = irodsServices.getCurrentUserZone();
        DataObjectAO dataObjectAO = irodsServices.getDataObjectAO();

        logger.debug("Setting {} permission on data object {} for user/group {}", permType, path, uName);

        boolean isPermissionSet = false;

        try {
            if(!inAdminMode) {
                FilePermissionEnum filePermission = FilePermissionEnum.valueOf(permType.toString());
                dataObjectAO.setAccessPermission(currentZone, path, uName, filePermission);
            }
            else if(permType.equals(DataGridPermType.READ))
                dataObjectAO.setAccessPermissionReadInAdminMode(currentZone, path, uName);

            else if(permType.equals(DataGridPermType.WRITE))
                dataObjectAO.setAccessPermissionWriteInAdminMode(currentZone, path, uName);

            else if(permType.equals(DataGridPermType.OWN))
                dataObjectAO.setAccessPermissionOwnInAdminMode(currentZone, path, uName);

            else dataObjectAO.removeAccessPermissionsForUserInAdminMode(currentZone, path, uName);

            isPermissionSet = true;
        } catch (JargonException e) {
            logger.error("Could not set {} permission on path {} for user/group {}", permType, path, uName, e);
        }

        return isPermissionSet;
    }

    @Override
    public void resolveMostPermissiveAccessForUser(DataGridCollectionAndDataObject obj, DataGridUser user) throws
            DataGridException {

        List<UserGroup> userGroups = null;
        List<UserFilePermission> acl = null;

        try {
            userGroups = irodsServices.getGroupAO().findUserGroupsForUser(user.getUsername());
            acl = getFilePermissionListForObject(obj.getPath());
        } catch (JargonException e) {
            throw new DataGridException();
        }

        // Building set containing group names for current user
        Set<String> userGroupsSet = new HashSet<>();
        for (UserGroup g : userGroups) {
            userGroupsSet.add(g.getUserGroupName());
        }

        // Instantiating comparison matrix for permissions
        List<String> permissions = new ArrayList<>();
        permissions.add("NONE");
        permissions.add("READ");
        permissions.add("WRITE");
        permissions.add("OWN");

        String resultingPermission = "NONE";
        for (UserFilePermission perm : acl) {

            String permUserName = perm.getUserName();

            // Checking if current permission is related to logged user
            if (permUserName.compareTo(user.getUsername()) == 0 || userGroupsSet.contains(permUserName)) {
                String permissionName = perm.getFilePermissionEnum().name();
                int userOrGroupPerm = permissions.indexOf(permissionName);
                int currentPermission = permissions.indexOf(resultingPermission);

                if (userOrGroupPerm > currentPermission) {
                    resultingPermission = permissionName;
                }
            }

            if (resultingPermission.compareToIgnoreCase("OWN") == 0) {
                break;
            }
        }

        obj.setMostPermissiveAccessForCurrentUser(resultingPermission.toLowerCase());
    }

    /***********************************************************************************/
    /******************************** PRIVATE METHODS **********************************/
    /***********************************************************************************/

    /**
     * Gets the list of file permissions on the requested object. The object can be a collection
     * as a single data object.
     *
     * @param path the path to the object
     * @return list of {@link UserFilePermission}
     * @throws FileNotFoundException
     * @throws JargonException
     * @throws DataGridConnectionRefusedException
     */
    private List<UserFilePermission> getFilePermissionListForObject(String path) throws JargonException,
            DataGridConnectionRefusedException {

        return this.getFilePermissionListForObject(path, "");
    }

    /**
     * Gets the list of file permissions on the requested object for a particular user. The object
     * can be a collection as a single data object.
     *
     * @param path     the path to the object
     * @param username user name to get the permissions on the given path. If no user name is required,
     *                 an empty String or null should be provided
     * @return list of {@link UserFilePermission}
     * @throws FileNotFoundException
     * @throws JargonException
     * @throws DataGridConnectionRefusedException
     */
    private List<UserFilePermission> getFilePermissionListForObject(String path, String username) throws DataGridConnectionRefusedException,
            FileNotFoundException, JargonException {
        Object obj = irodsServices.getCollectionAndDataObjectListAndSearchAO().getFullObjectForType(path);

        List<UserFilePermission> filePermissionList = new ArrayList<UserFilePermission>();
        List<UserFilePermission> dataGridfilePermissionList = null;

        // If the object is a collection
        if (obj instanceof Collection) {
            logger.debug("Getting permission info for collection {}", path);
            dataGridfilePermissionList = irodsServices.getCollectionAO().listPermissionsForCollection(path);
        }

        // If the object is a data object
        else {
            logger.debug("Getting permission info for data object {}", path);
            dataGridfilePermissionList = irodsServices.getDataObjectAO().listPermissionsForDataObject(path);
        }

        // adding to the final list of permissions only permissions related to the user given
        // as the parameter
        if (username != null && !username.isEmpty()) {
            for (UserFilePermission userFilePermission : dataGridfilePermissionList) {
                if (userFilePermission.getUserName().equalsIgnoreCase(username)) {
                    filePermissionList.add(userFilePermission);
                }
            }
        } else {
            filePermissionList = dataGridfilePermissionList;
        }

        return filePermissionList;
    }

    /**
     * Maps an UserFilePermission instance to DataGridFilePermission object
     *
     * @param ufp
     * @return instance of {@link DataGridFilePermission}
     */
    private DataGridFilePermission mapToDataGridFilePermission(UserFilePermission ufp) {

        logger.debug("\tMapping permissions for permissions controller");

        DataGridFilePermission dgfp = new DataGridFilePermission();
        dgfp.setUserId(ufp.getUserId());
        dgfp.setUsername(ufp.getUserName());
        dgfp.setPermission(ufp.getFilePermissionEnum().toString());
        dgfp.setUserType(ufp.getUserType().getTextValue());
        dgfp.setUserZone(ufp.getUserZone());
        return dgfp;
    }

    /**
     * Maps a list of UserFilePermission instances to a list of DataGridFilePermission
     * objects.
     *
     * @param filePermissionList
     * @return list of instances of {@link DataGridFilePermission}
     */
    private List<DataGridFilePermission> mapListToListDataGridFilePermission(List<UserFilePermission> filePermissionList) {

        logger.debug("Mapping list of UserFilePermissions to List of DataGridFilePermission");

        List<DataGridFilePermission> dgFilePermissionList = new ArrayList<DataGridFilePermission>();
        for (UserFilePermission ufp : filePermissionList) {
            DataGridFilePermission dgfp = mapToDataGridFilePermission(ufp);
            dgFilePermissionList.add(dgfp);
        }
        return dgFilePermissionList;
    }
}
