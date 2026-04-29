package com.minifullstack.ems.dto.response;

import com.minifullstack.ems.enums.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponseDto {
    private Long          id;
    private String        firstName;
    private String        lastName;
    private String        email;
    private Role          role;
    private boolean       enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
