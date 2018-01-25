/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.emc.metalnx.core.domain.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
	private String msiVersion;
	private List<String> msisInstaleld;

	private List<String> mlxMSIsExpected;
	private List<String> irodsMSIsExpected;
	private List<String> otherMSIsExpected;

	private Map<String, Boolean> metalnxMSIs;
	private Map<String, Boolean> irodsMSIs;
	private Map<String, Boolean> otherMSIs;

	public DataGridServer() {
		metalnxMSIs = new HashMap<>();
		irodsMSIs = new HashMap<>();
		otherMSIs = new HashMap<>();
	}

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
	 * 
	 * @return the resources
	 */
	public List<DataGridResource> getResources() {
		if (resources != null)
			Collections.sort(resources);
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
	 * 
	 * @param resource
	 *            to be added
	 */
	public void addResource(DataGridResource resource) {
		if (this.resources == null) {
			this.resources = new ArrayList<>();
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
	 * @param machineStatus
	 *            the machineStatus to set
	 */
	public void setMachineStatus(String machineStatus) {
		this.machineStatus = machineStatus;
	}

	/**
	 * @param dataGridStatus
	 *            the dataGridStatus to set
	 */
	public void setDataGridStatus(String dataGridStatus) {
		this.dataGridStatus = dataGridStatus;
	}

	/**
	 * @param memoryStatus
	 *            the memoryStatus to set
	 */
	public void setMemoryStatus(String memoryStatus) {
		this.memoryStatus = memoryStatus;
	}

	/**
	 * @param diskStatus
	 *            the diskStatus to set
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
	 * @param totalStorageUsed
	 *            the totalStorageUsed to set
	 */
	public void setTotalStorageUsed(long totalStorageUsed) {
		this.totalStorageUsed = totalStorageUsed;
	}

	/**
	 * @param totalStorageAvailable
	 *            the totalStorageAvailable to set
	 */
	public void setTotalStorageAvailable(long totalStorageAvailable) {
		this.totalStorageAvailable = totalStorageAvailable;
	}

	/**
	 * @param totalStorage
	 *            the totalStorage to set
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
	 * @param isRmdPackageRunning
	 *            the isRmdPackageRunning to set
	 */
	public void setRmdPackageRunning(boolean isRmdPackageRunning) {
		this.isRmdPackageRunning = isRmdPackageRunning;
	}

	/**
	 * This method compares if two DataGridServer objects are the same. They will be
	 * the same if they have the same host name.
	 * 
	 * @return true, if they are equal. False, otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (obj instanceof DataGridServer) {
			String hostName = ((DataGridServer) obj).getHostname();
			return this.getHostname().equals(hostName);
		}

		return false;
	}

	/**
	 * This method provides consistent results for the equals method. If two
	 * DataGridServer objects are equal, they both must have the same hash code.
	 * 
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
	 * @param rmdPackageRelease
	 *            the rmdPackageRelease to set
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
	 * @param rmdPackageVersion
	 *            the rmdPackageVersion to set
	 */
	public void setRmdPackageVersion(String rmdPackageVersion) {
		this.rmdPackageVersion = rmdPackageVersion;
	}

	public void setMSIVersion(String msiVersion) {
		this.msiVersion = msiVersion;
	}

	public String getMSIVersion() {
		if (msiVersion == null) {
			return "";
		}
		return msiVersion;
	}

	@Override
	public String toString() {
		final int maxLen = 10;
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridServer [");
		if (type != null) {
			builder.append("type=").append(type).append(", ");
		}
		if (hostname != null) {
			builder.append("hostname=").append(hostname).append(", ");
		}
		if (ip != null) {
			builder.append("ip=").append(ip).append(", ");
		}
		if (machineStatus != null) {
			builder.append("machineStatus=").append(machineStatus).append(", ");
		}
		if (dataGridStatus != null) {
			builder.append("dataGridStatus=").append(dataGridStatus).append(", ");
		}
		if (memoryStatus != null) {
			builder.append("memoryStatus=").append(memoryStatus).append(", ");
		}
		if (diskStatus != null) {
			builder.append("diskStatus=").append(diskStatus).append(", ");
		}
		builder.append("totalStorageUsed=").append(totalStorageUsed).append(", totalStorageAvailable=")
				.append(totalStorageAvailable).append(", totalStorage=").append(totalStorage).append(", ");
		if (resources != null) {
			builder.append("resources=").append(toString(resources, maxLen)).append(", ");
		}
		builder.append("isRmdPackageRunning=").append(isRmdPackageRunning).append(", ");
		if (rmdPackageRelease != null) {
			builder.append("rmdPackageRelease=").append(rmdPackageRelease).append(", ");
		}
		if (rmdPackageVersion != null) {
			builder.append("rmdPackageVersion=").append(rmdPackageVersion).append(", ");
		}
		if (msiVersion != null) {
			builder.append("msiVersion=").append(msiVersion).append(", ");
		}
		if (msisInstaleld != null) {
			builder.append("msisInstaleld=").append(toString(msisInstaleld, maxLen)).append(", ");
		}
		if (mlxMSIsExpected != null) {
			builder.append("mlxMSIsExpected=").append(toString(mlxMSIsExpected, maxLen)).append(", ");
		}
		if (irodsMSIsExpected != null) {
			builder.append("irodsMSIsExpected=").append(toString(irodsMSIsExpected, maxLen)).append(", ");
		}
		if (otherMSIsExpected != null) {
			builder.append("otherMSIsExpected=").append(toString(otherMSIsExpected, maxLen)).append(", ");
		}
		if (metalnxMSIs != null) {
			builder.append("metalnxMSIs=").append(toString(metalnxMSIs.entrySet(), maxLen)).append(", ");
		}
		if (irodsMSIs != null) {
			builder.append("irodsMSIs=").append(toString(irodsMSIs.entrySet(), maxLen)).append(", ");
		}
		if (otherMSIs != null) {
			builder.append("otherMSIs=").append(toString(otherMSIs.entrySet(), maxLen));
		}
		builder.append("]");
		return builder.toString();
	}

	public void setMSIInstalledList(List<String> msisInstalled) {
		if (msisInstalled == null || msisInstalled.isEmpty())
			return;

		this.msisInstaleld = msisInstalled;

		// classifying MSIs by their type
		for (String msi : msisInstalled) {
			if (mlxMSIsExpected.contains(msi))
				addToMsiMetalnx(msi);
			else if (irodsMSIsExpected.contains(msi))
				addToMsiIRODS(msi);
			else
				addToMsiOther(msi);
		}
	}

	public List<String> getMSIInstalledList() {
		return this.msisInstaleld != null ? this.msisInstaleld : new ArrayList<>();
	}

	public void setMetalnxExpectedMSIs(List<String> mlxMSIsExpected) {
		if (mlxMSIsExpected == null || mlxMSIsExpected.isEmpty())
			return;

		this.mlxMSIsExpected = mlxMSIsExpected;
		for (String msi : mlxMSIsExpected) {
			if (!msi.isEmpty())
				metalnxMSIs.put(msi, false);
		}
	}

	public void setIRodsExpectedMSIs(List<String> irodsMSIsExpected) {
		if (irodsMSIsExpected == null || irodsMSIsExpected.isEmpty())
			return;

		this.irodsMSIsExpected = irodsMSIsExpected;
		for (String msi : irodsMSIsExpected) {
			if (!msi.isEmpty())
				irodsMSIs.put(msi, false);
		}
	}

	public void setOtherExpectedMSIs(List<String> otherMSIsExpected) {
		if (otherMSIsExpected == null || otherMSIsExpected.isEmpty())
			return;

		this.otherMSIsExpected = otherMSIsExpected;
		for (String msi : otherMSIsExpected) {
			if (!msi.isEmpty())
				otherMSIs.put(msi, false);
		}
	}

	// used by frontend
	public Map<String, Boolean> getMetalnxMSIs() {
		return metalnxMSIs;
	}

	// used by frontend
	public Map<String, Boolean> getIRODSMSIs() {
		return irodsMSIs;
	}

	// used by frontend
	public Map<String, Boolean> getOtherMSIs() {
		return otherMSIs;
	}

	// used by frontend
	public boolean isThereAnyMSI() {
		return !getMSIInstalledList().isEmpty();
	}

	private void addToMsiMetalnx(String msi) {
		if (msi == null || msi.isEmpty())
			return;
		this.metalnxMSIs.put(msi, true);
	}

	private void addToMsiIRODS(String msi) {
		if (msi == null || msi.isEmpty())
			return;
		this.irodsMSIs.put(msi, true);
	}

	private void addToMsiOther(String msi) {
		if (msi == null || msi.isEmpty())
			return;
		this.otherMSIs.put(msi, true);
	}

	private String toString(Collection<?> collection, int maxLen) {
		StringBuilder builder = new StringBuilder();
		builder.append("[");
		int i = 0;
		for (Iterator<?> iterator = collection.iterator(); iterator.hasNext() && i < maxLen; i++) {
			if (i > 0) {
				builder.append(", ");
			}
			builder.append(iterator.next());
		}
		builder.append("]");
		return builder.toString();
	}
}
