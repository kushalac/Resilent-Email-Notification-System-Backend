package com.example.demo;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notification_copies")
public class NotificationCopy {
	    @Id
	    public String notificationId;
	    public String notificationType;
	    public String notificationSubject;
	    public String notificationContent;
	    public List<String> userList;
	    
	    public NotificationCopy() {
	        this.notificationId = UUID.randomUUID().toString();
	        this.userList = new ArrayList<>();
	    }

		public String getNotificationId() {
			return notificationId;
		}

		public void setNotificationId(String notificationId) {
			this.notificationId = notificationId;
		}

		public String getNotificationType() {
			return notificationType;
		}

		public void setNotificationType(String notificationType) {
			this.notificationType = notificationType;
		}

		public String getNotificationSubject() {
			return notificationSubject;
		}

		public void setNotificationSubject(String notificationSubject) {
			this.notificationSubject = notificationSubject;
		}

		public String getNotificationContent() {
			return notificationContent;
		}

		public void setNotificationContent(String notificationContent) {
			this.notificationContent = notificationContent;
		}

		public List<String> getUserList() {
			return userList;
		}

		public void setUserList(List<String> userList) {
			this.userList = userList;
		}
	   
}

