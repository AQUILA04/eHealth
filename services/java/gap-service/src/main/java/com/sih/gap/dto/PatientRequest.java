package com.sih.gap.dto;

import com.sih.gap.entity.Patient.FinancialCoverage;
import com.sih.gap.entity.Patient.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom de famille est obligatoire")
    private String lastName;

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateOfBirth;

    @NotNull(message = "Le genre est obligatoire")
    private Gender gender;

    /** UUID global de l'EMPI (optionnel — fourni si le patient existe déjà dans l'EMPI). */
    private String empiGlobalUuid;

    private String phoneNumber;
    private String email;
    private String address;
    private String city;
    private String nationality;

    private FinancialCoverage financialCoverage;
    private String insurancePolicyNumber;
    private String insuranceCompany;
    private String emergencyContactName;
    private String emergencyContactPhone;
}
