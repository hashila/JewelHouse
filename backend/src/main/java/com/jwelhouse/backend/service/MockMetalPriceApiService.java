package com.jwelhouse.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class MockMetalPriceApiService {

	private static final Pattern PRICE_ENTRY_PATTERN = Pattern.compile(
			"\\{[^{}]*?\\\"code\\\"\\s*:\\s*\\\"([A-Za-z0-9]+)\\\"[^{}]*?\\\"price\\\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)\\s*[^{}]*?\\}",
			Pattern.MULTILINE
	);
	private static final Pattern UNIT_PATTERN = Pattern.compile("\\\"priceUnit\\\"\\s*:\\s*\\\"(KG|G)\\\"", Pattern.CASE_INSENSITIVE);
	private static final BigDecimal THOUSAND = new BigDecimal("1000");

	private final Resource metalPriceResource;
	private volatile Map<String, BigDecimal> cachedPricesByCode;
	private volatile PriceUnit priceUnit;

	/**
	 * Initializes the file-backed mock price API service and empty in-memory cache.
	 *
	 * @param metalPriceResource classpath resource that contains mock metal prices JSON
	 */
	public MockMetalPriceApiService(@Value("classpath:mock/metal-prices.json") Resource metalPriceResource) {
		this.metalPriceResource = metalPriceResource;
		this.cachedPricesByCode = Collections.emptyMap();
		this.priceUnit = PriceUnit.G;
	}

	/**
	 * Returns the current metal prices map keyed by metal code.
	 * Data is loaded lazily once from the mock JSON file and then cached.
	 *
	 * @return immutable map of metal code to configured unit price
	 */
	public Map<String, BigDecimal> getCurrentPricesByCode() {
		ensureCacheLoaded();
		return cachedPricesByCode;
	}

	/**
	 * Calculates metal cost for a given weight using the configured unit in the
	 * mock price file (`KG` or `G`) and the metal's unit price.
	 *
	 * @param metalCode metal code used for price lookup (for example XAU)
	 * @param weightInGrams item weight in grams
	 * @return computed metal cost rounded to 2 decimals
	 */
	public BigDecimal calculateMetalCostForWeight(String metalCode, BigDecimal weightInGrams) {
		if (metalCode == null || metalCode.isBlank() || weightInGrams == null || weightInGrams.signum() <= 0) {
			return BigDecimal.ZERO;
		}

		Map<String, BigDecimal> priceMap = getCurrentPricesByCode();
		BigDecimal unitPrice = priceMap.get(metalCode.trim().toUpperCase());
		if (unitPrice == null) {
			return BigDecimal.ZERO;
		}

		BigDecimal normalizedWeight = switch (priceUnit) {
			case KG -> weightInGrams.divide(THOUSAND, 6, RoundingMode.HALF_UP);
			case G -> weightInGrams;
		};

		return unitPrice.multiply(normalizedWeight).setScale(2, RoundingMode.HALF_UP);
	}

	/**
	 * Loads and caches mock pricing data once in a thread-safe manner.
	 *
	 * @implNote uses double-checked locking on an immutable cache map.
	 */
	private void ensureCacheLoaded() {
		if (!cachedPricesByCode.isEmpty()) {
			return;
		}

		synchronized (this) {
			if (!cachedPricesByCode.isEmpty()) {
				return;
			}
			PriceData priceData = loadFromMockApiFile();
			cachedPricesByCode = priceData.pricesByCode();
			priceUnit = priceData.priceUnit();
		}
	}

	/**
	 * Reads the mock JSON file, extracts per-code prices, resolves the unit,
	 * and returns immutable data for cache assignment.
	 *
	 * @return parsed and immutable price data with unit metadata
	 */
	private PriceData loadFromMockApiFile() {
		try (InputStream inputStream = metalPriceResource.getInputStream()) {
			String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			Map<String, BigDecimal> pricesByCode = new HashMap<>();
			Matcher matcher = PRICE_ENTRY_PATTERN.matcher(jsonContent);

			while (matcher.find()) {
				String code = matcher.group(1).trim().toUpperCase();
				BigDecimal price = new BigDecimal(matcher.group(2));
				pricesByCode.put(code, price);
			}

			PriceUnit resolvedUnit = resolvePriceUnit(jsonContent);
			log.info("Loaded {} metal prices from mock API file with unit {}", pricesByCode.size(), resolvedUnit);
			return new PriceData(Collections.unmodifiableMap(pricesByCode), resolvedUnit);
		} catch (Exception ex) {
			log.error("Failed to load mock metal prices: {}", ex.getMessage(), ex);
			return new PriceData(Collections.emptyMap(), PriceUnit.G);
		}
	}

	/**
	 * Resolves the configured price unit from JSON; defaults to grams when absent.
	 *
	 * @param jsonContent full JSON string content
	 * @return resolved {@link PriceUnit}
	 */
	private PriceUnit resolvePriceUnit(String jsonContent) {
		Matcher unitMatcher = UNIT_PATTERN.matcher(jsonContent);
		if (unitMatcher.find()) {
			return PriceUnit.valueOf(unitMatcher.group(1).toUpperCase());
		}
		return PriceUnit.G;
	}

	private enum PriceUnit {
		KG,
		G
	}

	private record PriceData(Map<String, BigDecimal> pricesByCode, PriceUnit priceUnit) {
	}
}
