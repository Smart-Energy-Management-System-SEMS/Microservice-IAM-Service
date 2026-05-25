package com.sems.iam.interfaces.rest.transform;

import com.sems.iam.domain.model.commands.*;
import com.sems.iam.interfaces.rest.resources.*;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CommandMapper {
    public RegisterUserCommand toCommand(RegisterRequest request) { return new RegisterUserCommand(request.emailAddress(), request.password(), request.role()); }
    public LoginCommand toCommand(LoginRequest request) { return new LoginCommand(request.emailAddress(), request.password()); }
    public AssignRoleCommand toCommand(UUID userId, AssignRoleRequest request) { return new AssignRoleCommand(userId, request.role()); }
}
