package com.sems_iam.iam.domain.model.commands;

import java.util.UUID;

public record AssignRoleCommand(UUID userId, String role) {
}
