package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.request.EmployeeRequestDto;
import com.minifullstack.ems.dto.response.EmployeeResponseDto;
import com.minifullstack.ems.dto.response.FileResponse;
import com.minifullstack.ems.dto.response.PagedResponse;
import org.springframework.web.multipart.MultipartFile;

public interface EmployeeService {

    EmployeeResponseDto create(EmployeeRequestDto dto);

    EmployeeResponseDto getById(Long id);

    PagedResponse<EmployeeResponseDto> getAll(int page, int size, String sortBy, String sortDir);

    PagedResponse<EmployeeResponseDto> search(
            String keyword,
            Long departmentId,
            Long statusId,
            int page, int size,
            String sortBy, String sortDir
    );

    EmployeeResponseDto update(Long id, EmployeeRequestDto dto);

    void delete(Long id);

    EmployeeResponseDto uploadProfileImage(Long id, MultipartFile file);

    EmployeeResponseDto uploadResume(Long id, MultipartFile file);

    FileResponse getProfileImage(Long id);

    FileResponse getResume(Long id);
}
