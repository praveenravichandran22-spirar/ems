package com.minifullstack.ems.dto.request;

import com.minifullstack.ems.enums.Gender;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String address;
    private String bio;
    private Long departmentId;
    private Long statusId;
    private Gender gender;
    private BigDecimal salary;
    private Integer experienceYears;
    private Integer rating;
    private Boolean isRemote;
    private LocalDate dateOfBirth;
    private LocalDate joiningDate;
}
