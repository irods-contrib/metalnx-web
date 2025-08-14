/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.auth;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.connection.auth.AuthResponse;
import org.irods.jargon.core.exception.InvalidUserException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.protovalues.UserTypeEnum;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.UserAO;
import org.irods.jargon.core.pub.domain.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.transaction.TransactionException;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridAuthenticationException;
import com.emc.metalnx.core.domain.exceptions.DataGridDatabaseException;
import com.emc.metalnx.core.domain.exceptions.DataGridServerException;
import com.emc.metalnx.services.interfaces.AuthenticationProviderService;
import com.emc.metalnx.services.interfaces.IRODSServices;

public class IRODSAuthenticationProvider implements AuthenticationProviderService, Serializable {
	private static final String IRODS_ANONYMOUS_ACCOUNT = "anonymous";

	@Autowired
	UserDao userDao;

	@Autowired
	IRODSAccessObjectFactory irodsAccessObjectFactory;
	
	@Autowired
	private IRODSServices irodsServices;

	private String irodsHost;
	private String irodsPort;
	private String irodsZoneName;

	@Value("${irods.auth.scheme}")
	private String irodsAuthScheme;

	// Instance variables to be set to UserTokenDetails instance.
	private IRODSAccount irodsAccount;

	private DataGridUser user;

	private static final Logger logger = LogManager.getLogger(IRODSAuthenticationProvider.class);

	private static final long serialVersionUID = -4984545776727334580L;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		logger.info("authenticate()");
		String username = authentication.getName();
		String password = authentication.getCredentials().toString();
		AuthResponse authResponse;
		UsernamePasswordAuthenticationToken authObject;

		RequestAttributes attribs = RequestContextHolder.getRequestAttributes();
		AuthScheme authSchemeEnum = null;

		if (RequestContextHolder.getRequestAttributes() != null) {
			HttpServletRequest request = ((ServletRequestAttributes) attribs).getRequest();
			String authScheme = request.getParameter("authScheme");
			logger.info("authScheme:{}", authScheme);
			authSchemeEnum = AuthScheme.findTypeByString(authScheme);
			logger.info("authSchemeEnum found:{}", authSchemeEnum);
		}

		if (authSchemeEnum == null) {
			logger.error("cannot find auth scheme in request");
			throw new DataGridAuthenticationException("no auth scheme found in request");
		}

		logger.debug("Setting username {}", username);

		try {
			authResponse = this.authenticateAgainstIRODS(username, password, authSchemeEnum);

			// Settings iRODS account
			this.irodsAccount = authResponse.getAuthenticatedIRODSAccount();

			// Retrieving logging user
			User irodsUser = new User();

			try {
				irodsUser = this.irodsAccessObjectFactory.getUserAO(this.irodsAccount).findByName(username);
				logger.debug("irodsUser:{}", irodsUser);
			} catch (JargonException e) {
				logger.error("Could not find user: " + e.getMessage());
			}

			GrantedAuthority grantedAuth;
			if (irodsUser.getUserType().equals(UserTypeEnum.RODS_ADMIN)) {
				grantedAuth = new IRODSAdminGrantedAuthority();
			} else if (irodsUser.getUserType().equals(UserTypeEnum.GROUP_ADMIN)) {
				grantedAuth = new IRODSGroupadminGrantedAuthority();
			} else {
				grantedAuth = new IRODSUserGrantedAuthority();
			}

			logger.info("granted authority:{}", grantedAuth);

			// Settings granted authorities
			List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
			grantedAuths.add(grantedAuth);

			// Returning authentication token with the access object factory injected
			authObject = new UsernamePasswordAuthenticationToken(username, password, grantedAuths);

			// Creating UserTokenDetails instance for the current authenticated user
			UserTokenDetails userDetails = new UserTokenDetails();
			userDetails.setIrodsAccount(this.irodsAccount);
			userDetails.setUser(this.user);

			// Settings the user details object into the authentication object
			authObject.setDetails(userDetails);
		} catch (TransactionException e) {
			logger.error("Database not responding");
			throw new DataGridDatabaseException("database not responding", e);
		} catch (InvalidUserException | org.irods.jargon.core.exception.AuthenticationException e) {
			logger.error("Could not authenticate user:{}", username, e);
			throw new DataGridAuthenticationException("could not authenticate user", e);
		} catch (JargonException e) {
			logger.error("Server not responding", e);
			throw new DataGridServerException("exception", e);
		}

