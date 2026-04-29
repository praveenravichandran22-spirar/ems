package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.EmploymentStatusDto;
import com.minifullstack.ems.service.EmploymentStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statuses")
@RequiredArgsConstructor
public class EmploymentStatusController {

    private final EmploymentStatusService statusService;

    @GetMapping
    public ResponseEntity<List<EmploymentStatusDto>> getAll() {
        return ResponseEntity.ok(statusService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmploymentStatusDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(statusService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<EmploymentStatusDto> create(@RequestBody EmploymentStatusDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(statusService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<EmploymentStatusDto> update(@PathVariable Long id, @RequestBody EmploymentStatusDto dto) {
        return ResponseEntity.ok(statusService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        statusService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
