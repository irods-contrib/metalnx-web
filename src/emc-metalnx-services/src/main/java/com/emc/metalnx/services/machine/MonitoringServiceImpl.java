 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.machine;

import com.emc.metalnx.services.interfaces.MonitoringService;
import com.emc.metalnx.services.machine.enums.ServerRequestInfoType;
import com.emc.metalnx.services.machine.util.ServerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
