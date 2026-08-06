package com.sih.tenant.dto;

import com.sih.tenant.entity.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantRequest {

    @NotBlank(message = "L'identifiant du tenant ne peut pas être vide")
    @Pattern(regexp = "^[a-z0-9\\-]+$", message = "L'identifiant ne doit contenir que des lettres minuscules, chiffres et tirets")
    private String id;

    @NotBlank(message = "Le nom ne peut pas être vide")
    private String name;

    private String domain;

    private TenantStatus status;

    private String contactEmail;

    private String contactPhone;
}
