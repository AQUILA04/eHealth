package com.sih.ris.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=mock") @AutoConfigureMockMvc
class RadiologyStudyControllerIT {
    @Autowired MockMvc mvc;
    @Test void schedulesPerformsAndReportsStudy() throws Exception {
        mvc.perform(post("/api/v1/ris/studies").contentType(MediaType.APPLICATION_JSON).content("{\"clinicalEncounterId\":9,\"patientRef\":\"MRN-002\",\"procedureName\":\"Scanner thoracique\",\"modality\":\"CT\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("REQUESTED"));
        mvc.perform(patch("/api/v1/ris/studies/1/schedule").contentType(MediaType.APPLICATION_JSON).content("{\"scheduledAt\":\"2030-01-10T09:30:00\",\"assignedTechnologist\":\"M. Radio\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("SCHEDULED"));
        mvc.perform(patch("/api/v1/ris/studies/1/perform").contentType(MediaType.APPLICATION_JSON).content("{\"pacsStudyUid\":\"1.2.3.4\",\"radiationDoseMgy\":12.5}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
        mvc.perform(patch("/api/v1/ris/studies/1/report").contentType(MediaType.APPLICATION_JSON).content("{\"reportText\":\"Aucune anomalie aiguë.\",\"assignedRadiologist\":\"Dr Ray\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("REPORTED"));
    }
}
