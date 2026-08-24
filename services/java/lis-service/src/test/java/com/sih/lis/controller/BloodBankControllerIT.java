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
class BloodBankControllerIT {
    @Autowired MockMvc mvc;
    @Test void reservesCompatibleUnitThenValidatesAndCompletesTransfusion() throws Exception {
        mvc.perform(post("/api/v1/lis/blood-bank/units").contentType(MediaType.APPLICATION_JSON).content("{\"donationCode\":\"DON-O-001\",\"aboGroup\":\"O\",\"rhesus\":\"NEGATIVE\",\"component\":\"RED_CELLS\",\"collectedOn\":\"2026-08-01\",\"expiresOn\":\"2028-12-31\",\"storageLocation\":\"FRIDGE-A\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("AVAILABLE"));
        mvc.perform(post("/api/v1/lis/blood-bank/transfusions").contentType(MediaType.APPLICATION_JSON).content("{\"clinicalEncounterId\":20,\"patientRef\":\"MRN-TRANS-1\",\"recipientAboGroup\":\"A\",\"recipientRhesus\":\"NEGATIVE\",\"component\":\"RED_CELLS\",\"requestedBy\":\"Dr Urgences\"}")).andExpect(status().isCreated()).andExpect(jsonPath("$.donationCode").value("DON-O-001")).andExpect(jsonPath("$.status").value("REQUESTED"));
        mvc.perform(patch("/api/v1/lis/blood-bank/transfusions/1/crossmatch").contentType(MediaType.APPLICATION_JSON).content("{\"validatedBy\":\"Biologiste\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPATIBILITY_VALIDATED"));
        mvc.perform(patch("/api/v1/lis/blood-bank/transfusions/1/issue").contentType(MediaType.APPLICATION_JSON).content("{\"issuedBy\":\"Banque de sang\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("ISSUED"));
        mvc.perform(patch("/api/v1/lis/blood-bank/transfusions/1/complete").contentType(MediaType.APPLICATION_JSON).content("{\"completedBy\":\"Infirmier\"}")).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("COMPLETED"));
    }
}
