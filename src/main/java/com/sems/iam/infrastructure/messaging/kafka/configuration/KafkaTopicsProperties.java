package com.sems.iam.infrastructure.authorization.sfs.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "topics")
public class KafkaTopicsProperties {
    private String iamUserRegistered;
    private String iamUserLoggedIn;
    private String iamRoleAssigned;
}
