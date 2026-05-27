package com.sems.iam.infrastructure.messaging.kafka.outbound;

import com.sems.iam.application.internal.outboundservices.IamEventPublisher;
import com.sems.iam.infrastructure.messaging.kafka.configuration.KafkaTopicsProperties;
import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaIamEventPublisher implements IamEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    public void publishUserRegistered(String userId, String emailAddress, String role) {
        publish(topics.getIamUserRegistered(), userId, Map.of(
                "eventType", "iam.user.registered",
                "userId", userId,
                "emailAddress", emailAddress,
                "role", role,
                "occurredAt", Instant.now().toString()));
    }

    public void publishUserLoggedIn(String userId, String emailAddress) {
        publish(topics.getIamUserLoggedIn(), userId, Map.of(
                "eventType", "iam.user.logged-in",
                "userId", userId,
                "emailAddress", emailAddress,
                "occurredAt", Instant.now().toString()));
    }

    public void publishRoleAssigned(String userId, String role) {
        publish(topics.getIamRoleAssigned(), userId, Map.of(
                "eventType", "iam.role.assigned",
                "userId", userId,
                "role", role,
                "occurredAt", Instant.now().toString()));
    }

    private void publish(String topic, String key, Map<String, Object> payload) {
        kafkaTemplate.send(topic, key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Could not publish IAM event to topic {} with key {}", topic, key, ex);
                    } else {
                        log.info("Published IAM event to topic {} partition {} offset {}",
                                topic,
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
