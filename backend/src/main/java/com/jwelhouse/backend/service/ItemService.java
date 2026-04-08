package com.jwelhouse.backend.service;

import com.jwelhouse.backend.constants.AppConstants;
import com.jwelhouse.backend.dto.ItemRequestDTO;
import com.jwelhouse.backend.dto.ItemResponseDTO;
import com.jwelhouse.backend.dto.ItemTaxRequestDTO;
import com.jwelhouse.backend.dto.ItemTaxResponseDTO;
import com.jwelhouse.backend.entity.Item;
import com.jwelhouse.backend.entity.ItemTax;
import com.jwelhouse.backend.exception.CustomException;
import com.jwelhouse.backend.repository.ItemRepository;
import com.jwelhouse.backend.repository.specification.ItemSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
public class ItemService {

	private ItemRepository itemRepository;

	/**
	 * Get active items with pagination, optional filtering and sorting
	 */
	public Page<ItemResponseDTO> getActiveItems(int page, int pageSize,
			String name, String metalType, Character availability,
			String sortBy, String sortDir) {
		log.info("Fetching active items - page: {}, pageSize: {}, name: {}, metalType: {}, availability: {}, sortBy: {}, sortDir: {}",
				page, pageSize, name, metalType, availability, sortBy, sortDir);

		Sort sort = buildSort(sortBy, sortDir);
		Pageable pageable = PageRequest.of(page, pageSize, sort);

		Specification<Item> spec = ItemSpecification.buildActiveItemSpec(
				AppConstants.STATUS_ACTIVE, name, metalType, availability);

		Page<Item> items = itemRepository.findAll(spec, pageable);
		log.debug("Found {} active items", items.getTotalElements());
		return items.map(this::convertToResponseDTO);
	}

	private Sort buildSort(String sortBy, String sortDir) {
		String field = switch (sortBy == null ? "" : sortBy.toLowerCase()) {
			case "makingcharges", "making_charges" -> "makingCharges";
			case "name" -> "name";
			default -> "name";
		};

		Sort.Direction direction = "desc".equalsIgnoreCase(sortDir) ? Sort.Direction.DESC : Sort.Direction.ASC;
		return Sort.by(direction, field);
	}

	/**
	 * Get item by ID
	 */
	public ItemResponseDTO getItemById(Long id) {
		log.info("Fetching item with id: {}", id);
		Item item = findItemByIdOrThrow(id);
		return convertToResponseDTO(item);
	}

	/**
	 * Create a new item
	 */
	public ResponseEntity<ItemResponseDTO> createItem(ItemRequestDTO requestDTO) {
		log.info("Creating new item with name: {}", requestDTO.getName());
		validateItemRequest(requestDTO, null);

		Item item = new Item();
		populateItemFields(item, requestDTO);
		syncItemTaxes(item, requestDTO.getTaxes());

		Item savedItem = itemRepository.save(item);
		log.info("Item created successfully with id: {}", savedItem.getId());
		return ResponseEntity.ok(convertToResponseDTO(savedItem));
	}

	/**
	 * Update an existing item
	 */
	public ItemResponseDTO updateItem(Long id, ItemRequestDTO requestDTO) {
		log.info("Updating item with id: {}", id);
		validateItemRequest(requestDTO, id);
		Item item = findItemByIdOrThrow(id);

		populateItemFields(item, requestDTO);
		syncItemTaxes(item, requestDTO.getTaxes());

		Item updatedItem = itemRepository.save(item);
		log.info("Item updated successfully with id: {}", updatedItem.getId());
		return convertToResponseDTO(updatedItem);
	}

	/**
	 * Soft delete an item by ID
	 */
	public void deleteItem(Long id) {
		log.info("Soft deleting item with id: {}", id);
		Item item = findItemByIdOrThrow(id);
		item.setStatus(AppConstants.STATUS_DELETED);
		itemRepository.save(item);
		log.info("Item with id: {} marked as deleted", id);
	}

	private Item findItemByIdOrThrow(Long id) {
		Optional<Item> item = itemRepository.findById(id);
		if (item.isEmpty()) {
			log.warn("Item with id: {} not found", id);
			throw new CustomException("Item with ID " + id + " not found");
		}
		return item.get();
	}

