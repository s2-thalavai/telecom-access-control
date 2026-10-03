package com.telecom.access.security.abac;

import com.telecom.access.entity.PolicySetting;
import com.telecom.access.repository.PolicySettingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Optional;

/** Policy parameters read from the DB on each evaluation, so changes apply at runtime. */
@Service
public class PolicySettingsService {

    private final PolicySettingRepository repository;

    public PolicySettingsService(PolicySettingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Optional<String> get(String key) {
        return repository.findById(key).map(PolicySetting::getValue);
    }

    public BigDecimal decimal(String key, BigDecimal defaultValue) {
        return get(key).map(BigDecimal::new).orElse(defaultValue);
    }

    public LocalTime time(String key, LocalTime defaultValue) {
        return get(key).map(LocalTime::parse).orElse(defaultValue);
    }

    @Transactional
    public void put(String key, String value) {
        repository.findById(key).ifPresentOrElse(s -> s.setValue(value),
                () -> repository.save(new PolicySetting(key, value)));
    }
}
