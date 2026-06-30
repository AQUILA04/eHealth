package com.sih.dpi.dto;

import com.sih.dpi.entity.LabOrder.ResultInterpretation;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour la saisie des résultats d'un examen complémentaire.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabResultRequest {

    @NotBlank(message = "Le résultat est obligatoire")
    private String result;

    private String resultUnit;
    private String referenceRange;
    private ResultInterpretation interpretation;
    private LocalDateTime resultDate;
    private String resultComment;
}
