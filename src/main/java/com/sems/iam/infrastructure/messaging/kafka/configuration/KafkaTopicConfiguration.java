package com.sems.iam.infrastructure.messaging.kafka.configuration;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.kafka.admin.auto-create", havingValue = "true")
public class KafkaTopicConfiguration {
    private final KafkaTopicsProperties topics;

    @Bean
    public NewTopic iamEventsTopic() {
        return topic(topics.getIamEvents());
    }

    private NewTopic topic(String name) {
        return TopicBuilder.name(name)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
