/**
 * 
 */
package org.irods.metalnx.pluggablesearch;

import javax.annotation.PostConstruct;

import org.irods.jargon.extensions.searchplugin.SearchIndexInventory;
import org.irods.jargon.extensions.searchplugin.SearchPluginDiscoveryService;
import org.irods.jargon.extensions.searchplugin.SearchPluginRegistrationConfig;
import org.irods.jargon.extensions.searchplugin.exception.SearchPluginUnavailableException;
import org.irods.jargon.extensions.searchplugin.model.SearchAttributes;
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

/**
 * Pick up configuration properties for pluggable search and properly initialize
 * the pluggable search features of Metalnx
 * 
 * @author Mike Conway - NIEHS
 *
 */
@Component
@Scope(value = WebApplicationContext.SCOPE_APPLICATION)
public class PluggableSearchWrapperService {

	private static final Logger log = LoggerFactory.getLogger(PluggableSearchWrapperService.class);

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
	private final SearchIndexInventory searchIndexInventory = new SearchIndexInventory();

	/**
	 * Wrapped {@link SearchPluginDiscoveryService} is wired up in this component
	 * with configuration and JWT processing
	 */
	private SearchPluginDiscoveryService searchPluginDiscoveryService;

	@Value("${pluggablesearch.enabled}")
	private boolean pluggableSearchEnabled = false;

	@Value("${pluggablesearch.endpointRegistryList}")
	private String pluggableSearchEndpoints = "";

	@Value("${pluggablesearch.info.timeout}")
	private int pluggableSearchInfoTimeout = 0;

	@Value("${pluggablesearch.search.timeout}")
	private int pluggableSearchSearchTimeout = 0;

	@Value("${pluggablesearch.endpointAccessSubject}")
	private String pluggableSearchEndpointAccessSubject = "";

	public ConfigService getConfigService() {
		return configService;
	}

	@PostConstruct
	public void init() {
		log.info("init()");

		if (!pluggableSearchEnabled) {
			log.warn("search service not enabled, will bypass init. This is normal when search is not configured");
			return;
		}

		SearchPluginRegistrationConfig config = new SearchPluginRegistrationConfig();
		config.setEndpointAccessSubject(pluggableSearchEndpointAccessSubject);
		config.setEndpointAccessTimeout(getPluggableSearchInfoTimeout());
		config.setEndpointRegistryList(
				SearchPluginRegistrationConfig.convertEndpointListToArray(pluggableSearchEndpoints));
		config.setEndpointSearchAccessTimeout(pluggableSearchSearchTimeout);
		config.setJwtAlgo(configService.getJwtAlgo());
		config.setJwtIssuer(configService.getJwtIssuer());
		config.setJwtSecret(configService.getJwtSecret());
		this.searchPluginDiscoveryService = new SearchPluginDiscoveryService(config,
				this.jwtManagementWrapperService.getJwtIssueService());
		try {
			searchPluginDiscoveryService.queryEndpoints(config.getEndpointRegistryList(), searchIndexInventory);
		} catch (SearchPluginUnavailableException e) {
			// log and continue, don't hold startup and treat as a soft failure
			log.warn("search plugins not available:{}", config.getEndpointRegistryList(), e);
		}
		log.info("service init-ed");
		log.debug("registry:{}", this.getSearchIndexInventory());

	}

	/**
	 * Get the available search attributes for a given schema (this will cache
	 * schema attribs as they are requested)
	 * 
	 * @param endpointUrl {@code String} with the url for the endpoint
	 * @param schemaId    {@code String} with the schema
	 * @return {@link SearchAttributes} that list the attributes for the given
	 *         schema
	 * @throws DataGridException {@link DataGridException} for general errors
	 */
	public SearchAttributes listAttributes(final String endpointUrl, final String schemaId) throws DataGridException {
		log.info("listAttributes()");

		if (endpointUrl == null || endpointUrl.isEmpty()) {
			throw new IllegalArgumentException("null or empty endpointUrl");
		}

		if (schemaId == null || schemaId.isEmpty()) {
			throw new IllegalArgumentException("null or empty schemaId");
		}

		log.info("endpointUrl:{}", endpointUrl);
		log.info("schemaId:{}", schemaId);

		try {
			SearchAttributes searchAttributes = searchPluginDiscoveryService.queryAttributes(endpointUrl, schemaId,
					searchIndexInventory);
			return searchAttributes;
		} catch (SearchPluginUnavailableException e) {
			log.error("error searching for attributes for schema:{}", schemaId);
			throw new DataGridException("Unable to find attributes for the given search schema");
		}

	}

