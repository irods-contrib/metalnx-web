package org.irods.metalnx.jwt;

import javax.annotation.PostConstruct;

import org.irods.jargon.irodsext.jwt.AbstractJwtIssueService;
import org.irods.jargon.irodsext.jwt.JwtIssueServiceImpl;
import org.irods.jargon.irodsext.jwt.JwtServiceConfig;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.emc.metalnx.services.interfaces.ConfigService;

/**
 * @author Mike Conway - NIEHS
 *
 */
@Component
//@Scope(value = WebApplicationContext.SCOPE_APPLICATION)
public class JwtManagementWrapperService {

	private static final Logger log = LogManager.getLogger(JwtManagementWrapperService.class);
	/**
	 * {@link JwtIssueService} implementation that will be initialized based on
	 * provided configuration in this service wrapper
	 */
	private AbstractJwtIssueService jwtIssueService;

	@Autowired(required = true)
	private ConfigService configService;

	public JwtManagementWrapperService() {

	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public ConfigService getConfigService() {
		return configService;
	}

	@PostConstruct
	public void init() {
		log.info("init()");
		JwtServiceConfig jwtServiceConfig = new JwtServiceConfig();
		jwtServiceConfig.setAlgo(configService.getJwtAlgo());
		jwtServiceConfig.setIssuer(configService.getJwtIssuer());
		jwtServiceConfig.setSecret(configService.getJwtSecret());
		this.jwtIssueService = new JwtIssueServiceImpl(jwtServiceConfig);
		log.info("jwt service initialized");
	}

	/**
	 * Given a user account (subject), return a token that may be used to access
	 * back-end microservices
	 * 
	 * @param subject {@code String} with the user subject for the JWT
	 * @return {@code String} with an encoded JWT token for use in an http header
	 *         Bearer auth token
	 */
	public String encodeJwtForUser(final String subject) {
		log.info("encoding jwt for user:{}", subject);
		return jwtIssueService.issueJwtToken(subject);
	}

	public AbstractJwtIssueService getJwtIssueService() {
		return jwtIssueService;
	}

}
