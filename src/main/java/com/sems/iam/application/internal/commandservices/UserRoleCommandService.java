package com.sems.iam.application.internal.commandservices;

import com.sems.iam.application.internal.outboundservices.IamEventPublisher;
import com.sems.iam.domain.model.commands.AssignRoleCommand;
import com.sems.iam.domain.model.valueobjects.RoleName;
import com.sems.iam.infrastructure.persistence.jpa.repositories.*;
import com.sems.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserRoleCommandService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final IamEventPublisher eventPublisher;

    @Transactional
    public void assignRole(AssignRoleCommand command) {
        var user = userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("User not found"));
        RoleName roleName = RoleName.from(command.role());
        var role = roleRepository.findByName(roleName.name()).orElseThrow(() -> new NotFoundException("Role not found"));
        UserRoleId id = new UserRoleId(user.getUserId(), role.getRoleId());
        if (!userRoleRepository.existsById(id)) {
            UserRoleJpaEntity join = new UserRoleJpaEntity();
            join.setId(id); join.setUser(user); join.setRole(role);
            userRoleRepository.save(join);
            eventPublisher.publishRoleAssigned(user.getUserId().toString(), roleName.name());
        }
    }
}
