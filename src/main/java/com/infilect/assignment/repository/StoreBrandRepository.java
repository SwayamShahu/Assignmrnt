package com.infilect.assignment.repository;

import com.infilect.assignment.entity.StoreBrand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StoreBrandRepository extends JpaRepository<StoreBrand, Long> {
    Optional<StoreBrand> findByNameIgnoreCase(String name);
}
