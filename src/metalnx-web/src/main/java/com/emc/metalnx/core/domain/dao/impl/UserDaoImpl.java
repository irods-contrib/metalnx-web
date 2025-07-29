/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.core.domain.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.pub.IRODSGenQueryExecutorImpl;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.aohelper.UserAOHelper;
import org.irods.jargon.core.pub.domain.User;
import org.irods.jargon.core.query.IRODSGenQuery;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSetInterface;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.RodsGenQueryEnum;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;

@SuppressWarnings("unchecked")
@Repository
@Transactional
public class UserDaoImpl implements UserDao {
	
	@Autowired
	private IRODSServices irodsServices;
	
	final int DEFAULT_REC_COUNT = 500;

	private static final Logger logger = LogManager.getLogger(UserDaoImpl.class);

	@Override
	public List<DataGridUser> findByUsername(String username) {

		List<DataGridUser> userList = new ArrayList<DataGridUser>();
		
		UserAO userAO = null;
		try {
			userAO = irodsServices.getUserAO();
		} catch (DataGridConnectionRefusedException e) {
			logger.error("Connection refused when attempting to find user {}: {}", username, e);
			return userList;
		}
		User u;
		try {
			u = userAO.findByName(username);
		} catch (DataNotFoundException e) {
			// not found - return empty list
			return userList;
		} catch (JargonException e) {
			logger.error("JargonException when attempting to find user {}: {}", username, e);
			return userList;
		}
		
		// translate User to DataGridUser
		DataGridUser dataGridUser = new DataGridUser();
		dataGridUser.setUsername(u.getName());
		dataGridUser.setId(Integer.parseInt(u.getId()));
		dataGridUser.setDataGridId(dataGridUser.getId());
		dataGridUser.setUserType(u.getUserType().getTextValue());
		dataGridUser.setZone(u.getZone());
		
		// add user to list
		userList.add(dataGridUser);
		return userList;
	}

