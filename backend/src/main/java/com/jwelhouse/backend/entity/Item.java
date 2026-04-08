package com.jwelhouse.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "item")
public class Item {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", length = 255, nullable = false)
	private String name;

	@Column(name = "metal_type", length = 50, nullable = false)
	private String metalType;

	@Column(name = "weight", nullable = false)
	private BigDecimal weight;

	@Column(name = "making_charges", nullable = false)
	private BigDecimal makingCharges;

	@Column(name = "availability", nullable = false)
	private Character availability;

	@Column(name = "status", nullable = false)
	private Character status;

	@Column(name = "image", columnDefinition = "LONGBLOB")
	private byte[] image;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	@OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ItemTax> taxes = new ArrayList<>();

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}

