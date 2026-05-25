package com.sems.iam.infrastructure.authorization.sfs.services;

import com.sems.iam.application.internal.outboundservices.IamEventPublisher;
import com.sems.iam.infrastructure.authorization.sfs.configuration.KafkaTopicsProperties;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaIamEventPublisher implements IamEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;
    public void publishUserRegistered(String userId, String emailAddress, String role) { kafkaTemplate.send(topics.getIamUserRegistered(), userId, Map.of("userId", userId, "emailAddress", emailAddress, "role", role, "occurredAt", Instant.now().toString())); }
    public void publishUserLoggedIn(String userId, String emailAddress) { kafkaTemplate.send(topics.getIamUserLoggedIn(), userId, Map.of("userId", userId, "emailAddress", emailAddress, "occurredAt", Instant.now().toString())); }
    public void publishRoleAssigned(String userId, String role) { kafkaTemplate.send(topics.getIamRoleAssigned(), userId, Map.of("userId", userId, "role", role, "occurredAt", Instant.now().toString())); }
}
