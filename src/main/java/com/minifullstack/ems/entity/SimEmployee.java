package com.minifullstack.ems.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sim_employees", indexes = {
        @Index(name = "idx_sim_firstname",   columnList = "firstName"),
        @Index(name = "idx_sim_lastname",    columnList = "lastName"),
        @Index(name = "idx_sim_email",       columnList = "email"),
        @Index(name = "idx_sim_department",  columnList = "department"),
        @Index(name = "idx_sim_status",      columnList = "status"),
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimEmployee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String email;

    private String department;

    private String status;

    @Column(precision = 12, scale = 2)
    private BigDecimal salary;

    private LocalDate joiningDate;

    private Integer experienceYears;

    @Column(nullable = false)
    private Boolean isRemote;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
