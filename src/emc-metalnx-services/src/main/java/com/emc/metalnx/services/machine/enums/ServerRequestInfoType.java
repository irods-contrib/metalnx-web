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
