package com.jwelhouse.backend.service;

import com.jwelhouse.backend.dto.MetalLivePriceResponseDTO;
import com.jwelhouse.backend.dto.MetalResponseDTO;
import com.jwelhouse.backend.entity.Metal;
import com.jwelhouse.backend.repository.MetalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional(readOnly = true)
public class MetalService {

	private MetalRepository metalRepository;
	private MetalPriceClient metalPriceClient;

	public List<MetalResponseDTO> getMetalList() {
		log.info("Fetching metal list");
		return metalRepository.findAllByOrderByNameAsc()
				.stream()
				.map(this::convertToResponseDTO)
				.toList();
	}

	public List<MetalLivePriceResponseDTO> getMetalListWithLivePrices() {
		log.info("Fetching metal list with live prices");
		Map<String, BigDecimal> livePrices = metalPriceClient.fetchSpotPrices();
		log.info("Metal list has live prices : {}", livePrices);

		return List.of(
				buildLivePriceResponse("Gold", "XAU", livePrices.get("GOLD")),
				buildLivePriceResponse("Silver", "XAG", livePrices.get("SILVER")),
				buildLivePriceResponse("Platinum", "XPT", livePrices.get("PLATINUM"))
		);
	}

	private MetalResponseDTO convertToResponseDTO(Metal metal) {
		MetalResponseDTO dto = new MetalResponseDTO();
		dto.setId(metal.getId());
		dto.setName(metal.getName());
		return dto;
	}

	private MetalLivePriceResponseDTO buildLivePriceResponse(String metalName, String metalCode, BigDecimal price) {
		MetalLivePriceResponseDTO dto = new MetalLivePriceResponseDTO();
		dto.setMetalName(metalName);
		dto.setMetalCode(metalCode);
		dto.setPrice(price);
		return dto;
	}


	@Autowired
	public void setMetalRepository(MetalRepository metalRepository) {
		this.metalRepository = metalRepository;
	}

	@Autowired
	public void setMetalPriceClient(MetalPriceClient metalPriceClient) {
		this.metalPriceClient = metalPriceClient;
	}
}
