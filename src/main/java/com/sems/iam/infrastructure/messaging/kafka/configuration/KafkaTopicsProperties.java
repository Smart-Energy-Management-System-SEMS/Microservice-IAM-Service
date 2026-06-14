package com.sems.iam.infrastructure.messaging.kafka.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "topics")
public class KafkaTopicsProperties {
    private String iamEvents;
}
