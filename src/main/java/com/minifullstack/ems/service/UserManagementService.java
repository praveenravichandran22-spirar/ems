package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.UserResponseDto;

public interface UserManagementService {

    PagedResponse<UserResponseDto> search(
            String keyword, String role,
            int page, int size, String sortBy, String sortDir
    );
}
