package com.jwelhouse.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ItemTaxResponseDTO {

	private Long itemId;
	private String taxName;
	private BigDecimal taxPercentage;
}

