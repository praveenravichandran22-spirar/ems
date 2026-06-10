package com.minifullstack.ems.repository;

import com.minifullstack.ems.entity.User;
import com.minifullstack.ems.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByRole(Role role);

    @Query("""
            SELECT u FROM User u
            WHERE (:search = '' OR
                   LOWER(u.firstName) LIKE CONCAT('%', :search, '%') OR
                   LOWER(u.lastName)  LIKE CONCAT('%', :search, '%') OR
                   LOWER(u.email)     LIKE CONCAT('%', :search, '%'))
            """)
    Page<User> searchAll(@Param("search") String search, Pageable pageable);

    @Query("""
            SELECT u FROM User u
            WHERE (:search = '' OR
                   LOWER(u.firstName) LIKE CONCAT('%', :search, '%') OR
                   LOWER(u.lastName)  LIKE CONCAT('%', :search, '%') OR
                   LOWER(u.email)     LIKE CONCAT('%', :search, '%'))
              AND u.role = :role
            """)
    Page<User> searchByRole(@Param("search") String search, @Param("role") Role role, Pageable pageable);
}
