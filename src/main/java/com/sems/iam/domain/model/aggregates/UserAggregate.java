package com.sems.iam.domain.model.aggregates;

import com.sems.iam.domain.model.valueobjects.EmailAddress;
import com.sems.iam.domain.model.valueobjects.RoleName;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
/**
 * UserAggregate - Agregado de dominio que representa un usuario en el sistema IAM.
 * 
 * Esta clase encapsula toda la información y el comportamiento relacionado con un usuario,
 * incluyendo su identidad, credenciales, roles asignados y auditoría de cambios.
 * 
 * Patrón: Domain-Driven Design - Aggregate Root
 * Responsabilidades:
 * - Mantener la integridad de los datos del usuario
 * - Gestionar la asignación de roles
 * - Registrar cambios con timestamps
 * 
 * @author SEMS Team
 * @version 1.0
 */
public class UserAggregate {
    private final UUID userId;
    private final EmailAddress emailAddress;
    private final String passwordHash;
    private final Set<RoleName> roles;
    private final Instant createdAt;
    private Instant updatedAt;

    /**
     * Constructor de UserAggregate.
     * 
     * @param userId Identificador único del usuario
     * @param emailAddress Dirección de correo electrónico del usuario
     * @param passwordHash Hash de la contraseña del usuario
     * @param roles Conjunto inicial de roles del usuario
     * @param createdAt Timestamp de creación
     * @param updatedAt Timestamp de última actualización
     */
    public UserAggregate(UUID userId, EmailAddress emailAddress, String passwordHash, Set<RoleName> roles, Instant createdAt, Instant updatedAt) {
        this.userId = userId;
        this.emailAddress = emailAddress;
        this.passwordHash = passwordHash;
        this.roles = new HashSet<>(roles);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    /**
     * Asigna un nuevo rol al usuario y registra la actualización.
     * 
     * Este método implementa la lógica de negocio para añadir un rol, garantizando
     * que se actualice el timestamp de modificación.
     * 
     * @param roleName El rol a asignar al usuario
     */
    public void assignRole(RoleName roleName) {
        roles.add(roleName);
        updatedAt = Instant.now();
    }

    public UUID userId() { return userId; }
    public EmailAddress emailAddress() { return emailAddress; }
    public String passwordHash() { return passwordHash; }
    public Set<RoleName> roles() { return Set.copyOf(roles); }
    public Instant createdAt() { return createdAt; }
    public Instant updatedAt() { return updatedAt; }
}
