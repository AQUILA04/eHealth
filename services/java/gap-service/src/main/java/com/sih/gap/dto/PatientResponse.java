package com.sih.gap.dto;

import com.sih.gap.entity.Patient.FinancialCoverage;
import com.sih.gap.entity.Patient.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {

    private Long id;
    private String localMrn;
    private String empiGlobalUuid;

    private String firstName;
    private String lastName;
    private String fullName;
    private LocalDate dateOfBirth;
    private Gender gender;

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

    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
