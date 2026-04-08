package com.jwelhouse.backend.controller;

import com.jwelhouse.backend.dto.MetalLivePriceResponseDTO;
import com.jwelhouse.backend.dto.MetalResponseDTO;
import com.jwelhouse.backend.service.MetalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4800")
@RequestMapping("/api/metals")
public class MetalController {

	@Autowired
	private MetalService metalService;

	@GetMapping("/getList")
	public List<MetalResponseDTO> getMetalList() {
		log.info("GET /api/metals/getList");
		return metalService.getMetalList();
	}

	@GetMapping("/getLivePrices")
	public List<MetalLivePriceResponseDTO> getMetalLivePrices() {
		log.info("GET /api/metals/getLivePrices");
		return metalService.getMetalListWithLivePrices();
	}
}
