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

package com.emc.metalnx.jobs;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.emc.metalnx.core.domain.dao.GroupDao;
import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.entity.DataGridGroup;
import com.emc.metalnx.core.domain.entity.DataGridUser;

@EnableScheduling
public class SyncJobs {

	@Autowired
	GroupDao groupDao;
	
	@Autowired
	UserDao userDao;
	
	@Autowired
	IRODSAccessObjectFactory irodsAccessObjectFactory;
	
	private IRODSAccount irodsAccount;
	
	private String irodsHost;
	private String irodsPort;
	private String irodsZone;
	private String irodsJobUser;
	private String irodsJobPassword;

	@Value("${runSyncJobs}")
	private String runSyncJobs;
	
	@Value("${jobs.irods.auth.scheme}")
	private String irodsAuthScheme;
	
	private static final Logger logger = LoggerFactory.getLogger(SyncJobs.class);

	@Scheduled(fixedDelay = 15000)
	public void sync() throws JargonException {
		if (runSyncJobs.compareTo("true") == 0) {
			logger.debug("Starting synchronization job...");
			
			if (this.irodsAccount == null) {
				authenticateIRODSAccount();
			}
			
			this.executeUsersSync();
			this.executeGroupSync();
		}
		else {
			logger.debug("Ignoring synchronization jobs...");
		}
	}
	
	private void executeUsersSync() {
		
		try{	
			//Gets all users existing in iRODS
			List<User> irodsUsers = this.findAllIRODSUsers();
			
			HashMap<Long, User> hashMapIRodsUsers = new HashMap<Long, User>();
			for (User user : irodsUsers) {
				if(user.getUserType() != UserTypeEnum.RODS_GROUP) {
					hashMapIRodsUsers.put(Long.valueOf(user.getId()), user);						
				}
			}
			
			//Gets all users existing in our database
			List<DataGridUser> dbUsers = userDao.findAll(DataGridUser.class);
			
			HashMap<Long, DataGridUser> hashMapDBUsers = new HashMap<Long, DataGridUser>();
			for (DataGridUser dataGridUser : dbUsers) {
				hashMapDBUsers.put(dataGridUser.getDataGridId(), dataGridUser);	
			}
			
			Set<Long> irodsUserIDs = hashMapIRodsUsers.keySet();
			Set<Long> dbDataGridUserIDs = hashMapDBUsers.keySet();
			
			//case 1: a user exists in our database but doesn't exist in iRODS
			//action: remove this user from our database
			for (Long id : dbDataGridUserIDs) {
				if(!irodsUserIDs.contains(id)) {
					String usernameDeleted = hashMapDBUsers.get(id).getUsername();
					long userID = hashMapDBUsers.get(id).getId();
					long dataGridID = hashMapDBUsers.get(id).getDataGridId();
											
					userDao.deleteByDataGridId(id);
					
					logger.info("[DELETE] User " + usernameDeleted 
							+ " (iRODS id: " + userID + ") "
							+ " (DataGrid id: " + dataGridID + ") "
							+ " was deleted from database.");
				}
			}
			
			//case 2: a user exists in iRODS but doesn't exist in our database
			//action: add this user to our database
			for (Long id : irodsUserIDs) {
				if (!dbDataGridUserIDs.contains(id)) {
					User irodsUserMissingInDB = hashMapIRodsUsers.get(id);
					
					DataGridUser userMissingInDB = new DataGridUser();
					userMissingInDB.setDataGridId(Long.valueOf(irodsUserMissingInDB.getId()));
					userMissingInDB.setUsername(irodsUserMissingInDB.getName());
					userMissingInDB.setAdditionalInfo(irodsUserMissingInDB.getZone());
					userMissingInDB.setUserType(irodsUserMissingInDB.getUserType().getTextValue());
					userMissingInDB.setEnabled(true);
					
					userDao.save(userMissingInDB);
					
					logger.info("[INSERT] User " + userMissingInDB.getUsername() 
							+ " (iRODS id: " + userMissingInDB.getDataGridId() + ") "
							+ " was added to database.");
				}
			}		
			
		} catch (Exception e) {
			logger.error("Could not synchronize database and iRODS (users): ", e);
		} 
		
	}

