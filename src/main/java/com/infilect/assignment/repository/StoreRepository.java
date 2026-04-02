package com.infilect.assignment.repository;

import com.infilect.assignment.entity.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByStoreId(String storeId);
    List<Store> findByStoreIdIn(List<String> storeIds);
}
