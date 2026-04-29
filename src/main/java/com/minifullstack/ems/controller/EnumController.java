package com.minifullstack.ems.controller;

import com.minifullstack.ems.enums.Gender;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enums")
public class EnumController {

    // Departments and statuses are now DB-backed — use /api/departments and /api/statuses
    @GetMapping
    public ResponseEntity<Map<String, List<String>>> getAllEnums() {
        return ResponseEntity.ok(Map.of(
                "genders", enumNames(Gender.class)
        ));
    }

    @GetMapping("/genders")
    public ResponseEntity<List<String>> getGenders() {
        return ResponseEntity.ok(enumNames(Gender.class));
    }

    private <E extends Enum<E>> List<String> enumNames(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .toList();
    }
}
