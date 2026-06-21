package com.minifullstack.ems.dto.request;

import com.minifullstack.ems.enums.Gender;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRequestDto {
    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be at most 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be at most 100 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @Pattern(regexp = "^\\+?[0-9\\s\\-()]{7,20}$", message = "Phone number must be valid")
    private String phone;

    @Size(max = 500, message = "Address must be at most 500 characters")
    private String address;

    @Size(max = 1000, message = "Bio must be at most 1000 characters")
    private String bio;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Status is required")
    private Long statusId;

    private Long countryId;
    private Gender gender;

    @DecimalMin(value = "0.0", message = "Salary must be non-negative")
    private BigDecimal salary;

    @Min(value = 0, message = "Experience years must be non-negative")
    @Max(value = 60, message = "Experience years must be realistic")
    private Integer experienceYears;

    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating;

    private Boolean isRemote;
    private LocalDate dateOfBirth;
    private LocalDate joiningDate;
}
