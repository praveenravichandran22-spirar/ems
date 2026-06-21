package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.EmploymentStatusDto;
import com.minifullstack.ems.entity.EmploymentStatus;
import com.minifullstack.ems.exception.ResourceNotFoundException;
import com.minifullstack.ems.repository.EmploymentStatusRepository;
import com.minifullstack.ems.service.impl.EmploymentStatusServiceImpl;
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
class EmploymentStatusServiceImplTest {

    @Mock private EmploymentStatusRepository statusRepository;

    @InjectMocks private EmploymentStatusServiceImpl statusService;

    private EmploymentStatus active;

    @BeforeEach
    void setUp() {
        active = EmploymentStatus.builder().id(1L).name("Active").description("Full-time active").build();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_savesAndReturnsDto() {
        EmploymentStatusDto dto = EmploymentStatusDto.builder().name("Active").description("Full-time").build();
        when(statusRepository.existsByName("Active")).thenReturn(false);
        when(statusRepository.save(any())).thenReturn(active);

        EmploymentStatusDto result = statusService.create(dto);

        assertThat(result.getName()).isEqualTo("Active");
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void create_throwsWhenNameAlreadyExists() {
        EmploymentStatusDto dto = EmploymentStatusDto.builder().name("Active").build();
        when(statusRepository.existsByName("Active")).thenReturn(true);

        assertThatThrownBy(() -> statusService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_returnsDto_whenFound() {
        when(statusRepository.findById(1L)).thenReturn(Optional.of(active));

        EmploymentStatusDto result = statusService.getById(1L);

        assertThat(result.getName()).isEqualTo("Active");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(statusRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statusService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Status not found");
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsAllStatuses() {
        EmploymentStatus inactive = EmploymentStatus.builder().id(2L).name("Inactive").build();
        when(statusRepository.findAll()).thenReturn(List.of(active, inactive));

        List<EmploymentStatusDto> result = statusService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(EmploymentStatusDto::getName).contains("Active", "Inactive");
    }

    @Test
    void getAll_returnsEmptyList_whenNoneExist() {
        when(statusRepository.findAll()).thenReturn(List.of());

        assertThat(statusService.getAll()).isEmpty();
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_updatesNameAndDescription() {
        EmploymentStatusDto dto = EmploymentStatusDto.builder().name("On Leave").description("Temp leave").build();
        when(statusRepository.findById(1L)).thenReturn(Optional.of(active));
        when(statusRepository.existsByName("On Leave")).thenReturn(false);
        when(statusRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmploymentStatusDto result = statusService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("On Leave");
        assertThat(result.getDescription()).isEqualTo("Temp leave");
    }

    @Test
    void update_allowsSameNameWithoutConflict() {
        EmploymentStatusDto dto = EmploymentStatusDto.builder().name("Active").description("Updated desc").build();
        when(statusRepository.findById(1L)).thenReturn(Optional.of(active));
        when(statusRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        EmploymentStatusDto result = statusService.update(1L, dto);

        assertThat(result.getName()).isEqualTo("Active");
        verify(statusRepository, never()).existsByName(anyString());
    }

    @Test
    void update_throwsWhenNewNameAlreadyInUse() {
        EmploymentStatusDto dto = EmploymentStatusDto.builder().name("Inactive").build();
        when(statusRepository.findById(1L)).thenReturn(Optional.of(active));
        when(statusRepository.existsByName("Inactive")).thenReturn(true);

        assertThatThrownBy(() -> statusService.update(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void update_throwsWhenStatusNotFound() {
        when(statusRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statusService.update(99L, EmploymentStatusDto.builder().name("X").build()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_deletesStatus() {
        when(statusRepository.findById(1L)).thenReturn(Optional.of(active));

        statusService.delete(1L);

        verify(statusRepository).delete(active);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(statusRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> statusService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
