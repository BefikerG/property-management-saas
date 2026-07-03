package com.propmanager.modules.inventory.dto;

import com.propmanager.modules.inventory.entity.StructureNodeType;
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
public class PropertyStructureResponseDto {

    private UUID              id;
    private UUID              tenantId;
    private UUID              propertyId;
    private UUID              parentId;
    private StructureNodeType nodeType;
    private String            name;
    private Short             ordinal;
    private LocalDateTime     createdAt;
}

