package org.irods.metalnx.pluggableexport;

import javax.annotation.PostConstruct;

import org.irods.jargon.extensions.exportplugin.ExportIndexInventory;
import org.irods.jargon.extensions.exportplugin.ExportPluginDiscoveryService;
import org.irods.jargon.extensions.exportplugin.ExportPluginRegistrationConfig;
import org.irods.jargon.extensions.exportplugin.exception.ExportPluginUnavailableException;
import org.irods.metalnx.jwt.JwtManagementWrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.services.interfaces.ConfigService;

/*
 * pick up configuration properties for export plugin 
 * initialize the pluggable features of Metalnx
 */

@Component
@Scope(value = WebApplicationContext.SCOPE_APPLICATION)
public class PluggableExportWrapperService {

	private static final Logger log = LoggerFactory.getLogger(PluggableExportWrapperService.class);

	@Autowired
	private ConfigService configService;

	@Autowired
	private JwtManagementWrapperService jwtManagementWrapperService;
	/**
	 * This is an application-level registry of registered search endpoints and
	 * schemas/attributes. These endpoint metadata are acquired by Metalnx polling
	 * configured endpoints.
	 * 
	 * Note the init method below will poll configured endponts to build this
	 * inventory
	 */
	private final ExportIndexInventory exportIndexInventory = new ExportIndexInventory();
	
	/**
	 * Wrapped {@link ExportPluginDiscoveryService} is wired up in this component
	 * with configuration and JWT processing
	 */
	private ExportPluginDiscoveryService exportPluginDiscoveryService;

	@Value("${pluggableshoppingcart.enabled}")
	private boolean pluggableExportEnabled = false;

	@Value("${pluggableexport.endpointRegistryList}")
	private String pluggableExportEndpoints = "";

	// FIXME: merge following properties for all the plugins?
	@Value("${pluggablesearch.info.timeout}")
	private int pluggableExportInfoTimeout = 0;

	@Value("${pluggablesearch.search.timeout}")
	private int pluggableExportExportTimeout = 0;

	@Value("${pluggablesearch.endpointAccessSubject}")
	private String pluggableExportEndpointAccessSubject = "";

	public ConfigService getConfigService() {
		return configService;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public boolean isPluggableExportEnabled() {
		return pluggableExportEnabled;
	}

	public void setPluggableExportEnabled(boolean pluggableExportEnabled) {
		this.pluggableExportEnabled = pluggableExportEnabled;
	}

	public String getPluggableExportEndpoints() {
		return pluggableExportEndpoints;
	}

	public void setPluggableExportEndpoints(String pluggableExportEndpoints) {
		this.pluggableExportEndpoints = pluggableExportEndpoints;
	}

	public int getPluggableExportInfoTimeout() {
		return pluggableExportInfoTimeout;
	}

	public void setPluggableExportInfoTimeout(int pluggableExportInfoTimeout) {
		this.pluggableExportInfoTimeout = pluggableExportInfoTimeout;
	}

	public int getPluggableExportExportTimeout() {
		return pluggableExportExportTimeout;
	}

	public void setPluggableExportExportTimeout(int pluggableExportExportTimeout) {
		this.pluggableExportExportTimeout = pluggableExportExportTimeout;
	}

	public String getPluggableExportEndpointAccessSubject() {
		return pluggableExportEndpointAccessSubject;
	}

	public void setPluggableExportEndpointAccessSubject(String pluggableExportEndpointAccessSubject) {
		this.pluggableExportEndpointAccessSubject = pluggableExportEndpointAccessSubject;
	}

	public ExportIndexInventory getExportIndexInventory() {
		return exportIndexInventory;
	}

	@PostConstruct
	public void init() {
		log.info("init()");

		if (!pluggableExportEnabled) {
			log.warn("Export service is not enabled, will bypass init.");
			return;
		}

		ExportPluginRegistrationConfig config = new ExportPluginRegistrationConfig();
		config.setEndpointAccessSubject(pluggableExportEndpointAccessSubject);
		config.setEndpointAccessTimeout(getPluggableExportInfoTimeout());
		config.setEndpointRegistryList(
				ExportPluginRegistrationConfig.convertEndpointListToArray(pluggableExportEndpoints));
		config.setEndpointExportAccessTimeout(pluggableExportExportTimeout);
		config.setJwtAlgo(configService.getJwtAlgo());
		config.setJwtIssuer(config.getJwtIssuer());
		config.setJwtSecret(config.getJwtIssuer());

		this.exportPluginDiscoveryService = new ExportPluginDiscoveryService(config,
				this.jwtManagementWrapperService.getJwtIssueService());
		try {
			exportPluginDiscoveryService.queryEndpoints(config.getEndpointRegistryList(), exportIndexInventory);
		} catch (ExportPluginUnavailableException e) {
			// log and continue, don't hold startup and treat as a soft failure
			log.warn("Export plugins not available:{}", config.getEndpointRegistryList(), e);
		}
		log.info("service init-ed");
		log.debug("registry:{}", this.getExportIndexInventory());
	}
	
}
