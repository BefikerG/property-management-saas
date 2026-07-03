package com.propmanager.modules.organization.dto;

import com.propmanager.modules.organization.entity.StaffRole;
import com.propmanager.modules.organization.entity.StaffStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Outbound representation of a StaffMember returned by the API.
 *
 * The passwordHash field is intentionally absent — it must never
 * appear in any API response under any circumstance.
 *
 * The tenantId is included so the client can confirm which organization
 * the staff member belongs to without needing a separate lookup.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffMemberResponseDto {

    private UUID          id;
    private UUID          tenantId;
    private String        email;
    private String        fullName;
    private StaffRole     role;
    private StaffStatus   status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}