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

import java.util.List;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;

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
