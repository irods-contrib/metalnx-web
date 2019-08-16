/**
 * 
 */
package org.irods.metalnx.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.emc.metalnx.services.configuration.ConfigServiceImpl;
import com.emc.metalnx.services.interfaces.ConfigService;

/**
 * Spring wiring test configuration for JWT tests
 * 
 * @author Mike Conway - NIEHS
 *
 */
@Configuration
@PropertySource("classpath:test.metalnx.properties")
public class JwtTestConfig {
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

}
