package com.minifullstack.ems.repository;

import com.minifullstack.ems.entity.SimEmployee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimEmployeeRepository extends JpaRepository<SimEmployee, Long> {

    @Query("""
            SELECT e FROM SimEmployee e
            WHERE (:search = '' OR
                   LOWER(e.firstName) LIKE CONCAT('%', :search, '%') OR
                   LOWER(e.lastName)  LIKE CONCAT('%', :search, '%') OR
                   LOWER(e.email)     LIKE CONCAT('%', :search, '%'))
              AND (:department = '' OR e.department = :department)
              AND (:status     = '' OR e.status     = :status)
            """)
    Page<SimEmployee> search(
            @Param("search")     String search,
            @Param("department") String department,
            @Param("status")     String status,
            Pageable pageable
    );
}
