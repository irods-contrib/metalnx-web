package org.irods.metalnx.pluggablesearch;

import org.irods.metalnx.jwt.JwtManagementWrapperService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.emc.metalnx.services.configuration.ConfigServiceImpl;
import com.emc.metalnx.services.interfaces.ConfigService;

/***
 * Spring wiring test configuration for pluggable search tests**
 * 
 * @author Mike Conway - NIEHS
 *
 */
@Configuration
@PropertySource("classpath:test.metalnx.properties")
public class PluggableSearchTestConfig {

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

	@Bean
	public JwtManagementWrapperService jwtManagmementWrapperService(ConfigService configService) {
		JwtManagementWrapperService jwtManagementWrapperService = new JwtManagementWrapperService();
		jwtManagementWrapperService.setConfigService(configService);
		jwtManagementWrapperService.init();
		return jwtManagementWrapperService;
	}

	@Bean
	public ConfigService configService() {
		return new ConfigServiceImpl();
	}

	@Bean
	public PluggableSearchWrapperService pluggableSearchWrapperService(ConfigService configService,
			JwtManagementWrapperService jwtManagementWrapperService) {
		PluggableSearchWrapperService pluggableSearchWrapperService = new PluggableSearchWrapperService();
		pluggableSearchWrapperService.setConfigService(configService);
		pluggableSearchWrapperService.setPluggableSearchEnabled(pluggableSearchEnabled);
		pluggableSearchWrapperService.setPluggableSearchEndpointAccessSubject(pluggableSearchEndpointAccessSubject);
		pluggableSearchWrapperService.setPluggableSearchEndpoints(pluggableSearchEndpoints);
		pluggableSearchWrapperService.setPluggableSearchInfoTimeout(pluggableSearchInfoTimeout);
		pluggableSearchWrapperService.setPluggableSearchSearchTimeout(pluggableSearchSearchTimeout);
		pluggableSearchWrapperService.setJwtManagementWrapperService(jwtManagementWrapperService);
		pluggableSearchWrapperService.init();
		return pluggableSearchWrapperService;
	}

}