	private void executeGroupSync() {

		try {

			// Gets all groups existing in iRODS
			// OBS: we're using User to find Groups because UserGroup doesn't
			// have the get type
			// method and if we don't use such method we'll get a list of users
			// and groups,
			// rather than list containing only groups
			List<User> irodsGroups = this.findAllIRODSUsers();

			HashMap<Long, User> hashMapIRodsGroups = new HashMap<Long, User>();
			for (User group : irodsGroups) {
				if (group.getUserType() == UserTypeEnum.RODS_GROUP) {
					hashMapIRodsGroups.put(Long.valueOf(group.getId()), group);
				}
			}

			// Gets all groups existing in our database
			List<DataGridGroup> dbGroups = groupDao.findAll(DataGridGroup.class);

			HashMap<Long, DataGridGroup> hashMapDBGroups = new HashMap<Long, DataGridGroup>();
			for (DataGridGroup dataGridGroup : dbGroups) {
				hashMapDBGroups.put(dataGridGroup.getDataGridId(), dataGridGroup);
			}

			Set<Long> irodsGroupIDs = hashMapIRodsGroups.keySet();
			Set<Long> dbDataGridGroupIDs = hashMapDBGroups.keySet();

			// case 1: a Group exists in our database but doesn't exist in iRODS
			// action: remove this Group from our database
			for (Long id : dbDataGridGroupIDs) {
				if (!irodsGroupIDs.contains(id)) {
					String groupnameDeleted = hashMapDBGroups.get(id).getGroupname();
					long groupID = hashMapDBGroups.get(id).getId();
					long dataGridID = hashMapDBGroups.get(id).getDataGridId();

					groupDao.deleteByDataGridGroupId(id);

					logger.info("[DELETE] Group " + groupnameDeleted
							+ " (iRODS id: " + groupID + ") "
							+ " (DataGrid id: " + dataGridID + ") "
							+ " was deleted from database.");
				}
			}

			// case 2: a Group exists in iRODS but doesn't exist in our database
			// action: add this Group to our database
			for (Long id : irodsGroupIDs) {
				if (!dbDataGridGroupIDs.contains(id)) {
					// users and groups are dealt the same way by iRODS
					User irodsGroupMissingInDB = hashMapIRodsGroups.get(id);

					DataGridGroup groupMissingInDB = new DataGridGroup();
					groupMissingInDB.setDataGridId(Long.valueOf(irodsGroupMissingInDB.getId()));
					groupMissingInDB.setGroupname(irodsGroupMissingInDB.getName());
					groupMissingInDB.setAdditionalInfo(irodsGroupMissingInDB.getZone());

					groupDao.save(groupMissingInDB);

					logger.info("[INSERT] Group " + groupMissingInDB.getGroupname() 
							+ " (iRODS id: " + groupMissingInDB.getDataGridId() + ") "
							+ " was added to database");
				}
			}

		} catch (Exception e) {
			logger.error("Could not synchronize database and iRODS (Groups): ", e);
		}

	}
	
	private void authenticateIRODSAccount() throws JargonException {
		AuthResponse authResponse = null;

		if (this.irodsAccount == null) {
			// Getting iRODS protocol set
			IRODSAccount tempAccount = IRODSAccount.instance(irodsHost, Integer.parseInt(irodsPort), irodsJobUser, 
					irodsJobPassword, "", irodsZone, "demoResc");

			tempAccount.setAuthenticationScheme(AuthScheme.findTypeByString(irodsAuthScheme));
			authResponse = irodsAccessObjectFactory.authenticateIRODSAccount(tempAccount);

			if (authResponse.isSuccessful()) {
				this.irodsAccount = authResponse.getAuthenticatedIRODSAccount();
			}
		}
	}
	
	private List<User> findAllIRODSUsers() throws JargonException {
		
		// Retrieving logging user
		UserAO userAO = irodsAccessObjectFactory.getUserAO(this.irodsAccount);

		//returns all users existing in iRODS
		return userAO.findAll();

	}

	/**
	 * @return the irodsHost
	 */
	public String getIrodsHost() {
		return irodsHost;
	}

	/**
	 * @param irodsHost the irodsHost to set
	 */
	public void setIrodsHost(String irodsHost) {
		this.irodsHost = irodsHost;
	}

	/**
	 * @return the irodsPort
	 */
	public String getIrodsPort() {
		return irodsPort;
	}

	/**
	 * @param irodsPort the irodsPort to set
	 */
	public void setIrodsPort(String irodsPort) {
		this.irodsPort = irodsPort;
	}

	/**
	 * @return the irodsZone
	 */
	public String getIrodsZone() {
		return irodsZone;
	}

	/**
	 * @param irodsZone the irodsZone to set
	 */
	public void setIrodsZone(String irodsZone) {
		this.irodsZone = irodsZone;
	}

	/**
	 * @return the irodsJobUser
	 */
	public String getIrodsJobUser() {
		return irodsJobUser;
	}

	/**
	 * @param irodsJobUser the irodsJobUser to set
	 */
	public void setIrodsJobUser(String irodsJobUser) {
		this.irodsJobUser = irodsJobUser;
	}

	/**
	 * @return the irodsJobPassword
	 */
	public String getIrodsJobPassword() {
		return irodsJobPassword;
	}

	/**
	 * @param irodsJobPassword the irodsJobPassword to set
	 */
	public void setIrodsJobPassword(String irodsJobPassword) {
		this.irodsJobPassword = irodsJobPassword;
	}
}
