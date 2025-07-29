 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.machine;

import com.emc.metalnx.services.interfaces.MachineInfoService;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Service
public class MachineInfoServiceImpl implements MachineInfoService {

	@Override
	public String getAddress(String hostName) throws UnknownHostException {
		
		InetAddress inetAddress = InetAddress.getByName(hostName);
		
		return inetAddress.getHostAddress();
	}	

	@Override
	public String getHostName(String ip) throws UnknownHostException {
		InetAddress inetAddress = InetAddress.getByName(ip);
		
		return inetAddress.getHostName();
	}

}
