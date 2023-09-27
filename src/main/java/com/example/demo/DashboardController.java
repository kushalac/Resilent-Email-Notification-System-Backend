package com.example.demo;

import com.example.demo.repo.NotificationRepository;
import com.example.demo.repo.UserRepository;
import com.example.demo.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;

    @GetMapping("/getStats")
    public DashboardStats getDashboardStats() {
        List<User> allUsers = userRepository.findAll();

        long activeTrueCount = allUsers.stream().filter(User::isActive).count();
        long activeFalseCount = allUsers.stream().filter(user -> !user.isActive()).count();
        long receiveNotificationsCount = allUsers.stream()
                .filter(user -> user.isActive() && user.isReceiveNotifications())
                .count();
        long promotionsCount = allUsers.stream()
                .filter(user -> user.isReceiveNotifications() &&  user.isActive() && Boolean.TRUE.equals(user.getNotifications().get("promotions")))
                .count();
        long latestPlansCount = allUsers.stream()
                .filter(user -> user.isReceiveNotifications() &&  user.isActive() && Boolean.TRUE.equals(user.getNotifications().get("latestPlans")))
                .count();
        long releaseEventsCount = allUsers.stream()
                .filter(user -> user.isReceiveNotifications()&&  user.isActive() && Boolean.TRUE.equals(user.getNotifications().get("releaseEvents")))
                .count();
        long notReceiveNotificationsCount = allUsers.stream().filter(user -> user.isActive() && !user.isReceiveNotifications()).count();
        
        //new code
        List<Notification> allNotifications = notificationRepository.findAll();
        
        long totalPromotionsCount = allNotifications.stream()
                .filter(notification -> "promotions".equals(notification.getNotificationType()))
                .count();

        long totalReleaseEventsCount = allNotifications.stream()
                .filter(notification -> "releaseEvents".equals(notification.getNotificationType()))
                .count();

        long totalLatestPlansCount = allNotifications.stream()
                .filter(notification -> "latestPlans".equals(notification.getNotificationType()))
                .count();

        long totalNotificationsCount = allNotifications.size();

        long totalPromotionsToUsersCount = 0;
        long totalReleaseEventsToUsersCount = 0;
        long totalLatestPlansToUsersCount = 0;


        for (Notification notification : allNotifications) {
            List<String> userList = notification.getUserList();

            if (userList != null && !userList.isEmpty()) {
                for (@SuppressWarnings("unused") String userId : userList) {
                    if ("promotions".equals(notification.getNotificationType())) {
                        totalPromotionsToUsersCount++;
                    } else if ("releaseEvents".equals(notification.getNotificationType())) {
                        totalReleaseEventsToUsersCount++;
                    } else if ("latestPlans".equals(notification.getNotificationType())) {
                        totalLatestPlansToUsersCount++;
                    }
                }
            }
        }
        long totalNotificationSent = totalPromotionsToUsersCount+totalReleaseEventsToUsersCount+totalLatestPlansToUsersCount;
    
        

        return new DashboardStats(activeTrueCount, activeFalseCount, receiveNotificationsCount,
                promotionsCount, latestPlansCount, releaseEventsCount, notReceiveNotificationsCount,
                totalPromotionsCount,totalReleaseEventsCount,totalLatestPlansCount,totalNotificationsCount,totalPromotionsToUsersCount,
                totalReleaseEventsToUsersCount,totalLatestPlansToUsersCount,totalNotificationSent);
        
    }
    @PostMapping("/getUsersByNotifications")
    public List<User> getUsersByNotifications(@RequestBody List<String> selectedNotifications) {
        // Filter users based on selectedNotifications
        return userRepository.findAll().stream()
                .filter(user -> user.isActive() && user.isReceiveNotifications() &&
                        selectedNotifications.stream().allMatch(notification ->
                                Boolean.TRUE.equals(user.getNotifications().get(notification))))
                .collect(Collectors.toList());
    }
    
    @PostMapping("/getNotificationsByTypeAndDate")
    public List<Map<String, Object>> getNotificationsByTypeAndDate(@RequestBody Map<String, Object> request) {

        String notificationType = (String) request.get("notificationType");
        String startDateStr = (String) request.get("startDate");
        String endDateStr = (String) request.get("endDate");

        // Check if startDateStr and endDateStr are not empty
        if (startDateStr == null || startDateStr.isEmpty() || endDateStr == null || endDateStr.isEmpty()) {
            // Handle the case where startDate or endDate is empty
            // You can return an error response or handle it according to your requirements
            return Collections.emptyList(); // For example, return an empty list
        }

        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate = LocalDate.parse(endDateStr);

        List<Map<String, Object>> result = new ArrayList<>();

        List<Notification> notifications = notificationRepository.findAll(); // Fetch all notifications

        for (Notification notification : notifications) {
            if (notificationType.equals(notification.getNotificationType())) {
                List<String> userList = notification.getUserList();

                for (String userId : userList) {
                    User user = userRepository.findById(userId).orElse(null);

                    if (user != null) {
                        Map<String, String> receivedNotifications = user.getReceivedNotifications();
                        for (Map.Entry<String, String> entry : receivedNotifications.entrySet()) {
                            String notificationId = entry.getKey();
                            String formattedTimestamp = entry.getValue();
                            LocalDate notificationDate = LocalDate.parse(formattedTimestamp.substring(0, 10));

                            // Compare the notificationId from the inner loop
                            // with the notificationId you came from in the outer loop
                            if (notificationId.equals(notification.getNotificationId())
                                    && notificationDate.isAfter(startDate)
                                    && notificationDate.isBefore(endDate.plusDays(1))) {
                                Map<String, Object> resultMap = Map.of(
                                        "userId", userId,
                                        "userName", user.getName(),
                                        "notificationId", notification.getNotificationId(),
                                        "notificationType", notification.getNotificationType(),
                                        "notificationSubject", notification.getNotificationSubject(),
                                        "notificationTimestamp", formattedTimestamp
                                );

                                result.add(resultMap);
                            }
                        }
                    }
                }
            }
        }
        System.out.println(result);

        return result;
    }

}