package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.SimEmployeeResponseDto;

public interface SimEmployeeService {

    PagedResponse<SimEmployeeResponseDto> search(
            String keyword, String department, String status,
            int page, int size, String sortBy, String sortDir
    );
}
