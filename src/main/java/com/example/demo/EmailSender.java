package com.example.demo;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import java.io.File;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

                // Create the message body part for the email content
                MimeBodyPart messageBodyPart = new MimeBodyPart();
                messageBodyPart.setContent(content, "text/html");

                // Check if attachmentFilePath is not null and the subject is "Welcome to our service"
                if ("Welcome to our service".equals(subject)) {
                    String attachmentFilePath = "src/main/resources/Purpose & Values.pdf";
                    // Create the message body part for the attachment
                    MimeBodyPart attachmentBodyPart = new MimeBodyPart();
                    DataSource source = new FileDataSource(attachmentFilePath);
                    attachmentBodyPart.setDataHandler(new DataHandler(source));
                    attachmentBodyPart.setFileName(new File(attachmentFilePath).getName());

                    // Create a multipart message to combine the content and attachment
                    Multipart multipart = new MimeMultipart();
                    multipart.addBodyPart(messageBodyPart);
                    multipart.addBodyPart(attachmentBodyPart);

                    // Set the message content to the multipart message
                    message.setContent(multipart);
                } else {
                    // Set the message content for subjects other than "Welcome to our service"
                    message.setContent(content, "text/html");
                }

                // Send the email
                Transport.send(message);

                System.out.println("Email sent successfully to " + recipient);
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