	private void validateItemRequest(ItemRequestDTO requestDTO, Long itemId) {
		if (!isValidAvailability(requestDTO.getAvailability())) {
			log.warn("Invalid availability character: {}", requestDTO.getAvailability());
			throw new CustomException("Invalid availability character. Allowed values: S, O, L");
		}

		if (!isValidStatus(requestDTO.getStatus())) {
			log.warn("Invalid status character: {}", requestDTO.getStatus());
			throw new CustomException("Invalid status character. Allowed values: A, I, D, P, R, X");
		}

		validateTaxes(requestDTO.getTaxes(), itemId);
	}

	private void validateTaxes(List<ItemTaxRequestDTO> taxes, Long itemId) {
		if (taxes == null) {
			return;
		}

		for (ItemTaxRequestDTO tax : taxes) {
			if (itemId == null && tax.getItemId() != null) {
				log.warn("Tax itemId must not be provided during item creation: {}", tax.getItemId());
				throw new CustomException("Tax itemId must not be provided during item creation");
			}

			if (itemId != null && tax.getItemId() != null && !itemId.equals(tax.getItemId())) {
				log.warn("Tax itemId {} does not match item id {}", tax.getItemId(), itemId);
				throw new CustomException("Tax itemId must match item ID " + itemId);
			}
		}
	}

	private boolean isValidAvailability(Character availability) {
		if (availability == null) {
			return false;
		}
		return availability == AppConstants.AVAILABILITY_IN_STOCK
				|| availability == AppConstants.AVAILABILITY_OUT_OF_STOCK
				|| availability == AppConstants.AVAILABILITY_LIMITED_STOCK;
	}

	private boolean isValidStatus(Character status) {
		if (status == null) {
			return false;
		}
		return status == AppConstants.STATUS_ACTIVE
				|| status == AppConstants.STATUS_INACTIVE
				|| status == AppConstants.STATUS_DELETED
				|| status == AppConstants.STATUS_PENDING
				|| status == AppConstants.STATUS_APPROVED
				|| status == AppConstants.STATUS_REJECTED;
	}

	private void populateItemFields(Item item, ItemRequestDTO requestDTO) {
		item.setName(requestDTO.getName());
		item.setMetalType(requestDTO.getMetalType());
		item.setWeight(requestDTO.getWeight());
		item.setMakingCharges(requestDTO.getMakingCharges());
		item.setAvailability(requestDTO.getAvailability());
		item.setStatus(requestDTO.getStatus());
		item.setImage(requestDTO.getImage());
	}

	private void syncItemTaxes(Item item, List<ItemTaxRequestDTO> taxRequests) {
		if (item.getTaxes() == null) {
			item.setTaxes(new ArrayList<>());
		}
		item.getTaxes().clear();

		if (taxRequests == null || taxRequests.isEmpty()) {
			return;
		}

		for (ItemTaxRequestDTO taxRequest : taxRequests) {
			ItemTax tax = new ItemTax();
			tax.setTaxName(taxRequest.getTaxName());
			tax.setTaxPercentage(taxRequest.getTaxPercentage());
			tax.setItem(item);
			item.getTaxes().add(tax);
		}
	}

	/**
	 * Convert Item entity to ItemResponseDTO
	 */
	private ItemResponseDTO convertToResponseDTO(Item item) {
		ItemResponseDTO dto = new ItemResponseDTO();
		dto.setId(item.getId());
		dto.setName(item.getName());
		dto.setMetalType(item.getMetalType());
		dto.setWeight(item.getWeight());
		dto.setMakingCharges(item.getMakingCharges());
		dto.setAvailability(item.getAvailability());
		dto.setStatus(item.getStatus());
		dto.setImage(item.getImage());
		dto.setTaxes(item.getTaxes().stream().map(this::convertTaxToResponseDTO).toList());
		dto.setCreatedAt(item.getCreatedAt());
		dto.setUpdatedAt(item.getUpdatedAt());
		return dto;
	}

	private ItemTaxResponseDTO convertTaxToResponseDTO(ItemTax tax) {
		ItemTaxResponseDTO dto = new ItemTaxResponseDTO();
		dto.setItemId(tax.getItem() != null ? tax.getItem().getId() : null);
		dto.setTaxName(tax.getTaxName());
		dto.setTaxPercentage(tax.getTaxPercentage());
		return dto;
	}

	@Autowired
	public void setItemRepository(ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}
}

