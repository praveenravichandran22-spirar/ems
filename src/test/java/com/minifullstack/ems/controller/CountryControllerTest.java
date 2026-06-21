package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.CountryDto;
import com.minifullstack.ems.service.impl.CountryService;
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
class CountryControllerTest {

    @Mock private CountryService countryService;

    @InjectMocks private CountryController countryController;

    private CountryDto dto(Long id, String name) {
        return CountryDto.builder().id(id).countryName(name).build();
    }

    @Test
    void getAll_returns200WithList() {
        when(countryService.getAll()).thenReturn(List.of(dto(1L, "India"), dto(2L, "USA")));

        ResponseEntity<List<CountryDto>> response = countryController.getAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getById_returns200WithCountry() {
        when(countryService.getById(1L)).thenReturn(dto(1L, "India"));

        ResponseEntity<CountryDto> response = countryController.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCountryName()).isEqualTo("India");
    }

    @Test
    void create_returns201WithSavedCountry() {
        CountryDto input = dto(null, "Germany");
        CountryDto saved = dto(3L, "Germany");
        when(countryService.create(any())).thenReturn(saved);

        ResponseEntity<CountryDto> response = countryController.create(input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getId()).isEqualTo(3L);
    }

    @Test
    void update_returns200WithUpdatedCountry() {
        CountryDto input = dto(null, "Bharat");
        CountryDto updated = dto(1L, "Bharat");
        when(countryService.update(eq(1L), any())).thenReturn(updated);

        ResponseEntity<CountryDto> response = countryController.update(1L, input);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCountryName()).isEqualTo("Bharat");
    }

    @Test
    void delete_returns204AndDeletesCountry() {
        doNothing().when(countryService).delete(1L);

        ResponseEntity<Void> response = countryController.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(countryService).delete(1L);
    }
}
