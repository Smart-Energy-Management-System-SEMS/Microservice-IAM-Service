package com.sems.iam.application.internal.queryservice;

import com.sems.iam.infrastructure.persistence.jpa.repositories.*;
import com.sems.iam.interfaces.rest.resources.UserResource;
import com.sems.iam.shared.exception.NotFoundException;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public UserResource getById(UUID userId) {
        UserJpaEntity user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        return toResource(user);
    }

    public List<UserResource> getAll() { return userRepository.findAll().stream().map(this::toResource).toList(); }

    private UserResource toResource(UserJpaEntity user) {
        List<String> roles = userRoleRepository.findByUser_UserId(user.getUserId()).stream().map(ur -> ur.getRole().getName()).toList();
        return new UserResource(user.getUserId(), user.getEmailAddress(), roles);
    }
}
