package com.emc.metalnx.services.interfaces;

import java.net.UnknownHostException;

/**
 * This interface defines all information we want to get from a server
 * @author guerra
 *
 */

public interface MachineInfoService {

	/**
	 * Gets the IP Address from a given host name
	 * @param host
	 * 			host name to find the IP address
	 * @return the IP address in string format 		
	 * @throws UnknownHostException 
	 */
	public String getAddress(String host) throws UnknownHostException;
	
	/**
	 * Gets the host name from a given ip
	 * @param ip
	 * 			ip to find the host name
	 * @return the host name relative to the given IP
	 * @throws UnknownHostException 
	 */
	public String getHostName(String ip) throws UnknownHostException;
}
