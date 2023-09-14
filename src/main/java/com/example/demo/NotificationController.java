package com.example.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.user.User;
import com.example.demo.repo.NotificationRepository;
import com.example.demo.repo.UserRepository;

import org.springframework.data.mongodb.core.MongoTemplate;

@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private MyKafkaProducer kafkaproducer;
    
    @Autowired
    private  NotificationRepository notificationRepoCall;
    
    @Autowired
    private MongoTemplate mongoTemplate;

    
    @PostMapping("/createNotification")
    public ResponseEntity<String> createNewNotification(@RequestBody Notification notification) {
        // Check if a notification with the same subject exists for the given type
        if (notificationRepoCall.existsByNotificationTypeAndNotificationSubject(notification.getNotificationType(), notification.getNotificationSubject())) {
            return ResponseEntity.badRequest().body("Sorry, the message of type '" + notification.getNotificationType() + "' with the subject '" + notification.getNotificationSubject() + "' has already been created.");
        }

        // Save the notification (MongoDB will generate the _id)
        notificationRepoCall.save(notification);

        return ResponseEntity.ok("Notification created and saved successfully.");
    }
    
    @PostMapping("/sendNotification")
    public ResponseEntity<String> sendNotification(@RequestBody Map<String, String> requestMap) {
        String notificationType = requestMap.get("notificationType");
        String notificationSubject = requestMap.get("notificationSubject");
        Notification notification = notificationRepoCall.findByNotificationTypeAndNotificationSubject(notificationType, notificationSubject);

        if (notification == null) {
            return ResponseEntity.badRequest().body("Notification not found.");
        }

        // Send the notification email
        sendNotificationEmail(notification.getNotificationType(), notification.getNotificationSubject(), notification.getNotificationContent(), notification);

        return ResponseEntity.ok("Notification sent successfully.");
    }
     
    @PutMapping("/updateNotification")
    public ResponseEntity<String> updateNotification(@RequestBody Notification updatedNotification) {
        // Check if a notification with the same type and subject exists
        Notification existingNotification = notificationRepoCall.findByNotificationTypeAndNotificationSubject(
                updatedNotification.getNotificationType(), updatedNotification.getNotificationSubject());

        if (existingNotification == null) {
        	return ResponseEntity.ok("Notification with type "+updatedNotification.getNotificationType()+ " and with subject "+updatedNotification.getNotificationSubject() +" doent exist.");
        }

        try {
            // Update the existing notification with the new content
            existingNotification.setNotificationContent(updatedNotification.getNotificationContent());
            // You can update other fields as needed

            // Save the updated notification
            notificationRepoCall.save(existingNotification);

            return ResponseEntity.ok("Notification updated successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating notification: " + e.getMessage());
        }
    }
    
    @GetMapping("/getNotificationSubjects")
    public ResponseEntity<List<String>> getNotificationSubjects(@RequestParam String notificationType) {
        System.out.println(notificationType);
        List<Notification> notifications = notificationRepoCall.findAll();
        List<String> subjects = new ArrayList<>();
        for (Notification notification : notifications) {
            if (notification.getNotificationType().equals(notificationType)) {
                subjects.add(notification.getNotificationSubject());
            }
        }
        System.out.println(subjects);

        // Create a ResponseEntity and set the list of subjects as the response body
        return ResponseEntity.ok(subjects);
    }
    
    @GetMapping("/getNotificationContent")
    public ResponseEntity<String> getNotificationContent(@RequestParam String notificationType, @RequestParam String notificationSubject) {
        List<Notification> notifications = notificationRepoCall.findAll();
        System.out.println(notifications);
        for (Notification notification : notifications) {
        	System.out.println(notification.getNotificationSubject());
        	System.out.println(notification.getNotificationType());
        	System.out.println(notification.getNotificationContent());
            if (notification.getNotificationType().equals(notificationType) &&
                notification.getNotificationSubject().equals(notificationSubject)) {
            	System.out.println(notification.getNotificationContent());
                return ResponseEntity.ok(notification.getNotificationContent());
            }
        }

        return ResponseEntity.notFound().build();
    }
      

    private void sendNotificationEmail(String notificationType, String subject, String message, Notification notification) {
        Query query = new Query();
        query.addCriteria(Criteria.where("receiveNotifications").is(true)
                                 .and("notifications." + notificationType).is(true)
                                 .and("id").nin(notification.getUserList()));

        List<User> eligibleUsers = mongoTemplate.find(query, User.class);
        //System.out.println(eligibleUsers);
        String id = notification.getNotificationId();
        for (User user : eligibleUsers) {
            String email = user.getEmail();
            String topic = notificationType.replaceAll("\\s+", "-").toLowerCase() + "-topic";
            String info = email+"%"+id;
            kafkaproducer.sendNotificationMessage(topic,info);
            notification.getUserList().add(user.getId());
        }

        
        notificationRepoCall.save(notification);
    }

    }