package com.propmanager.modules.inventory.dto;

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
public class PropertyResponseDto {

    private UUID          id;
    private UUID          tenantId;
    private String        name;
    private String        address;
    private String        locationCity;
    private String        description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
