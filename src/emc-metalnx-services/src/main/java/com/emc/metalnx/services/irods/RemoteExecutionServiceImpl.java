 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.RemoteExecutionService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.RemoteExecutionOfCommandsAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class RemoteExecutionServiceImpl implements RemoteExecutionService {

	@Autowired
	IRODSServices irodsServices;

	private static final int bufferSize = 8192;
	private static final String defaultEncoding = "UTF-8";

	//private static final Logger logger = LoggerFactory.getLogger(RemoteExecutionServiceImpl.class);
	
	@Override
	public String execute(String command) throws JargonException, IOException, 
		DataGridConnectionRefusedException {
		
			
		byte[] buffer = new byte[bufferSize];
		RemoteExecutionOfCommandsAO commandsAO = irodsServices.getRemoteExecutionOfCommandsAO();
		InputStream stream = commandsAO.executeARemoteCommandAndGetStreamGivingCommandNameAndArgs(command, "");
		
		StringBuilder resultBuilder = new StringBuilder();
		
		while(stream.read(buffer) != -1) {
			resultBuilder.append(new String(buffer, defaultEncoding));
		}
		
		stream.close();
		
		return resultBuilder.toString();
	}
}
