package com.minifullstack.ems.repository;

import com.minifullstack.ems.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    boolean existsByEmail(String email);

    @Query("""
            SELECT e FROM Employee e
            WHERE (:search = '' OR
                   LOWER(e.firstName) LIKE CONCAT('%', :search, '%') OR
                   LOWER(e.lastName)  LIKE CONCAT('%', :search, '%') OR
                   LOWER(e.email)     LIKE CONCAT('%', :search, '%'))
              AND (:departmentId = 0 OR e.department.id = :departmentId)
              AND (:statusId     = 0 OR e.status.id     = :statusId)
              AND e.email NOT IN (SELECT u.email FROM User u)

            """)
    Page<Employee> search(
            @Param("search") String search,
            @Param("departmentId") Long departmentId,
            @Param("statusId") Long statusId,
            Pageable pageable
    );
}
