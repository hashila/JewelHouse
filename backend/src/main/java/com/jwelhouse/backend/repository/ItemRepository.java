package com.jwelhouse.backend.repository;

import com.jwelhouse.backend.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, JpaSpecificationExecutor<Item> {

	/**
	 * Find all active items with pagination
	 */
	Page<Item> findByStatus(char status, Pageable pageable);
}

