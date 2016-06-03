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

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;

public class IRODSLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler implements LogoutSuccessHandler {

    @Autowired
    IRODSAccessObjectFactory irodsAccessObjectFactory;

    private static final Logger logger = LoggerFactory.getLogger(IRODSLogoutSuccessHandler.class);

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {

        logger.info("Logging out...");

        try {
            IRODSAccount irodsAccount = ((UserTokenDetails) authentication.getDetails()).getIrodsAccount();
            String username = irodsAccount.getUserName();

            logger.debug("Closing session and eating all exceptions");
            irodsAccessObjectFactory.closeSessionAndEatExceptions(irodsAccount);
            irodsAccessObjectFactory.closeSessionAndEatExceptions();

            logger.debug("Removing current session temporary directory for file upload");
            try {
                File tmpSessionDir = new File(username);
                if (tmpSessionDir.exists()) {
                    FileUtils.forceDelete(tmpSessionDir);
                }
            }
            catch (Exception e) {
                logger.error("User {} temporary directory for upload does not exist.", username);
            }

            response.sendRedirect("/emc-metalnx-web/login/");
            logger.info("User {} disconnected successfully", username);
        }
        catch (Exception e) {
            logger.info("User session is already expired. There is no need to clear session.");
        }

        super.onLogoutSuccess(request, response, authentication);
    }

}