	/**
	 * Wrapper calls a simple text search
	 * 
	 * @param endpointUrl {@code String} with the endpointUrl
	 * @param schemaId    {@code String} with the search schema id
	 * @param queryText   {@code String} with the query text
	 * @param offset      {@code int} with the offset, if supported, otherwise 0
	 * @param length      {@code int} with the result length, if supported,
	 *                    otherwise 0
	 * @param principal   {@code String} with the identity of the individual doing
	 *                    the search
	 * @return {@code String} with the result json as a string
	 * @throws DataGridException {@link DataGridException}
	 */
	public String simpleTextSearch(final String endpointUrl, final String schemaId, final String queryText,
			final int offset, final int length, final String principal) throws DataGridException {

		log.info("simpleTextSearch()");

		if (endpointUrl == null || endpointUrl.isEmpty()) {
			throw new IllegalArgumentException("null or empty endpointUrl");
		}

		if (schemaId == null || schemaId.isEmpty()) {
			throw new IllegalArgumentException("null or empty schemaId");
		}

		if (queryText == null || queryText.isEmpty()) {
			throw new IllegalArgumentException("null or empty queryText");
		}

		if (principal == null || principal.isEmpty()) {
			throw new IllegalArgumentException("null or empty principal");
		}

		log.info("endpointUrl:{}", endpointUrl);
		log.info("schemaId:{}", schemaId);
		log.info("queryText:{}", queryText);
		log.info("length:{}", length);
		log.info("offset:{}", offset);
		log.info("principal:{}", principal);

		try {
			log.info("doing search...");
			String jsonResultString = searchPluginDiscoveryService.textSearch(queryText, endpointUrl, schemaId, offset,
					length, principal);
			log.debug("searchResult:{}", jsonResultString);
			return jsonResultString;
		} catch (SearchPluginUnavailableException e) {
			log.error("error querying schema:{}", schemaId);
			throw new DataGridException("Unable to query with given search schema");
		}

	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public boolean isPluggableSearchEnabled() {
		return pluggableSearchEnabled;
	}

	public void setPluggableSearchEnabled(boolean pluggableSearchEnabled) {
		this.pluggableSearchEnabled = pluggableSearchEnabled;
	}

	public String getPluggableSearchEndpoints() {
		return pluggableSearchEndpoints;
	}

	public void setPluggableSearchEndpoints(String pluggableSearchEndpoints) {
		this.pluggableSearchEndpoints = pluggableSearchEndpoints;
	}

	public int getPluggableSearchInfoTimeout() {
		return pluggableSearchInfoTimeout;
	}

	public void setPluggableSearchInfoTimeout(int pluggableSearchInfoTimeout) {
		this.pluggableSearchInfoTimeout = pluggableSearchInfoTimeout;
	}

	public int getPluggableSearchSearchTimeout() {
		return pluggableSearchSearchTimeout;
	}

	public void setPluggableSearchSearchTimeout(int pluggableSearchSearchTimeout) {
		this.pluggableSearchSearchTimeout = pluggableSearchSearchTimeout;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PluggableSearchWrapperService [configService=").append(configService)
				.append(", pluggableSearchEnabled=").append(pluggableSearchEnabled)
				.append(", pluggableSearchEndpoints=").append(pluggableSearchEndpoints)
				.append(", pluggableSearchInfoTimeout=").append(pluggableSearchInfoTimeout)
				.append(", pluggableSearchSearchTimeout=").append(pluggableSearchSearchTimeout)
				.append(", pluggableSearchEndpointAccessSubject=").append(pluggableSearchEndpointAccessSubject)
				.append("]");
		return builder.toString();
	}

	public String getPluggableSearchEndpointAccessSubject() {
		return pluggableSearchEndpointAccessSubject;
	}

	public void setPluggableSearchEndpointAccessSubject(String pluggableSearchEndpointAccessSubject) {
		this.pluggableSearchEndpointAccessSubject = pluggableSearchEndpointAccessSubject;
	}

	public SearchIndexInventory getSearchIndexInventory() {
		return searchIndexInventory;
	}

	public JwtManagementWrapperService getJwtManagementWrapperService() {
		return jwtManagementWrapperService;
	}

	public SearchPluginDiscoveryService getSearchPluginDiscoveryService() {
		return searchPluginDiscoveryService;
	}

	public void setJwtManagementWrapperService(JwtManagementWrapperService jwtManagementWrapperService) {
		this.jwtManagementWrapperService = jwtManagementWrapperService;
	}

}
