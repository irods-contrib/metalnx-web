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

package com.emc.metalnx.services.machine.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.emc.metalnx.services.machine.enums.ServerRequestInfoType;

public class ServerInformationRetrievalThread extends Thread {

	private String serverHost;
	private String serverPort;
	private ServerRequestInfoType command;
	private String result;
	private int timeout;
	
	private static final Logger logger = LoggerFactory.getLogger(ServerInformationRetrievalThread.class);
	
	public ServerInformationRetrievalThread(String serverHost, String serverPort, ServerRequestInfoType command, int timeout) {
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.command = command;
	}

	public void run() {
		logger.debug("Getting {} info for server {}", this.command.toString(), this.serverHost);
		this.result = ServerUtil.getMachineInformation(this.serverHost, this.serverPort, this.command, this.timeout);
	}
	
	/**
	 * @return the serverHost
	 */
	public String getServerHost() {
		return serverHost;
	}
	
	/**
	 * @return the result
	 */
	public String getResult() {
		return result;
	}
	
}
