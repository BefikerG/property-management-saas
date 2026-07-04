package com.propmanager.modules.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Inbound payload for creating or updating a Property.
 *
 * tenant_id is intentionally absent — always sourced from
 * TenantContext, never from the request body (TRD §3.1).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyRequestDto {

    @NotBlank(message = "Property name must not be blank.")
    @Size(max = 255, message = "Property name must not exceed 255 characters.")
    private String name;

    @NotBlank(message = "Address must not be blank.")
    private String address;

    @NotBlank(message = "City must not be blank.")
    @Size(max = 100, message = "City must not exceed 100 characters.")
    private String locationCity;

    @Size(max = 2000, message = "Description must not exceed 2000 characters.")
    private String description;
}