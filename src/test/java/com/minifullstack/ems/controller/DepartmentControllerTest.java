package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.DepartmentDto;
import com.minifullstack.ems.service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    @Mock private DepartmentService departmentService;

    @InjectMocks private DepartmentController departmentController;

    private DepartmentDto dto(Long id, String name) {
        return DepartmentDto.builder().id(id).name(name).description("desc").build();
    }

    @Test
    void getAll_returns200WithList() {
        when(departmentService.getAll()).thenReturn(List.of(dto(1L, "Engineering"), dto(2L, "HR")));

        ResponseEntity<List<DepartmentDto>> response = departmentController.getAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getById_returns200WithDepartment() {
        when(departmentService.getById(1L)).thenReturn(dto(1L, "Engineering"));

        ResponseEntity<DepartmentDto> response = departmentController.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Engineering");
    }

    @Test
    void create_returns201WithSavedDepartment() {
        DepartmentDto input = dto(null, "Finance");
        DepartmentDto saved = dto(3L, "Finance");
        when(departmentService.create(any())).thenReturn(saved);

        ResponseEntity<DepartmentDto> response = departmentController.create(input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isEqualTo(3L);
        verify(departmentService).create(input);
    }

    @Test
    void update_returns200WithUpdatedDepartment() {
        DepartmentDto input = dto(null, "Engineering Updated");
        DepartmentDto updated = dto(1L, "Engineering Updated");
        when(departmentService.update(eq(1L), any())).thenReturn(updated);

        ResponseEntity<DepartmentDto> response = departmentController.update(1L, input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Engineering Updated");
    }

    @Test
    void delete_returns204AndDeletesDepartment() {
        doNothing().when(departmentService).delete(1L);

        ResponseEntity<Void> response = departmentController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(departmentService).delete(1L);
    }
}
