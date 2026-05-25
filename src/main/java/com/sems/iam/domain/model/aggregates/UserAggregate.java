package com.sems.iam.domain.model.aggregates;

import com.sems.iam.domain.model.valueobjects.EmailAddress;
import com.sems.iam.domain.model.valueobjects.RoleName;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UserAggregate {
    private final UUID userId;
    private final EmailAddress emailAddress;
    private final String passwordHash;
    private final Set<RoleName> roles;
    private final Instant createdAt;
    private Instant updatedAt;

    public UserAggregate(UUID userId, EmailAddress emailAddress, String passwordHash, Set<RoleName> roles, Instant createdAt, Instant updatedAt) {
        this.userId = userId;
        this.emailAddress = emailAddress;
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void assignRole(RoleName roleName) {
        roles.add(roleName);
        updatedAt = Instant.now();
    }

    public UUID userId() { return userId; }
    public EmailAddress emailAddress() { return emailAddress; }
    public String passwordHash() { return passwordHash; }
    public Set<RoleName> roles() { return Set.copyOf(roles); }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
