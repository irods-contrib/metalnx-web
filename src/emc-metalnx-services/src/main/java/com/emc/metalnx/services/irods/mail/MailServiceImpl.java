package com.emc.metalnx.services.irods.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserService;

/**
 * Mail services
 * 
 * @author Mike Conway - NIEHS
 *
 */

@Service
@Transactional
public class MailServiceImpl {

	@Autowired
	IRODSServices irodsServices;

	@Autowired
	UserService userService;

	private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

	public IRODSServices getIrodsServices() {
		return irodsServices;
	}

	public void setIrodsServices(IRODSServices irodsServices) {
		this.irodsServices = irodsServices;
	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

}
