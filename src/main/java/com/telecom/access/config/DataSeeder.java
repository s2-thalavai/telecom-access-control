package com.telecom.access.config;

import com.telecom.access.entity.*;
import com.telecom.access.repository.*;
import com.telecom.access.security.rbac.DynamicRoleHierarchy;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Set;

/** Seeds roles (with hierarchy + permissions), users (with ABAC attributes), subscribers, policy settings. */
@Component
public class DataSeeder implements ApplicationRunner {

    private final RoleRepository roles;
    private final AppUserRepository users;
    private final SubscriberRepository subscribers;
    private final PolicySettingRepository settings;
    private final PasswordEncoder encoder;
    private final DynamicRoleHierarchy hierarchy;

    public DataSeeder(RoleRepository roles, AppUserRepository users, SubscriberRepository subscribers,
                      PolicySettingRepository settings, PasswordEncoder encoder, DynamicRoleHierarchy hierarchy) {
        this.roles = roles;
        this.users = users;
        this.subscribers = subscribers;
        this.settings = settings;
        this.encoder = encoder;
        this.hierarchy = hierarchy;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (roles.count() == 0) seed();
        hierarchy.refresh();
    }

    private void seed() {
        // ---- RBAC: roles, permissions, hierarchy (ADMIN > SUPERVISOR > AGENT) ----
        Role customer = roles.save(new Role("CUSTOMER", null,
                Set.of("subscriber:read", "plan:change", "recharge:create")));
        Role agent = roles.save(new Role("AGENT", null,
                Set.of("subscriber:read", "subscriber:list", "recharge:create")));
        Role supervisor = roles.save(new Role("SUPERVISOR", null,
                Set.of("plan:change", "recharge:refund", "subscriber:suspend", "report:export")));
        Role admin = roles.save(new Role("ADMIN", null, Set.of("role:manage", "user:manage")));
        agent.setParent(supervisor);         // SUPERVISOR inherits AGENT
        supervisor.setParent(admin);         // ADMIN inherits SUPERVISOR

        // ---- users with ABAC attributes ----
        String pwd = encoder.encode("Telecom@123");
        users.save(new AppUser("alice", pwd, "TN", EmploymentType.CUSTOMER, Set.of(customer)));
        users.save(new AppUser("ravi", pwd, "TN", EmploymentType.EMPLOYEE, Set.of(agent)));
        users.save(new AppUser("sita", pwd, "TN", EmploymentType.EMPLOYEE, Set.of(supervisor)));
        users.save(new AppUser("kiran", pwd, "KA", EmploymentType.CONTRACTOR, Set.of(supervisor)));
        users.save(new AppUser("admin", pwd, "ALL", EmploymentType.EMPLOYEE, Set.of(admin)));

        // ---- resources with ABAC attributes ----
        subscribers.save(new Subscriber("9876543210", "Alice Kumar", "alice@example.com", "12 Anna Salai, Chennai",
                "TN", false, "RETAIL", "UNL299", new BigDecimal("120.00"), "alice"));
        subscribers.save(new Subscriber("9876543211", "R. Venkat (CEO, Acme)", "ceo@acme.example.com", "Acme Tower, Chennai",
                "TN", true, "CORPORATE", "CORP999", new BigDecimal("5000.00"), null));
        subscribers.save(new Subscriber("9876543212", "Carol Rao", "carol@example.com", "MG Road, Bengaluru",
                "KA", false, "RETAIL", "POST599", new BigDecimal("0.00"), null));
        Subscriber suspended = new Subscriber("9876543213", "Dev Prakash", "dev@example.com", "T Nagar, Chennai",
                "TN", false, "RETAIL", "UNL719", new BigDecimal("45.50"), null);
        suspended.setStatus("SUSPENDED");
        subscribers.save(suspended);

        // ---- runtime-tunable policy parameters ----
        settings.save(new PolicySetting("refund.limit.SUPERVISOR", "1000"));
        settings.save(new PolicySetting("refund.limit.ADMIN", "10000"));
        settings.save(new PolicySetting("business-hours.start", "08:00"));
        settings.save(new PolicySetting("business-hours.end", "20:00"));
    }
}
