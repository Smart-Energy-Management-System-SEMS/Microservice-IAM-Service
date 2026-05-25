package com.sems_iam.iam.domain.services;

import com.sems_iam.iam.domain.model.aggregates.UserAggregate;

public interface TokenService {
    String generateToken(UserAggregate userAggregate);
    String extractUserId(String token);
    boolean isTokenValid(String token);
}
