package com.sems_iam.iam.domain.services;

public interface PasswordHashingService {
    String hash(String rawPassword);
    boolean matches(String rawPassword, String hashedPassword);
}
