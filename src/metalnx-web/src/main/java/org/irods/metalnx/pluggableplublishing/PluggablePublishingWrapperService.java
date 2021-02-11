package org.irods.metalnx.pluggableplublishing;

import javax.annotation.PostConstruct;

import org.irods.jargon.extensions.publishingplugin.PublishingIndexInventory;
import org.irods.jargon.extensions.publishingplugin.PublishingPluginDiscoveryService;
import org.irods.jargon.extensions.publishingplugin.PublishingPluginRegistrationConfig;
import org.irods.jargon.extensions.publishingplugin.exception.PublishingPluginUnavailableException;
import org.irods.metalnx.jwt.JwtManagementWrapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.ConfigService;

/*
 * pick up configuration properties for publishing plugin 
 * initialize the pluggable features of Metalnx
 */

@Component
@Scope(value = WebApplicationContext.SCOPE_APPLICATION)
public class PluggablePublishingWrapperService {
	private static final Logger log = LoggerFactory.getLogger(PluggablePublishingWrapperService.class);

	@Autowired
	private ConfigService configService;

	@Autowired
	private JwtManagementWrapperService jwtManagementWrapperService;

	/**
	 * This is an application-level registry of registered publishing endpoints.
	 * These endpoint metadata are acquired by Metalnx polling configured endpoints.
	 * 
	 * Note the init method below will poll configured endpoints to build this
	 * inventory
	 */
	private final PublishingIndexInventory publishingIndexInventory = new PublishingIndexInventory();

	/**
	 * Wrapped {@link PublishingPluginDiscoveryService} is wired up in this
	 * component with configuration and JWT processing
	 */
	private PublishingPluginDiscoveryService publishingPluginDiscoveryService;

	@Value("${pluggableshoppingcart.enabled}")
	private boolean pluggablePublishingEnabled = false;

	// FIXME:
	@Value("${pluggablepublishing.endpointRegistryList}")
	private String pluggablePublishingEndpoints = "";

	@Value("${pluggablepublishing.info.timeout}")
	private int pluggablePublishingInfoTimeout = 0;

	@Value("${pluggablepublishing.publishing.timeout}")
	private int pluggablePublishingPublishingTimeout = 0;

	@Value("${pluggablesearch.endpointAccessSubject}")
	private String pluggablePublishingEndpointAccessSubject = "";

	public ConfigService getConfigService() {
		return configService;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public boolean isPluggablePublishingEnabled() {
		return pluggablePublishingEnabled;
	}

	public void setPluggablePublishingEnabled(boolean pluggablePublishingEnabled) {
		this.pluggablePublishingEnabled = pluggablePublishingEnabled;
	}

	public String getPluggablePublishingEndpoints() {
		return pluggablePublishingEndpoints;
	}

	public void setPluggablePublishingEndpoints(String pluggablePublishingEndpoints) {
		this.pluggablePublishingEndpoints = pluggablePublishingEndpoints;
	}

	public int getPluggablePublishingInfoTimeout() {
		return pluggablePublishingInfoTimeout;
	}

	public void setPluggablePublishingInfoTimeout(int pluggablePublishingInfoTimeout) {
		this.pluggablePublishingInfoTimeout = pluggablePublishingInfoTimeout;
	}

	public int getPluggablePublishingPublishingTimeout() {
		return pluggablePublishingPublishingTimeout;
	}

	public void setPluggablePublishingPublishingTimeout(int pluggablePublishingPublishingTimeout) {
		this.pluggablePublishingPublishingTimeout = pluggablePublishingPublishingTimeout;
	}

	public String getPluggablePublishingEndpointAccessSubject() {
		return pluggablePublishingEndpointAccessSubject;
	}

	public void setPluggablePublishingEndpointAccessSubject(String pluggablePublishingEndpointAccessSubject) {
		this.pluggablePublishingEndpointAccessSubject = pluggablePublishingEndpointAccessSubject;
	}

	public PublishingIndexInventory getPublishingIndexInventory() {
		return publishingIndexInventory;
	}

	@PostConstruct
	public void init() {
		log.info("init()");

		if (!pluggablePublishingEnabled) {
			log.warn("Publishing service is not enabled, will bypass init.");
			return;
		}

		PublishingPluginRegistrationConfig config = new PublishingPluginRegistrationConfig();
		config.setEndpointAccessSubject(pluggablePublishingEndpointAccessSubject);
		config.setEndpointAccessTimeout(getPluggablePublishingInfoTimeout());
		config.setEndpointRegistryList(
				PublishingPluginRegistrationConfig.convertEndpointListToArray(pluggablePublishingEndpoints));
		config.setEndpointPublishingAccessTimeout(pluggablePublishingPublishingTimeout);
		config.setJwtAlgo(configService.getJwtAlgo());
		config.setJwtIssuer(config.getJwtIssuer());
		config.setJwtSecret(config.getJwtIssuer());

		this.publishingPluginDiscoveryService = new PublishingPluginDiscoveryService(config,
				this.jwtManagementWrapperService.getJwtIssueService());

		try {
			publishingPluginDiscoveryService.queryEndpoints(config.getEndpointRegistryList(), publishingIndexInventory);
		} catch (PublishingPluginUnavailableException e) {
			// log and continue, don't hold startup and treat as a soft failure
			log.warn("Publishing plugins not available:{}", config.getEndpointRegistryList(), e);
		}
		log.info("service init-ed");
		log.debug("registry:{}", this.getPublishingIndexInventory());
	}

	/**
	 * Wrapper calls to download publish spreadsheet
	 * 
	 * @param endpointUrl {@code String} with the endpointUrl
	 * @param schemaId    {@code String} with the publish schema id
	 * @param principal   {@code String} with the identity of the individual doing
	 *                    the search
	 * @return {@code String} with the result json as a string
	 * @throws DataGridException {@link DataGridException}
	 */
	public String executePublish(final String endpointUrl, final String schemaId, final String principal,
			final String cartId) throws DataGridException {

		log.info("executePublish()");

		if (endpointUrl == null || endpointUrl.isEmpty()) {
			throw new IllegalArgumentException("null or empty endpointUrl");
		}

		if (schemaId == null || schemaId.isEmpty()) {
			throw new IllegalArgumentException("null or empty schemaId");
		}

		if (principal == null || principal.isEmpty()) {
			throw new IllegalArgumentException("null or empty principal");
		}

		try {
			log.info("initiate publish download");
			String jsonResultString = publishingPluginDiscoveryService.downloadpublishSpreadSheet(endpointUrl, schemaId,
					principal, cartId);
			return jsonResultString;
		} catch (PublishingPluginUnavailableException e) {
			// log and continue, don't hold startup and treat as a soft failure
			log.error("error querying schema: {}", schemaId);
			throw new DataGridException("Unable to publish with given schema");
		}
	}
}
