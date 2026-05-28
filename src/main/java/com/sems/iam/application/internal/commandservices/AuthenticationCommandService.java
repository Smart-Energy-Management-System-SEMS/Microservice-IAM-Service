package com.sems.iam.application.internal.commandservices;

import com.sems.iam.application.internal.outboundservices.IamEventPublisher;
import com.sems.iam.domain.model.aggregates.UserAggregate;
import com.sems.iam.domain.model.commands.*;
import com.sems.iam.domain.model.exceptions.*;
import com.sems.iam.domain.model.valueobjects.*;
import com.sems.iam.domain.services.*;
import com.sems.iam.infrastructure.oauth.google.GoogleTokenVerifier;
import com.sems.iam.infrastructure.oauth.google.GoogleOAuthClient;
import com.sems.iam.infrastructure.persistence.jpa.repositories.*;
import com.sems.iam.interfaces.rest.resources.LoginResponse;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    private final GoogleTokenVerifier googleTokenVerifier;
    private final GoogleOAuthClient googleOAuthClient;

    @Value("${security.oauth2.google.client-id:}")
    private String googleClientId;

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

    @Transactional
    public LoginResponse loginWithGoogle(String idToken) {
        Map<String, Object> claims = googleTokenVerifier.verify(idToken);
        String audience = String.valueOf(claims.getOrDefault("aud", ""));
        String email = String.valueOf(claims.getOrDefault("email", ""));
        String emailVerified = String.valueOf(claims.getOrDefault("email_verified", "false"));

        if (googleClientId.isBlank()) throw new IllegalArgumentException("Google OAuth is not configured");
        if (!googleClientId.equals(audience)) throw new UnauthorizedException("Google token audience is invalid");
        if (email.isBlank()) throw new UnauthorizedException("Google token does not include email");
        if (!"true".equalsIgnoreCase(emailVerified)) throw new UnauthorizedException("Google email is not verified");

        var existing = userRepository.findByEmailAddress(email);
        if (existing.isPresent()) {
            UserJpaEntity user = existing.get();
            List<String> roles = userRoleRepository.findByUser_UserId(user.getUserId()).stream().map(ur -> ur.getRole().getName()).toList();
            UserAggregate agg = new UserAggregate(
                    user.getUserId(),
                    new EmailAddress(email),
                    user.getPasswordHash(),
                    roles.stream().map(RoleName::from).collect(java.util.stream.Collectors.toSet()),
                    user.getCreatedAt(),
                    user.getUpdatedAt());
            String token = tokenService.generateToken(agg);
            eventPublisher.publishUserLoggedIn(user.getUserId().toString(), email);
            return new LoginResponse(token, user.getUserId(), email, roles);
        }

        RoleName defaultRole = RoleName.RESIDENT;
        var role = roleRepository.findByName(defaultRole.name()).orElseThrow(() -> new NotFoundException("Role not found"));
        UserJpaEntity user = new UserJpaEntity();
        user.setUserId(UUID.randomUUID());
        user.setEmailAddress(email);
        user.setPasswordHash(passwordHashingService.hash(UUID.randomUUID().toString()));
        user.setCreatedAt(Instant.now());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

        UserRoleJpaEntity userRole = new UserRoleJpaEntity();
        userRole.setId(new UserRoleId(user.getUserId(), role.getRoleId()));
        userRole.setUser(user);
        userRole.setRole(role);
        userRoleRepository.save(userRole);

        UserAggregate agg = new UserAggregate(
                user.getUserId(),
                new EmailAddress(email),
                user.getPasswordHash(),
                Set.of(defaultRole),
                user.getCreatedAt(),
                user.getUpdatedAt());
        String token = tokenService.generateToken(agg);
        eventPublisher.publishUserRegistered(user.getUserId().toString(), email, defaultRole.name());
        eventPublisher.publishUserLoggedIn(user.getUserId().toString(), email);
        return new LoginResponse(token, user.getUserId(), email, List.of(defaultRole.name()));
    }

    @Transactional
    public LoginResponse loginWithGoogleAuthorizationCode(String code) {
        String idToken = googleOAuthClient.exchangeCodeForIdToken(code);
        if (idToken.isBlank()) throw new UnauthorizedException("Google OAuth token exchange failed");
        return loginWithGoogle(idToken);
    }
}
