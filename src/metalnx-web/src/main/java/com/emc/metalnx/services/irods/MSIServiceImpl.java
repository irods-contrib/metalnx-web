/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.irods;

import java.util.ArrayList;
import java.util.List;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OperationNotSupportedByThisServerException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.core.domain.entity.DataGridMSIPkgInfo;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import com.emc.metalnx.core.domain.utils.DataGridCoreUtils;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.MSIService;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.RuleService;

@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class MSIServiceImpl implements MSIService {
	private static final Logger logger = LogManager.getLogger(MSIServiceImpl.class);

	@Autowired
	private RuleService ruleService;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private IRODSServices irodsServices;

	@Autowired
	private ConfigService configService;

	private List<DataGridServer> servers = new ArrayList<>();

	@Override
	public DataGridMSIPkgInfo getMSIPkgInfo() throws DataGridException {
		return new DataGridMSIPkgInfo(getMSIInfoForAllServers(), configService.getMsiAPIVersionSupported());
	}

	@Override
	public DataGridServer getMSIsInstalled(String host) throws DataGridException {
		if (host == null || host.isEmpty())
			return null;
		return findServerByHostname(host);
	}

	@Override
	public List<DataGridServer> getMSIInfoForAllServers() throws DataGridException {
		servers = resourceService.getAllResourceServers(resourceService.findAll());
		for (DataGridServer server : servers)
			setMSIInfoForServer(server);
		return servers;
	}

	@Override
	public void setMSIInfoForServer(DataGridServer server) throws DataGridException {
		List<String> irodsMSIs = irodsServices.isAtLeastIrods420() ? configService.getIrods42MSIsExpected()
				: configService.getIrods41MSIsExpected();

		server.setMetalnxExpectedMSIs(configService.getMlxMSIsExpected());
		server.setIRodsExpectedMSIs(irodsMSIs);
		server.setOtherExpectedMSIs(configService.getOtherMSIsExpected());

		try {
			server.setMSIVersion(ruleService.execGetVersionRule(server.getHostname()));
		} catch (DataGridRuleException e) {
			logger.error("Failed to get MSI version for server: ", server.getHostname());
		}

		try {
			server.setMSIInstalledList(ruleService.execGetMSIsRule(server.getHostname()));
		} catch (DataGridRuleException e) {
			logger.error("Failed to get MSIs installed for server: ", server.getHostname());
			throw new DataGridException("general exception getting msis for server", e);
		} catch (OperationNotSupportedByThisServerException e) {
			logger.warn("msi information not supported by this server version, will ignore");
		} catch (JargonException e) {
			logger.error("general jargon exception getting msis for server", e);
			throw new DataGridException(e);
		}

	}

	@Override
	public boolean isMSIAPICompatibleInResc(String resource) throws DataGridException {
		if (servers == null || servers.isEmpty())
			getMSIInfoForAllServers();

		DataGridServer server = null;

		for (DataGridServer s : servers) {
			for (DataGridResource dgResc : s.getResources()) {
				if (resource.equals(dgResc.getName())) {
					server = s;
					break;
				}
			}

			if (server != null)
				break;
		}

		String apiVersionSupported = DataGridCoreUtils.getAPIVersion(configService.getMsiAPIVersionSupported());
		String apiVersionInstalled = server != null ? DataGridCoreUtils.getAPIVersion(server.getMSIVersion()) : "";
		return apiVersionSupported.equalsIgnoreCase(apiVersionInstalled);
	}

	/**
	 * Looks for a server instance based on its hostname
	 * 
	 * @param host
	 *            server's hostname
	 * @return server instance
	 */
	private DataGridServer findServerByHostname(String host) throws DataGridException {
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
