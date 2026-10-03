package com.telecom.access.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/** The protected resource. Its circle, status, vip flag and owner are ABAC resource attributes. */
@Entity
@Table(name = "subscribers")
public class Subscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 15)
    private String msisdn;

    @Column(nullable = false)
    private String fullName;

    private String email;
    private String address;

    @Column(nullable = false, length = 5)
    private String circle;

    @Column(nullable = false, length = 15)
    private String status = "ACTIVE";

    private boolean vip;

    @Column(length = 15)
    private String accountType;          // RETAIL / CORPORATE

    @Column(nullable = false, length = 30)
    private String planCode;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    /** Username of the customer who owns this line (null for corporate lines). */
    @Column(length = 50)
    private String ownerUsername;

    @Version
    private long version;

    protected Subscriber() { }

    public Subscriber(String msisdn, String fullName, String email, String address, String circle,
                      boolean vip, String accountType, String planCode, BigDecimal balance, String ownerUsername) {
        this.msisdn = msisdn;
        this.fullName = fullName;
        this.email = email;
        this.address = address;
        this.circle = circle;
        this.vip = vip;
        this.accountType = accountType;
        this.planCode = planCode;
        this.balance = balance;
        this.ownerUsername = ownerUsername;
    }

    public Long getId() { return id; }
    public String getMsisdn() { return msisdn; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getCircle() { return circle; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean isVip() { return vip; }
    public String getAccountType() { return accountType; }
    public String getPlanCode() { return planCode; }
    public void setPlanCode(String planCode) { this.planCode = planCode; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public String getOwnerUsername() { return ownerUsername; }
}
