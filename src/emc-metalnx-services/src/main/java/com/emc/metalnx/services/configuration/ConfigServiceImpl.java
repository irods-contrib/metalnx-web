/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.services.configuration;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.emc.metalnx.services.interfaces.ConfigService;

/**
 * Class that will load all all configurable parameters from *.properties files.
 */
@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class ConfigServiceImpl implements ConfigService {

	public final static Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);

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

	@Value("${jobs.irods.username}")
	private String irodsJobUser;

	@Value("${jobs.irods.password}")
	private String irodsJobPassword;

	@Value("${jobs.irods.auth.scheme}")
	private String irodsAuthScheme;

	@Value("${populate.msi.enabled}")
	private boolean populateMsiEnabled;

	@Value("${metalnx.enable.tickets}")
	private boolean ticketsEnabled;

	@Value("${metalnx.enable.upload.rules}")
	private boolean uploadRulesEnabled;

	@Value("${metalnx.download.limit}")
	private long downloadLimit;

	@Value("${access.proxy}")
	private boolean handleNoAccessViaProxy;

	@Override
	public GlobalConfig getGlobalConfig() {
		logger.info("getGlobalConfig()");
		GlobalConfig globalConfig = new GlobalConfig();
		globalConfig.setTicketsEnabled(this.isTicketsEnabled());
		globalConfig.setUploadRulesEnabled(isUploadRulesEnabled());
		globalConfig.setHandleNoAccessViaProxy(handleNoAccessViaProxy);
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
	public String getIrodsJobUser() {
		return irodsJobUser;
	}

	@Override
	public String getIrodsJobPassword() {
		return irodsJobPassword;
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
		if (irodsJobUser != null) {
			builder.append("irodsJobUser=").append(irodsJobUser).append(", ");
		}

		if (irodsAuthScheme != null) {
			builder.append("irodsAuthScheme=").append(irodsAuthScheme).append(", ");
		}
		builder.append("populateMsiEnabled=").append(populateMsiEnabled).append(", ticketsEnabled=")
				.append(ticketsEnabled).append(", uploadRulesEnabled=").append(uploadRulesEnabled)
				.append(", downloadLimit=").append(downloadLimit).append(", handleNoAccessViaProxy=")
				.append(handleNoAccessViaProxy).append("]");
		return builder.toString();
	}

	@Override
	public boolean isUploadRulesEnabled() {
		return uploadRulesEnabled;
	}

	public void setUploadRulesEnabled(boolean uploadRulesEnabled) {
		this.uploadRulesEnabled = uploadRulesEnabled;
	}

	public boolean isHandleNoAccessViaProxy() {
		return handleNoAccessViaProxy;
	}

	public void setHandleNoAccessViaProxy(boolean handleNoAccessViaProxy) {
		this.handleNoAccessViaProxy = handleNoAccessViaProxy;
	}
}
