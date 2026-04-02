package com.infilect.assignment.repository;

import com.infilect.assignment.entity.StoreType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StoreTypeRepository extends JpaRepository<StoreType, Long> {
    Optional<StoreType> findByNameIgnoreCase(String name);
}
