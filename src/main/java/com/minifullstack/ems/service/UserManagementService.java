package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.request.CreateUserRequestDto;
import com.minifullstack.ems.dto.request.UpdateUserRequestDto;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.UserResponseDto;
import com.minifullstack.ems.enums.Role;

import java.util.List;

public interface UserManagementService {

    PagedResponse<UserResponseDto> search(
            String keyword, String role,
            int page, int size, String sortBy, String sortDir
    );

    UserResponseDto createUser(CreateUserRequestDto dto);

    UserResponseDto updateUser(Long id, UpdateUserRequestDto dto);

    void deleteUser(Long id);

    List<UserResponseDto> listByRole(Role role);
}
