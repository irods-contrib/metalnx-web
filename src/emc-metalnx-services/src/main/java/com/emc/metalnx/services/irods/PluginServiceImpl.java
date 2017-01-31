package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.entity.DataGridMSIByServer;
import com.emc.metalnx.core.domain.entity.DataGridMSIPkgInfo;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import com.emc.metalnx.core.domain.utils.DataGridCoreUtils;
import com.emc.metalnx.services.interfaces.PluginService;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.RuleService;
import com.emc.metalnx.services.interfaces.ServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class PluginServiceImpl implements PluginService {
    private static final Logger logger = LoggerFactory.getLogger(PluginServiceImpl.class);
    private static final int EXPIRATION_CACHE_TIME = 5 * 1000;

    @Autowired
    RuleService ruleService;

    @Autowired
    ServerService serverService;

    @Autowired
    ResourceService resourceService;

    @Value("${msi.api.version}")
    private String msiAPIVersionSupported;

    @Value("#{'${msi.metalnx.list}'.split(',')}")
    private Set<String> msiMetalnxList;

    private Map<String, String> msiVersionCache = new HashMap<>();
    private List<DataGridServer> servers = new ArrayList<>();
    private Long serversCacheTime = System.currentTimeMillis();

    @PostConstruct
    public void init() throws DataGridConnectionRefusedException {
        refreshServersCache();
    }

    @Override
    public DataGridMSIPkgInfo getMSIPkgInfo() throws DataGridConnectionRefusedException {
        return new DataGridMSIPkgInfo(getMSIInfoForAllServers(), msiAPIVersionSupported);
    }

    @Override
    public DataGridMSIByServer getMSIsInstalled(String host) throws DataGridConnectionRefusedException {
        List<String> msis = new ArrayList<>();
        DataGridMSIByServer msisByServer = new DataGridMSIByServer(host, msiMetalnxList);

        if(host != null && !host.isEmpty()) {
            if (System.currentTimeMillis() > serversCacheTime || servers.isEmpty()) getMSIInfoForAllServers();

            for (DataGridServer server : servers) {
                if (host.equals(server.getHostname())) {
                    msis = server.getMSIInstalledList();
                    break;
                }
            }
        }

        if(!msis.isEmpty()) {
            // classifying MSIs by their type
            for(String msi: msis) {
                if(msiMetalnxList.contains(msi)) msisByServer.addToMsiMetalnx(msi);
                else msisByServer.addToMsiIRODS(msi);
            }
        }

        return msisByServer;
    }

    @Override
    public List<DataGridServer> getMSIInfoForAllServers() throws DataGridConnectionRefusedException {
        if(System.currentTimeMillis() > serversCacheTime || servers.isEmpty()) refreshServersCache();

        for (DataGridServer server: servers) setMSIInfoForServer(server);
        return servers;
    }

    @Override
    public void setMSIInfoForServer(DataGridServer server) throws DataGridConnectionRefusedException {
        long currTime = System.currentTimeMillis();

        // cache is still up-to-date
        if(msiVersionCache.containsKey(server.getHostname()) && currTime < serversCacheTime) {
            logger.info("Retrieving MSI info from cache.");
            server.setMSIVersion(msiVersionCache.get(server.getHostname()));
            return;
        }

        logger.info("Refreshing MSI version cache.");

        String version;

        try {

            version = ruleService.execGetVersionRule(server.getHostname());
            server.setMSIVersion(version);

            // adding info to cache
            msiVersionCache.put(server.getHostname(), version);
            serversCacheTime = System.currentTimeMillis() + EXPIRATION_CACHE_TIME;
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

    private void refreshServersCache() throws DataGridConnectionRefusedException {
        logger.info("Cache of servers is out-of-date. Refreshing it.");
        servers = resourceService.getAllResourceServers(resourceService.findAll());
        serversCacheTime = System.currentTimeMillis() + EXPIRATION_CACHE_TIME;
    }
}
