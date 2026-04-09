package com.jwelhouse.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "metal")
public class Metal {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "name", length = 100, nullable = false)
	private String name;

	@Column(name = "code", length = 10, nullable = false)
	private String code;
}

