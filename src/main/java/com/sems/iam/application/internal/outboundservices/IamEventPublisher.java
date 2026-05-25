package com.sems.iam.application.internal.outboundservices;

public interface IamEventPublisher {
    void publishUserRegistered(String userId, String emailAddress, String role);
    void publishUserLoggedIn(String userId, String emailAddress);
    void publishRoleAssigned(String userId, String role);
}
