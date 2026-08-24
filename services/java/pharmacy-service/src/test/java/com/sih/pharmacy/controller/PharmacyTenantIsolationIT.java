package com.sih.pharmacy.controller;

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
class PharmacyTenantIsolationIT {
    @Autowired MockMvc mvc;

    @Test
    void isolatesProductCatalogAndAllowsSameSkuAcrossTenants() throws Exception {
        String tenantA = "hospital-pharmacy-a";
        String tenantB = "hospital-pharmacy-b";
        String sku = "TENANT-SKU-500";

        String payload = "{\"sku\":\"" + sku + "\",\"name\":\"Produit tenant\",\"unit\":\"boîte\",\"minimumStock\":5}";
        mvc.perform(post("/api/v1/pharmacy/products").header("X-Tenant-ID", tenantA).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/v1/pharmacy/products").header("X-Tenant-ID", tenantB).contentType(MediaType.APPLICATION_JSON).content(payload))
            .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/pharmacy/products").header("X-Tenant-ID", tenantA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].sku").value(sku));
        mvc.perform(get("/api/v1/pharmacy/products").header("X-Tenant-ID", tenantB))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].sku").value(sku));
    }
}
