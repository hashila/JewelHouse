package com.jwelhouse.backend.service;

import com.jwelhouse.backend.dto.MetalResponseDTO;
import com.jwelhouse.backend.entity.Metal;
import com.jwelhouse.backend.repository.MetalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MetalService {

	private MetalRepository metalRepository;

	/**
	 * Retrieves all metals sorted by name and maps them to response DTOs.
	 *
	 * @return list of mapped {@link MetalResponseDTO} records
	 */
	public List<MetalResponseDTO> getMetalList() {
		log.info("Fetching metal list");
		return metalRepository.findAllByOrderByNameAsc()
				.stream()
				.map(this::convertToResponseDTO)
				.toList();
	}

	/**
	 * Converts a Metal entity into its API response DTO shape.
	 *
	 * @param metal source metal entity
	 * @return mapped {@link MetalResponseDTO}
	 */
	private MetalResponseDTO convertToResponseDTO(Metal metal) {
		MetalResponseDTO dto = new MetalResponseDTO();
		dto.setId(metal.getId());
		dto.setName(metal.getName());
		dto.setCode(metal.getCode());
		return dto;
	}

	@Autowired
	public void setMetalRepository(MetalRepository metalRepository) {
		this.metalRepository = metalRepository;
	}
}
