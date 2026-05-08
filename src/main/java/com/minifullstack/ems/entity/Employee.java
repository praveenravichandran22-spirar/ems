package com.minifullstack.ems.entity;

import com.minifullstack.ems.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ── Basic text fields ──────────────────────────────────────────────────
    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String address;

    @Column(length = 1000)
    private String bio;

    // ── DB-backed lookup fields (dropdown data managed by admin) ───────────
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id")
    private EmploymentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    // ── Enum field (fixed global set — stays as enum) ──────────────────────
    @Enumerated(EnumType.STRING)
    private Gender gender;

    // ── Numeric fields ─────────────────────────────────────────────────────
    @Column(precision = 12, scale = 2)
    private BigDecimal salary;

    private Integer experienceYears;

    private Integer rating;

    // ── Boolean field ──────────────────────────────────────────────────────
    @Column(nullable = false)
    private Boolean isRemote;

    // ── Date fields ────────────────────────────────────────────────────────
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private LocalDate joiningDate;

    // ── File upload fields (stored as binary blobs in DB) ─────────────────
    @Column(name = "profile_image_data", columnDefinition = "bytea")
    private byte[] profileImageData;

    @Column(name = "profile_image_content_type")
    private String profileImageContentType;

    @Column(name = "resume_data", columnDefinition = "bytea")
    private byte[] resumeData;

    @Column(name = "resume_content_type")
    private String resumeContentType;

    @Column(name = "resume_file_name")
    private String resumeFileName;

    // ── Audit timestamps ───────────────────────────────────────────────────
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
