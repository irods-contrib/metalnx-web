package com.emc.metalnx.services.machine;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.springframework.stereotype.Service;

import com.emc.metalnx.services.interfaces.MachineInfoService;

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
