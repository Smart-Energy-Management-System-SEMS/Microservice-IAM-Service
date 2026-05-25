package com.sems_iam.iam.domain.model.entities;

import com.sems_iam.iam.domain.model.valueobjects.RoleName;
import java.util.UUID;

public record Role(UUID roleId, RoleName name) {
}
