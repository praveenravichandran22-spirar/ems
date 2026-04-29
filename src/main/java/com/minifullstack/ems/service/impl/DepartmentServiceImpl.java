package com.minifullstack.ems.service.impl;

import com.minifullstack.ems.dto.DepartmentDto;
import com.minifullstack.ems.entity.Department;
import com.minifullstack.ems.exception.ResourceNotFoundException;
import com.minifullstack.ems.repository.DepartmentRepository;
import com.minifullstack.ems.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentDto create(DepartmentDto dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Department already exists: " + dto.getName());
        }
        return toDto(departmentRepository.save(toEntity(dto)));
    }

    @Override
    public DepartmentDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Override
    public List<DepartmentDto> getAll() {
        return departmentRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public DepartmentDto update(Long id, DepartmentDto dto) {
        Department dept = findOrThrow(id);
        if (!dept.getName().equals(dto.getName()) && departmentRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Department name already in use: " + dto.getName());
        }
        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());
        return toDto(departmentRepository.save(dept));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        departmentRepository.delete(findOrThrow(id));
    }

    private Department findOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    public DepartmentDto toDto(Department d) {
        return DepartmentDto.builder()
                .id(d.getId())
                .name(d.getName())
                .description(d.getDescription())
                .build();
    }

    private Department toEntity(DepartmentDto dto) {
        return Department.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }
}
