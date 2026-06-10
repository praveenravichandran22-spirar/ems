package com.minifullstack.ems.controller;

import com.minifullstack.ems.dto.request.CreateUserRequestDto;
import com.minifullstack.ems.dto.request.UpdateUserRequestDto;
import com.minifullstack.ems.dto.response.PagedResponse;
import com.minifullstack.ems.dto.response.UserResponseDto;
import com.minifullstack.ems.enums.Role;
import com.minifullstack.ems.service.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody CreateUserRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userManagementService.createUser(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id,
                                                      @RequestBody UpdateUserRequestDto dto) {
        return ResponseEntity.ok(userManagementService.updateUser(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/reviewers")
    public ResponseEntity<List<UserResponseDto>> listReviewers() {
        return ResponseEntity.ok(userManagementService.listByRole(Role.ROLE_REVIEWER));
    }

    @GetMapping("/approvers")
    public ResponseEntity<List<UserResponseDto>> listApprovers() {
        return ResponseEntity.ok(userManagementService.listByRole(Role.ROLE_APPROVER));
    }
}
