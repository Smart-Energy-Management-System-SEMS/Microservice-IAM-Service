package com.sems.iam.infrastructure.messaging.kafka.outbound;

import com.sems.iam.application.internal.outboundservices.IamEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false")
public class NoOpIamEventPublisher implements IamEventPublisher {
    @Override
    public void publishUserRegistered(String userId, String emailAddress, String role) {
        log.info("Kafka disabled; skipping iam.user.registered event for user {}", userId);
    }

    @Override
    public void publishUserLoggedIn(String userId, String emailAddress) {
        log.info("Kafka disabled; skipping iam.user.logged-in event for user {}", userId);
    }

    @Override
    public void publishRoleAssigned(String userId, String role) {
        log.info("Kafka disabled; skipping iam.role.assigned event for user {}", userId);
    }
}
