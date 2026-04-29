package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.UserResponseDto;
import com.minifullstack.ems.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserManagementService userManagementService;

    @GetMapping
    public ResponseEntity<PagedResponse<UserResponseDto>> search(
            @RequestParam(required = false)             String keyword,
            @RequestParam(required = false)             String role,
            @RequestParam(defaultValue = "0")           int    page,
            @RequestParam(defaultValue = "10")          int    size,
            @RequestParam(defaultValue = "firstName")   String sortBy,
            @RequestParam(defaultValue = "asc")         String sortDir
    ) {
        return ResponseEntity.ok(
                userManagementService.search(keyword, role, page, size, sortBy, sortDir));
    }
}
