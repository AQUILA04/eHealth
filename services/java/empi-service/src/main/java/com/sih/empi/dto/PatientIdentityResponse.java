package com.sih.empi.dto;

import com.sih.empi.entity.PatientIdentity.Gender;
import com.sih.empi.entity.PatientIdentity.SourceSystem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de réponse renvoyé par l'API EMPI.
 * Ne contient pas les champs techniques internes (id, etc.).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientIdentityResponse {

    private String globalUuid;
    private String mrn;
    private String nationalId;

    private String firstName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;

    private String phoneNumber;
    private String email;
    private String address;
    private String city;
    private String postalCode;
    private String nationality;

    private SourceSystem sourceSystem;
    private boolean duplicateSuspected;
    private double confidenceScore;
    private boolean active;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
