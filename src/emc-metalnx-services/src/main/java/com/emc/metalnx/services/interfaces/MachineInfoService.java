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
