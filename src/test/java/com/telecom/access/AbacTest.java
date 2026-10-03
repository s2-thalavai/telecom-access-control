package com.telecom.access;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Attribute-based checks: same role, different outcome depending on attributes. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(FixedClockConfig.class)
class AbacTest {

    @Autowired MockMvc mvc;

    static RequestPostProcessor as(String user) { return httpBasic(user, "Telecom@123"); }

    @Test
    void customerOwnershipPolicy() throws Exception {
        mvc.perform(get("/api/v1/subscribers/9876543210").with(as("alice"))).andExpect(status().isOk());
        mvc.perform(get("/api/v1/subscribers/9876543212").with(as("alice"))).andExpect(status().isForbidden());
    }

    @Test
    void circleBoundaryAndVipPolicies() throws Exception {
        mvc.perform(get("/api/v1/subscribers/9876543210").with(as("ravi"))).andExpect(status().isOk());       // TN
        mvc.perform(get("/api/v1/subscribers/9876543212").with(as("ravi"))).andExpect(status().isForbidden()); // KA
        mvc.perform(get("/api/v1/subscribers/9876543211").with(as("ravi"))).andExpect(status().isForbidden()); // VIP
        mvc.perform(get("/api/v1/subscribers/9876543211").with(as("sita"))).andExpect(status().isOk());       // supervisor
        mvc.perform(get("/api/v1/subscribers/9876543212").with(as("admin"))).andExpect(status().isOk());      // admin exempt
    }

    @Test
    void listIsFilteredRowByRow() throws Exception {
        mvc.perform(get("/api/v1/subscribers").with(as("ravi")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[*].msisdn", containsInAnyOrder("9876543210", "9876543213")));
        mvc.perform(get("/api/v1/subscribers").with(as("admin")))
           .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void refundLimitDependsOnAmountAndRole() throws Exception {
        mvc.perform(post("/api/v1/subscribers/9876543210/refund").with(as("sita"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":500}"))
           .andExpect(status().isOk());
        mvc.perform(post("/api/v1/subscribers/9876543210/refund").with(as("sita"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":5000}"))
           .andExpect(status().isForbidden());                         // > SUPERVISOR limit 1000
        mvc.perform(post("/api/v1/subscribers/9876543210/refund").with(as("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":5000}"))
           .andExpect(status().isOk());                                // ADMIN limit 10000
    }

    @Test
    void suspendedLineBlocksCommercialChanges() throws Exception {
        mvc.perform(patch("/api/v1/subscribers/9876543213/plan").with(as("sita"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"planCode\":\"UNL299\"}"))
           .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/subscribers/9876543213/refund").with(as("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":10}"))
           .andExpect(status().isForbidden());
    }

    @Test
    void contractorSupervisorCannotSeePiiOrExport() throws Exception {
        mvc.perform(get("/api/v1/subscribers/9876543212").with(as("kiran")).accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk());                                         // masked view is fine
        mvc.perform(get("/api/v1/subscribers/9876543212").with(as("kiran"))
                        .accept("application/vnd.telecom.subscriber-full+json"))
           .andExpect(status().isForbidden());                                  // READ_PII denied
        mvc.perform(get("/api/v1/reports/subscribers").with(as("kiran")).accept("text/csv")
                        .header("X-Channel", "INTERNAL"))
           .andExpect(status().isForbidden());                                  // EXPORT denied
    }

    @Test
    void suspendAllowedDuringBusinessHours() throws Exception {
        mvc.perform(post("/api/v1/subscribers/9876543213/suspend").with(as("sita")))
           .andExpect(status().isOk());                                         // fixed clock: 10:00 IST
    }

    @Test
    void unknownLineIsForbiddenNotNotFound() throws Exception {
        mvc.perform(get("/api/v1/subscribers/9000000000").with(as("ravi"))).andExpect(status().isForbidden());
    }
}
