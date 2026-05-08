package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.request.StudentRequestDto;
import com.minifullstack.ems.dto.response.FileResponse;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.StudentResponseDto;
import org.springframework.web.multipart.MultipartFile;

public interface StudentService {

    StudentResponseDto create(StudentRequestDto dto);

    StudentResponseDto getById(Long id);

    PagedResponse<StudentResponseDto> search(
            String keyword,
            String course,
            Integer year,
            String gender,
            int page, int size,
            String sortBy, String sortDir
    );

    StudentResponseDto update(Long id, StudentRequestDto dto);

    void delete(Long id);

    StudentResponseDto uploadProfileImage(Long id, MultipartFile file);

    FileResponse getProfileImage(Long id);
}