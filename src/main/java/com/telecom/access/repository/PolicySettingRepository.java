package com.telecom.access.repository;

import com.telecom.access.entity.PolicySetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicySettingRepository extends JpaRepository<PolicySetting, String> {
}
