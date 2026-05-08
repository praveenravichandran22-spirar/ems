package com.minifullstack.ems.entity;

import com.minifullstack.ems.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.engine.jdbc.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String enrollmentNumber;

    private String phone;

    private String address;

    private String guardianName;

    @Column(length = 1000)
    private String bio;

    @Column(nullable = false)
    private String course;

    private Integer year;

    @Column(precision = 4, scale = 2)
    private BigDecimal gpa;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private LocalDate enrollmentDate;

    @Column(nullable = false)
    private Boolean isActive;

    @Column(name = "profile_image_data", columnDefinition = "bytea")
    private byte[] profileImageData;

    @Column(name = "profile_image_content_type")
    private String profileImageContentType;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}