package com.infilect.assignment.repository;

import com.infilect.assignment.entity.State;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
    Optional<State> findByNameIgnoreCase(String name);
}
