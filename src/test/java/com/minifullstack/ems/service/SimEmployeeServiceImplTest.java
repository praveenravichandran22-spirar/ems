package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.SimEmployeeResponseDto;
import com.minifullstack.ems.entity.SimEmployee;
import com.minifullstack.ems.repository.SimEmployeeRepository;
import com.minifullstack.ems.service.impl.SimEmployeeServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SimEmployeeServiceImplTest {

    @Mock private SimEmployeeRepository repository;

    @InjectMocks private SimEmployeeServiceImpl simEmployeeService;

    @Test
    void search_returnsPagedResponse() {
        SimEmployee sim = new SimEmployee();
        Page<SimEmployee> page = new PageImpl<>(List.of(sim));
        when(repository.search(anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(page);

        PagedResponse<SimEmployeeResponseDto> result =
                simEmployeeService.search("john", "Engineering", "Active", 0, 10, "id", "asc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void search_withNullFilters_usesEmptyDefaults() {
        Page<SimEmployee> page = new PageImpl<>(List.of());
        when(repository.search(eq(""), eq(""), eq(""), any(Pageable.class))).thenReturn(page);

        PagedResponse<SimEmployeeResponseDto> result =
                simEmployeeService.search(null, null, null, 0, 10, "id", "asc");

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void search_withDescSort_returnsResults() {
        Page<SimEmployee> page = new PageImpl<>(List.of(new SimEmployee()));
        when(repository.search(anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(page);

        PagedResponse<SimEmployeeResponseDto> result =
                simEmployeeService.search("", "", "", 0, 10, "lastName", "desc");

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void search_returnsEmptyPage_whenNoResults() {
        Page<SimEmployee> emptyPage = new PageImpl<>(List.of());
        when(repository.search(anyString(), anyString(), anyString(), any(Pageable.class))).thenReturn(emptyPage);

        PagedResponse<SimEmployeeResponseDto> result =
                simEmployeeService.search("unknown", "", "", 0, 10, "id", "asc");

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }
}
