package com.jwelhouse.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class MetalPriceClient {

	private static final String GOLD = "GOLD";
	private static final String SILVER = "SILVER";
	private static final String PLATINUM = "PLATINUM";

	private static final Pattern PRICE_PATTERN = Pattern.compile(
			"\\\"(gold|silver|platinum|xau|xag|xpt|au|ag|pt)\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)",
			Pattern.CASE_INSENSITIVE
	);

	private final RestClient restClient;

	public MetalPriceClient(@Value("${metal.price.base-url:https://api.metals.live/v1}") String baseUrl) {
		this.restClient = RestClient.builder().baseUrl(baseUrl).build();
	}

	public Map<String, BigDecimal> fetchSpotPrices() {
		try {
			String responseBody = restClient.get()
					.uri("/spot")
					.retrieve()
					.body(String.class);

			if (responseBody == null || responseBody.isBlank()) {
				log.warn("Metal price API returned empty response");
				return Map.of();
			}

			Map<String, BigDecimal> prices = extractPrices(responseBody);
			Map<String, BigDecimal> filtered = new HashMap<>();
			putIfPresent(prices, GOLD, filtered);
			putIfPresent(prices, SILVER, filtered);
			putIfPresent(prices, PLATINUM, filtered);
			return filtered;
		} catch (Exception ex) {
			log.warn("Unable to fetch live metal prices from external API: {}", ex.getMessage());
			return Map.of();
		}
	}

	private Map<String, BigDecimal> extractPrices(String responseBody) {
		Map<String, BigDecimal> prices = new HashMap<>();
		Matcher matcher = PRICE_PATTERN.matcher(responseBody);

		while (matcher.find()) {
			String normalizedMetal = normalizeMetalName(matcher.group(1));
			String numericValue = matcher.group(2);

			if (!isSupported(normalizedMetal)) {
				continue;
			}

			try {
				prices.put(normalizedMetal, new BigDecimal(numericValue));
			} catch (NumberFormatException ex) {
				log.debug("Skipping unparsable metal price '{}' for {}", numericValue, normalizedMetal);
			}
		}
		return prices;
	}

	private void putIfPresent(Map<String, BigDecimal> source, String key, Map<String, BigDecimal> target) {
		if (source.containsKey(key)) {
			target.put(key, source.get(key));
		}
	}

	private boolean isSupported(String metalName) {
		return GOLD.equals(metalName) || SILVER.equals(metalName) || PLATINUM.equals(metalName);
	}

	private String normalizeMetalName(String rawName) {
		if (rawName == null) {
			return "";
		}
		String normalized = rawName.trim().toUpperCase();
		return switch (normalized) {
			case "XAU", "AU" -> GOLD;
			case "XAG", "AG" -> SILVER;
			case "XPT", "PT" -> PLATINUM;
			default -> normalized;
		};
	}
}

