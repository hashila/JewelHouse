package com.jwelhouse.backend.repository;

import com.jwelhouse.backend.entity.Metal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetalRepository extends JpaRepository<Metal, Long> {

	List<Metal> findAllByOrderByNameAsc();
}

