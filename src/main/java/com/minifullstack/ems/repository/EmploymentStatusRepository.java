package com.minifullstack.ems.repository;

import com.minifullstack.ems.entity.EmploymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmploymentStatusRepository extends JpaRepository<EmploymentStatus, Long> {
    Optional<EmploymentStatus> findByName(String name);
    boolean existsByName(String name);
}
