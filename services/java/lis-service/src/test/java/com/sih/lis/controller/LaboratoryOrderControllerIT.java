package com.sih.lis.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=mock") @AutoConfigureMockMvc
class LaboratoryOrderControllerIT {
    @Autowired MockMvc mvc;
    @Test void completesLaboratoryWorkflowAndRecordsCriticalNotification() throws Exception {
        String order = "{\"clinicalEncounterId\":12,\"patientRef\":\"MRN-001\",\"examName\":\"Potassium\",\"sampleType\":\"SANG\",\"priority\":\"URGENT\",\"orderedBy\":\"Dr Test\"}";
        mvc.perform(post("/api/v1/lis/orders").contentType(MediaType.APPLICATION_JSON).content(order)).andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("ORDERED"));
        mvc.perform(patch("/api/v1/lis/orders/1/collect").contentType(MediaType.APPLICATION_JSON).content("{\"collectedBy\":\"Inf Test\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COLLECTED"));
        mvc.perform(patch("/api/v1/lis/orders/1/receive").contentType(MediaType.APPLICATION_JSON).content("{\"receivedBy\":\"Tech Lab\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RECEIVED"));
        mvc.perform(post("/api/v1/lis/orders/1/results").contentType(MediaType.APPLICATION_JSON).content("{\"analyteName\":\"Potassium\",\"resultValue\":\"6.8\",\"unit\":\"mmol/L\",\"interpretation\":\"CRITICAL_HIGH\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.results[0].resultValue").value("6.8"));
        mvc.perform(patch("/api/v1/lis/orders/1/validate").contentType(MediaType.APPLICATION_JSON).content("{\"validatedBy\":\"Dr Bio\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("BIOLOGICALLY_VALIDATED"));
        mvc.perform(post("/api/v1/lis/orders/1/critical-notification").contentType(MediaType.APPLICATION_JSON).content("{\"notifiedTo\":\"Dr Test\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.criticalNotifiedTo").value("Dr Test"));
    }
}
