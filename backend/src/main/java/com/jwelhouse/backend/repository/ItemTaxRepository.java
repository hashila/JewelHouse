package com.jwelhouse.backend.repository;

import com.jwelhouse.backend.entity.ItemTax;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemTaxRepository extends JpaRepository<ItemTax, Long> {
}

