package com.example.demo.user;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.demo.EmailSender;
import com.example.demo.MyKafkaProducer;
import com.example.demo.repo.UserCopyRepository;
import com.example.demo.repo.UserRepository;

import java.util.Collections;
import java.util.Map;

@Controller
public class DeleteUserController {

    @Autowired
    private UserRepository userRepoCall;
    
    @Autowired
    private UserCopyRepository userCopyRepo;

    @Autowired
    private MyKafkaProducer kafkaproducer;

    @Autowired
    private TokenService tokenService;

    @DeleteMapping("/deleteUser")
    public ResponseEntity<Map<String, String>> deleteUser(@RequestBody Map<String, String> requestBody) {
        try {
            String email = requestBody.get("email");

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("message", "Email is missing or empty"));
            }

            User userToDelete = userRepoCall.findByEmail(email);
            

            if (userToDelete == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Collections.singletonMap("message", "User with email " + email + " not found"));
            }

            // Generate a confirmation token
            String confirmationToken = tokenService.generateToken(email);

            // Send a confirmation email to the user with the token

            String topic = "deleteConfirmation-topic";
            String info=email+"%"+
                    getConfirmationLink(confirmationToken);
            kafkaproducer.sendMessage(topic, info);
            

            return ResponseEntity.status(HttpStatus.OK)
                    .body(Collections.singletonMap("message", "Confirmation email sent to " + email));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("message", "An error occurred"));
        }
    }

    @GetMapping("/confirmDelete/{token}")
    public ResponseEntity<String> confirmDelete(@PathVariable String token) {
        if (tokenService.isValidToken(token)) {
            String email = tokenService.getEmailByToken(token);
            User userToDelete = userRepoCall.findByEmail(email);

            if (userToDelete != null) {
                // Create a copy of the user data to store in another database
                UserCopy userCopy = new UserCopy();
                userCopy.setId(userToDelete.getId());
                userCopy.setName(userToDelete.getName());
                userCopy.setEmail(userToDelete.getEmail());
                userCopy.setReceiveNotifications(userToDelete.isReceiveNotifications());
                userCopy.setNotifications(userToDelete.getNotifications());
                userCopy.setReceivedNotifications(userToDelete.getReceivedNotifications());

                // Save the copy of user data in another database (e.g., MongoDB)
                userCopyRepo.save(userCopy);
               
                // Send a notification to the user about account deletion
                String topic = "account-deleted-topic";

                kafkaproducer.sendMessage(topic,email);
                
                // Remove the confirmation token
                tokenService.removeToken(token);

                return ResponseEntity.status(HttpStatus.OK).body("Account deleted successfully");
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired confirmation link");
    }

    // Helper method to generate the confirmation link
    private String getConfirmationLink(String token) {
        // we should replace 'http://localhost:8080' with the actual base URL of your application
        return "http://localhost:8080/confirmDelete/" + token;
    }
}