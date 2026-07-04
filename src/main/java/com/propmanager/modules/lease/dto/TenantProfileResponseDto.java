package com.propmanager.modules.lease.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantProfileResponseDto {

    private UUID          id;
    private UUID          tenantId;
    private String        fullName;
    private String        email;
    private String        phone;
    private String        identificationReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}