package com.example.demo;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EmailSender {
    // Create a thread pool with multiple threads to send emails concurrently.
    private static final ExecutorService executorService = Executors.newFixedThreadPool(50); // Adjust the thread pool size as needed

    public static void sendEmail(String recipient, String subject, String content) {
        executorService.submit(() -> {
            final String senderEmail = "notificationsystem03@gmail.com";
            final String senderPassword = "tgqfhjwyjwfhdmvn";
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.port", "587");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(senderEmail, senderPassword);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(senderEmail));
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
                message.setSubject(subject);
                message.setContent(content, "text/html");

               

                Transport.send(message);
            } catch (MessagingException e) {
                System.err.println("Error sending email: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    // Shutdown the executor service when the application exits.
    public static void shutdownExecutorService() {
        executorService.shutdown();
    }
}