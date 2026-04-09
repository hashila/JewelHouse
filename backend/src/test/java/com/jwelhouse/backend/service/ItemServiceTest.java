package com.jwelhouse.backend.service;

import com.jwelhouse.backend.dto.ItemRequestDTO;
import com.jwelhouse.backend.dto.ItemResponseDTO;
import com.jwelhouse.backend.dto.ItemTaxRequestDTO;
import com.jwelhouse.backend.dto.ItemWithPriceResponseDTO;
import com.jwelhouse.backend.entity.Item;
import com.jwelhouse.backend.entity.ItemTax;
import com.jwelhouse.backend.entity.Metal;
import com.jwelhouse.backend.repository.ItemRepository;
import com.jwelhouse.backend.repository.MetalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

	@Mock
	private ItemRepository itemRepository;

	@Mock
	private MetalRepository metalRepository;

	@Mock
	private MockMetalPriceApiService mockMetalPriceApiService;

	private ItemService itemService;

	@BeforeEach
	void setUp() {
		itemService = new ItemService();
		itemService.setItemRepository(itemRepository);
		itemService.setMetalRepository(metalRepository);
		itemService.setMockMetalPriceApiService(mockMetalPriceApiService);
	}

	@Test
	void getActiveItemsWithPriceShouldUseWeightBasedMetalCost() {
		Item item = new Item();
		item.setId(11L);
		item.setName("Chain");
		item.setMetalType("Gold");
		item.setWeight(new BigDecimal("10.00"));
		item.setMakingCharges(new BigDecimal("100.00"));
		item.setShippingCharges(new BigDecimal("20.00"));

		ItemTax tax = new ItemTax();
		tax.setTaxName("VAT");
		tax.setTaxPercentage(new BigDecimal("10.00"));
		tax.setItem(item);
		item.getTaxes().add(tax);

		Metal metal = new Metal();
		metal.setName("Gold");
		metal.setCode("XAU");

		Page<Item> page = new PageImpl<>(List.of(item));
		when(itemRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Pageable.class)))
				.thenReturn(page);
		when(metalRepository.findByNameIn(anyList())).thenReturn(List.of(metal));
		when(mockMetalPriceApiService.calculateMetalCostForWeight(eq("XAU"), eq(new BigDecimal("10.00"))))
				.thenReturn(new BigDecimal("50.00"));

		Page<ItemWithPriceResponseDTO> response = itemService.getActiveItemsWithPrice(
				0, 10, null, null, null, "name", "asc");

		assertEquals(1, response.getTotalElements());
		assertEquals(new BigDecimal("187.00"), response.getContent().get(0).getPrice());
	}

	@Test
	void createItemShouldReturnTaxesInResponse() {
		ItemRequestDTO request = buildRequest(null, List.of(
				buildTaxRequest(null, "VAT", "5.00"),
				buildTaxRequest(null, "GST", "3.50")
		));

		when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
			Item item = invocation.getArgument(0);
			item.setId(1L);
			return item;
		});

		ResponseEntity<ItemResponseDTO> response = itemService.createItem(request);

		assertNotNull(response.getBody());
		assertEquals(2, response.getBody().getTaxes().size());
		assertEquals("VAT", response.getBody().getTaxes().get(0).getTaxName());
		assertEquals(new BigDecimal("5.00"), response.getBody().getTaxes().get(0).getTaxPercentage());
		assertEquals(1L, response.getBody().getTaxes().get(0).getItemId());
		assertEquals("GST", response.getBody().getTaxes().get(1).getTaxName());
	}

	@Test
	void updateItemShouldReplaceTaxes() {
		Item item = new Item();
		item.setId(5L);
		ItemTax existingTax = new ItemTax();
		existingTax.setTaxName("OLD TAX");
		existingTax.setTaxPercentage(new BigDecimal("1.00"));
		existingTax.setItem(item);
		item.getTaxes().add(existingTax);

		ItemRequestDTO request = buildRequest(5L, List.of(
				buildTaxRequest(5L, "NEW TAX", "7.25")
		));

		when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
		when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

		ItemResponseDTO response = itemService.updateItem(5L, request);

		assertEquals(1, response.getTaxes().size());
		assertEquals("NEW TAX", response.getTaxes().get(0).getTaxName());
		assertEquals(new BigDecimal("7.25"), response.getTaxes().get(0).getTaxPercentage());
		assertEquals(5L, response.getTaxes().get(0).getItemId());
	}

	@Test
	void getItemByIdShouldIncludeTaxes() {
		Item item = new Item();
		item.setId(10L);
		item.setName("Ring");
		item.setMetalType("Gold");

		ItemTax tax = new ItemTax();
		tax.setTaxName("VAT");
		tax.setTaxPercentage(new BigDecimal("6.00"));
		tax.setItem(item);
		item.getTaxes().add(tax);

		when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

		ItemResponseDTO response = itemService.getItemById(10L);

		assertEquals(1, response.getTaxes().size());
		assertEquals("VAT", response.getTaxes().get(0).getTaxName());
		assertEquals(new BigDecimal("6.00"), response.getTaxes().get(0).getTaxPercentage());
		assertEquals(10L, response.getTaxes().get(0).getItemId());
	}

	private ItemRequestDTO buildRequest(Long itemId, List<ItemTaxRequestDTO> taxes) {
		ItemRequestDTO request = new ItemRequestDTO();
		request.setName("Ring");
		request.setMetalType("Gold");
		request.setWeight(new BigDecimal("10.50"));
		request.setMakingCharges(new BigDecimal("250.00"));
		request.setShippingCharges(new BigDecimal("50.00"));
		request.setAvailability('S');
		request.setStatus('A');
		request.setTaxes(taxes);
		return request;
	}

	private ItemTaxRequestDTO buildTaxRequest(Long itemId, String taxName, String taxPercentage) {
		ItemTaxRequestDTO request = new ItemTaxRequestDTO();
		request.setItemId(itemId);
		request.setTaxName(taxName);
		request.setTaxPercentage(new BigDecimal(taxPercentage));
		return request;
	}
}

