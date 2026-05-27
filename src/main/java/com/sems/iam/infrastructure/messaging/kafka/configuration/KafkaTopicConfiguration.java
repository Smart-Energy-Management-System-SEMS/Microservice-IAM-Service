package com.sems.iam.infrastructure.messaging.kafka.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
public class KafkaTopicConfiguration {
    private final KafkaTopicsProperties topics;

    @Bean
    public NewTopic iamUserRegisteredTopic() {
        return topic(topics.getIamUserRegistered());
    }

    @Bean
    public NewTopic iamUserLoggedInTopic() {
        return topic(topics.getIamUserLoggedIn());
    }

    @Bean
    public NewTopic iamRoleAssignedTopic() {
        return topic(topics.getIamRoleAssigned());
    }

    @Bean
    public NewTopic iamRoleAssignmentRequestedTopic() {
        return topic(topics.getIamRoleAssignmentRequested());
    }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
