package com.minifullstack.ems.service.impl;

import com.minifullstack.ems.dto.DepartmentDto;
import com.minifullstack.ems.dto.EmploymentStatusDto;
import com.minifullstack.ems.dto.request.EmployeeRequestDto;
import com.minifullstack.ems.dto.response.EmployeeResponseDto;
import com.minifullstack.ems.dto.response.FileResponse;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.entity.Department;
import com.minifullstack.ems.entity.Employee;
import com.minifullstack.ems.entity.EmploymentStatus;
import com.minifullstack.ems.exception.ResourceNotFoundException;
import com.minifullstack.ems.repository.DepartmentRepository;
import com.minifullstack.ems.repository.EmployeeRepository;
import com.minifullstack.ems.repository.EmploymentStatusRepository;
import com.minifullstack.ems.repository.UserRepository;
import com.minifullstack.ems.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final EmploymentStatusRepository statusRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public EmployeeResponseDto create(EmployeeRequestDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("This email belongs to a registered user account and cannot be added as an employee.");
        }
        return toDto(employeeRepository.save(toEntity(dto)));
    }

    @Override
    public EmployeeResponseDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Override
    public PagedResponse<EmployeeResponseDto> getAll(int page, int size, String sortBy, String sortDir) {
        return toPagedResponse(employeeRepository.findAll(buildPageable(page, size, sortBy, sortDir)));
    }

    @Override
    public PagedResponse<EmployeeResponseDto> search(
            String keyword, Long departmentId, Long statusId,
            int page, int size, String sortBy, String sortDir) {
        String search = (keyword != null && !keyword.isBlank()) ? keyword.toLowerCase() : "";
        Page<Employee> result = employeeRepository.search(
                search,
                departmentId != null ? departmentId : 0L,
                statusId     != null ? statusId     : 0L,
                buildPageable(page, size, sortBy, sortDir)
        );
        return toPagedResponse(result);
    }

    @Override
    @Transactional
    public EmployeeResponseDto update(Long id, EmployeeRequestDto dto) {
        Employee employee = findOrThrow(id);
        if (!employee.getEmail().equals(dto.getEmail()) && employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }
        if (!employee.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("This email belongs to a registered user account and cannot be assigned to an employee.");
        }
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setAddress(dto.getAddress());
        employee.setBio(dto.getBio());
        employee.setDepartment(resolveDepartment(dto.getDepartmentId()));
        employee.setStatus(resolveStatus(dto.getStatusId()));
        employee.setGender(dto.getGender());
        employee.setSalary(dto.getSalary());
        employee.setExperienceYears(dto.getExperienceYears());
        employee.setRating(dto.getRating());
        employee.setIsRemote(dto.getIsRemote());
        employee.setDateOfBirth(dto.getDateOfBirth());
        employee.setJoiningDate(dto.getJoiningDate());
        return toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        employeeRepository.delete(findOrThrow(id));
    }

    @Override
    @Transactional
    public EmployeeResponseDto uploadProfileImage(Long id, MultipartFile file) {
        Employee employee = findOrThrow(id);
        try {
            employee.setProfileImageData(file.getBytes());
            employee.setProfileImageContentType(file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read profile image", e);
        }
        return toDto(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponseDto uploadResume(Long id, MultipartFile file) {
        Employee employee = findOrThrow(id);
        try {
            employee.setResumeData(file.getBytes());
            employee.setResumeContentType(file.getContentType());
            employee.setResumeFileName(file.getOriginalFilename());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resume", e);
        }
        return toDto(employeeRepository.save(employee));
    }

    @Override
    public FileResponse getProfileImage(Long id) {
        Employee employee = findOrThrow(id);
        if (employee.getProfileImageData() == null) {
            throw new ResourceNotFoundException("No profile image for employee: " + id);
        }
        return new FileResponse(employee.getProfileImageData(), employee.getProfileImageContentType(), null);
    }

    @Override
    public FileResponse getResume(Long id) {
        Employee employee = findOrThrow(id);
        if (employee.getResumeData() == null) {
            throw new ResourceNotFoundException("No resume for employee: " + id);
        }
        return new FileResponse(employee.getResumeData(), employee.getResumeContentType(), employee.getResumeFileName());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Employee findOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

    private Department resolveDepartment(Long id) {
        if (id == null) return null;
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
    }

    private EmploymentStatus resolveStatus(Long id) {
        if (id == null) return null;
        return statusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Status not found with id: " + id));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    private PagedResponse<EmployeeResponseDto> toPagedResponse(Page<Employee> p) {
        return PagedResponse.<EmployeeResponseDto>builder()
                .content(p.getContent().stream().map(this::toDto).toList())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }

    private Employee toEntity(EmployeeRequestDto dto) {
        return Employee.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .bio(dto.getBio())
                .department(resolveDepartment(dto.getDepartmentId()))
                .status(resolveStatus(dto.getStatusId()))
                .gender(dto.getGender())
                .salary(dto.getSalary())
                .experienceYears(dto.getExperienceYears())
                .rating(dto.getRating())
                .isRemote(dto.getIsRemote())
                .dateOfBirth(dto.getDateOfBirth())
                .joiningDate(dto.getJoiningDate())
                .build();
    }

    private EmployeeResponseDto toDto(Employee e) {
        return EmployeeResponseDto.builder()
                .id(e.getId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .address(e.getAddress())
                .bio(e.getBio())
                .department(e.getDepartment() != null ? DepartmentDto.builder()
                        .id(e.getDepartment().getId())
                        .name(e.getDepartment().getName())
                        .description(e.getDepartment().getDescription())
                        .build() : null)
                .status(e.getStatus() != null ? EmploymentStatusDto.builder()
                        .id(e.getStatus().getId())
                        .name(e.getStatus().getName())
                        .description(e.getStatus().getDescription())
                        .build() : null)
                .gender(e.getGender())
                .salary(e.getSalary())
                .experienceYears(e.getExperienceYears())
                .rating(e.getRating())
                .isRemote(e.getIsRemote())
                .dateOfBirth(e.getDateOfBirth())
                .joiningDate(e.getJoiningDate())
                .profileImageUrl(e.getProfileImageData() != null ? "/api/employees/" + e.getId() + "/profile-image" : null)
                .resumeUrl(e.getResumeData() != null ? "/api/employees/" + e.getId() + "/resume" : null)
                .resumeFileName(e.getResumeFileName())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
