package com.propmanager.modules.organization.entity;

import com.propmanager.core.tenant.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA entity mapping to public.staff_members.
 *
 * This is the first Tenant-Scoped entity in the platform — it extends
 * TenantEntity which provides:
 *   - the tenant_id column (FK → organizations.id)
 *   - the @FilterDef / @Filter Hibernate annotations that the
 *     TenantFilterAspect binds before every repository call
 *
 * StaffMember is also the Spring Security principal for the platform.
 * StaffMemberDetailsService wraps it into a UserDetails object that
 * the JwtAuthenticationFilter uses to populate the SecurityContext.
 *
 * Design decisions:
 *   - @SuperBuilder: required when extending a @MappedSuperclass that
 *     uses @SuperBuilder. Regular @Builder does not propagate
 *     superclass fields (tenantId) into the generated builder chain.
 *   - Unique constraint on (tenant_id, email): the same email address
 *     may legitimately exist in two different organizations (e.g. a
 *     consultant working for multiple firms). Uniqueness is scoped to
 *     the tenant boundary, not the global database.
 *   - password_hash column: never mapped as a plain String field that
 *     might appear in toString() or logs. @Column(name="password_hash")
 *     is the only place this value lives — it is never returned in any
 *     DTO response.
 */
@Entity
@Table(
    name   = "staff_members",
    schema = "public",
    uniqueConstraints = {
        @UniqueConstraint(
            name        = "uq_staff_members_tenant_email",
            columnNames = {"tenant_id", "email"}
        )
    }
)
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StaffMember extends TenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    /**
     * BCrypt-hashed password. Never returned in any DTO.
     * Set exclusively by StaffMemberServiceImpl.createStaffMember()
     * after running the plain-text password through PasswordEncoder.
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 30)
    @Builder.Default
    private StaffRole role = StaffRole.PROPERTY_MANAGER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private StaffStatus status = StaffStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
