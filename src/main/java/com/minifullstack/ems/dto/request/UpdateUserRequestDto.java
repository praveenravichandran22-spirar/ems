package com.minifullstack.ems.dto.request;

import com.minifullstack.ems.enums.Role;
import lombok.Data;

@Data
public class UpdateUserRequestDto {
    private String firstName;
    private String lastName;
    private Role   role;
    private String password; // optional — only updated when non-blank
}
