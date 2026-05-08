package com.minifullstack.ems.dto.response;

import com.minifullstack.ems.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class StudentResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String enrollmentNumber;
    private String phone;
    private String address;
    private String guardianName;
    private String bio;
    private String course;
    private Integer year;
    private BigDecimal gpa;
    private Gender gender;
    private LocalDate dateOfBirth;
    private LocalDate enrollmentDate;
    private Boolean isActive;
    private String profileImageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}