package com.jwelhouse.backend.service;

import com.jwelhouse.backend.constants.AppConstants;
import com.jwelhouse.backend.dto.ItemRequestDTO;
import com.jwelhouse.backend.dto.ItemResponseDTO;
import com.jwelhouse.backend.dto.ItemTaxRequestDTO;
import com.jwelhouse.backend.dto.ItemTaxResponseDTO;
import com.jwelhouse.backend.dto.ItemWithPriceResponseDTO;
import com.jwelhouse.backend.entity.Item;
import com.jwelhouse.backend.entity.ItemTax;
import com.jwelhouse.backend.entity.Metal;
import com.jwelhouse.backend.exception.CustomException;
import com.jwelhouse.backend.repository.ItemRepository;
import com.jwelhouse.backend.repository.MetalRepository;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class ItemService {

	private static final BigDecimal HUNDRED = new BigDecimal("100");

	private ItemRepository itemRepository;
	private MetalRepository metalRepository;
	private MockMetalPriceApiService mockMetalPriceApiService;

	/**
	 * Returns active items using pagination, optional filtering, and optional sorting.
	 *
	 * @param page zero-based page index
	 * @param pageSize number of records per page
	 * @param name optional partial name match (LIKE)
	 * @param metalType optional exact metal type filter
	 * @param availability optional availability code filter
	 * @param sortBy optional sort field (name or makingCharges)
	 * @param sortDir optional sort direction (asc or desc)
	 * @return paged active items mapped to {@link ItemResponseDTO}
	 */
	public Page<ItemResponseDTO> getActiveItems(int page, int pageSize,
			String name, String metalType, Character availability,
			String sortBy, String sortDir) {
		log.info("Fetching active items - page: {}, pageSize: {}, name: {}, metalType: {}, availability: {}, sortBy: {}, sortDir: {}",
				page, pageSize, name, metalType, availability, sortBy, sortDir);

		Page<Item> items = findActiveItemsPage(page, pageSize, name, metalType, availability, sortBy, sortDir);
		log.debug("Found {} active items", items.getTotalElements());
		return items.map(this::convertToResponseDTO);
	}

	/**
	 * Returns active items with calculated final price included in each row.
	 *
	 * @param page zero-based page index
	 * @param pageSize number of records per page
	 * @param name optional partial name match (LIKE)
	 * @param metalType optional exact metal type filter
	 * @param availability optional availability code filter
	 * @param sortBy optional sort field (name or makingCharges)
	 * @param sortDir optional sort direction (asc or desc)
	 * @return paged active items mapped to {@link ItemWithPriceResponseDTO}
	 */
	public Page<ItemWithPriceResponseDTO> getActiveItemsWithPrice(int page, int pageSize,
			String name, String metalType, Character availability,
			String sortBy, String sortDir) {
		log.info("Fetching active items with price - page: {}, pageSize: {}, name: {}, metalType: {}, availability: {}, sortBy: {}, sortDir: {}",
				page, pageSize, name, metalType, availability, sortBy, sortDir);

		Page<Item> items = findActiveItemsPage(page, pageSize, name, metalType, availability, sortBy, sortDir);
		Map<String, String> metalCodeByName = buildMetalCodeByName(items.getContent());

		return items.map(item -> convertToItemWithPriceResponseDTO(item, metalCodeByName));
	}

	/**
	 * Executes the active-item query with optional filters and requested sorting,
	 * then returns a paged Item result.
	 *
	 * @param page zero-based page index
	 * @param pageSize number of records per page
	 * @param name optional partial name match
	 * @param metalType optional exact metal type filter
	 * @param availability optional availability filter
	 * @param sortBy optional sort field name
	 * @param sortDir optional sort direction
	 * @return paged active {@link Item} entities
	 */

	private Page<Item> findActiveItemsPage(int page, int pageSize,
			String name, String metalType, Character availability,
			String sortBy, String sortDir) {
		Sort sort = buildSort(sortBy, sortDir);
		Pageable pageable = PageRequest.of(page, pageSize, sort);
		Specification<Item> spec = ItemSpecification.buildActiveItemSpec(
				AppConstants.STATUS_ACTIVE, name, metalType, availability);
		return itemRepository.findAll(spec, pageable);
	}

	/**
	 * Converts incoming sort parameters into a safe Sort object.
	 * Only supported fields are exposed to prevent invalid property access.
	 *
	 * @param sortBy incoming field name
	 * @param sortDir incoming sort direction
	 * @return resolved {@link Sort} object with safe defaults
	 */
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
	 * Returns a single item by identifier.
	 *
	 * @param id item identifier
	 * @return item mapped to {@link ItemResponseDTO}
	 * @throws CustomException when item does not exist
	 */
	public ItemResponseDTO getItemById(Long id) {
		log.info("Fetching item with id: {}", id);
		Item item = findItemByIdOrThrow(id);
		return convertToResponseDTO(item);
	}

	/**
	 * Creates a new item and its child tax rows.
	 *
	 * @param requestDTO item creation payload
	 * @return created item payload wrapped in {@link ResponseEntity}
	 * @throws CustomException when request validation fails
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
	 * Updates an existing item and replaces its tax rows.
	 *
	 * @param id item identifier
	 * @param requestDTO item update payload
	 * @return updated item payload
	 * @throws CustomException when item does not exist or validation fails
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
	 * Soft deletes an item by changing status to deleted.
	 *
	 * @param id item identifier
	 * @throws CustomException when item does not exist
	 */
	public void deleteItem(Long id) {
		log.info("Soft deleting item with id: {}", id);
		Item item = findItemByIdOrThrow(id);
		item.setStatus(AppConstants.STATUS_DELETED);
		itemRepository.save(item);
		log.info("Item with id: {} marked as deleted", id);
	}

	/**
	 * Loads an item by id or throws a business exception when not found.
	 *
	 * @param id item identifier
	 * @return found {@link Item} entity
	 * @throws CustomException when item is missing
	 */
	private Item findItemByIdOrThrow(Long id) {
		Optional<Item> item = itemRepository.findById(id);
		if (item.isEmpty()) {
			log.warn("Item with id: {} not found", id);
			throw new CustomException("Item with ID " + id + " not found");
		}
		return item.get();
	}

	/**
	 * Validates top-level request constraints that are enforced in service logic.
	 *
	 * @param requestDTO incoming item payload
	 * @param itemId target item id for update, null for create
	 * @throws CustomException when any validation rule fails
	 */
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

	/**
	 * Validates tax-item relationship rules for create/update requests.
	 *
	 * @param taxes incoming tax list
	 * @param itemId target item id for update, null for create
	 * @throws CustomException when tax-item relationship is invalid
	 */
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

	/**
	 * Checks whether availability value is one of the supported status codes.
	 *
	 * @param availability availability character
	 * @return true when value is valid; otherwise false
	 */
	private boolean isValidAvailability(Character availability) {
		if (availability == null) {
			return false;
		}
		return availability == AppConstants.AVAILABILITY_IN_STOCK
				|| availability == AppConstants.AVAILABILITY_OUT_OF_STOCK
				|| availability == AppConstants.AVAILABILITY_LIMITED_STOCK;
	}

	/**
	 * Checks whether item status value is one of the supported status codes.
	 *
	 * @param status status character
	 * @return true when value is valid; otherwise false
	 */
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

	/**
	 * Copies mutable item fields from request payload to entity.
	 *
	 * @param item target entity
	 * @param requestDTO source payload
	 */
	private void populateItemFields(Item item, ItemRequestDTO requestDTO) {
		item.setName(requestDTO.getName());
		item.setMetalType(requestDTO.getMetalType());
		item.setWeight(requestDTO.getWeight());
		item.setMakingCharges(requestDTO.getMakingCharges());
		item.setShippingCharges(requestDTO.getShippingCharges());
		item.setAvailability(requestDTO.getAvailability());
		item.setStatus(requestDTO.getStatus());
		item.setImage(requestDTO.getImage());
	}

	/**
	 * Rebuilds item tax rows from incoming request taxes.
	 * Existing taxes are replaced to keep update semantics explicit.
	 *
	 * @param item parent item entity
	 * @param taxRequests incoming tax payload list
	 */
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
	 * Converts an item entity to base item response DTO.
	 *
	 * @param item source entity
	 * @return mapped {@link ItemResponseDTO}
	 */
	private ItemResponseDTO convertToResponseDTO(Item item) {
		ItemResponseDTO dto = new ItemResponseDTO();
		dto.setId(item.getId());
		dto.setName(item.getName());
		dto.setMetalType(item.getMetalType());
		dto.setWeight(item.getWeight());
		dto.setMakingCharges(item.getMakingCharges());
		dto.setShippingCharges(item.getShippingCharges());
		dto.setAvailability(item.getAvailability());
		dto.setStatus(item.getStatus());
		dto.setImage(item.getImage());
		dto.setTaxes(item.getTaxes().stream().map(this::convertTaxToResponseDTO).toList());
		dto.setCreatedAt(item.getCreatedAt());
		dto.setUpdatedAt(item.getUpdatedAt());
		return dto;
	}

	/**
	 * Converts an item entity into an item-with-price DTO by calculating:
	 * metal cost (weight-aware) + making + shipping + aggregated taxes.
	 *
	 * @param item source item entity
	 * @param metalCodeByName lookup map of metal name to metal code
	 * @return mapped {@link ItemWithPriceResponseDTO} including final price
	 */
	private ItemWithPriceResponseDTO convertToItemWithPriceResponseDTO(Item item,
			Map<String, String> metalCodeByName) {
		String normalizedMetalName = item.getMetalType() == null ? "" : item.getMetalType().trim().toLowerCase();
		String metalCode = metalCodeByName.get(normalizedMetalName);

		BigDecimal metalCost = mockMetalPriceApiService.calculateMetalCostForWeight(metalCode, item.getWeight());

		BigDecimal makingCharges = item.getMakingCharges() == null ? BigDecimal.ZERO : item.getMakingCharges();
		BigDecimal shippingCharges = item.getShippingCharges() == null ? BigDecimal.ZERO : item.getShippingCharges();
		BigDecimal baseAmount = metalCost.add(makingCharges).add(shippingCharges);

		BigDecimal totalTaxPercentage = item.getTaxes().stream()
				.map(ItemTax::getTaxPercentage)
				.filter(Objects::nonNull)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal totalTaxAmount = baseAmount.multiply(totalTaxPercentage).divide(HUNDRED, 2, RoundingMode.HALF_UP);
		BigDecimal finalPrice = baseAmount.add(totalTaxAmount).setScale(2, RoundingMode.HALF_UP);

		ItemWithPriceResponseDTO dto = new ItemWithPriceResponseDTO();
		dto.setId(item.getId());
		dto.setName(item.getName());
		dto.setMetalType(item.getMetalType());
		dto.setWeight(item.getWeight());
		dto.setMakingCharges(item.getMakingCharges());
		dto.setShippingCharges(item.getShippingCharges());
		dto.setAvailability(item.getAvailability());
		dto.setStatus(item.getStatus());
		dto.setImage(item.getImage());
		dto.setTaxes(item.getTaxes().stream().map(this::convertTaxToResponseDTO).toList());
		dto.setCreatedAt(item.getCreatedAt());
		dto.setUpdatedAt(item.getUpdatedAt());
		dto.setPrice(finalPrice);
		return dto;
	}

	/**
	 * Builds a lookup map of metal name (lowercase) to metal code for the
	 * currently loaded item page to avoid repeated DB calls per item.
	 *
	 * @param items page content items
	 * @return map keyed by normalized metal name with metal code as value
	 */
	private Map<String, String> buildMetalCodeByName(List<Item> items) {
		if (items == null || items.isEmpty()) {
			return Map.of();
		}

		Set<String> metalNames = items.stream()
				.map(Item::getMetalType)
				.filter(name -> name != null && !name.isBlank())
				.collect(Collectors.toSet());

		if (metalNames.isEmpty()) {
			return Map.of();
		}

		List<Metal> metals = metalRepository.findByNameIn(new ArrayList<>(metalNames));
		return metals.stream()
				.filter(metal -> metal.getName() != null && metal.getCode() != null)
				.collect(Collectors.toMap(
						metal -> metal.getName().trim().toLowerCase(),
						metal -> metal.getCode().trim(),
						(existing, replacement) -> existing
				));
	}

	/**
	 * Maps a tax entity to its response DTO representation.
	 *
	 * @param tax source tax entity
	 * @return mapped {@link ItemTaxResponseDTO}
	 */
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

	@Autowired
	public void setMetalRepository(MetalRepository metalRepository) {
		this.metalRepository = metalRepository;
	}

	@Autowired
	public void setMockMetalPriceApiService(MockMetalPriceApiService mockMetalPriceApiService) {
		this.mockMetalPriceApiService = mockMetalPriceApiService;
	}
}
