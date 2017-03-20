/*
 * Copyright (c) 2015-2017, Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.entity.DataGridMSIPkgInfo;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import com.emc.metalnx.core.domain.utils.DataGridCoreUtils;
import com.emc.metalnx.services.interfaces.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class MSIServiceImpl implements MSIService {
    private static final Logger logger = LoggerFactory.getLogger(MSIServiceImpl.class);

    @Autowired
    private RuleService ruleService;

    @Autowired
    private ServerService serverService;

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private IRODSServices irodsServices;

    @Value("${msi.api.version}")
    private String msiAPIVersionSupported;

    @Value("#{'${msi.metalnx.list}'.split(',')}")
    private List<String> mlxMSIsExpected;

    @Value("#{'${msi.irods.list}'.split(',')}")
    private List<String> irods41MSIsExpected;

    @Value("#{'${msi.irods.42.list}'.split(',')}")
    private List<String> irods42MSIsExpected;

    @Value("#{'${msi.other.list}'.split(',')}")
    private List<String> otherMSIsExpected;

    private List<DataGridServer> servers = new ArrayList<>();

    @Override
    public DataGridMSIPkgInfo getMSIPkgInfo() throws DataGridConnectionRefusedException {
        return new DataGridMSIPkgInfo(getMSIInfoForAllServers(), msiAPIVersionSupported);
    }

    @Override
    public DataGridServer getMSIsInstalled(String host) throws DataGridConnectionRefusedException {
        if(host == null || host.isEmpty()) return null;
        return findServerByHostname(host);
    }

    @Override
    public List<DataGridServer> getMSIInfoForAllServers() throws DataGridConnectionRefusedException {
        servers = resourceService.getAllResourceServers(resourceService.findAll());
        for (DataGridServer server: servers) setMSIInfoForServer(server);
        return servers;
    }

    @Override
    public void setMSIInfoForServer(DataGridServer server) throws DataGridConnectionRefusedException {
        List<String> irodsMSIs = irodsServices.isAtLeastIrods420() ? irods42MSIsExpected : irods41MSIsExpected;

        server.setMetalnxExpectedMSIs(mlxMSIsExpected);
        server.setIRodsExpectedMSIs(irodsMSIs);
        server.setOtherExpectedMSIs(otherMSIsExpected);

        try {
            server.setMSIVersion(ruleService.execGetVersionRule(server.getHostname()));
        } catch (DataGridRuleException e) {
            logger.error("Failed to get MSI version for server: ", server.getHostname());
        }

        try {
            server.setMSIInstalledList(ruleService.execGetMSIsRule(server.getHostname()));
        } catch (DataGridRuleException e) {
            logger.error("Failed to get MSIs installed for server: ", server.getHostname());
        }

    }

    @Override
    public boolean isMSIAPICompatibleInResc(String resource) throws DataGridConnectionRefusedException {
        if(servers == null || servers.isEmpty()) getMSIInfoForAllServers();

        DataGridServer server = null;

        for(DataGridServer s: servers) {
            for(DataGridResource dgResc: s.getResources()) {
                if(resource.equals(dgResc.getName())) {
                    server = s;
                    break;
                }
            }

            if(server != null) break;
        }

        String apiVersionSupported = DataGridCoreUtils.getAPIVersion(msiAPIVersionSupported);
        String apiVersionInstalled = server != null ? DataGridCoreUtils.getAPIVersion(server.getMSIVersion()) : "";
        return apiVersionSupported.equalsIgnoreCase(apiVersionInstalled);
    }

    /**
     * Looks for a server instance based on its hostname
     * @param host server's hostname
     * @return server instance
     */
    private DataGridServer findServerByHostname(String host) throws DataGridConnectionRefusedException {
        getMSIInfoForAllServers(); // update list of servers

        DataGridServer server = null;

        for (DataGridServer s : servers) {
            if (host.equals(s.getHostname())) {
                server = s;
                break;
            }
        }

        return server;
    }
}
