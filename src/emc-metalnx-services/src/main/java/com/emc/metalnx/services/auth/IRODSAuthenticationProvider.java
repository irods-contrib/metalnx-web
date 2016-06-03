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

import java.io.Serializable;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;

import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.services.exceptions.DataGridAuthenticationException;
import com.emc.metalnx.services.interfaces.AuthenticationProviderService;
import org.springframework.util.StringUtils;

public class IRODSAuthenticationProvider implements AuthenticationProviderService, Serializable {

    @Autowired
    UserDao userDao;

    @Autowired
    IRODSAccessObjectFactory irodsAccessObjectFactory;

    private String irodsHost;
    private String irodsPort;
    private String irodsZoneName;

    @Value("${irods.auth.scheme}")
    private String irodsAuthScheme;

    // Instance variables to be set to UserTokenDetails instance.
    private IRODSAccount irodsAccount;

    private DataGridUser user;

    private static final Logger logger = LoggerFactory.getLogger(IRODSAuthenticationProvider.class);

    private static final long serialVersionUID = -4984545776727334580L;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        AuthResponse authResponse = null;
        UsernamePasswordAuthenticationToken authObject = null;

        logger.debug("Setting username {} and password {}.", username, password);

        try {
            authResponse = this.authenticateAgainstIRODS(username, password);

            // Settings iRODS account
            this.irodsAccount = authResponse.getAuthenticatedIRODSAccount();

            // Retrieving logging user
            User irodsUser = new User();
            GrantedAuthority grantedAuth = null;

            try {
                irodsUser = this.irodsAccessObjectFactory.getUserAO(this.irodsAccount).findByName(username);
            } catch (JargonException e) {
                logger.error("Could not find user: " + e.getMessage());
            }

            if (irodsUser.getUserType().equals(UserTypeEnum.RODS_ADMIN)) {
                grantedAuth = new IRODSAdminGrantedAuthority();
            } else {
                grantedAuth = new IRODSUserGrantedAuthority();
            }

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
        } catch (Exception e) {
            logger.error("Could not authenticate user: ", username);

            if (e.getCause() instanceof ConnectException) {
                throw new DataGridAuthenticationException(e.getMessage());
            }
        }

        return authObject;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

    private AuthResponse authenticateAgainstIRODS(String username, String password) throws Exception {

        AuthResponse authResponse = null;

        // Getting iRODS protocol set
        logger.debug("Creating IRODSAccount object.");
        this.irodsAccount = IRODSAccount.instance(this.irodsHost, Integer.parseInt(this.irodsPort), username, password,
                "", this.irodsZoneName, "demoResc");
        this.irodsAccount.setAuthenticationScheme(AuthScheme.findTypeByString(this.irodsAuthScheme));
        logger.debug("Done.");

        logger.debug(
                "Authenticating IRODSAccount:\n\tusername: {}\n\tpassword: ***********\n\tirodsHost: {}\n\tirodsZone: {}",
                username, this.irodsHost, this.irodsZoneName);
        authResponse = this.irodsAccessObjectFactory.authenticateIRODSAccount(this.irodsAccount);
        logger.debug("Done.");

        if (authResponse.isSuccessful()) {

            if (StringUtils.isEmpty(authResponse.getAuthMessage())) {
                logger.debug("AuthMessage: {}", authResponse.getAuthMessage());
            }

            // Settings iRODS account
            this.irodsAccount = authResponse.getAuthenticatingIRODSAccount();

            // Retrieving logging user
            UserAO userAO = this.irodsAccessObjectFactory.getUserAO(this.irodsAccount);
            User irodsUser = userAO.findByName(username);

            // If the user is found and has administrator permissions
            if (irodsUser.getUserType().equals(UserTypeEnum.RODS_ADMIN)
                    || irodsUser.getUserType().equals(UserTypeEnum.RODS_USER)) {

                // If the user is not yet persisted in our database
                DataGridUser user = this.userDao.findByUsernameAndZone(irodsUser.getName(), irodsUser.getZone());

                if (user == null) {
                    user = new DataGridUser();
                    user.setUsername(irodsUser.getName());
                    user.setAdditionalInfo(irodsUser.getZone());
                    user.setDataGridId(Long.parseLong(irodsUser.getId()));
                    user.setEnabled(true);
                    user.setFirstName("");
                    user.setLastName("");
                    if (irodsUser.getUserType().equals(UserTypeEnum.RODS_ADMIN)) {
                        user.setUserType(UserTypeEnum.RODS_ADMIN.getTextValue());
                    } else {
                        user.setUserType(UserTypeEnum.RODS_USER.getTextValue());
                    }
                    this.userDao.save(user);
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
     * @param irodsHost
     *            the irodsHost to set
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
     * @param irodsPort
     *            the irodsPort to set
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
     * @param irodsZoneName
     *            the irodsZoneName to set
     */
    public void setIrodsZoneName(String irodsZoneName) {
        this.irodsZoneName = irodsZoneName;
    }

    /**
     * Temporary implementation of the GrantedAuthority interface for Admin authentication
     */
    private class IRODSAdminGrantedAuthority implements GrantedAuthority {

        private static final long serialVersionUID = 357603546013216540L;

        @Override
        public String getAuthority() {
            return "ROLE_ADMIN";
        }
    };

    /**
     * Temporary implementation of the GrantedAuthority interface for User authentication
     */
    private class IRODSUserGrantedAuthority implements GrantedAuthority {

        private static final long serialVersionUID = 357603546013216540L;

        @Override
        public String getAuthority() {
            return "ROLE_USER";
        }
    };
}
