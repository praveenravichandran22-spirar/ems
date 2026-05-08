package com.minifullstack.ems.service.impl;

import com.minifullstack.ems.dto.CountryDto;

import java.util.List;

public interface CountryService {
    CountryDto create(CountryDto countryDto);
    CountryDto getById(Long id);
    List<CountryDto> getAll();
    CountryDto update(Long id, CountryDto dto);
    void delete(Long id);
}
