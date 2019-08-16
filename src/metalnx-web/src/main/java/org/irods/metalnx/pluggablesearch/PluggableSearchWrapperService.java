/**
 * 
 */
package org.irods.metalnx.pluggablesearch;

import javax.annotation.PostConstruct;

import org.irods.jargon.extensions.searchplugin.SearchIndexInventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

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

	/**
	 * This is an application-level registry of registered search endpoints and
	 * schemas/attributes. These endpoint metadata are acquired by Metalnx polling
	 * configured endpoints.
	 * 
	 * Note the init method below will poll configured endponts to build this
	 * inventory
	 */
	private final SearchIndexInventory searchIndexInventory = new SearchIndexInventory();

	@Value("${pluggablesearch.enabled}")
	private boolean pluggableSearchEnabled = false;

	@Value("${pluggablesearch.endpointRegistryList}")
	private String pluggableSearchEndpoints = "";

	@Value("${pluggablesearch.info.timeout}")
	private int pluggableSearchInfoTimeout = 0;

	@Value("${pluggablesearch.search.timeout}")
	private int pluggableSearchSearchTimeout = 0;

	@Value("${pluggablesearch.search.endpointAccessSubject}")
	private String pluggableSearchEndpointAccessSubject = "";

	public ConfigService getConfigService() {
		return configService;
	}

	@PostConstruct
	public void init() {
		log.info("init()");

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

}
