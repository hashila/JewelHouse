package com.jwelhouse.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for Item response
 */
@Getter
@Setter
public class ItemResponseDTO {

	private Long id;
	private String name;
	private String metalType;
	private BigDecimal weight;
	private BigDecimal makingCharges;
	private BigDecimal shippingCharges;
	private Character availability;
	private Character status;
	private byte[] image;
	private List<ItemTaxResponseDTO> taxes;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}

