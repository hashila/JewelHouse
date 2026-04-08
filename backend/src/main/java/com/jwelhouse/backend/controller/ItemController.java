package com.jwelhouse.backend.controller;

import com.jwelhouse.backend.dto.ItemRequestDTO;
import com.jwelhouse.backend.dto.ItemResponseDTO;
import com.jwelhouse.backend.service.ItemService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@CrossOrigin(origins = "http://localhost:4800")
@RequestMapping("/api/items")
public class ItemController {

	@Autowired
	private ItemService itemService;

	/**
	 * Get active items with pagination, filtering and sorting.
	 * Request params:
	 *   page        (default 0)
	 *   pageSize    (default 10)
	 *   name        - name LIKE filter (optional)
	 *   metalType   - exact metal type filter (optional)
	 *   availability - single char filter: S, O, L (optional)
	 *   sortBy      - field to sort: name | makingCharges (default: name)
	 *   sortDir     - asc | desc (default: asc)
	 */
	@GetMapping("/getActiveList")
	public Page<ItemResponseDTO> getActiveItems(
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
			@RequestParam(name = "name", required = false) String name,
			@RequestParam(name = "metalType", required = false) String metalType,
			@RequestParam(name = "availability", required = false) Character availability,
			@RequestParam(name = "sortBy", required = false) String sortBy,
			@RequestParam(name = "sortDir", required = false) String sortDir) {
		log.info("GET /api/items/getActiveList - page: {}, pageSize: {}, name: {}, metalType: {}, availability: {}, sortBy: {}, sortDir: {}",
				page, pageSize, name, metalType, availability, sortBy, sortDir);
		return itemService.getActiveItems(page, pageSize, name, metalType, availability, sortBy, sortDir);
	}

	@PostMapping("/create")
	public ResponseEntity<ItemResponseDTO> createItem(@Valid @RequestBody ItemRequestDTO requestDTO) {
		log.info("POST /api/items/create - name: {}", requestDTO.getName());
		return itemService.createItem(requestDTO);
	}

	@GetMapping("/getItemById")
	public ItemResponseDTO getItemById(@RequestParam(name = "id") Long id) {
		log.info("GET /api/items/getItemById - id: {}", id);
		return itemService.getItemById(id);
	}

	@PutMapping("/updateItem")
	public ItemResponseDTO updateItem(
			@RequestParam(name = "id") Long id,
			@Valid @RequestBody ItemRequestDTO requestDTO) {
		log.info("PUT /api/items/updateItem - id: {}", id);
		return itemService.updateItem(id, requestDTO);
	}

	@DeleteMapping("/delete")
	public ResponseEntity<Void> deleteItem(@RequestParam(name = "id") Long id) {
		log.info("DELETE /api/items/delete - id: {}", id);
		itemService.deleteItem(id);
		return ResponseEntity.ok().build();
	}
}
