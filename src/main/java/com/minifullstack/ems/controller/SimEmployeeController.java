package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.SimEmployeeResponseDto;
import com.minifullstack.ems.service.SimEmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sim-employees")
@RequiredArgsConstructor
public class SimEmployeeController {

    private final SimEmployeeService service;

    @GetMapping("/search")
    public ResponseEntity<PagedResponse<SimEmployeeResponseDto>> search(
            @RequestParam(required = false)             String keyword,
            @RequestParam(required = false)             String department,
            @RequestParam(required = false)             String status,
            @RequestParam(defaultValue = "0")           int    page,
            @RequestParam(defaultValue = "10")          int    size,
            @RequestParam(defaultValue = "firstName")   String sortBy,
            @RequestParam(defaultValue = "asc")         String sortDir
    ) {
        return ResponseEntity.ok(
                service.search(keyword, department, status, page, size, sortBy, sortDir));
    }
}
