package com.sems_iam.iam.domain.model.commands;

public record RegisterUserCommand(String emailAddress, String password, String role) {
}
