package com.propmanager.modules.organization.dto;

import com.propmanager.modules.organization.entity.OrgStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Outbound representation of an Organization returned by the API.
 *
 * All fields are read-only from the client's perspective. The id is the UUID
 * that will be embedded in JWT tokens as the org_id claim once the security
 * layer (core/security/) is wired in Milestone 2's subsequent steps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponseDto {

    private UUID id;
    private String name;
    private OrgStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}