		return authObject;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return authentication.equals(UsernamePasswordAuthenticationToken.class);
	}

	private AuthResponse authenticateAgainstIRODS(String username, String password, AuthScheme authScheme)
			throws JargonException {

		logger.info("authenticateAgainstIRODS()");

		if (username == null || username.isEmpty() || password == null) {
			logger.error("null or empty username");
			throw new DataGridAuthenticationException("Username or password invalid: null or empty value(s) provided");
		}

		/*
		 * add if ladder so that if anon is allowed...let it happen
		 */

		if (authScheme == null) {
			logger.error("authScheme is null");
			throw new DataGridAuthenticationException("authScheme is null");
		}

		logger.info("username:{}", username);
		logger.info("authScheme:{}", authScheme);

		AuthResponse authResponse;

		// Getting iRODS protocol set
		logger.debug("Creating IRODSAccount object.");
		this.irodsAccount = IRODSAccount.instance(this.irodsHost, Integer.parseInt(this.irodsPort), username, password,
				"", this.irodsZoneName, "");
		this.irodsAccount.setAuthenticationScheme(authScheme);
		logger.info("set irodsAccount auth scheme to :{}", irodsAccount.getAuthenticationScheme());

		logger.debug(
				"Authenticating IRODSAccount:\n\tusername: {}\n\tpassword: ***********\n\tirodsHost: {}\n\tirodsZone: {}",
				username, this.irodsHost, this.irodsZoneName);
		authResponse = this.irodsAccessObjectFactory.authenticateIRODSAccount(this.irodsAccount);
		logger.debug("Done.");

		if (authResponse.isSuccessful()) {

			logger.info("auth is successful");
			if (authResponse.getAuthMessage().isEmpty()) {
				logger.debug("AuthMessage: {}", authResponse.getAuthMessage());
			}

			// Settings iRODS account
			this.irodsAccount = authResponse.getAuthenticatingIRODSAccount();
			logger.debug("authenticating irodsAccount:{}", this.irodsAccount);
			
			// Save iRODS account to AdminServices 
			irodsServices.setIrodsAccount(this.irodsAccount);
			

			// Retrieving logging user
			UserAO userAO = this.irodsAccessObjectFactory.getUserAO(this.irodsAccount);
			User irodsUser = userAO.findByName(username);

			// If the user is found
			if (irodsUser.getUserType().equals(UserTypeEnum.RODS_ADMIN)
					|| irodsUser.getUserType().equals(UserTypeEnum.GROUP_ADMIN)
					|| irodsUser.getUserType().equals(UserTypeEnum.RODS_USER)) {

				// If the user is not yet persisted in our database
				DataGridUser user = this.userDao.findByUsernameAndZone(irodsUser.getName(), irodsUser.getZone());

				if (user == null) {
					user = new DataGridUser();
					user.setUsername(irodsUser.getName());
					user.setZone(irodsUser.getZone());
					user.setDataGridId(Long.parseLong(irodsUser.getId()));
					user.setEnabled(true);
					if (irodsUser.getUserType().equals(UserTypeEnum.RODS_ADMIN)) {
						logger.debug("setting user type admin:{}", irodsUser.getUserType());
						user.setUserType(UserTypeEnum.RODS_ADMIN.getTextValue());
					} else if (irodsUser.getUserType().equals(UserTypeEnum.GROUP_ADMIN)) {
						logger.debug("setting user type groupadmin:{}", irodsUser.getUserType());
						user.setUserType(UserTypeEnum.GROUP_ADMIN.getTextValue());
					} else {
						logger.debug("setting user type rodsuser:{}", irodsUser.getUserType());
						user.setUserType(UserTypeEnum.RODS_USER.getTextValue());
					}
				} else {
					// check for an update of user type

					if (user.getUserType() != irodsUser.getUserType().getTextValue()) {
						logger.info("updating user type based on iRODS current value");
						user.setUserType(irodsUser.getUserType().getTextValue());
					}
				}

				this.user = user;
			}
		}

		return authResponse;
	}

	/**
	 * @return the irodsHost
	 */
	public String getIrodsHost() {
		return this.irodsHost;
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
		return this.irodsPort;
	}

	/**
	 * @param irodsPort the irodsPort to set
	 */
	public void setIrodsPort(String irodsPort) {
		this.irodsPort = irodsPort;
	}

	/**
	 * @return the irodsZoneName
	 */
	public String getIrodsZoneName() {
		return this.irodsZoneName;
	}

	/**
	 * @param irodsZoneName the irodsZoneName to set
	 */
	public void setIrodsZoneName(String irodsZoneName) {
		this.irodsZoneName = irodsZoneName;
	}

	/**
	 * Temporary implementation of the GrantedAuthority interface for Admin
	 * authentication
	 */
	private class IRODSAdminGrantedAuthority implements GrantedAuthority {

		private static final long serialVersionUID = 357603546013216540L;

		@Override
		public String getAuthority() {
			return "ROLE_ADMIN";
		}
	}

	/*
	 * Temporary implementation of the GrantedAuthority interface for GroupAdmin
	 * authentication
	 */
	private class IRODSGroupadminGrantedAuthority implements GrantedAuthority {

		private static final long serialVersionUID = 1L;

		@Override
		public String getAuthority() {
			return "ROLE_GROUPADMIN";
		}
	}

	/**
	 * Temporary implementation of the GrantedAuthority interface for User
	 * authentication
	 */
	private class IRODSUserGrantedAuthority implements GrantedAuthority {

		private static final long serialVersionUID = 357603546013216540L;

		@Override
		public String getAuthority() {
			return "ROLE_USER";
		}
	}

	public String getIrodsAuthScheme() {
		return irodsAuthScheme;
	}

	public void setIrodsAuthScheme(String irodsAuthScheme) {
		this.irodsAuthScheme = irodsAuthScheme;
	}
}
