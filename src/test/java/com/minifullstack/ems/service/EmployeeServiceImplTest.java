package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.request.EmployeeRequestDto;
import com.minifullstack.ems.dto.response.EmployeeResponseDto;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.entity.*;
import com.minifullstack.ems.enums.WorkflowStatus;
import com.minifullstack.ems.exception.ResourceNotFoundException;
import com.minifullstack.ems.repository.*;
import com.minifullstack.ems.service.impl.EmployeeServiceImpl;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock private EmployeeRepository employeeRepository;
    @Mock private DepartmentRepository departmentRepository;
    @Mock private EmploymentStatusRepository statusRepository;
    @Mock private CountryRepository countryRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private EmployeeServiceImpl employeeService;

    private Employee employee;
    private EmployeeRequestDto requestDto;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .id(1L)
                .firstName("John").lastName("Doe")
                .email("john@test.com")
                .isRemote(false)
                .joiningDate(LocalDate.now())
                .workflowStatus(WorkflowStatus.DRAFT)
                .build();

        requestDto = new EmployeeRequestDto();
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setEmail("john@test.com");
        requestDto.setIsRemote(false);
        requestDto.setJoiningDate(LocalDate.now());
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_savesAndReturnsDto() {
        when(employeeRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(employeeRepository.save(any())).thenReturn(employee);

        EmployeeResponseDto result = employeeService.create(requestDto);

        assertThat(result.getEmail()).isEqualTo("john@test.com");
        assertThat(result.getFirstName()).isEqualTo("John");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void create_throwsWhenEmployeeEmailAlreadyInUse() {
        when(employeeRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void create_throwsWhenEmailBelongsToUser() {
        when(employeeRepository.existsByEmail("john@test.com")).thenReturn(false);
        when(userRepository.existsByEmail("john@test.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.create(requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registered user account");
    }

    @Test
    void create_resolvesDepartmentWhenIdProvided() {
        Department dept = Department.builder().id(1L).name("Engineering").build();
        requestDto.setDepartmentId(1L);

        when(employeeRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(dept));
        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.create(requestDto);

        verify(departmentRepository).findById(1L);
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_returnsDto_whenFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        EmployeeResponseDto result = employeeService.getById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@test.com");
    }

    @Test
    void getById_throwsResourceNotFound_whenMissing() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Employee not found");
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsPagedResponse() {
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<EmployeeResponseDto> result = employeeService.getAll(0, 10, "id", "asc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getAll_withDescSort_returnsPagedResponse() {
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAll(any(Pageable.class))).thenReturn(page);

        PagedResponse<EmployeeResponseDto> result = employeeService.getAll(0, 10, "lastName", "desc");

        assertThat(result.getContent()).hasSize(1);
    }

    // ── search ────────────────────────────────────────────────────────────────

    @Test
    void search_withKeyword_delegatesToRepository() {
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.search(anyString(), anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(page);

        PagedResponse<EmployeeResponseDto> result =
                employeeService.search("john", null, null, 0, 10, "id", "asc");

        assertThat(result.getContent()).hasSize(1);
        verify(employeeRepository).search(eq("john"), eq(0L), eq(0L), any(Pageable.class));
    }

    @Test
    void search_withDepartmentAndStatusFilter_passesCorrectIds() {
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.search(anyString(), anyLong(), anyLong(), any(Pageable.class)))
                .thenReturn(page);

        employeeService.search(null, 2L, 3L, 0, 10, "id", "asc");

        verify(employeeRepository).search(eq(""), eq(2L), eq(3L), any(Pageable.class));
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_updatesFieldsAndReturnsDto() {
        requestDto.setFirstName("Jane");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.update(1L, requestDto);

        assertThat(employee.getFirstName()).isEqualTo("Jane");
        verify(employeeRepository).save(employee);
    }

    @Test
    void update_throwsWhenEmailChangedToExistingEmployeeEmail() {
        requestDto.setEmail("taken@test.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.update(1L, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already in use");
    }

    @Test
    void update_throwsWhenEmailChangedToUserEmail() {
        requestDto.setEmail("user@test.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByEmail("user@test.com")).thenReturn(false);
        when(userRepository.existsByEmail("user@test.com")).thenReturn(true);

        assertThatThrownBy(() -> employeeService.update(1L, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("registered user account");
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_deletesEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        employeeService.delete(1L);

        verify(employeeRepository).delete(employee);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── uploadProfileImage ────────────────────────────────────────────────────

    @Test
    void uploadProfileImage_savesImageDataToEmployee() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn("photo.jpg");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.uploadProfileImage(1L, file);

        assertThat(employee.getProfileImageData()).isEqualTo(new byte[]{1, 2, 3});
        assertThat(employee.getProfileImageContentType()).isEqualTo("image/jpeg");
        assertThat(employee.getProfileImageFileName()).isEqualTo("photo.jpg");
    }

    @Test
    void uploadProfileImage_throwsRuntimeException_whenIOFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenThrow(new IOException("disk full"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> employeeService.uploadProfileImage(1L, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to read profile image");
    }

    // ── uploadResume ──────────────────────────────────────────────────────────

    @Test
    void uploadResume_savesResumeToEmployee() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(500L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getBytes()).thenReturn(new byte[]{10, 20});
        when(file.getOriginalFilename()).thenReturn("cv.pdf");

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.uploadResume(1L, file);

        assertThat(employee.getResumeData()).isEqualTo(new byte[]{10, 20});
        assertThat(employee.getResumeContentType()).isEqualTo("application/pdf");
    }

    @Test
    void uploadResume_throwsWhenFileTooLarge() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(11L * 1024 * 1024);

        assertThatThrownBy(() -> employeeService.uploadResume(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("10mb");
    }

    @Test
    void uploadResume_throwsWhenNotPdf() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(500L);
        when(file.getContentType()).thenReturn("image/jpeg");

        assertThatThrownBy(() -> employeeService.uploadResume(1L, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only PDF");
    }

    @Test
    void uploadResume_throwsRuntimeException_whenIOFails() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(500L);
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getBytes()).thenThrow(new IOException("disk error"));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> employeeService.uploadResume(1L, file))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Failed to read resume");
    }

    // ── getProfileImage ───────────────────────────────────────────────────────

    @Test
    void getProfileImage_returnsFileResponse_whenImageExists() {
        employee.setProfileImageData(new byte[]{1, 2});
        employee.setProfileImageContentType("image/jpeg");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var result = employeeService.getProfileImage(1L);

        assertThat(result.data()).isEqualTo(new byte[]{1, 2});
        assertThat(result.contentType()).isEqualTo("image/jpeg");
    }

    @Test
    void getProfileImage_throwsWhenNoImageSet() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> employeeService.getProfileImage(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No profile image");
    }

    // ── getResume ─────────────────────────────────────────────────────────────

    @Test
    void getResume_returnsFileResponse_whenResumeExists() {
        employee.setResumeData(new byte[]{5, 6});
        employee.setResumeContentType("application/pdf");
        employee.setResumeFileName("cv.pdf");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        var result = employeeService.getResume(1L);

        assertThat(result.data()).isEqualTo(new byte[]{5, 6});
        assertThat(result.fileName()).isEqualTo("cv.pdf");
    }

    @Test
    void getResume_throwsWhenNoResumeSet() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> employeeService.getResume(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("No resume");
    }

    // ── removeProfileImage ────────────────────────────────────────────────────

    @Test
    void removeProfileImage_clearsImageFields() {
        employee.setProfileImageData(new byte[]{1});
        employee.setProfileImageContentType("image/jpeg");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.removeProfileImage(1L);

        assertThat(employee.getProfileImageData()).isNull();
        assertThat(employee.getProfileImageContentType()).isNull();
    }

    // ── removeResume ──────────────────────────────────────────────────────────

    @Test
    void removeResume_clearsResumeFields() {
        employee.setResumeData(new byte[]{1});
        employee.setResumeContentType("application/pdf");
        employee.setResumeFileName("cv.pdf");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenReturn(employee);

        employeeService.removeResume(1L);

        assertThat(employee.getResumeData()).isNull();
        assertThat(employee.getResumeFileName()).isNull();
    }
}
