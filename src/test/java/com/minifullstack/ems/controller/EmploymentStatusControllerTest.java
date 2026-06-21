package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.EmploymentStatusDto;
import com.minifullstack.ems.service.EmploymentStatusService;
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
class EmploymentStatusControllerTest {

    @Mock private EmploymentStatusService statusService;

    @InjectMocks private EmploymentStatusController statusController;

    private EmploymentStatusDto dto(Long id, String name) {
        return EmploymentStatusDto.builder().id(id).name(name).description("desc").build();
    }

    @Test
    void getAll_returns200WithList() {
        when(statusService.getAll()).thenReturn(List.of(dto(1L, "Active"), dto(2L, "Inactive")));

        ResponseEntity<List<EmploymentStatusDto>> response = statusController.getAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getById_returns200WithStatus() {
        when(statusService.getById(1L)).thenReturn(dto(1L, "Active"));

        ResponseEntity<EmploymentStatusDto> response = statusController.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Active");
    }

    @Test
    void create_returns201WithSavedStatus() {
        EmploymentStatusDto input = dto(null, "On Leave");
        EmploymentStatusDto saved = dto(3L, "On Leave");
        when(statusService.create(any())).thenReturn(saved);

        ResponseEntity<EmploymentStatusDto> response = statusController.create(input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isEqualTo(3L);
    }

    @Test
    void update_returns200WithUpdatedStatus() {
        EmploymentStatusDto input = dto(null, "Active Updated");
        EmploymentStatusDto updated = dto(1L, "Active Updated");
        when(statusService.update(eq(1L), any())).thenReturn(updated);

        ResponseEntity<EmploymentStatusDto> response = statusController.update(1L, input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getName()).isEqualTo("Active Updated");
    }

    @Test
    void delete_returns204AndDeletesStatus() {
        doNothing().when(statusService).delete(1L);

        ResponseEntity<Void> response = statusController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(statusService).delete(1L);
    }
}
