package com.minifullstack.ems.dto.request;

import com.minifullstack.ems.enums.Gender;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class StudentRequestDto {
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Enrollment number is required")
    @Size(max = 50, message = "Enrollment number must be at most 50 characters")
    private String enrollmentNumber;

    @Size(max = 20, message = "Phone must be at most 20 characters")
    private String phone;

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @Size(max = 200, message = "Guardian name must be at most 200 characters")
    private String guardianName;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    private String bio;

    @Size(max = 200, message = "Course must be at most 200 characters")
    private String course;

    @Min(value = 1, message = "Year must be at least 1")
    @Max(value = 6, message = "Year must be at most 6")
    private Integer year;

    @DecimalMin(value = "0.0", message = "GPA must be non-negative")
    @DecimalMax(value = "10.0", message = "GPA must be at most 10.0")
    private BigDecimal gpa;

    private Gender gender;
    private LocalDate dateOfBirth;
    private LocalDate enrollmentDate;
    private Boolean isActive;
}