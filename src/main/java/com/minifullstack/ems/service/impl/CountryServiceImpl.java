package com.minifullstack.ems.service.impl;

import com.minifullstack.ems.dto.CountryDto;
import com.minifullstack.ems.entity.Country;
import com.minifullstack.ems.exception.ResourceNotFoundException;
import com.minifullstack.ems.repository.CountryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CountryServiceImpl implements CountryService {

    private final CountryRepository countryRepository;

    @Override
    @Transactional
    public CountryDto create(CountryDto dto) {
        if (countryRepository.existsByCountryName(dto.getCountryName())) {
            throw new IllegalArgumentException("Country already exists" + dto.getCountryName());
        }
        return toDto(countryRepository.save(toEntity(dto)));
    }

    @Override
    public CountryDto getById(Long id) { return toDto(findOrThrow(id));}

    @Override
    public List<CountryDto> getAll() { return countryRepository.findAll().stream().map(this::toDto).toList(); }

    @Override
    @Transactional
    public CountryDto update(Long id, CountryDto dto) {
        Country ctry = findOrThrow(id);
        if (!ctry.getCountryName().equals(dto.getCountryName()) && countryRepository.existsByCountryName(dto.getCountryName())) {
            throw new IllegalArgumentException("Country name already in use:" + dto.getCountryName());
        }
        ctry.setCountryName(dto.getCountryName());
        return toDto(countryRepository.save(ctry));
    }

    @Override
    @Transactional
    public void delete(Long id) { countryRepository.delete(findOrThrow(id)); }

    private Country findOrThrow(Long id) {
        return countryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Country not found with id" + id));
    }

    public CountryDto toDto(Country c) {
        return CountryDto.builder()
                .id(c.getId())
                .countryName(c.getCountryName())
                .build();
    }

    private Country toEntity(CountryDto dto) {
        return Country.builder()
                .countryName(dto.getCountryName())
                .build();
    }
}
