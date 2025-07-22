 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import org.irods.jargon.core.exception.JargonException;

import java.io.IOException;

public interface RemoteExecutionService {

	String execute(String command) throws JargonException, IOException, 
		DataGridConnectionRefusedException ;
	
}
