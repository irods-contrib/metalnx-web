package com.emc.metalnx.services.interfaces;

import java.util.List;

public interface NotificationService {

	public List<String> getAllNotification(String userId);
	public List<String> getNotificationById(String userId , String notificationId);
	//public List<String> addAllNotification(Notification userId);
	
	
	
}
