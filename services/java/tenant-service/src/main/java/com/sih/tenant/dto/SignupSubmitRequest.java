package com.sih.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SignupSubmitRequest {
    @NotBlank
    private String organizationName;

    @NotBlank
    @Email
    private String adminEmail;

    @NotBlank
    private String adminFirstName;

    @NotBlank
    private String adminLastName;

    private String adminPhone;

    @NotBlank
    private String planId;

    private String subdomain;
}
