/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.services.machine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.emc.metalnx.services.interfaces.MonitoringService;
import com.emc.metalnx.services.machine.enums.ServerRequestInfoType;
import com.emc.metalnx.services.machine.util.ServerUtil;

@Service
public class MonitoringServiceImpl implements MonitoringService {

    @Value("${rmd.connection.port}")
    private String rmdConnectionPort;

    @Value("${rmd.connection.timeout}")
    private String rmdConnectionTimeout;

    private static final Logger logger = LoggerFactory.getLogger(MonitoringServiceImpl.class);

    @Override
    public String getDataFromHost(String type, String host) {
        logger.debug("Making {} request to {}.", type, host);
        Integer timeout = Integer.parseInt(rmdConnectionTimeout);
        ServerRequestInfoType infoType = ServerRequestInfoType.valueOf(type);
        return ServerUtil.getMachineInformation(host, rmdConnectionPort, infoType, timeout);
    }

}
