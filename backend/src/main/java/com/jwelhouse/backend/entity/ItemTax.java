package com.jwelhouse.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name = "item_tax")
public class ItemTax {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "tax_name", length = 100, nullable = false)
	private String taxName;

	@Column(name = "tax_percentage", nullable = false, precision = 5, scale = 2)
	private BigDecimal taxPercentage;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "item_id", nullable = false)
	private Item item;
}

