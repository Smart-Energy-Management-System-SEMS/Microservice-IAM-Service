package com.sems.iam.infrastructure.messaging.kafka.outbound;

import com.sems.iam.application.internal.outboundservices.IamEventPublisher;
import com.sems.iam.infrastructure.messaging.kafka.configuration.KafkaTopicsProperties;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaIamEventPublisher implements IamEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicsProperties topics;

    public void publishUserRegistered(String userId, String emailAddress, String role) {
        publish(userId, "iam.user.registered", Map.of(
                "userId", userId,
                "emailAddress", emailAddress,
                "role", role));
    }

    public void publishUserLoggedIn(String userId, String emailAddress) {
        publish(userId, "iam.user.logged-in", Map.of(
                "userId", userId,
                "emailAddress", emailAddress));
    }

    public void publishRoleAssigned(String userId, String role) {
        publish(userId, "iam.role.assigned", Map.of(
                "userId", userId,
                "role", role));
    }

    private void publish(String key, String eventType, Map<String, Object> data) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("eventType", eventType);
        payload.put("eventId", UUID.randomUUID().toString());
        payload.put("occurredAt", Instant.now().toString());
        payload.put("data", data);

        kafkaTemplate.send(topics.getIamEvents(), key, payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Could not publish IAM event {} to topic {} with key {}", eventType, topics.getIamEvents(), key, ex);
                    } else {
                        log.info("Published IAM event {} to topic {} partition {} offset {}",
                                eventType,
                                topics.getIamEvents(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
