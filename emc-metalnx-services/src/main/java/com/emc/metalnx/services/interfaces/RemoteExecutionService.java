package com.emc.metalnx.services.interfaces;

import java.io.IOException;

import org.irods.jargon.core.exception.JargonException;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

public interface RemoteExecutionService {

	String execute(String command) throws JargonException, IOException, 
		DataGridConnectionRefusedException ;
	
}
