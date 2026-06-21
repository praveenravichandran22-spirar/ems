package com.minifullstack.ems.service;

import com.minifullstack.ems.dto.CountryDto;
import com.minifullstack.ems.entity.Country;
import com.minifullstack.ems.exception.ResourceNotFoundException;
import com.minifullstack.ems.repository.CountryRepository;
import com.minifullstack.ems.service.impl.CountryServiceImpl;
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
class CountryServiceImplTest {

    @Mock private CountryRepository countryRepository;

    @InjectMocks private CountryServiceImpl countryService;

    private Country india;

    @BeforeEach
    void setUp() {
        india = Country.builder().id(1L).countryName("India").build();
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void create_savesAndReturnsDto() {
        CountryDto dto = CountryDto.builder().countryName("India").build();
        when(countryRepository.existsByCountryName("India")).thenReturn(false);
        when(countryRepository.save(any())).thenReturn(india);

        CountryDto result = countryService.create(dto);

        assertThat(result.getCountryName()).isEqualTo("India");
        assertThat(result.getId()).isEqualTo(1L);
        verify(countryRepository).save(any(Country.class));
    }

    @Test
    void create_throwsWhenCountryAlreadyExists() {
        CountryDto dto = CountryDto.builder().countryName("India").build();
        when(countryRepository.existsByCountryName("India")).thenReturn(true);

        assertThatThrownBy(() -> countryService.create(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    // ── getById ───────────────────────────────────────────────────────────────

    @Test
    void getById_returnsDto_whenFound() {
        when(countryRepository.findById(1L)).thenReturn(Optional.of(india));

        CountryDto result = countryService.getById(1L);

        assertThat(result.getCountryName()).isEqualTo("India");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(countryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> countryService.getById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Country not found");
    }

    // ── getAll ────────────────────────────────────────────────────────────────

    @Test
    void getAll_returnsAllCountries() {
        Country usa = Country.builder().id(2L).countryName("USA").build();
        when(countryRepository.findAll()).thenReturn(List.of(india, usa));

        List<CountryDto> result = countryService.getAll();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(CountryDto::getCountryName).contains("India", "USA");
    }

    @Test
    void getAll_returnsEmptyList_whenNoneExist() {
        when(countryRepository.findAll()).thenReturn(List.of());

        assertThat(countryService.getAll()).isEmpty();
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void update_updatesCountryName() {
        CountryDto dto = CountryDto.builder().countryName("Bharat").build();
        when(countryRepository.findById(1L)).thenReturn(Optional.of(india));
        when(countryRepository.existsByCountryName("Bharat")).thenReturn(false);
        when(countryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CountryDto result = countryService.update(1L, dto);

        assertThat(result.getCountryName()).isEqualTo("Bharat");
    }

    @Test
    void update_allowsSameName_withoutConflictCheck() {
        CountryDto dto = CountryDto.builder().countryName("India").build();
        when(countryRepository.findById(1L)).thenReturn(Optional.of(india));
        when(countryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CountryDto result = countryService.update(1L, dto);

        assertThat(result.getCountryName()).isEqualTo("India");
        verify(countryRepository, never()).existsByCountryName(anyString());
    }

    @Test
    void update_throwsWhenNewNameAlreadyInUse() {
        CountryDto dto = CountryDto.builder().countryName("USA").build();
        when(countryRepository.findById(1L)).thenReturn(Optional.of(india));
        when(countryRepository.existsByCountryName("USA")).thenReturn(true);

        assertThatThrownBy(() -> countryService.update(1L, dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void update_throwsWhenCountryNotFound() {
        when(countryRepository.findById(99L)).thenReturn(Optional.empty());

        CountryDto dto = CountryDto.builder().countryName("X").build();
        assertThatThrownBy(() -> countryService.update(99L, dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void delete_deletesCountry() {
        when(countryRepository.findById(1L)).thenReturn(Optional.of(india));

        countryService.delete(1L);

        verify(countryRepository).delete(india);
    }

    @Test
    void delete_throwsWhenNotFound() {
        when(countryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> countryService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
