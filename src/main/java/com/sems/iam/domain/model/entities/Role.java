package com.sems.iam.domain.model.entities;

import com.sems.iam.domain.model.valueobjects.RoleName;
import java.util.UUID;

public record Role(UUID roleId, RoleName name) {
}
