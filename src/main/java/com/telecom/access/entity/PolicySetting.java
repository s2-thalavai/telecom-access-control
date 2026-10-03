package com.telecom.access.entity;

import jakarta.persistence.*;

/** Runtime-tunable policy parameters (refund limits, business hours...). */
@Entity
@Table(name = "policy_settings")
public class PolicySetting {

    @Id
    @Column(name = "setting_key", length = 60)
    private String key;

    @Column(name = "setting_value", nullable = false, length = 100)
    private String value;

    protected PolicySetting() { }

    public PolicySetting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}
