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

package com.emc.metalnx.services.machine.enums;

public enum ServerRequestInfoType {
	
	ALL(""),
	GENERAL_STATUS("serverstatus"),
	CPU_STATS("cpustat"),
	CPU_INFO("cpu"),
	MEMORY_STATS("memory"),
	MOUNT("mounts"),
	DISK_STATS("disk"),
	IRODS_LOGS("irodslogs"),
	IRODS_STATUS("irodsstatus"),
	NORMAL_STATUS("normal"),
	WARNING_STATUS("warning"),
	ERROR_STATUS("error");
	
	private String infoType;
	
	private ServerRequestInfoType(String infoType) {
		this.infoType = infoType;
	}
	
	public String toString() {
		return this.infoType;
	}
}
