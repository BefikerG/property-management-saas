package com.propmanager.modules.inventory.dto;

import com.propmanager.modules.inventory.entity.StructureNodeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyStructureRequestDto {

    @NotNull(message = "Node type must not be null.")
    private StructureNodeType nodeType;

    @NotBlank(message = "Structure name must not be blank.")
    @Size(max = 100, message = "Structure name must not exceed 100 characters.")
    private String name;

    /**
     * Optional parent structure UUID. Null means top-level.
     */
    private UUID parentId;

    private Short ordinal;
}
