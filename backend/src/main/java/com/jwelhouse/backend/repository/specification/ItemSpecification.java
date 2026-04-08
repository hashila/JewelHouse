package com.jwelhouse.backend.repository.specification;

import com.jwelhouse.backend.entity.Item;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ItemSpecification {

	private ItemSpecification() {}

	public static Specification<Item> buildActiveItemSpec(char activeStatus, String name, String metalType, Character availability) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			// Always filter by active status
			predicates.add(cb.equal(root.get("status"), activeStatus));

			// Optional name LIKE filter
			if (name != null && !name.isBlank()) {
				predicates.add(cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
			}

			// Optional metalType exact filter
			if (metalType != null && !metalType.isBlank()) {
				predicates.add(cb.equal(cb.lower(root.get("metalType")), metalType.trim().toLowerCase()));
			}

			// Optional availability filter
			if (availability != null) {
				predicates.add(cb.equal(root.get("availability"), availability));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}

