package com.sems.iam.infrastructure.messaging.kafka.inbound;

import com.sems.iam.application.internal.commandservices.UserRoleCommandService;
import com.sems.iam.domain.model.commands.AssignRoleCommand;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaIamEventConsumer {
    private final UserRoleCommandService userRoleCommandService;

    @KafkaListener(topics = "${topics.iam-role-assignment-requested}")
    public void onRoleAssignmentRequested(Map<String, Object> event) {
        try {
            UUID userId = UUID.fromString(required(event, "userId"));
            String role = required(event, "role");
            userRoleCommandService.assignRole(new AssignRoleCommand(userId, role));
            log.info("Consumed IAM role assignment request for user {} and role {}", userId, role);
        } catch (RuntimeException ex) {
            log.error("Could not consume IAM role assignment request: {}", event, ex);
            throw ex;
        }
    }

    private String required(Map<String, Object> event, String key) {
        Object value = event.get(key);
        if (value == null || value.toString().isBlank()) {
            throw new IllegalArgumentException("Missing Kafka event field: " + key);
        }
        return value.toString();
    }
}
