package com.minifullstack.ems.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SimEmployeeResponseDto {
    private Long    id;
    private String  firstName;
    private String  lastName;
    private String  email;
    private String  department;
    private String  status;
    private BigDecimal salary;
    private LocalDate  joiningDate;
    private Integer    experienceYears;
    private Boolean    isRemote;
}