	@Override
	public DataGridUser findByUsernameAndZone(String username, String zone) {
		
		UserAO userAO = null;
		try {
			userAO = irodsServices.getUserAO();
		} catch (DataGridConnectionRefusedException e) {
			logger.error("Connection refused when attempting to find by user name and zone - username=[{}] zone=[{}]: {}", username, zone, e);
			return null;
		}
		
		// This uses a modified copy of the code from Jargon UserAOImpl.findByIdInZone
		
        IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = null;
        IRODSAccount irodsAccount = null; 
		try {
			irodsAccount = userAO.getIRODSAccount();
			irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(userAO.getIRODSSession(), irodsAccount);
			StringBuilder userQuery = new StringBuilder();

			userQuery.append(UserAOHelper.buildUserSelects());
			userQuery.append(" where ");
			userQuery.append(RodsGenQueryEnum.COL_USER_NAME.getName());
			userQuery.append(" = '");
			userQuery.append(username);
			userQuery.append("'");

			String userQueryString = userQuery.toString();

			IRODSGenQuery irodsQuery;
			irodsQuery = IRODSGenQuery.instance(userQueryString, DEFAULT_REC_COUNT);


			IRODSQueryResultSetInterface resultSet;
			try {
				resultSet = irodsGenQueryExecutorImpl.executeIRODSQueryAndCloseResultInZone(irodsQuery, 0, zone);
			} catch (JargonQueryException e) {
				logger.error("query exception for user query:{}", userQueryString, e);
				return null;
			} 

			if (resultSet.getResults().size() == 0) {
				StringBuilder messageBuilder = new StringBuilder();
				messageBuilder.append("user not found for username:");
				messageBuilder.append(username);
				String message = messageBuilder.toString();
				logger.warn(message);
				return null;
			}

			if (resultSet.getResults().size() > 1) {
				StringBuilder messageBuilder = new StringBuilder();
				messageBuilder.append("more than one user found for username:");
				messageBuilder.append(username);
				String message = messageBuilder.toString();
				logger.error(message);
				return null;
			}

			// I know I have just one user

			IRODSQueryResultRow row;
			try {
				row = resultSet.getFirstResult();
			} catch (DataNotFoundException e) {
				return null;
			}
			IRODSAccessObjectFactory aof = irodsServices.getIrodsAccessObjectFactory();
			IRODSGenQueryExecutor irodsGenQueryExecutor = aof.getIRODSGenQueryExecutor(userAO.getIRODSAccount());
			User u = UserAOHelper.buildUserFromResultSet(row, irodsGenQueryExecutor, true);

			// translate User to DataGridUser
			DataGridUser dataGridUser = new DataGridUser();
			dataGridUser.setUsername(u.getName());
			dataGridUser.setId(Integer.parseInt(u.getId()));
			dataGridUser.setDataGridId(dataGridUser.getId());
			dataGridUser.setUserType(u.getUserType().getTextValue());
			dataGridUser.setZone(u.getZone());
			logger.info("findByUsernameAndZone: dataGridUser.id=[{}] dataGridUser.dataGridId=[{}]", dataGridUser.getId(), dataGridUser.getDataGridId());
			return dataGridUser;
		} catch (JargonException e) {
			logger.error("JargonException encounted when attempting to find by user name and zone - username=[{}] zone=[{}]: {}", username, zone, e);
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean deleteByUsername(String username) {
		
		UserAO userAO = null;
		try {
			userAO = irodsServices.getUserAO();
		} catch (DataGridConnectionRefusedException e) {
			logger.error("Connection refused when attempting to delete user {}: {}", username, e);
			return false;
		}
		
		try {
			userAO.deleteUser(username);
		} catch (InvalidUserException e) {
			return false;
		} catch (JargonException e) {
			logger.error("JargonException when attempting to delete user {}: {}", username, e);
			return false;
		}
		
		return true;
	}

	@Override
	public List<DataGridUser> findByQueryString(String query) {
		
		// TODO this needs testing as I am not sure what the intent is
		
		List<DataGridUser> userList = new ArrayList<DataGridUser>();
		
		UserAO userAO = null;
		try {
			userAO = irodsServices.getUserAO();
		} catch (DataGridConnectionRefusedException e) {
			logger.error("Connection refused when finding users by query string {}: {}", query, e);
			return userList;
		}
		
        IRODSGenQueryExecutorImpl irodsGenQueryExecutorImpl = null;
        IRODSAccount irodsAccount = null; 
		try {
			irodsAccount = userAO.getIRODSAccount();
			irodsGenQueryExecutorImpl = new IRODSGenQueryExecutorImpl(userAO.getIRODSSession(), irodsAccount);
			StringBuilder userQuery = new StringBuilder();

			// original code searched for matches to either username, first name, last name,
			// email, additional info but I think only username applies here since we are using
			// the iRODS database
			userQuery.append(UserAOHelper.buildUserSelects());
			userQuery.append(" where ");
			userQuery.append(RodsGenQueryEnum.COL_USER_NAME.getName());
			userQuery.append(" = '");
			userQuery.append("%" + query + "%");
			userQuery.append("'");

			String userQueryString = userQuery.toString();

			IRODSGenQuery irodsQuery;
			irodsQuery = IRODSGenQuery.instance(userQueryString, DEFAULT_REC_COUNT);

			IRODSQueryResultSetInterface resultSet;
			try {
				resultSet = irodsGenQueryExecutorImpl.executeIRODSQueryAndCloseResult(irodsQuery, 0);
			} catch (JargonQueryException e) {
				logger.error("query exception for user query:{}", userQueryString, e);
				return userList;
			} 

			if (resultSet.getResults().size() == 0) {
				// nothing found - just return empty userList
				return userList;
			}

			List<IRODSQueryResultRow> results = resultSet.getResults();
			
			for (IRODSQueryResultRow row : results) {
				IRODSAccessObjectFactory aof = irodsServices.getIrodsAccessObjectFactory();
				IRODSGenQueryExecutor irodsGenQueryExecutor = aof.getIRODSGenQueryExecutor(userAO.getIRODSAccount());
				User u = UserAOHelper.buildUserFromResultSet(row, irodsGenQueryExecutor, true);

				// translate User to DataGridUser
				DataGridUser dataGridUser = new DataGridUser();
				dataGridUser.setUsername(u.getName());
				dataGridUser.setId(Integer.parseInt(u.getId()));
				dataGridUser.setDataGridId(dataGridUser.getId());
				dataGridUser.setUserType(u.getUserType().getTextValue());
				dataGridUser.setZone(u.getZone());
				userList.add(dataGridUser);
			}
			return userList;
		} catch (JargonException e) {
			logger.error("JargonException encountered when finding users by query string {}: {}", query, e);
			e.printStackTrace();
			return userList;
		}
	}

	@Override
	public List<DataGridUser> findByDataGridIdList(String[] ids) {
		
		logger.info("findByDataGridIdList()");
		List<DataGridUser> result = new ArrayList<DataGridUser>();
		
		if (ids == null) {
			return result;
		}
		
		for (String id : ids) {
			DataGridUser dataGridUser = new DataGridUser();
			
			UserAO userAO = null;
			try {
				userAO = irodsServices.getUserAO();
			} catch (DataGridConnectionRefusedException e) {
				logger.error("Connection refused when attempting to find user by id {}: {}", id, e);
				return result;
			}
			User u;
			try {
				u = userAO.findById(id);
			} catch (DataNotFoundException e) {
				// not found - return empty list
				continue;
			} catch (JargonException e) {
				logger.error("JargonException when attempting to find user by id {}: {}", id, e);
				continue;
			}
			
			// translate User to DataGridUser
			dataGridUser.setUsername(u.getName());
			dataGridUser.setId(Integer.parseInt(u.getId()));
			dataGridUser.setDataGridId(dataGridUser.getId());
			dataGridUser.setUserType(u.getUserType().getTextValue());
			dataGridUser.setZone(u.getZone());
			
			result.add(dataGridUser);
		}
		
		return result;
	}

	@Override
	public DataGridUser findByDataGridId(long id) {
		
		UserAO userAO = null;
		try {
			userAO = irodsServices.getUserAO();
		} catch (DataGridConnectionRefusedException e) {
			logger.error("Connection refused when attempting to find user by id {}: {}", id, e);
			return null;
		}
		User u;
		try {
			u = userAO.findById(Long.toString(id));
		} catch (DataNotFoundException e) {
			// not found - return null;
			return null;
		} catch (JargonException e) {
			logger.error("JargonException when attempting to find user by id {}: {}", id, e);
			return null;
		}
		
		// translate User to DataGridUser
		DataGridUser dataGridUser = new DataGridUser();
		dataGridUser.setUsername(u.getName());
		dataGridUser.setId(Integer.parseInt(u.getId()));
		dataGridUser.setDataGridId(dataGridUser.getId());
		dataGridUser.setUserType(u.getUserType().getTextValue());
		dataGridUser.setZone(u.getZone());
		
		return dataGridUser;
	}

	@Override
	public boolean deleteByDataGridId(long id) {
		
		UserAO userAO = null;
		try {
			userAO = irodsServices.getUserAO();
		} catch (DataGridConnectionRefusedException e) {
			logger.error("Connection refused when attempting to delete user by id {}: {}", id, e);
			return false;
		}
		User u;
		try {
			u = userAO.findById(Long.toString(id));
		} catch (DataNotFoundException e) {
			// not found - return empty list
			return false;
		} catch (JargonException e) {
			logger.error("JargonException when attempting to delete user by id {}: {}", id, e);
			return false;
		}
		try {
			userAO.deleteUser(u.getName());
		} catch (InvalidUserException e) {
			return false;
		} catch (JargonException e) {
			logger.error("JargonException when attempting to delete user by id {}: {}", id, e);
			return false;
		}
		
		return true;

	}

	@Override
	public List<DataGridUser> findAll() {
		
		List<DataGridUser> dataGridUserList = new ArrayList<DataGridUser>();
		
		UserAO userAO = null;
		try {
			userAO = irodsServices.getUserAO();
		} catch (DataGridConnectionRefusedException e) {
			logger.error("Connection refused when attempting to find all users: {}", e);
			return dataGridUserList;
		}
		
		List<User> userList;
		try {
			userList = userAO.findAll();
		} catch (JargonException e) {
			logger.error("JargonException when attempting to find all users: {}", e);
			e.printStackTrace();
			return dataGridUserList;
		}
		
		// Iterate, convert from User to DataGridUser, and insert into dataGridUserList
		for (User u : userList) {
			// translate User to DataGridUser
			DataGridUser dataGridUser = new DataGridUser();
			dataGridUser.setUsername(u.getName());
			dataGridUser.setId(Integer.parseInt(u.getId()));
			dataGridUser.setDataGridId(dataGridUser.getId());
			dataGridUser.setUserType(u.getUserType().getTextValue());
			dataGridUser.setZone(u.getZone());

			// add user to list
			dataGridUserList.add(dataGridUser);
		}
		return dataGridUserList;
	}

}
