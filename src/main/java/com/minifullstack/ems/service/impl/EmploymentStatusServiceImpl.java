package com.minifullstack.ems.service.impl;

import com.minifullstack.ems.dto.EmploymentStatusDto;
import com.minifullstack.ems.entity.EmploymentStatus;
import com.minifullstack.ems.exception.ResourceNotFoundException;
import com.minifullstack.ems.repository.EmploymentStatusRepository;
import com.minifullstack.ems.service.EmploymentStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmploymentStatusServiceImpl implements EmploymentStatusService {

    private final EmploymentStatusRepository statusRepository;

    @Override
    @Transactional
    public EmploymentStatusDto create(EmploymentStatusDto dto) {
        if (statusRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Status already exists: " + dto.getName());
        }
        return toDto(statusRepository.save(toEntity(dto)));
    }

    @Override
    public EmploymentStatusDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Override
    public List<EmploymentStatusDto> getAll() {
        return statusRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public EmploymentStatusDto update(Long id, EmploymentStatusDto dto) {
        EmploymentStatus status = findOrThrow(id);
        if (!status.getName().equals(dto.getName()) && statusRepository.existsByName(dto.getName())) {
            throw new IllegalArgumentException("Status name already in use: " + dto.getName());
        }
        status.setName(dto.getName());
        status.setDescription(dto.getDescription());
        return toDto(statusRepository.save(status));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        statusRepository.delete(findOrThrow(id));
    }

    private EmploymentStatus findOrThrow(Long id) {
        return statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with id: " + id));
    }

    public EmploymentStatusDto toDto(EmploymentStatus s) {
        return EmploymentStatusDto.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .build();
    }

    private EmploymentStatus toEntity(EmploymentStatusDto dto) {
        return EmploymentStatus.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }
}
