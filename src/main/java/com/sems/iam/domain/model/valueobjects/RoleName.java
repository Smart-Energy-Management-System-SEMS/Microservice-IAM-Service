package com.sems_iam.iam.domain.model.valueobjects;

public enum RoleName {
    ADMIN,
    RESIDENT;

    public static RoleName from(String raw) {
        return RoleName.valueOf(raw.trim().toUpperCase());
    }
}
