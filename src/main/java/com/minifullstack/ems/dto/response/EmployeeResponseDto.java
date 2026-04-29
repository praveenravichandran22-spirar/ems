package com.minifullstack.ems.dto.response;

import com.minifullstack.ems.dto.DepartmentDto;
import com.minifullstack.ems.dto.EmploymentStatusDto;
import com.minifullstack.ems.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String bio;
    private DepartmentDto department;
    private EmploymentStatusDto status;
    private Gender gender;
    private BigDecimal salary;
    private Integer experienceYears;
    private Integer rating;
    private Boolean isRemote;
    private LocalDate dateOfBirth;
    private LocalDate joiningDate;
    private String profileImageUrl;
    private String resumeUrl;
    private String resumeFileName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
