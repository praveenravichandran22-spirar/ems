package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.EmploymentStatusDto;

import java.util.List;

public interface EmploymentStatusService {
    EmploymentStatusDto create(EmploymentStatusDto dto);
    EmploymentStatusDto getById(Long id);
    List<EmploymentStatusDto> getAll();
    EmploymentStatusDto update(Long id, EmploymentStatusDto dto);
    void delete(Long id);
}
