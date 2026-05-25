package com.sems.iam.application.internal.commandservices;

import com.sems.iam.application.internal.outboundservices.IamEventPublisher;
import com.sems.iam.domain.model.aggregates.UserAggregate;
import com.sems.iam.domain.model.commands.*;
import com.sems.iam.domain.model.valueobjects.*;
import com.sems.iam.domain.services.*;
import com.sems.iam.infrastructure.persistence.jpa.repositories.*;
import com.sems.iam.interfaces.rest.resources.LoginResponse;
import com.sems.iam.shared.exception.*;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationCommandService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordHashingService passwordHashingService;
    private final TokenService tokenService;
    private final IamEventPublisher eventPublisher;

    @Transactional
    public LoginResponse register(RegisterUserCommand command) {
        EmailAddress email = new EmailAddress(command.emailAddress());
        if (userRepository.existsByEmailAddress(email.value())) throw new ConflictException("Email already exists");
        RoleName roleName = RoleName.from(command.role());
        var role = roleRepository.findByName(roleName.name()).orElseThrow(() -> new NotFoundException("Role not found"));
        UserJpaEntity user = new UserJpaEntity();
        user.setUserId(UUID.randomUUID()); user.setEmailAddress(email.value()); user.setPasswordHash(passwordHashingService.hash(command.password())); user.setCreatedAt(Instant.now()); user.setUpdatedAt(Instant.now());
        userRepository.save(user);
        UserRoleJpaEntity userRole = new UserRoleJpaEntity(); userRole.setId(new UserRoleId(user.getUserId(), role.getRoleId())); userRole.setUser(user); userRole.setRole(role); userRoleRepository.save(userRole);
        UserAggregate agg = new UserAggregate(user.getUserId(), email, user.getPasswordHash(), Set.of(roleName), user.getCreatedAt(), user.getUpdatedAt());
        String token = tokenService.generateToken(agg);
        eventPublisher.publishUserRegistered(user.getUserId().toString(), email.value(), roleName.name());
        return new LoginResponse(token, user.getUserId(), email.value(), List.of(roleName.name()));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginCommand command) {
        EmailAddress email = new EmailAddress(command.emailAddress());
        UserJpaEntity user = userRepository.findByEmailAddress(email.value()).orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!passwordHashingService.matches(command.password(), user.getPasswordHash())) throw new UnauthorizedException("Invalid credentials");
        List<String> roles = userRoleRepository.findByUser_UserId(user.getUserId()).stream().map(ur -> ur.getRole().getName()).toList();
        UserAggregate agg = new UserAggregate(user.getUserId(), email, user.getPasswordHash(), roles.stream().map(RoleName::from).collect(java.util.stream.Collectors.toSet()), user.getCreatedAt(), user.getUpdatedAt());
        String token = tokenService.generateToken(agg);
        eventPublisher.publishUserLoggedIn(user.getUserId().toString(), email.value());
        return new LoginResponse(token, user.getUserId(), email.value(), roles);
    }
}
