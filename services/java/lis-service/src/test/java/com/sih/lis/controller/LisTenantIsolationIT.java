package com.sih.lis.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.profiles.active=mock")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class LisTenantIsolationIT {
    @Autowired MockMvc mvc;

    @Test
    void isolatesLaboratoryOrdersAcrossTenants() throws Exception {
        String tenantA = "hospital-lis-a";
        String tenantB = "hospital-lis-b";
        String patientRef = "MRN-LIS-TENANT-A";

        mvc.perform(post("/api/v1/lis/orders")
                .header("X-Tenant-ID", tenantA)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clinicalEncounterId\":71,\"patientRef\":\"" + patientRef + "\",\"examName\":\"Glycémie\",\"sampleType\":\"SANG\",\"orderedBy\":\"Dr Tenant A\"}"))
            .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/lis/orders").header("X-Tenant-ID", tenantB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isEmpty());

        mvc.perform(get("/api/v1/lis/orders").header("X-Tenant-ID", tenantA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].patientRef").value(patientRef));
    }
}
