/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.irods.jargon.core.connection.AuthScheme;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.services.interfaces.ConfigService;

/**
 * Class that will load all all configurable parameters from *.properties files.
 */
@Service
@Transactional
//@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class ConfigServiceImpl implements ConfigService {

	public final static Logger logger = LogManager.getLogger(ConfigServiceImpl.class);

	@Value("${msi.api.version}")
	private String msiAPIVersionSupported;

	@Value("${msi.metalnx.list}")
	private String mlxMSIsExpected;

	@Value("${msi.irods.list}")
	private String irods41MSIsExpected;

	@Value("${msi.irods.42.list}")
	private String irods42MSIsExpected;

	@Value("${msi.other.list}")
	private String otherMSIsExpected;

	@Value("${irods.host}")
	private String irodsHost;

	@Value("${irods.port}")
	private String irodsPort;

	@Value("${irods.zoneName}")
	private String irodsZone;

	@Value("${irods.auth.scheme}")
	private String irodsAuthScheme;

	@Value("${populate.msi.enabled}")
	private boolean populateMsiEnabled;

	@Value("${metalnx.enable.tickets}")
	private boolean ticketsEnabled;

	@Value("${metalnx.enable.upload.rules}")
	private boolean uploadRulesEnabled;

	@Value("${metalnx.download.limit}")
	private long downloadLimit;

	@Value("${irods.auth.scheme}")
	private String defaultIrodsAuthScheme;

	@Value("${metalnx.enable.dashboard}")
	private boolean dashboardEnabled;

	@Value("${pluggablesearch.enabled}")
	private boolean pluggableSearchEnabled = false;

	@Value("${pluggableshoppingcart.enabled}")
	private boolean pluggableShoppingCartEnabled = false;
	
	@Value("${sidebar.show.public:false}")
	private boolean publicSidebarLinkEnabled;

	/**
	 * Indicates whether the old file props and AVU search is visible in the menu
	 * options, if AVU via elasticsearch is enabled you should probably turn this
	 * off
	 */
	@Value("${classicsearch.enabled}")
	private boolean classicSearchEnabled = true;

	/**
	 * Issuer (iss) in the jwt token for access to microservices
	 */
	@Value("${jwt.issuer}")
	private String jwtIssuer;

	/**
	 * Secret for jwt creation. Note that the underlying property should be treated
	 * as secret data with appropriate controls
	 */
	@Value("${jwt.secret}")
	private String jwtSecret;

	/**
	 * algo for computing JWTs
	 */
	@Value("${jwt.algo}")
	private String jwtAlgo;

	/**
	 * This is a string representation of AuthType mappings in the form
	 * iRODType:userFriendlyType| (bar delimited) This is parsed from the
	 * metalnx.properties and can be accessed as a parsed mapping via
	 * {@code ConfigService.listAuthTypeMappings()}
	 */
	@Value("${metalnx.authtype.mappings}")
	private String authtypeMappings;

	@Override
	public GlobalConfig getGlobalConfig() {
		logger.info("getGlobalConfig()");
		GlobalConfig globalConfig = new GlobalConfig();
		globalConfig.setTicketsEnabled(this.isTicketsEnabled());
		globalConfig.setUploadRulesEnabled(isUploadRulesEnabled());
		globalConfig.setDashboardEnabled(dashboardEnabled);
		globalConfig.setPluggableSearchEnabled(pluggableSearchEnabled);
		globalConfig.setPluggableShoppingCartEnabled(pluggableShoppingCartEnabled);
		globalConfig.setClassicSearchEnabled(classicSearchEnabled);
		globalConfig.setPublicSidebarLinkEnabled(publicSidebarLinkEnabled);
		logger.debug("globalConfig:{}", globalConfig);
		return globalConfig;
	}

	@Override
	public String getMsiAPIVersionSupported() {
		if (msiAPIVersionSupported == null)
			return "";
		return msiAPIVersionSupported;
	}

	@Override
	public List<String> getMlxMSIsExpected() {
		if (mlxMSIsExpected == null)
			return Collections.emptyList();
		return Arrays.asList(mlxMSIsExpected.split(","));
	}

	@Override
	public List<String> getIrods41MSIsExpected() {
		if (irods41MSIsExpected == null)
			return Collections.emptyList();
		return Arrays.asList(irods41MSIsExpected.split(","));
	}

	@Override
	public List<String> getIrods42MSIsExpected() {
		if (irods42MSIsExpected == null)
			return Collections.emptyList();
		return Arrays.asList(irods42MSIsExpected.split(","));
	}

	@Override
	public List<String> getOtherMSIsExpected() {
		if (otherMSIsExpected == null)
			return Collections.emptyList();
		return Arrays.asList(otherMSIsExpected.split(","));
	}

	@Override
	public String getIrodsHost() {
		return irodsHost;
	}

	@Override
	public String getIrodsPort() {
		return irodsPort;
	}

	@Override
	public String getIrodsZone() {
		return irodsZone;
	}

	@Override
	public String getIrodsAuthScheme() {
		return irodsAuthScheme;
	}

	@Override
	public long getDownloadLimit() {
		return downloadLimit;
	}

	@Override
	public boolean isPopulateMsiEnabled() {
		return populateMsiEnabled;
	}

	public boolean isTicketsEnabled() {
		return ticketsEnabled;
	}

	public void setTicketsEnabled(boolean ticketsEnabled) {
		this.ticketsEnabled = ticketsEnabled;
	}

	@Override
	public boolean isUploadRulesEnabled() {
		return uploadRulesEnabled;
	}

	public void setUploadRulesEnabled(boolean uploadRulesEnabled) {
		this.uploadRulesEnabled = uploadRulesEnabled;
	}

	@Override
	public String getDefaultIrodsAuthScheme() {
		return defaultIrodsAuthScheme;
	}

	public void setDefaultIrodsAuthScheme(String defaultIrodsAuthScheme) {
		this.defaultIrodsAuthScheme = defaultIrodsAuthScheme;
	}

	@Override
	public boolean isDashboardEnabled() {
		return dashboardEnabled;
	}

	public void setDashboardEnabled(boolean dashboardEnabled) {
		this.dashboardEnabled = dashboardEnabled;
	}

	@Override
	public List<AuthTypeMapping> listAuthTypeMappings() {
		List<AuthTypeMapping> authTypeList = new ArrayList<AuthTypeMapping>();
		if (this.getAuthtypeMappings() == null || this.getAuthtypeMappings().isEmpty()
				|| this.getAuthtypeMappings().equals("${metalnx.authtype.mappings}")) {
			for (String scheme : AuthScheme.getAuthSchemeList()) {
				authTypeList.add(new AuthTypeMapping(scheme, scheme));
			}
		} else {
			String[] entries;
			// parse and create a custom auth type list
			entries = this.getAuthtypeMappings().split("\\|");
			for (String entry : entries) {
				String[] parsedEntry = entry.split(":");
				if (parsedEntry.length != 2) {
					throw new IllegalArgumentException("unparsable authTypeMapping");
				}
				authTypeList.add(new AuthTypeMapping(parsedEntry[0], parsedEntry[1]));
			}

		}
		return authTypeList;
	}

	public String getAuthtypeMappings() {
		return authtypeMappings;
	}

	public void setAuthtypeMappings(String authtypeMappings) {
		this.authtypeMappings = authtypeMappings;
	}

	@Override
	public String getJwtIssuer() {
		return jwtIssuer;
	}

	public void setJwtIssuer(String jwtIssuer) {
		this.jwtIssuer = jwtIssuer;
	}

	@Override
	public String getJwtSecret() {
		return jwtSecret;
	}

	public void setJwtSecret(String jwtSecret) {
		this.jwtSecret = jwtSecret;
	}

	@Override
	public String getJwtAlgo() {
		return jwtAlgo;
	}

	public void setJwtAlgo(String jwtAlgo) {
		this.jwtAlgo = jwtAlgo;
	}

	public boolean isPluggableSearchEnabled() {
		return pluggableSearchEnabled;
	}

	public void setPluggableSearchEnabled(boolean pluggableSearchEnabled) {
		this.pluggableSearchEnabled = pluggableSearchEnabled;
	}

	public boolean isPluggableShoppingCartEnabled() {
		return pluggableShoppingCartEnabled;
	}

	public void setPluggableShoppingCartEnabled(boolean pluggableShoppingCartEnabled) {
		this.pluggableShoppingCartEnabled = pluggableShoppingCartEnabled;
	}

	public boolean isClassicSearchEnabled() {
		return classicSearchEnabled;
	}

	public void setClassicSearchEnabled(boolean classicSearchEnabled) {
		this.classicSearchEnabled = classicSearchEnabled;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ConfigServiceImpl [");
		if (msiAPIVersionSupported != null) {
			builder.append("msiAPIVersionSupported=").append(msiAPIVersionSupported).append(", ");
		}
		if (mlxMSIsExpected != null) {
			builder.append("mlxMSIsExpected=").append(mlxMSIsExpected).append(", ");
		}
		if (irods41MSIsExpected != null) {
			builder.append("irods41MSIsExpected=").append(irods41MSIsExpected).append(", ");
		}
		if (irods42MSIsExpected != null) {
			builder.append("irods42MSIsExpected=").append(irods42MSIsExpected).append(", ");
		}
		if (otherMSIsExpected != null) {
			builder.append("otherMSIsExpected=").append(otherMSIsExpected).append(", ");
		}
		if (irodsHost != null) {
			builder.append("irodsHost=").append(irodsHost).append(", ");
		}
		if (irodsPort != null) {
			builder.append("irodsPort=").append(irodsPort).append(", ");
		}
		if (irodsZone != null) {
			builder.append("irodsZone=").append(irodsZone).append(", ");
		}
		if (irodsAuthScheme != null) {
			builder.append("irodsAuthScheme=").append(irodsAuthScheme).append(", ");
		}
		builder.append("populateMsiEnabled=").append(populateMsiEnabled).append(", ticketsEnabled=")
				.append(ticketsEnabled).append(", uploadRulesEnabled=").append(uploadRulesEnabled)
				.append(", downloadLimit=").append(downloadLimit).append(", handleNoAccessViaProxy=");
		if (defaultIrodsAuthScheme != null) {
			builder.append("defaultIrodsAuthScheme=").append(defaultIrodsAuthScheme).append(", ");
		}
		builder.append("dashboardEnabled=").append(dashboardEnabled).append(", pluggableSearchEnabled=")
				.append(pluggableSearchEnabled).append(", pluggableShoppingCartEnabled=")
				.append(pluggableShoppingCartEnabled).append(", classicSearchEnabled=").append(classicSearchEnabled)
				.append(", ");

		if (jwtIssuer != null) {
			builder.append("jwtIssuer=").append(jwtIssuer).append(", ");
		}
		if (jwtAlgo != null) {
			builder.append("jwtAlgo=").append(jwtAlgo).append(", ");
		}
		if (authtypeMappings != null) {
			builder.append("authtypeMappings=").append(authtypeMappings);
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public void setMsiAPIVersionSupported(String msiAPIVersionSupported) {
		this.msiAPIVersionSupported = msiAPIVersionSupported;
	}

	@Override
	public void setMlxMSIsExpected(String mlxMSIsExpected) {
		this.mlxMSIsExpected = mlxMSIsExpected;
	}

	@Override
	public void setIrods41MSIsExpected(String irods41msIsExpected) {
		irods41MSIsExpected = irods41msIsExpected;
	}

	@Override
	public void setIrods42MSIsExpected(String irods42msIsExpected) {
		irods42MSIsExpected = irods42msIsExpected;
	}

	@Override
	public void setOtherMSIsExpected(String otherMSIsExpected) {
		this.otherMSIsExpected = otherMSIsExpected;
	}

	@Override
	public void setIrodsHost(String irodsHost) {
		this.irodsHost = irodsHost;
	}

	@Override
	public void setIrodsPort(String irodsPort) {
		this.irodsPort = irodsPort;
	}

	@Override
	public void setIrodsZone(String irodsZone) {
		this.irodsZone = irodsZone;
	}

	@Override
	public void setIrodsAuthScheme(String irodsAuthScheme) {
		this.irodsAuthScheme = irodsAuthScheme;
	}

	@Override
	public void setPopulateMsiEnabled(boolean populateMsiEnabled) {
		this.populateMsiEnabled = populateMsiEnabled;
	}

	@Override
	public void setDownloadLimit(long downloadLimit) {
		this.downloadLimit = downloadLimit;
	}

	@Override
	public boolean isPublicSidebarLinkEnabled() {
		return publicSidebarLinkEnabled;
	}
	
	@Override
	public void setPublicSidebarLinkEnabled(boolean publicSidebarLinkEnabled) {
		this.publicSidebarLinkEnabled = publicSidebarLinkEnabled;
	}

}
