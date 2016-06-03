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
package com.emc.metalnx.core.domain.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.emc.metalnx.core.domain.entity.enums.DataGridServerType;

public class DataGridServer implements Comparable<DataGridServer> {
	
	private DataGridServerType type;
	private String hostname;
	private String ip;
	private String machineStatus;
	private String dataGridStatus;
	private String memoryStatus;
	private String diskStatus;
	private long totalStorageUsed;
	private long totalStorageAvailable;
	private long totalStorage;
	private List<DataGridResource> resources;
	private boolean isRmdPackageRunning;
	private String rmdPackageRelease;
	private String rmdPackageVersion;

	/**
	 * @return the type
	 */
	public DataGridServerType getType() {
		return type;
	}

	/**
	 * @return the hostname
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * Gets the resources of this server sorted by resource name
	 * @return the resources
	 */
	public List<DataGridResource> getResources() {
		if(resources != null) Collections.sort(resources);
		return resources;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(DataGridServerType type) {
		this.type = type;
	}

	/**
	 * @param hostname
	 *            the hostname to set
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	/**
	 * @param resources
	 *            the resources to set
	 */
	public void setResources(List<DataGridResource> resources) {
		this.resources = resources;
	}
	
	/**
	 * Adds resource to the server
	 * @param resources
	 */
	public void addResource(DataGridResource resource) {
		if (this.resources == null) {
			this.resources = new ArrayList<DataGridResource>();
		}
		this.resources.add(resource);
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	/**
	 * @return the machineStatus
	 */
	public String getMachineStatus() {
		return machineStatus;
	}

	/**
	 * @return the dataGridStatus
	 */
	public String getDataGridStatus() {
		return dataGridStatus;
	}

	/**
	 * @return the memoryStatus
	 */
	public String getMemoryStatus() {
		return memoryStatus;
	}

	/**
	 * @return the diskStatus
	 */
	public String getDiskStatus() {
		return diskStatus;
	}

	/**
	 * @param machineStatus the machineStatus to set
	 */
	public void setMachineStatus(String machineStatus) {
		this.machineStatus = machineStatus;
	}

	/**
	 * @param dataGridStatus the dataGridStatus to set
	 */
	public void setDataGridStatus(String dataGridStatus) {
		this.dataGridStatus = dataGridStatus;
	}

	/**
	 * @param memoryStatus the memoryStatus to set
	 */
	public void setMemoryStatus(String memoryStatus) {
		this.memoryStatus = memoryStatus;
	}

	/**
	 * @param diskStatus the diskStatus to set
	 */
	public void setDiskStatus(String diskStatus) {
		this.diskStatus = diskStatus;
	}

	/**
	 * @return the totalStorageUsed
	 */
	public long getTotalStorageUsed() {
		return totalStorageUsed;
	}

	/**
	 * @return the totalStorageAvailable
	 */
	public long getTotalStorageAvailable() {
		return totalStorageAvailable;
	}

	/**
	 * @return the totalStorage
	 */
	public long getTotalStorage() {
		return totalStorage;
	}

	/**
	 * @param totalStorageUsed the totalStorageUsed to set
	 */
	public void setTotalStorageUsed(long totalStorageUsed) {
		this.totalStorageUsed = totalStorageUsed;
	}

	/**
	 * @param totalStorageAvailable the totalStorageAvailable to set
	 */
	public void setTotalStorageAvailable(long totalStorageAvailable) {
		this.totalStorageAvailable = totalStorageAvailable;
	}

	/**
	 * @param totalStorage the totalStorage to set
	 */
	public void setTotalStorage(long totalStorage) {
		this.totalStorage = totalStorage;
	}

	/**
	 * @return the isRmdPackageRunning
	 */
	public boolean isRmdPackageRunning() {
		return isRmdPackageRunning;
	}

	/**
	 * @param isRmdPackageRunning the isRmdPackageRunning to set
	 */
	public void setRmdPackageRunning(boolean isRmdPackageRunning) {
		this.isRmdPackageRunning = isRmdPackageRunning;
	}

	/**
	 * This method compares if two DataGridServer objects are the same. They will be the same
	 * if they have the same host name.
	 * @return true, if they are equal. False, otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		
		if(this == obj) {
			return true;
		}
		
		if(obj instanceof DataGridServer) {
			String hostName = ((DataGridServer) obj).getHostname();
			return this.getHostname().equals(hostName) ;
		}
		
		return false;		
	}
	
	/**
	 * This method provides consistent results for the equals method. If two DataGridServer objects
	 * are equal, they both must have the same hash code. 
	 * @return a hash code based on the host name
	 */
	@Override
	public int hashCode() {
	    return this.getHostname().hashCode();
	}
	
	@Override
	public int compareTo(DataGridServer dgs) {
		return this.ip.compareTo(dgs.getIp());
	}

	/**
	 * @return the rmdPackageRelease
	 */
	public String getRmdPackageRelease() {
		return rmdPackageRelease;
	}

	/**
	 * @param rmdPackageRelease the rmdPackageRelease to set
	 */
	public void setRmdPackageRelease(String rmdPackageRelease) {
		this.rmdPackageRelease = rmdPackageRelease;
	}

	/**
	 * @return the rmdPackageVersion
	 */
	public String getRmdPackageVersion() {
		return rmdPackageVersion;
	}

	/**
	 * @param rmdPackageVersion the rmdPackageVersion to set
	 */
	public void setRmdPackageVersion(String rmdPackageVersion) {
		this.rmdPackageVersion = rmdPackageVersion;
	}

}
