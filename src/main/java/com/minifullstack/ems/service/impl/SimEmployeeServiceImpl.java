package com.minifullstack.ems.service.impl;

import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.SimEmployeeResponseDto;
import com.minifullstack.ems.entity.SimEmployee;
import com.minifullstack.ems.repository.SimEmployeeRepository;
import com.minifullstack.ems.service.SimEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimEmployeeServiceImpl implements SimEmployeeService {

    private final SimEmployeeRepository repository;

    @Override
    public PagedResponse<SimEmployeeResponseDto> search(
            String keyword, String department, String status,
            int page, int size, String sortBy, String sortDir) {

        String search = (keyword != null && !keyword.isBlank()) ? keyword.toLowerCase() : "";
        String dept   = (department != null && !department.isBlank()) ? department : "";
        String stat   = (status    != null && !status.isBlank())     ? status     : "";

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<SimEmployee> result = repository.search(search, dept, stat, pageable);

        return PagedResponse.<SimEmployeeResponseDto>builder()
                .content(result.getContent().stream().map(this::toDto).toList())
                .page(result.getNumber())
                .size(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .last(result.isLast())
                .build();
    }

    private SimEmployeeResponseDto toDto(SimEmployee e) {
        return SimEmployeeResponseDto.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .department(e.getDepartment())
                .status(e.getStatus())
                .salary(e.getSalary())
                .joiningDate(e.getJoiningDate())
                .experienceYears(e.getExperienceYears())
                .isRemote(e.getIsRemote())
                .build();
    }
}
