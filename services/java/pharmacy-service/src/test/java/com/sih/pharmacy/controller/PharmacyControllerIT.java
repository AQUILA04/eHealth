package com.sih.pharmacy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = "spring.profiles.active=mock") @AutoConfigureMockMvc
class PharmacyControllerIT {
    @Autowired MockMvc mvc;
    @Test void receivesFefoStockAndDispensesValidatedPrescription() throws Exception {
        mvc.perform(post("/api/v1/pharmacy/products").contentType(MediaType.APPLICATION_JSON).content("{\"sku\":\"PARA-500\",\"name\":\"Paracétamol 500 mg\",\"unit\":\"comprimé\",\"minimumStock\":10}")).andExpect(status().isCreated()).andExpect(jsonPath("$.quantityOnHand").value(0));
        mvc.perform(post("/api/v1/pharmacy/inventory/receipts").contentType(MediaType.APPLICATION_JSON).content("{\"productId\":1,\"lotNumber\":\"LOT-2028\",\"quantity\":50,\"expiryDate\":\"2028-12-31\",\"storageLocation\":\"PHARMA-A1\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.lotNumber").value("LOT-2028"));
        mvc.perform(post("/api/v1/pharmacy/dispensations").contentType(MediaType.APPLICATION_JSON).content("{\"clinicalEncounterId\":11,\"patientRef\":\"MRN-003\",\"productId\":1,\"quantity\":4,\"pharmacist\":\"Ph. Test\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("VALIDATED"));
        mvc.perform(patch("/api/v1/pharmacy/dispensations/1/dispense").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("DISPENSED")).andExpect(jsonPath("$.lotNumber").value("LOT-2028"));
    }
}
