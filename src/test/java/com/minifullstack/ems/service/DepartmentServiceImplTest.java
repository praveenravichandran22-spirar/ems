package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.DepartmentDto;
import com.minifullstack.ems.entity.Department;
import com.minifullstack.ems.exception.ResourceNotFoundException;
import com.minifullstack.ems.repository.DepartmentRepository;
import com.minifullstack.ems.service.impl.DepartmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceImplTest {

    @Mock private DepartmentRepository departmentRepository;

    @InjectMocks private DepartmentServiceImpl departmentService;

    private Department engineering;

    @BeforeEach
    void setUp() {
        engineering = Department.builder().id(1L).name("Engineering").description("Eng dept").build();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_savesAndReturnsDepartment() {
        DepartmentDto dto = DepartmentDto.builder().name("Engineering").description("Eng dept").build();
        when(departmentRepository.existsByName("Engineering")).thenReturn(false);
        when(departmentRepository.save(any())).thenReturn(engineering);

        DepartmentDto result = departmentService.create(dto);

        assertThat(result.getName()).isEqualTo("Engineering");
        assertThat(result.getId()).isEqualTo(1L);
        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void create_throwsWhenNameAlreadyExists() {
        DepartmentDto dto = DepartmentDto.builder().name("Engineering").build();
        when(departmentRepository.existsByName("Engineering")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_returnsDto_whenFound() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(engineering));

        DepartmentDto result = departmentService.getById(1L);

        assertThat(result.getName()).isEqualTo("Engineering");
    }

    @Test
    void getById_throwsResourceNotFoundException_whenNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Department not found");
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsAllDepartments() {
        Department hr = Department.builder().id(2L).name("HR").build();
        when(departmentRepository.findAll()).thenReturn(List.of(engineering, hr));

        List<DepartmentDto> result = departmentService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(DepartmentDto::getName).contains("Engineering", "HR");
    }

    @Test
    void getAll_returnsEmptyListWhenNoDepartments() {
        when(departmentRepository.findAll()).thenReturn(List.of());

        assertThat(departmentService.getAll()).isEmpty();
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_updatesNameAndDescription() {
        DepartmentDto dto = DepartmentDto.builder().name("Engineering Updated").description("New desc").build();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(engineering));
        when(departmentRepository.existsByName("Engineering Updated")).thenReturn(false);
        when(departmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DepartmentDto result = departmentService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("Engineering Updated");
        assertThat(result.getDescription()).isEqualTo("New desc");
    }

    @Test
    void update_allowsSameNameWithoutConflict() {
        DepartmentDto dto = DepartmentDto.builder().name("Engineering").description("Same name").build();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(engineering));
        when(departmentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DepartmentDto result = departmentService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("Engineering");
        verify(departmentRepository, never()).existsByName(anyString());
    }

    @Test
    void update_throwsWhenNewNameAlreadyInUse() {
        DepartmentDto dto = DepartmentDto.builder().name("HR").build();
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(engineering));
        when(departmentRepository.existsByName("HR")).thenReturn(true);

        assertThatThrownBy(() -> departmentService.update(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void update_throwsWhenDepartmentNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.update(99L, DepartmentDto.builder().name("X").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_deletesDepartment() {
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(engineering));

        departmentService.delete(1L);

        verify(departmentRepository).delete(engineering);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(departmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> departmentService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
