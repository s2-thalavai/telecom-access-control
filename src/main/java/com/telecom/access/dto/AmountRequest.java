package com.telecom.access.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AmountRequest(@NotNull @DecimalMin("1.00") BigDecimal amount) {
}
