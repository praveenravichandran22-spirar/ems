package com.minifullstack.ems.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.minifullstack.ems.dto.request.StudentRequestDto;
import com.minifullstack.ems.dto.response.FileResponse;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.StudentResponseDto;
import com.minifullstack.ems.service.StudentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    // ── Create ───────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<StudentResponseDto> create(@RequestBody StudentRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(dto));
    }

    // ── Get by ID ────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(studentService.getById(id));
    }

    // ── Search / filter (paginated + sortable) ───────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<PagedResponse<StudentResponseDto>> search(
            @RequestParam(required = false)     String keyword,
            @RequestParam(required = false)     String course,
            @RequestParam(required = false)     Integer year,
            @RequestParam(required = false)     String gender,
            @RequestParam(defaultValue = "0")   int page,
            @RequestParam(defaultValue = "10")  int size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        return ResponseEntity.ok(
                studentService.search(keyword, course, year, gender, page, size, sortBy, sortDir));
    }

    // ── Update ───────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponseDto> update(
            @PathVariable Long id,
            @RequestBody StudentRequestDto dto) {
        return ResponseEntity.ok(studentService.update(id, dto));
    }

    // ── Delete ───────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        studentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Upload profile image ──────────────────────────────────────────────────
    @PostMapping("/{id}/profile-image")
    public ResponseEntity<StudentResponseDto> uploadProfileImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(studentService.uploadProfileImage(id, file));
    }

    // ── Serve profile image from DB ───────────────────────────────────────────
    @GetMapping("/{id}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long id) {
        FileResponse file = studentService.getProfileImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.contentType()))
                .body(file.data());
    }
}