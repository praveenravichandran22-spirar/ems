package com.minifullstack.ems.repository;

import com.minifullstack.ems.entity.Country;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CountryRepository extends JpaRepository<Country, Long> {
    Optional<Country> findByCountryName(String countryName);
    boolean existsByCountryName(String countryName);
}
