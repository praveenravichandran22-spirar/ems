package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.request.EmployeeRequestDto;
import com.minifullstack.ems.dto.response.EmployeeResponseDto;
import com.minifullstack.ems.dto.response.FileResponse;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // ── Create ───────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<EmployeeResponseDto> create(@RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(dto));
    }

    // ── Get by ID ────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    // ── List all (paginated + sortable) ──────────────────────────────────────
    @GetMapping
    public ResponseEntity<PagedResponse<EmployeeResponseDto>> getAll(
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(employeeService.getAll(page, size, sortBy, sortDir));
    }

    // ── Search / filter (paginated + sortable) ───────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<EmployeeResponseDto>> search(
            @RequestParam(required = false)     String keyword,
            @RequestParam(required = false)     Long departmentId,
            @RequestParam(required = false)     Long statusId,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(
                employeeService.search(keyword, departmentId, statusId, page, size, sortBy, sortDir));
    }

    // ── Update ───────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> update(
            @PathVariable Long id,
            @RequestBody EmployeeRequestDto dto) {
        return ResponseEntity.ok(employeeService.update(id, dto));
    }

    // ── Delete ───────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Upload profile image ──────────────────────────────────────────────────
    @PostMapping("/{id}/profile-image")
    public ResponseEntity<EmployeeResponseDto> uploadProfileImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(employeeService.uploadProfileImage(id, file));
    }

    // ── Upload resume ─────────────────────────────────────────────────────────
    @PostMapping("/{id}/resume")
    public ResponseEntity<EmployeeResponseDto> uploadResume(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(employeeService.uploadResume(id, file));
    }

    // ── Serve profile image from DB ───────────────────────────────────────────
    @GetMapping("/{id}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long id) {
        FileResponse file = employeeService.getProfileImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.data());
    }

    // ── Serve resume from DB ──────────────────────────────────────────────────
    @GetMapping("/{id}/resume")
    public ResponseEntity<byte[]> getResume(@PathVariable Long id) {
        FileResponse file = employeeService.getResume(id);
        String fileName = file.fileName() != null ? file.fileName() : "resume";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(file.data());
    }
}
