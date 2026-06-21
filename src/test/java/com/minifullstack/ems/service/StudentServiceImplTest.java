package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.request.StudentRequestDto;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.StudentResponseDto;
import com.minifullstack.ems.entity.Student;
import com.minifullstack.ems.exception.ResourceNotFoundException;
import com.minifullstack.ems.repository.StudentRepository;
import com.minifullstack.ems.service.impl.StudentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock private StudentRepository studentRepository;

    @InjectMocks private StudentServiceImpl studentService;

    private Student student;
    private StudentRequestDto requestDto;

    @BeforeEach
    void setUp() {
        student = Student.builder()
                .id(1L)
                .firstName("Alice").lastName("Brown")
                .email("alice@student.com")
                .enrollmentNumber("EN001")
                .course("Computer Science")
                .enrollmentDate(LocalDate.of(2024, Month.JANUARY, 15))
                .isActive(true)
                .build();

        requestDto = new StudentRequestDto();
        requestDto.setFirstName("Alice");
        requestDto.setLastName("Brown");
        requestDto.setEmail("alice@student.com");
        requestDto.setEnrollmentNumber("EN001");
        requestDto.setCourse("Computer Science");
        requestDto.setEnrollmentDate(LocalDate.of(2024, Month.JANUARY, 15));
        requestDto.setIsActive(true);
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_savesAndReturnsDto() {
        when(studentRepository.existsByEmail("alice@student.com")).thenReturn(false);
        when(studentRepository.existsByEnrollmentNumber("EN001")).thenReturn(false);
        when(studentRepository.save(any())).thenReturn(student);

        StudentResponseDto result = studentService.create(requestDto);

        assertThat(result.getEmail()).isEqualTo("alice@student.com");
        assertThat(result.getEnrollmentNumber()).isEqualTo("EN001");
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void create_throwsWhenEmailAlreadyInUse() {
        when(studentRepository.existsByEmail("alice@student.com")).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void create_throwsWhenEnrollmentNumberAlreadyInUse() {
        when(studentRepository.existsByEmail("alice@student.com")).thenReturn(false);
        when(studentRepository.existsByEnrollmentNumber("EN001")).thenReturn(true);

        assertThatThrownBy(() -> studentService.create(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Enrollment number already in use");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_returnsDto_whenFound() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        StudentResponseDto result = studentService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getCourse()).isEqualTo("Computer Science");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Student not found");
    }

    // ── search ────────────────────────────────────────────────────────────────

    @Test
    void search_returnsPagedResponse() {
        Page<Student> page = new PageImpl<>(List.of(student));
        when(studentRepository.search(anyString(), anyString(), anyInt(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        PagedResponse<StudentResponseDto> result =
                studentService.search("alice", "CS", 1, "Male", 0, 10, "id", "asc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void search_withNullFilters_usesDefaults() {
        Page<Student> page = new PageImpl<>(List.of(student));
        when(studentRepository.search(eq(""), eq(""), eq(0), eq(""), any(Pageable.class)))
                .thenReturn(page);

        PagedResponse<StudentResponseDto> result =
                studentService.search(null, null, null, null, 0, 10, "id", "asc");

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void search_withDescSort_returnsResults() {
        Page<Student> page = new PageImpl<>(List.of(student));
        when(studentRepository.search(anyString(), anyString(), anyInt(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        PagedResponse<StudentResponseDto> result =
                studentService.search(null, null, null, null, 0, 10, "lastName", "desc");

        assertThat(result.getContent()).hasSize(1);
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_updatesFieldsAndSaves() {
        requestDto.setFirstName("Alice Updated");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any())).thenReturn(student);

        studentService.update(1L, requestDto);

        assertThat(student.getFirstName()).isEqualTo("Alice Updated");
        verify(studentRepository).save(student);
    }

    @Test
    void update_throwsWhenEmailChangedToExistingOne() {
        requestDto.setEmail("taken@student.com");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEmail("taken@student.com")).thenReturn(true);

        assertThatThrownBy(() -> studentService.update(1L, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void update_throwsWhenEnrollmentNumberChangedToExistingOne() {
        requestDto.setEnrollmentNumber("EN999");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByEnrollmentNumber("EN999")).thenReturn(true);

        assertThatThrownBy(() -> studentService.update(1L, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Enrollment number already in use");
    }

    @Test
    void update_throwsWhenStudentNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.update(99L, requestDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_deletesStudent() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentService.delete(1L);

        verify(studentRepository).delete(student);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(studentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── uploadProfileImage ────────────────────────────────────────────────────

    @Test
    void uploadProfileImage_savesImageData() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(file.getContentType()).thenReturn("image/jpeg");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(studentRepository.save(any())).thenReturn(student);

        studentService.uploadProfileImage(1L, file);

        assertThat(student.getProfileImageData()).isEqualTo(new byte[]{1, 2, 3});
        assertThat(student.getProfileImageContentType()).isEqualTo("image/jpeg");
    }

    @Test
    void uploadProfileImage_throwsRuntimeException_onIOError() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("disk error"));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> studentService.uploadProfileImage(1L, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to read profile image");
    }

    // ── getProfileImage ───────────────────────────────────────────────────────

    @Test
    void getProfileImage_returnsFileResponse_whenImageExists() {
        student.setProfileImageData(new byte[]{1, 2});
        student.setProfileImageContentType("image/png");
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        var result = studentService.getProfileImage(1L);

        assertThat(result.data()).isEqualTo(new byte[]{1, 2});
        assertThat(result.contentType()).isEqualTo("image/png");
    }

    @Test
    void getProfileImage_throwsWhenNoImageSet() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        assertThatThrownBy(() -> studentService.getProfileImage(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No profile image for student");
    }
}
