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

/** Role-based checks: permissions, hierarchy, URL rules, media types. Real users from the DB. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(FixedClockConfig.class)
class RbacTest {

    @Autowired MockMvc mvc;

    static RequestPostProcessor as(String user) { return httpBasic(user, "Telecom@123"); }

    @Test
    void anonymousIsRejected() throws Exception {
        mvc.perform(get("/api/v1/subscribers/9876543210")).andExpect(status().isUnauthorized());
    }

    @Test
    void hierarchyGivesSupervisorTheAgentRoleAndItsPermissions() throws Exception {
        mvc.perform(get("/api/v1/me").with(as("sita")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.roles", containsInAnyOrder("SUPERVISOR", "AGENT")))
           .andExpect(jsonPath("$.authorities", hasItems(
                   "ROLE_SUPERVISOR", "ROLE_AGENT", "recharge:refund", "recharge:create", "subscriber:read")));
        mvc.perform(get("/api/v1/me").with(as("admin")))
           .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMIN", "SUPERVISOR", "AGENT")));
    }

    @Test
    void missingPermissionIsForbidden() throws Exception {
        mvc.perform(post("/api/v1/subscribers/9876543210/refund").with(as("ravi"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":50}"))
           .andExpect(status().isForbidden());                         // agent lacks recharge:refund
        mvc.perform(get("/api/v1/subscribers").with(as("alice")))
           .andExpect(status().isForbidden());                         // customer lacks subscriber:list
    }

    @Test
    void inheritedPermissionIsGranted() throws Exception {
        mvc.perform(post("/api/v1/subscribers/9876543210/recharge").with(as("sita"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":100}"))
           .andExpect(status().isOk());                                // recharge:create inherited from AGENT
    }

    @Test
    void adminEndpointsRequireAdminRole() throws Exception {
        mvc.perform(get("/api/v1/admin/roles").with(as("sita"))).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/roles").with(as("admin")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.name=='AGENT')].inheritedBy", contains("SUPERVISOR")));
    }

    @Test
    void adminCannotEscalateOwnRoles() throws Exception {
        mvc.perform(put("/api/v1/admin/users/admin/roles").with(as("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"roles\":[\"ADMIN\",\"CUSTOMER\"]}"))
           .andExpect(status().isForbidden());
    }

    // ---------- content negotiation ----------
    @Test
    void fullPiiRepresentationNeedsSupervisor() throws Exception {
        mvc.perform(get("/api/v1/subscribers/9876543210").with(as("ravi")).accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.email").value("a***@example.com"));           // masked
        mvc.perform(get("/api/v1/subscribers/9876543210").with(as("ravi"))
                        .accept("application/vnd.telecom.subscriber-full+json"))
           .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/subscribers/9876543212").with(as("admin"))
                        .accept("application/vnd.telecom.subscriber-full+json"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.email").value("carol@example.com"))
           .andExpect(jsonPath("$.address").exists());
    }

    @Test
    void reportJsonVersusCsvHaveDifferentRules() throws Exception {
        mvc.perform(get("/api/v1/reports/subscribers").with(as("ravi")).accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/reports/subscribers").with(as("sita")).accept(MediaType.APPLICATION_JSON))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.ACTIVE").exists());
        // CSV = export: needs report:export AND INTERNAL channel (ABAC)
        mvc.perform(get("/api/v1/reports/subscribers").with(as("sita")).accept("text/csv"))
           .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/reports/subscribers").with(as("sita")).accept("text/csv")
                        .header("X-Channel", "INTERNAL"))
           .andExpect(status().isOk())
           .andExpect(content().contentTypeCompatibleWith("text/csv"))
           .andExpect(content().string(startsWith("msisdn,circle,status")))
           .andExpect(content().string(not(containsString(",KA,"))));            // own circle only
    }
}
