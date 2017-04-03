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

import com.emc.metalnx.services.interfaces.ConfigService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Class that will load all all configurable parameters from *.properties files.
 */
@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class ConfigServiceImpl implements ConfigService {

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

    public String getMsiAPIVersionSupported() {
        if (msiAPIVersionSupported == null) return "";
        return msiAPIVersionSupported;
    }

    public List<String> getMlxMSIsExpected() {
        if (mlxMSIsExpected == null) return Collections.emptyList();
        return Arrays.asList(mlxMSIsExpected.split(","));
    }

    public List<String> getIrods41MSIsExpected() {
        if (irods41MSIsExpected == null) return Collections.emptyList();
        return Arrays.asList(irods41MSIsExpected.split(","));
    }

    public List<String> getIrods42MSIsExpected() {
        if (irods42MSIsExpected == null) return Collections.emptyList();
        return Arrays.asList(irods42MSIsExpected.split(","));
    }

    public List<String> getOtherMSIsExpected() {
        if (otherMSIsExpected == null) return Collections.emptyList();
        return Arrays.asList(otherMSIsExpected.split(","));
    }

    public String getIrodsHost() {
        return irodsHost;
    }

    public String getIrodsPort() {
        return irodsPort;
    }

    public String getIrodsZone() {
        return irodsZone;
    }

    public String getIrodsJobUser() {
        return irodsJobUser;
    }

    public String getIrodsJobPassword() {
        return irodsJobPassword;
    }

    public String getIrodsAuthScheme() {
        return irodsAuthScheme;
    }
}
