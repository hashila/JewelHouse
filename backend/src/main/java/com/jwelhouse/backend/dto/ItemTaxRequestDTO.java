package com.jwelhouse.backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemTaxRequestDTO {

	private Long itemId;

	@NotBlank(message = "Tax name is required")
	@Size(max = 100, message = "Tax name must not exceed 100 characters")
	private String taxName;

	@NotNull(message = "Tax percentage is required")
	@DecimalMin(value = "0.0", message = "Tax percentage must not be less than zero")
	@DecimalMax(value = "100.0", message = "Tax percentage must not exceed 100")
	private BigDecimal taxPercentage;
}

