package com.jwelhouse.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for Item creation and update requests
 */
@Getter
@Setter
public class ItemRequestDTO {

	@NotBlank(message = "Name is required")
	@Size(max = 255, message = "Name must not exceed 255 characters")
	private String name;

	@NotBlank(message = "Metal type is required")
	@Size(max = 50, message = "Metal type must not exceed 50 characters")
	private String metalType;

	@NotNull(message = "Weight is required")
	@DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than zero")
	private BigDecimal weight;

	@NotNull(message = "Making charges is required")
	@DecimalMin(value = "0.0", message = "Making charges must not be less than zero")
	private BigDecimal makingCharges;

	@NotNull(message = "Availability is required")
	private Character availability;

	@NotNull(message = "Status is required")
	private Character status;

	private byte[] image;

	@Valid
	private List<ItemTaxRequestDTO> taxes;
}

