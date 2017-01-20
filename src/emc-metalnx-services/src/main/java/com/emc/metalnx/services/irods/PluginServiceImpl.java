package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.entity.DataGridMSIGridInfo;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import com.emc.metalnx.core.domain.utils.DataGridCoreUtils;
import com.emc.metalnx.services.interfaces.PluginsService;
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
public class PluginServiceImpl implements PluginsService {
    private static final Logger logger = LoggerFactory.getLogger(PluginServiceImpl.class);

    @Autowired
    RuleService ruleService;

    @Autowired
    ServerService serverService;

    @Autowired
    ResourceService resourceService;

    @Value("${msi.api.version}")
    private String msiAPIVersion;

    private Map<String, String> msiVersionCache;
    private Map<String, Long> msiVersionCacheTime;
    private List<DataGridServer> servers;

    @PostConstruct
    public void init() throws DataGridConnectionRefusedException {
        servers = new ArrayList<>();
        msiVersionCache = new HashMap<>();
        msiVersionCacheTime = new Hashtable<>();
        servers = resourceService.getAllResourceServers(resourceService.findAll());
    }

    @Override
    public DataGridMSIGridInfo getMSIGridInfo() throws DataGridConnectionRefusedException {
        return new DataGridMSIGridInfo(getMSIVersionForAllServers());
    }

    @Override
    public List<DataGridServer> getMSIVersionForAllServers() throws DataGridConnectionRefusedException {
        for (DataGridServer server: servers) setMSIVersionForServer(server);
        return servers;
    }

    @Override
    public void setMSIVersionForServer(DataGridServer server) throws DataGridConnectionRefusedException {
        long currTime = System.currentTimeMillis();

        if(msiVersionCache != null && msiVersionCache.containsKey(server) && currTime < msiVersionCacheTime.get(server)) {
            server.setMSIVersion(msiVersionCache.get(server.getHostname()));
            return;
        }

        String version = "";

        try {
            String destResc = server.getResources().get(0).getName();
            version = ruleService.execGetVersionRule(destResc);

            server.setIsMSIVersionCompatible(DataGridCoreUtils.getAPIVersion(version).equalsIgnoreCase(DataGridCoreUtils.getAPIVersion(msiAPIVersion)));
        } catch (DataGridRuleException e) {
            logger.error("Failed to get MSI version for server: ", server.getHostname());
            server.setIsMSIVersionCompatible(false);
        } finally {
            msiVersionCache.put(server.getHostname(), version);
            msiVersionCacheTime.put(server.getHostname(), System.currentTimeMillis() + 15 * 1000);

            server.setMSIVersion(version);
        }
    }

    @Override
    public boolean isMSIAPICompatibleInResc(String resource) {
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

        return server.isMSIVersionCompatible();
    }
}
