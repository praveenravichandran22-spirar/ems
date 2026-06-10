package com.minifullstack.ems.dto.request;

import com.minifullstack.ems.enums.Role;
import lombok.Data;

@Data
public class CreateUserRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role; // only ROLE_REVIEWER or ROLE_APPROVER accepted
}