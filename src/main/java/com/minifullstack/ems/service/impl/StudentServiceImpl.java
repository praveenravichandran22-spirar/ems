package com.minifullstack.ems.service.impl;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.minifullstack.ems.dto.request.StudentRequestDto;
import com.minifullstack.ems.dto.response.FileResponse;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.StudentResponseDto;
import com.minifullstack.ems.entity.Student;
import com.minifullstack.ems.exception.ResourceNotFoundException;
import com.minifullstack.ems.repository.StudentRepository;
import com.minifullstack.ems.service.StudentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;

    @Override
    @Transactional
    public StudentResponseDto create(StudentRequestDto dto) {
        if (studentRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }
        if (studentRepository.existsByEnrollmentNumber(dto.getEnrollmentNumber())) {
            throw new IllegalArgumentException("Enrollment number already in use: " + dto.getEnrollmentNumber());
        }
        return toDto(studentRepository.save(toEntity(dto)));
    }

    @Override
    public StudentResponseDto getById(Long id) {
        return toDto(findOrThrow(id));
    }

    @Override
    public PagedResponse<StudentResponseDto> search(
            String keyword, String course, Integer year, String gender,
            int page, int size, String sortBy, String sortDir) {
        String search = (keyword != null && !keyword.isBlank()) ? keyword.toLowerCase() : "";
        String courseFilter = (course != null && !course.isBlank()) ? course : "";
        Integer yearFilter = (year != null) ? year : 0;
        String genderFilter = (gender != null && !gender.isBlank()) ? gender : "";
        Page<Student> result = studentRepository.search(
                search, courseFilter, yearFilter, genderFilter,
                buildPageable(page, size, sortBy, sortDir)
        );
        return toPagedResponse(result);
    }

    @Override
    @Transactional
    public StudentResponseDto update(Long id, StudentRequestDto dto) {
        Student student = findOrThrow(id);
        if (!student.getEmail().equals(dto.getEmail()) && studentRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }
        if (!student.getEnrollmentNumber().equals(dto.getEnrollmentNumber())
                && studentRepository.existsByEnrollmentNumber(dto.getEnrollmentNumber())) {
            throw new IllegalArgumentException("Enrollment number already in use: " + dto.getEnrollmentNumber());
        }
        student.setFirstName(dto.getFirstName());
        student.setLastName(dto.getLastName());
        student.setEmail(dto.getEmail());
        student.setEnrollmentNumber(dto.getEnrollmentNumber());
        student.setPhone(dto.getPhone());
        student.setAddress(dto.getAddress());
        student.setBio(dto.getBio());
        student.setCourse(dto.getCourse());
        student.setYear(dto.getYear());
        student.setGpa(dto.getGpa());
        student.setGender(dto.getGender());
        student.setDateOfBirth(dto.getDateOfBirth());
        student.setEnrollmentDate(dto.getEnrollmentDate());
        student.setIsActive(dto.getIsActive());
        return toDto(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        studentRepository.delete(findOrThrow(id));
    }

    @Override
    @Transactional
    public StudentResponseDto uploadProfileImage(Long id, MultipartFile file) {
        Student student = findOrThrow(id);
        try {
            student.setProfileImageData(file.getBytes());
            student.setProfileImageContentType(file.getContentType());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read profile image", e);
        }
        return toDto(studentRepository.save(student));
    }

    @Override
    public FileResponse getProfileImage(Long id) {
        Student student = findOrThrow(id);
        if (student.getProfileImageData() == null) {
            throw new ResourceNotFoundException("No profile image for student: " + id);
        }
        return new FileResponse(student.getProfileImageData(), student.getProfileImageContentType(), null);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Student findOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    private PagedResponse<StudentResponseDto> toPagedResponse(Page<Student> p) {
        return PagedResponse.<StudentResponseDto>builder()
                .content(p.getContent().stream().map(this::toDto).toList())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .last(p.isLast())
                .build();
    }

    private Student toEntity(StudentRequestDto dto) {
        return Student.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .enrollmentNumber(dto.getEnrollmentNumber())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .guardianName(dto.getGuardianName())
                .bio(dto.getBio())
                .course(dto.getCourse())
                .year(dto.getYear())
                .gpa(dto.getGpa())
                .gender(dto.getGender())
                .dateOfBirth(dto.getDateOfBirth())
                .enrollmentDate(dto.getEnrollmentDate())
                .isActive(dto.getIsActive())
                .build();
    }

    private StudentResponseDto toDto(Student s) {
        return StudentResponseDto.builder()
                .id(s.getId())
                .firstName(s.getFirstName())
                .lastName(s.getLastName())
                .email(s.getEmail())
                .enrollmentNumber(s.getEnrollmentNumber())
                .phone(s.getPhone())
                .address(s.getAddress())
                .guardianName(s.getGuardianName())
                .bio(s.getBio())
                .course(s.getCourse())
                .year(s.getYear())
                .gpa(s.getGpa())
                .gender(s.getGender())
                .dateOfBirth(s.getDateOfBirth())
                .enrollmentDate(s.getEnrollmentDate())
                .isActive(s.getIsActive())
                .profileImageUrl(s.getProfileImageData() != null
                        ? "/api/students/" + s.getId() + "/profile-image" : null)
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}