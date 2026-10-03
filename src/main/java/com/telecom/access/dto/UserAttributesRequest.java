package com.telecom.access.dto;

import com.telecom.access.entity.EmploymentType;

public record UserAttributesRequest(String circle, EmploymentType employmentType) {
}
