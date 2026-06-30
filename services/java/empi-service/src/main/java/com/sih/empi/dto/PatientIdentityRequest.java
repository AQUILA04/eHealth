package com.sih.empi.dto;

import com.sih.empi.entity.PatientIdentity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de création / mise à jour d'une identité patient dans l'EMPI.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientIdentityRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom de famille est obligatoire")
    private String lastName;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateOfBirth;

    @NotNull(message = "Le genre est obligatoire")
    private Gender gender;

    private String nationalId;
    private String phoneNumber;
    private String email;
    private String address;
    private String city;
    private String postalCode;
    private String nationality;
}
