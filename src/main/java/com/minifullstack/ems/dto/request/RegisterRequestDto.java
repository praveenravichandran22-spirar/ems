package com.minifullstack.ems.dto.request;

import com.minifullstack.ems.enums.Role;
import lombok.Data;

@Data
public class RegisterRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;  // ROLE_ADMIN or ROLE_USER
}
