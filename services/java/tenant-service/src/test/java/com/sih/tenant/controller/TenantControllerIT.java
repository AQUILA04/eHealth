package com.sih.tenant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sih.tenant.dto.TenantRequest;
import com.sih.tenant.entity.TenantStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
@DisplayName("Tenant — TenantController (Tests d'intégration)")
class TenantControllerIT {

    private static final String BASE_URL = "/api/v1/tenants";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST — Enregistrement d'un tenant → 201 Created")
    void createTenant_validRequest_returns201() throws Exception {
        TenantRequest request = TenantRequest.builder()
                .id("hospital-test")
                .name("Hôpital de Test")
                .domain("test.ehealth.saas")
                .status(TenantStatus.ACTIVE)
                .contactEmail("admin@test.ehealth.saas")
                .contactPhone("+241 01 02 03 04")
                .build();

        mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status", is("CREATED")))
            .andExpect(jsonPath("$.statusCode", is(201)))
            .andExpect(jsonPath("$.service", is("TENANT-SERVICE")))
            .andExpect(jsonPath("$.data.id", is("hospital-test")))
            .andExpect(jsonPath("$.data.name", is("Hôpital de Test")));
    }

    @Test
    @DisplayName("GET — Liste de tous les tenants")
    void findAll_returnsTenants() throws Exception {
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status", is("OK")))
            .andExpect(jsonPath("$.statusCode", is(200)))
            .andExpect(jsonPath("$.data").isArray());
    }
}
