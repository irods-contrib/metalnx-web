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

package com.emc.metalnx.services.auth;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.core.domain.entity.DataGridUser;

/**
 * The object that is encapsulated in the user session
 *
 */
public class UserTokenDetails {

	private DataGridUser user;
	private IRODSAccount irodsAccount;
	
	private static final Logger logger = LoggerFactory.getLogger(UserTokenDetails.class);
	
	/**
	 * @return the irodsAccount
	 */
	public IRODSAccount getIrodsAccount() {
		return irodsAccount;
	}
	/**
	 * @param irodsAccount the irodsAccount to set
	 */
	public void setIrodsAccount(IRODSAccount irodsAccount) {
		this.irodsAccount = irodsAccount;
	}
	/**
	 * @return the irodsFileSystem
	 */
	public IRODSFileSystem getIrodsFileSystem() {
		try {
			return IRODSFileSystem.instance();
		} catch (JargonException e) {
			logger.error("Could not get instance of IRODSFileSystem: ", e);
		}
		return null;
	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		try {
			return this.getIrodsFileSystem().getIRODSAccessObjectFactory();
		} catch (JargonException e) {
			logger.error("Could not get Access Object Factory from IRODS: ", e);
		}
		return null;
	}

	/**
	 * @return the user
	 */
	public DataGridUser getUser() {
		return user;
	}
	/**
	 * @param user the user to set
	 */
	public void setUser(DataGridUser user) {
		this.user = user;
	}
	
}
