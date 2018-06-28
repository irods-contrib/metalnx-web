 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.machine.util;

import com.emc.metalnx.services.machine.enums.ServerRequestInfoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
