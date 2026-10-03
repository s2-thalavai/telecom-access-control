package com.telecom.access;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Roles, permissions, attributes and policy parameters changed at RUNTIME take effect immediately. */
@SpringBootTest
@AutoConfigureMockMvc
@Import(FixedClockConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DynamicAccessControlTest {

    @Autowired MockMvc mvc;

    static RequestPostProcessor as(String user) { return httpBasic(user, "Telecom@123"); }

    private void refund(String user, int amount, int expectedStatus) throws Exception {
        mvc.perform(post("/api/v1/subscribers/9876543210/refund").with(as(user))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\":" + amount + "}"))
           .andExpect(status().is(expectedStatus));
    }

    private void setting(String key, String value) throws Exception {
        mvc.perform(put("/api/v1/admin/policies/" + key).with(as("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"value\":\"" + value + "\"}"))
           .andExpect(status().isNoContent());
    }

    @Test
    void grantingPermissionAndRaisingLimitAtRuntime() throws Exception {
        refund("ravi", 100, 403);                                              // RBAC: no permission
        mvc.perform(put("/api/v1/admin/roles/AGENT/permissions/recharge:refund").with(as("admin")))
           .andExpect(status().isOk());
        try {
            refund("ravi", 100, 403);                                          // ABAC: AGENT limit is 0
            setting("refund.limit.AGENT", "200");
            refund("ravi", 100, 200);                                          // both satisfied
            refund("ravi", 300, 403);                                          // over the new limit
        } finally {
            mvc.perform(delete("/api/v1/admin/roles/AGENT/permissions/recharge:refund").with(as("admin")));
            setting("refund.limit.AGENT", "0");
        }
        refund("ravi", 100, 403);
    }

    @Test
    void changingUserAttributeChangesDecisions() throws Exception {
        mvc.perform(patch("/api/v1/admin/users/ravi/attributes").with(as("admin"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"circle\":\"KA\"}"))
           .andExpect(status().isNoContent());
        try {
            mvc.perform(get("/api/v1/subscribers/9876543212").with(as("ravi"))).andExpect(status().isOk());
            mvc.perform(get("/api/v1/subscribers/9876543210").with(as("ravi"))).andExpect(status().isForbidden());
        } finally {
            mvc.perform(patch("/api/v1/admin/users/ravi/attributes").with(as("admin"))
                    .contentType(MediaType.APPLICATION_JSON).content("{\"circle\":\"TN\"}"));
        }
    }

    @Test
    void businessHoursAreConfigurable() throws Exception {
        setting("business-hours.start", "11:00");
        try {
            mvc.perform(post("/api/v1/subscribers/9876543213/suspend").with(as("sita")))
               .andExpect(status().isForbidden());                             // 10:00 < 11:00
            mvc.perform(post("/api/v1/subscribers/9876543213/suspend").with(as("admin")))
               .andExpect(status().isOk());                                    // admins exempt
        } finally {
            setting("business-hours.start", "08:00");
        }
    }

    @Test
    void hierarchyCyclesAreRejected() throws Exception {
        mvc.perform(put("/api/v1/admin/roles/ADMIN/parent/AGENT").with(as("admin")))
           .andExpect(status().isBadRequest());
    }
}
