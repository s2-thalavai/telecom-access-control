package com.telecom.access.dto;

import jakarta.validation.constraints.NotBlank;

public record SettingRequest(@NotBlank String value) {
}
