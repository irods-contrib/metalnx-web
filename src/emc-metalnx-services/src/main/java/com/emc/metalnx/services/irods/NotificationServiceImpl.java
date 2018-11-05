package com.emc.metalnx.services.irods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.datacommons.api.NotificationsApi;
import org.datacommons.client.ApiException;
import org.datacommons.model.Notification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.emc.metalnx.services.interfaces.NotificationService;

@Service
public class NotificationServiceImpl implements NotificationService{

	//@Autowired
	NotificationsApi apiInstance = new NotificationsApi();
	
	@Override
	public List<Notification> getAllNotification(String userId) {
		// TODO Auto-generated method stub
		List<Notification> result = new ArrayList<>();
		 try {
			result = apiInstance.getNotification(userId);
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public List<Notification> getNotificationById(String userId, String notificationId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Integer> getUnseenCounts(String userId) {
		Map<String, Integer> result = new HashMap<>();
		try {
			result = apiInstance.getUnseenCount(userId);
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		return result;
	}

	@Override
	public void deleteNotifications(List<String> uuids) {
		// TODO Auto-generated method stub
		
	}

}
