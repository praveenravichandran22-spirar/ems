package com.minifullstack.ems.repository;

import com.minifullstack.ems.entity.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByEmail(String email);

    boolean existsByEnrollmentNumber(String enrollmentNumber);

    @Query("""
            SELECT s FROM Student s
            WHERE (:search = '' OR
                   LOWER(s.firstName)        LIKE CONCAT('%', :search, '%') OR
                   LOWER(s.lastName)         LIKE CONCAT('%', :search, '%') OR
                   LOWER(s.email)            LIKE CONCAT('%', :search, '%') OR
                   LOWER(s.enrollmentNumber) LIKE CONCAT('%', :search, '%'))
              AND (:course = '' OR LOWER(s.course) = LOWER(:course))
              AND (:year   = 0  OR s.year = :year)
              AND (:gender = '' OR CAST(s.gender AS string) = :gender)
            """)
    Page<Student> search(
            @Param("search") String search,
            @Param("course") String course,
            @Param("year")   Integer year,
            @Param("gender")  String gender,
            Pageable pageable
    );
}