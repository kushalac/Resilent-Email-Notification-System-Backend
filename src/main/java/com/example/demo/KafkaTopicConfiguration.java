package com.example.demo;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class KafkaTopicConfiguration {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Create a thread pool with a single thread to create Kafka topics asynchronously.
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Bean
    public NewTopic signupTopic() throws ExecutionException, InterruptedException {
        return createNewTopic("signup-topic");
    }

    @Bean
    public NewTopic promotionsTopic() throws ExecutionException, InterruptedException {
        return createNewTopic("promotions-topic");
    }

    @Bean
    public NewTopic latestPlansTopic() throws ExecutionException, InterruptedException {
        return createNewTopic("latestPlans-topic");
    }

    @Bean
    public NewTopic releaseEventsTopic() throws ExecutionException, InterruptedException {
        return createNewTopic("releaseEvents-topic");
    }

    @Bean
    public NewTopic accountDeletedTopic() throws ExecutionException, InterruptedException {
        return createNewTopic("account-deleted-topic");
    }

    @Bean
    public NewTopic accountModifiedTopic() throws ExecutionException, InterruptedException {
        return createNewTopic("account-modified-topic");
    }

    @Bean
    public NewTopic accountConfirmDeletedTopic() throws ExecutionException, InterruptedException {
        return createNewTopic("deleteConfirmation-topic");
    }

    private NewTopic createNewTopic(String topicName) throws ExecutionException, InterruptedException {
        CompletableFuture<NewTopic> future = CompletableFuture.supplyAsync(() -> {
            Properties adminConfig = new Properties();
            adminConfig.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

            try (AdminClient adminClient = AdminClient.create(adminConfig)) {
                // Check if the topic already exists
                if (!adminClient.listTopics().names().get().contains(topicName)) {
                    // If the topic doesn't exist, create it
                    NewTopic newTopic = new NewTopic(topicName, 1, (short) 1);
                    adminClient.createTopics(Collections.singleton(newTopic)).all().get();
                    return newTopic;
                } else {
                    // Topic already exists, print a message
                    System.out.println("Topic '" + topicName + "' already exists.");
                    return null;
                }
            } catch (InterruptedException | ExecutionException e) {
                // Handle any exceptions here
                e.printStackTrace();
                throw new RuntimeException("Failed to create or check topic: " + topicName, e);
            }
        }, executorService);

        // Wait for the CompletableFuture to complete and return the result.
        return future.get();
    }

    // Shutdown the executor service when the application exits.
    public static void shutdownExecutorService() {
        executorService.shutdown();
    }
}