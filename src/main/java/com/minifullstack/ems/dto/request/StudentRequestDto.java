package com.minifullstack.ems.dto.request;

import com.minifullstack.ems.enums.Gender;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StudentRequestDto {
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
}