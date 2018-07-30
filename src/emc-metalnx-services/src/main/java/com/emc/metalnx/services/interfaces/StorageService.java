 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

import java.util.List;

public interface StorageService {
	
	/**
	 * Calculates the amount of used storage in the entire grid.
	 * @return the total of used storage
	 */
	public long totalUsedStorageOfGrid(List<DataGridServer> servers);
	
	/**
	 * Calculates the amount of available storage in the entire grid.
	 * @return the total of available storage
	 */
	public long totalAvailableStorageOfGrid(List<DataGridServer> servers);

	/**
	 * Calculates the total used storage of a server.
	 * @param hostname 
	 * @param partitionsLocation
	 * 			partitions existing in the server
	 * @param diskInfoJson
	 * 			JSON response from RMD
	 * @param currentServerResources
	 * 			current resources of a server available in cache (in case the iCAT is down)
	 * @return totalUsedStorage
	 * 			number that represents the amount of storage used
	 * @throws DataGridConnectionRefusedException 
	 */
	public long totalUsedStorageOfAServer(String hostname, String diskInfoJson, 
		List<DataGridResource> currentServerResources) 
		throws DataGridConnectionRefusedException;
	
	/**
	 * Calculates the total available storage a server.
	 * @param hostname
	 * 			server hostname
	 * @param diskInfoJson
	 * 			JSON response from RMD
	 * @param currentServerResources
	 * 			current resources of a server available in cache (in case the iCAT is down)
	 * @return totalAvailableStorage
	 * 			number that represents the amount of storage available
	 * @throws DataGridConnectionRefusedException
	 */
	public long totalAvailableStorageOfAServer(String hostname, String diskInfoJson, 
		List<DataGridResource> currentServerResources) throws DataGridConnectionRefusedException;
		
}
